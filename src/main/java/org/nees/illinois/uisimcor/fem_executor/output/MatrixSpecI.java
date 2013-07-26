package org.nees.illinois.uisimcor.fem_executor.output;

/**
 * Description of a matrix to account for the fact that we do not want to know
 * the internal representation.
 * @author Michael Bletzinger
 */
public interface MatrixSpecI {
	/**
	 * @param withTime
	 *            include the time columns
	 * @return number of columns
	 */
	int getNumberOfColumns(boolean withTime);

	/**
	 * The number of time columns.
	 * @return The number of time columns.
	 */
	int numberOfTimeColumns();

	/**
	 * Append the columns of the other matrix to this one.
	 * @param other
	 *            The other matrix.
	 */
	void appendColumns(MatrixSpecI other);

}
