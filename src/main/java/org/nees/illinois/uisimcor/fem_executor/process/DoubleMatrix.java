package org.nees.illinois.uisimcor.fem_executor.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which allows easy transitions between various ways to store a matrix.
 * @author Michael Bletzinger
 */
public class DoubleMatrix {
	/**
	 * Internal representation of data set.
	 */
	private final List<List<Double>> data;
	/**
	 * Number of columns for each row. Needed because there is no guarantee that
	 * every List<Double> row will have the same number of elements.
	 */
	private final int numberOfColumns;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(DoubleMatrix.class);

	/**
	 * Constructor from List<List<Double>>.
	 * @param idata
	 *            Input data set.
	 */
	public DoubleMatrix(final List<List<Double>> idata) {
		data = idata;
		// Because we are paranoid we assume that idata is sparse row-wise.
		// So we find the longest row and append nulls to the others
		// to make all rows of equal size
		int col = 0;
		for (List<Double> r : idata) {
			if (r.size() > col) {
				col = r.size();
			}
		}
		numberOfColumns = col;
		for (List<Double> r : data) {
			if (r.size() == numberOfColumns) {
				continue;
			}
			for (int n = r.size(); n < numberOfColumns; n++) {
				r.add(null);
			}
		}
	}

	/**
	 * Constructor from double[][].
	 * @param idata
	 *            Input data set.
	 */
	public DoubleMatrix(final double[][] idata) {
		data = new ArrayList<List<Double>>();
		int row = idata.length;
		int col = idata[0].length;
		numberOfColumns = col;
		for (int r = 0; r < row; r++) {
			List<Double> rowL = new ArrayList<Double>();
			for (int c = 0; c < col; c++) {
				rowL.add(new Double(idata[r][c]));
			}
			data.add(rowL);
		}
	}

	/**
	 * @return the data set in double[][] format. A elements that are null are
	 *         set to Double.NaN.
	 */
	public final double[][] getData() {
		double[][] result = new double[data.size()][numberOfColumns];
		int rc = 0;
		for (List<Double> r : data) {
			for (int c = 0; c < numberOfColumns; c++) {
				Double d = r.get(c);
				if (d == null) {
					result[rc][c] = Double.NaN;
					continue;
				}
				log.debug("Setting r " + rc + ", c " + c);
				result[rc][c] = d.doubleValue();
			}
			rc++;
		}
		return result;
	}

	/**
	 * Determine if a particular element in the dataset contains a number.
	 * @param row
	 *            Element row.
	 * @param col
	 *            Element column.
	 * @return True if the element does not contain a number.
	 */
	public final boolean isNull(final int row, final int col) {
		List<Double> rowL = data.get(row);
		if (rowL.size() < col) {
			return true;
		}
		return rowL.get(col) == null;
	}

	/**
	 * Sets an element.
	 * @param row
	 *            Element row.
	 * @param col
	 *            Element column.
	 * @param value
	 *            Element value.
	 */
	public final void set(final int row, final int col, final double value) {
		List<Double> rowL = data.get(row);
		for (int c = rowL.size(); c < col + 1; c++) {
			rowL.add(null);
		}
		rowL.set(col, new Double(value));
	}

	/**
	 * returns the row and column sizes.
	 * @return array with the number of rows and then the number of columns.
	 */
	public final int[] sizes() {
		int[] result = new int[2];
		result[0] = data.size();
		result[1] = numberOfColumns;
		return result;
	}

	/**
	 * Returns the data set in List<LIst<Double>> format.
	 * @return The data set.
	 */
	public final List<List<Double>> toList() {
		List<List<Double>> result = new ArrayList<List<Double>>();
		for (List<Double> r : data) {
			List<Double> nr = new ArrayList<Double>();
			nr.addAll(r);
			result.add(nr);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		String result = "";
		for (List<Double> r : data) {
			boolean first = true;
			result += "\n\t[";
			for (int c = 0; c < r.size(); c++) {
				Double d = r.get(c);
				if (d == null) {
					result += (first ? "" : ", ") + "null";
					continue;
				}
				result += (first ? "" : ", ") + d.toString();
				first = false;
			}
			result += "]";
		}
		return result;
	}

	/**
	 * Returns the value of an element. If the element is null the function
	 * returns Double.NaN.
	 * @param row
	 *            Element row.
	 * @param col
	 *            Element column.
	 * @return Element value.
	 */
	public final double value(final int row, final int col) {
		if (data.isEmpty() == false) {
			List<Double> rowL = data.get(row);
			if (rowL == null) {
				log.error("Row " + row + " in " + toString() + " is null ");
				return Double.NaN;
			}
			Double d = rowL.get(col);
			if (d == null) {
				return Double.NaN;
			}
			return rowL.get(col);
		}
		return Double.NaN;
	}
}
