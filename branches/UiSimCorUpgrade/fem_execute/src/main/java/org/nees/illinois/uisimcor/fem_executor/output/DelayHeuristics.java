package org.nees.illinois.uisimcor.fem_executor.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which predicts the completion of simulation step so that the response
 * can be read.
 * @author Michael Bletzinger
 */
public class DelayHeuristics {
	/**
	 * Time to wait after the current step has started.
	 */
	private long delay;

	/**
	 * History of actual step times.
	 */
	private final long[] history;

	/**
	 * Amount of history to use for calculation.
	 */
	private final int historySize = 5;

	/**
	 * Index of current slot in history.
	 */
	private int idx = 0;
	/**
	 * Start time of current step.
	 */
	private long start;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DelayHeuristics.class);

	/**
	 *
	 */
	public DelayHeuristics() {
		history = new long[historySize];
	}

	/**
	 * Record a new delay based on the latest history.
	 * @param completedTime
	 *            Last modified time from the file containing the response.
	 */
	public final void completedStep(final long completedTime) {
		// Sometimes start comes after completed. This is because the last
		// modified time does not include milliseconds. So if both are the same
		// second the start will be afterwards by at most 999 milliseconds.
		long interval = Math.abs(completedTime - start);
		log.debug("/start=" + start + "/stop=" + completedTime + "/interval="
				+ interval);
		history[idx] = interval;
		idx++;
		if (idx == historySize) {
			idx = 0;
		}

		long sum = 0;
		for (long d : history) {
			sum += d;
		}
		delay = sum / historySize;
	}

	/**
	 * @return the delay
	 */
	public final long getDelay() {
		return delay;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public final void setDelay(final long delay) {
		this.delay = delay;
	}

	/**
	 * Record the start time for the step.
	 */
	public final void startStep() {
		start = System.currentTimeMillis();
	}
}
