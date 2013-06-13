package org.nees.illinois.uisimcor.fem_executor.output;

/**
 * Class which predicts the completion of similation step so that the response
 * can be read.
 * @author Michael Bletzinger
 */
public class DelayHeuristics {
	/**
	 * @return the delay
	 */
	public final long getDelay() {
		return delay;
	}

	/**
	 * Time to wait after the current step has started.
	 */
	private long delay;
	/**
	 * Amount of history to use for calculation.
	 */
	private final int historySize = 5;
	/**
	 * History of actual step times.
	 */
	private long[] history = new long[historySize];
	/**
	 * Index of current slot in history.
	 */
	private int idx = 0;
	/**
	 * Start time of current step.
	 */
	private long start;

	/**
	 * Record the start time for the step.
	 */
	public final void startStep() {
		start = System.currentTimeMillis();
	}

	/**
	 * Record a new delay based on the latest history.
	 */
	public final void completedStep() {
		long time = System.currentTimeMillis();
		long interval = time - start;
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
}
