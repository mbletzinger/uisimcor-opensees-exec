/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.output;

/**
 * @author Michael Bletzinger
 */
public class MatrixSpec implements MatrixSpecI {
	/**
	 * Number of time columns is 1.
	 */
	private static int timeColumns = 1;

	/**
	 * @return the timeColumns
	 */
	public static final int getTimeColumns() {
		return timeColumns;
	}

	/**
	 * Number of columns in the matrix.
	 */
	private int numberOfColumns;

	/**
	 * @param numberOfColumns
	 *            Number of columns in the matrix.
	 */
	public MatrixSpec(final int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.nees.illinois.replay.common.types.MatrixSpecsI#appendColumns(org.
	 * nees.illinois.replay.common.types.MatrixSpecsI)
	 */
	@Override
	public final void appendColumns(final MatrixSpecI other) {
		numberOfColumns += other.getNumberOfColumns(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.nees.illinois.replay.common.types.MatrixSpecsI#getNumberOfColumns
	 * (boolean)
	 */
	@Override
	public final int getNumberOfColumns(final boolean withTime) {
		return numberOfColumns - (withTime ? 0 : timeColumns);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.nees.illinois.replay.common.types.MatrixSpecsI#numberOfTimeColumns()
	 */
	@Override
	public final int numberOfTimeColumns() {
		return timeColumns;
	}

}
