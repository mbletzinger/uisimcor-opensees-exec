package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which sends command strings to the FEM process and returns responses.
 * @author Michael Bletzinger
 */
public class StdInExchange implements Abortable, Observer {
	/**
	 * State of the FEM exchange.
	 * @author Michael Bletzinger
	 */
	public enum StdInExState {
		/**
		 * Just started.
		 */
		Idle,
		/**
		 * Reading the displacement and reaction files.
		 */
		ReadingOutputFile,
		/**
		 * Return the response strings.
		 */
		ReturnResponses,
		/**
		 * Writing a command to the FEM process STDIN stream.
		 */
		SendingCommand,
		/**
		 * Waiting on the command queue.
		 */
		WaitingForCommand,
		/**
		 * Waiting for an output file to update indicating that the FEM process
		 * has finished the step.
		 */
		WaitingForOutputFileChange
	}

	/**
	 * Step has finished processing.
	 */
	private volatile boolean analysisDone = false;

	/**
	 * Current command string.
	 */
	private String command;

	/**
	 * Blocking queue containing the command strings.
	 */
	private final BlockingQueue<String> commandQ;

	/**
	 * The output file for displacements.
	 */
	private final OutputFileMonitor dispF;

	/**
	 * How long to sleep before checking the files again.
	 */
	private final int filecheckInterval;

	/**
	 * The output file for forces.
	 */
	private final OutputFileMonitor forceF;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(StdInExchange.class);

	/**
	 * Quit flag for Abortable interface.
	 */
	private volatile boolean quit = false;
	/**
	 * Current response line.
	 */
	private List<String> responseLines = new ArrayList<String>();
	/**
	 * Queue to write the response strings to.
	 */
	private final BlockingQueue<String> responseQ;
	/**
	 * Current state.
	 */
	private StdInExState state = StdInExState.Idle;
	/**
	 * STDIN for the FEM process where the commands are written to.
	 */
	private final PrintWriter strm;
	/**
	 * @param commandQ
	 *            Blocking queue containing the command strings.
	 * @param dispF
	 *            The output file for displacements.
	 * @param forceF
	 *            The output file for forces.
	 * @param responseQ
	 *            Queue to write the response strings to.
	 * @param strm
	 *            STDIN for the FEM process where the commands are written to.
	 * @param filecheckInterval
	 *            Number of milliseconds to sleep between response file change
	 *            checks.
	 */
	public StdInExchange(final BlockingQueue<String> commandQ,
			final String dispF, final String forceF,
			final BlockingQueue<String> responseQ, final OutputStream strm,
			final int filecheckInterval) {
		this.commandQ = commandQ;
		this.commandQ.clear();
		this.dispF = new OutputFileMonitor(dispF);
		this.forceF = new OutputFileMonitor(forceF);
		this.responseQ = responseQ;
		this.strm = new PrintWriter(new BufferedOutputStream(strm));
		this.filecheckInterval = filecheckInterval;
		this.responseQ.clear();
	}
	/**
	 * @return the analysisDone
	 */
	public final synchronized boolean isAnalysisDone() {
		return analysisDone;
	}

	@Override
	public final synchronized boolean isQuit() {
//		log.debug("quit is " + quit);
		return quit;
	}

	/**
	 * Read the responses.
	 */
	private void readFiles() {
		responseLines.clear();
		try {
			dispF.readOutput();
			forceF.readOutput();
		} catch (OutputFileException e) {
			log.error("Aborting due to output read problems");
			setQuit(true);
		}
		responseLines.add(dispF.getResponse());
		responseLines.add(forceF.getResponse());
		state = StdInExState.ReturnResponses;
	}

	/**
	 * Sends the response strings into the response queue.
	 */
	private void returnResponses() {
		for (String r : responseLines) {
			try {
				responseQ.put(r);
			} catch (InterruptedException e) {
				log.debug("Checking quit");
				return;
			}
		}
		responseLines.clear();
		state = StdInExState.WaitingForCommand;
	}

	@Override
	public final void run() {
		log.debug("Starting STDIN Monitoring");
		state = StdInExState.WaitingForCommand;
		while (isQuit() == false) {
			log.debug("State: " + state);
			if (state.equals(StdInExState.WaitingForCommand)) {
				waitForCommand();
				continue;
			}
			if (state.equals(StdInExState.SendingCommand)) {
				sendCommand();
				continue;
			}
			if (state.equals(StdInExState.WaitingForOutputFileChange)) {
				waitForOutputFileChange();
				continue;
			}
			if (state.equals(StdInExState.ReadingOutputFile)) {
				readFiles();
				continue;
			}
			if (state.equals(StdInExState.ReturnResponses)) {
				returnResponses();
				continue;
			}

		}
		state = StdInExState.Idle;
		log.debug("Ending STDIN Monitoring");
	}

	/**
	 * Print command to the process STDIN.
	 */
	private void sendCommand() {
		strm.println(command);
		strm.flush();
		state = StdInExState.WaitingForOutputFileChange;
	}

	/**
	 * @param analysisDone the analysisDone to set
	 */
	public final synchronized void setAnalysisDone(final boolean analysisDone) {
		this.analysisDone = analysisDone;
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
//		log.debug("Setting quit " + quit);
		this.quit = quit;
	}

	@Override
	public final void update(final Observable o, final Object arg) {
		setAnalysisDone(true);
	}

	/**
	 * Reads the command and changes the state to SendingCommand if there is an
	 * actual command in the queue.
	 */
	private void waitForCommand() {
		try {
			command = commandQ.take();
		} catch (InterruptedException e) {
			log.debug("Checking abort flag");
			return;
		}
		log.debug("Command is \"" + command + "\"");
		state = StdInExState.SendingCommand;
	}

	/**
	 * Checks to see if the output file has changed yet by checking the
	 * analysisDone flag. The flag is set by the Observable ProsessResponse class.
	 */
	private void waitForOutputFileChange() {
		if(isAnalysisDone()) {
			state = StdInExState.ReadingOutputFile;
			setAnalysisDone(false);
			return;
		}
		try {
			Thread.sleep(filecheckInterval);
		} catch (InterruptedException e) {
			log.debug("Checking quit flag");
		}
	}
}
