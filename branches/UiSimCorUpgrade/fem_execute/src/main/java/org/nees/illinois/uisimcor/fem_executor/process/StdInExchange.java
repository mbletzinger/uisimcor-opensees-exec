package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which sends command strings to the FEM process and returns responses.
 * @author Michael Bletzinger
 */
public class StdInExchange implements AbortableI {

	/**
	 * Current command string.
	 */
	private QMessageT<?> command;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(StdInExchange.class);

	/**
	 * Interval for poll command.
	 */
	private final int queueCheckInterval;

	/**
	 * Quit flag for Abortable interface.
	 */
	private volatile boolean quit = false;

	/**
	 * Blocking queue containing the command strings.
	 */
	private final BlockingQueue<QMessageT<String>> stdinQ = new LinkedBlockingQueue<QMessageT<String>>();

	/**
	 * STDIN for the FEM process where the commands are written to.
	 */
	private final PrintWriter strm;

	/**
	 * Count for debug messages during a poll.
	 */
	private int pollCount = 0;
	/**
	 * Number of times to log a polling message.
	 */
	private final int pollPrintWait = 20;

	/**
	 * @param queueCheckInterval
	 *            Interval for poll command.
	 * @param strm
	 *            STDIN for the FEM process where the commands are written to.
	 */
	public StdInExchange(final int queueCheckInterval, final OutputStream strm) {
		this.queueCheckInterval = queueCheckInterval;
		this.strm = new PrintWriter(new BufferedOutputStream(strm));
	}

	/**
	 * @return the queueCheckInterval
	 */
	public final int getQueueCheckInterval() {
		return queueCheckInterval;
	}

	/**
	 * @return the commandQ
	 */
	public final BlockingQueue<QMessageT<String>> getStdinQ() {
		return stdinQ;
	}

	@Override
	public final synchronized boolean isQuit() {
		// log.debug("quit is " + quit);
		return quit;
	}

	@Override
	public final void run() {
		log.info("Starting STDIN monitoring");
		while (isQuit() == false) {
			boolean recieved = waitForCommand();
			if (recieved) {
				sendCommand();
			}
		}
		log.info("Ending STDIN monitoring");
	}

	/**
	 * Print command to the process STDIN.
	 */
	private void sendCommand() {
		log.debug("Sending command " + command);
		strm.println(command.getContent());
		strm.flush();
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
		// log.debug("Setting quit " + quit);
		this.quit = quit;
	}

	/**
	 * Reads the command and changes the state to SendingCommand if there is an
	 * actual command in the queue.
	 * @return True if a command was read.
	 */
	private boolean waitForCommand() {
		if (pollCount > pollPrintWait) {
			pollCount = 0;
		} else {
			pollCount++;
		}
		if (pollCount == 0) {
			log.debug("Waiting for command");
		}
		try {
			command = stdinQ.poll(queueCheckInterval, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.debug("Checking quit flag");
			return false;
		}
		if (command == null) {
			return false;
		}
		return true;
	}

}
