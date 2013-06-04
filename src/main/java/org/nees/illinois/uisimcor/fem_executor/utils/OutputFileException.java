package org.nees.illinois.uisimcor.fem_executor.utils;

/**
 * Catchable exception for output file read errors.
 * @author Michael Bletzinger
 */

public class OutputFileException extends Exception {

	/**
	 * Required by Eclipse.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public OutputFileException() {
	}

	/**
	 * @param arg0
	 *            Message associated with the exception.
	 * @param arg1
	 *            Chained exception.
	 */
	public OutputFileException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 *            Message associated with the exception.
	 */
	public OutputFileException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 *            Chained exception.
	 */
	public OutputFileException(final Throwable arg0) {
		super(arg0);
	}

}
