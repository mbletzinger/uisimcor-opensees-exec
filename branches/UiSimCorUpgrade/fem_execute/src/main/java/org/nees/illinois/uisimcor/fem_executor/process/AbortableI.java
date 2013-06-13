package org.nees.illinois.uisimcor.fem_executor.process;
/**
 * Manages the abort flag.
 * @author Michael Bletzinger
 *
 */
public interface AbortableI extends Runnable {

	/**
	 * @return the abort flag.
	 */
	boolean isQuit();
	/**
	 * @param quit Value to set the abort flag to.
	 */
	void setQuit(final boolean quit);

}
