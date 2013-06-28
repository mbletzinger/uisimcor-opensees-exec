package org.nees.illinois.uisimcor.fem_executor.utils;

import org.slf4j.Logger;

import ch.qos.logback.classic.Level;

/**
 * Class which maintains a counter so that a message is only logged once for
 * every max count. This helps keep the log output from becoming cluttered.
 * @author Michael Bletzinger
 */
public class LogMessageWithCounter {
	/**
	 * Counter for debug messages.
	 */
	private int count = 0;

	/**
	 * Debugging level that is used to print output.
	 */
	private final Level level;
	/**
	 * Logger to print message with.
	 */
	private final Logger log;
	/**
	 * Number of counts until reset.
	 */
	private final int maxCnt;

	/**
	 * @param maxCnt
	 *            Number of counts until message is logged.
	 * @param log
	 *            Logger to print message with.
	 * @param level
	 *            Debugging level that is used to print output.
	 */
	public LogMessageWithCounter(final int maxCnt, final Logger log,
			final Level level) {
		this.maxCnt = maxCnt;
		this.log = log;
		this.level = level;
	}

	/**
	 * Log the message if the count == to MaxCnt.
	 * @param msg
	 *            Message to log.
	 */
	public final void log(final String msg) {
		if (count < maxCnt) {
			count++;
			return;
		}
		reset();
		if (level.equals(Level.ERROR)) {
			log.error(msg);
		}
		if (level.equals(Level.INFO)) {
			log.info(msg);
		}
		if (level.equals(Level.DEBUG)) {
			log.debug(msg);
		}
	}

	/**
	 * Restart the counter.
	 */
	public final void reset() {
		count = 0;
	}

}
