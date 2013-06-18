package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import name.pachler.nio.file.Path;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;

import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageType;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which monitors if OpenSees has gotten around to creating an output file
 * with its node recorders. It then reads the file and deletes it.
 * @author Michael Bletzinger
 */
public class OutputFileMonitor implements AbortableI {
	/**
	 * Operational states for the file monitor.
	 * @author Michael Bletzinger
	 */
	public enum OfmStates {
		/**
		 * Wait for File creation.
		 */
		CheckFileCreation,
		/**
		 * Wait for no more modifications.
		 */
		CheckingForMods,
		/**
		 * Heuristic delay to get all mod events at once.
		 */
		HeuristicDelay,
		/**
		 * The thread is not running.
		 */
		Idle,
		/**
		 * Read the file.
		 */
		ReadingOutput,
	}

	/**
	 * Determines poll wait time for events.
	 */
	private final DelayHeuristics delay = new DelayHeuristics();

	/**
	 * Queue for the watcher service to send events.
	 */
	private final BlockingQueue<List<WatchEvent<?>>> eventQ = new LinkedBlockingQueue<List<WatchEvent<?>>>();

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(OutputFileMonitor.class);

	/**
	 * Amount of time we wait for file changes before assuming the output file
	 * is ready.
	 */
	private final int modCheckWait = 400;

	/**
	 * Quit flag.
	 */
	private volatile boolean quit;

	/**
	 * Reader which reads the file being monitored.
	 */
	private final BinaryFileReader dispReader;

	/**
	 * Reader which reads the file being monitored.
	 */
	private final BinaryFileReader forceReader;

	/**
	 * Queue for sending the contents of the file.
	 */
	private final BlockingQueue<QMessageT<List<Double>>> responseQ = new LinkedBlockingQueue<QMessageT<List<Double>>>();
	/**
	 * Current state of monitor.
	 */
	private volatile OfmStates state;
	/**
	 * Directory watch service.
	 */
	private final WorkDirWatcher watcher;

	/**
	 * @param dispReader
	 *            Displacement output file to monitor.
	 * @param forceReader
	 *            Force output file to monitor.
	 * @param watcher
	 *            Directory watch service.
	 */
	public OutputFileMonitor(final BinaryFileReader dispReader,
			final BinaryFileReader forceReader, final WorkDirWatcher watcher) {
		this.dispReader = dispReader;
		this.forceReader = forceReader;
		try {
			dispReader.clean();
		} catch (OutputFileException e) {
			log.error("Remove failed", e);
			setQuit(true);
		}
		final int initialDelay = 5000;
		delay.setDelay(initialDelay);
		delay.startStep();
		this.watcher = watcher;
	}

	/**
	 * Wait for a file creation event.
	 */
	@SuppressWarnings("unchecked")
	private void checkForFileCreation() {
		List<WatchEvent<?>> events;
		try {
			events = eventQ.take();
		} catch (InterruptedException e) {
			log.debug("Checking quit");
			return;
		}
		for (WatchEvent<?> e : events) {
			WatchEvent<Path> pe = (WatchEvent<Path>) e;
			log.debug("Checking " + pe.kind().name());
			if (pe.kind().equals(StandardWatchEventKind.ENTRY_CREATE)
					|| pe.kind().equals(StandardWatchEventKind.ENTRY_MODIFY)) {
				setState(OfmStates.HeuristicDelay);
				return;
			}
		}
	}

	/**
	 * Check for a lack of events within the modCheckWait timeframe.
	 */
	@SuppressWarnings("unchecked")
	private void checkForMods() {
		List<WatchEvent<?>> events;
		try {
			events = eventQ.poll(modCheckWait, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.debug("Checking quit");
			return;
		}
		if (events == null) {
			delay.completedStep(forceReader.getFileModDate());
			setState(OfmStates.ReadingOutput);
			return;
		}
		for (WatchEvent<?> e : events) {
			WatchEvent<Path> pe = (WatchEvent<Path>) e;
			log.debug("Checking " + pe.kind().name());
		}
	}

	/**
	 * @return the eventQ
	 */
	public final BlockingQueue<List<WatchEvent<?>>> getEventQ() {
		return eventQ;
	}

	/**
	 * @return the reader
	 */
	public final BinaryFileReader getDispReader() {
		return dispReader;
	}

	/**
	 * @return the responseQ
	 */
	public final BlockingQueue<QMessageT<List<Double>>> getResponseQ() {
		return responseQ;
	}

	/**
	 * @return the state
	 */
	public final synchronized OfmStates getState() {
		return state;
	}

	/**
	 * Wait the heuristic delay.
	 */
	private void heuristicDelay() {
		try {
			Thread.sleep(delay.getDelay());
		} catch (InterruptedException e) {
			log.debug("interrupted. Checking quit");
			return; // We are going to chance a double delay rather on that was
					// shortened by a spurious interrupt. If there is such a
					// thing.
		}
		setState(OfmStates.CheckingForMods);
	}

	@Override
	public final synchronized boolean isQuit() {
		return quit;
	}

	/**
	 * Read the output files by skipping lineCount lines and then reading the
	 * last line for both force and displacement files.
	 */
	private void readOutput() {
		List<Double> dresponse = null;
		List<Double> fresponse = null;
		try {
			dresponse = dispReader.read();
			log.debug("Read displacements " + MtxUtils.list2String(dresponse));
			dispReader.clean();
			fresponse = forceReader.read();
			log.debug("Read forces " + MtxUtils.list2String(fresponse));
			forceReader.clean();
		} catch (OutputFileException e) {
			log.error("Read failed", e);
			setQuit(true);
		}
		setState(OfmStates.CheckFileCreation); // Do this before the response is
												// sent so that we are sure the
												// file is created after we are
												// watching it.
		delay.startStep();
		try {
			responseQ.put(new QMessageT<List<Double>>(QMessageType.Response,
					dresponse));
			responseQ.put(new QMessageT<List<Double>>(QMessageType.Response,
					fresponse));
		} catch (InterruptedException e) {
			log.error("Can't send response because ", e);
			setQuit(true);
		}
		watcher.addFileWatch(dispReader.getDirectory(), getEventQ());
	}

	@Override
	public final void run() {
		watcher.addFileWatch(dispReader.getDirectory(), getEventQ());
		log.info("Starting fie monitoring of \"" + dispReader.getDirectory()
				+ "\"");
		setState(OfmStates.CheckFileCreation);
		while (isQuit() == false) {
			OfmStates st = getState();
			log.debug("State: " + st);
			if (st.equals(OfmStates.CheckFileCreation)) {
				checkForFileCreation();
				continue;
			}
			if (st.equals(OfmStates.HeuristicDelay)) {
				heuristicDelay();
				continue;
			}
			if (st.equals(OfmStates.CheckingForMods)) {
				checkForMods();
				continue;
			}
			if (st.equals(OfmStates.ReadingOutput)) {
				readOutput();
				continue;
			}
		}
		setState(OfmStates.Idle);
		log.info("Ending output file monitoring");
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
		this.quit = quit;

	}

	/**
	 * @param state
	 *            the state to set
	 */
	public final synchronized void setState(final OfmStates state) {
		this.state = state;
	}

}
