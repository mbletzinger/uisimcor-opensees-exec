/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.utils;

/**
 * Catchable exception for configuration parameter errors.
 * @author Michael Bletzinger
 */
public class IllegalParameterException extends Exception {

	/**
	 * Required by Eclipse.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public IllegalParameterException() {
		super();
	}

	/**
	 * Replicated Constructor.
	 * @param message
	 *            Message associated with the exception.
	 */
	public IllegalParameterException(final String message) {
		super(message);
	}

	/**
	 * Replicated Constructor.
	 * @param message
	 *            Message associated with the exception.
	 * @param cause
	 *            Chained exception.
	 */
	public IllegalParameterException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Replicated Constructor.
	 * @param cause
	 *            Chained exception.
	 */
	public IllegalParameterException(final Throwable cause) {
		super(cause);
	}

}
