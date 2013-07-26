package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for a matrix of double data. The matrix can contain null elements
 * which get transformed into Double.NaN values when exported to a double [][]
 * datatype.
 * @author Michael Bletzinger
 */
public class DoubleMatrix implements DoubleMatrixI {
	/**
	 * Internal representation of data. The double list structure allows for row
	 * swapping.
	 */
	private final List<List<Double>> data;

	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(DoubleMatrix.class);

	/**
	 * Specification for the matrix.
	 */
	private MatrixSpecI spec;

	/**
	 * Create an instance from a double[][] array type.
	 * @param idata
	 *            The double [][] array.
	 */
	public DoubleMatrix(final double[][] idata) {
		data = convertData(idata);
		spec = createSpec(idata[0].length);
	}

	/**
	 * Create an instance from a double[][] array type.
	 * @param idata
	 *            The double [][] array.
	 * @param spec
	 *            Specification for this matrix.
	 */
	public DoubleMatrix(final double[][] idata, final MatrixSpecI spec) {
		data = convertData(idata);
		this.spec = spec;
	}

	/**
	 * Create an instance from a double list.
	 * @param idata
	 *            The double list.
	 */
	public DoubleMatrix(final List<List<Double>> idata) {
		data = idata;
		spec = createSpec(idata);
		padData();
	}

	/**
	 * Create an instance from a double list.
	 * @param idata
	 *            The double list.
	 * @param spec
	 *            Specification for this matrix.
	 */
	public DoubleMatrix(final List<List<Double>> idata, final MatrixSpecI spec) {
		data = idata;
		this.spec = spec;
		padData();
	}

	/**
	 * Put the data into a double list format.
	 * @param idata
	 *            input data.
	 * @return the double list version.
	 */
	private List<List<Double>> convertData(final double[][] idata) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		int row = idata.length;
		int col = idata[0].length;
		for (int r = 0; r < row; r++) {
			List<Double> rowL = new ArrayList<Double>();
			for (int c = 0; c < col; c++) {
				rowL.add(new Double(idata[r][c]));
			}
			result.add(rowL);
		}
		return result;
	}

	/**
	 * Create a matrix spec using the number of columns.
	 * @param numberOfColumns
	 *            duh.
	 * @return New matrix spec.
	 */
	private MatrixSpecI createSpec(final int numberOfColumns) {
		return new MatrixSpec(numberOfColumns);
	}

	/**
	 * Create a matrix spec using the size of the largest row in the double
	 * list.
	 * @param idata
	 *            The double list.
	 * @return The new matrix spec.
	 */
	private MatrixSpecI createSpec(final List<List<Double>> idata) {
		int col = 0;
		for (List<Double> r : idata) {
			if (r.size() > col) {
				col = r.size();
			}
		}
		return createSpec(col);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#getData()
	 */
	@Override
	public final double[][] getData() {
		double[][] result = new double[data.size()][spec
				.getNumberOfColumns(true)];
		int rc = 0;
		for (List<Double> r : data) {
			for (int c = 0; c < spec.getNumberOfColumns(true); c++) {
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
	 * Return a reference to the internal representation.
	 * @return The reference.
	 */
	protected final List<List<Double>> getInternalReference() {
		return data;
	}

	/**
	 * @return the spec
	 */
	@Override
	public final MatrixSpecI getSpec() {
		return spec;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#isNull(int, int)
	 */
	@Override
	public final boolean isNull(final int row, final int col) {
		List<Double> rowL = data.get(row);
		if (rowL.size() < col) {
			return true;
		}
		return rowL.get(col) == null;
	}

	/**
	 * Because we are paranoid we assume that data is sparse row-wise. So we
	 * find the longest row and append nulls to the others to make all rows of
	 * equal size
	 */
	private void padData() {
		int numberOfColumns = spec.getNumberOfColumns(true);
		for (List<Double> r : data) {
			if (r.size() == numberOfColumns) {
				continue;
			}
			for (int n = r.size(); n < numberOfColumns; n++) {
				r.add(null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#set(int, int,
	 * java.lang.Double)
	 */
	@Override
	public final void set(final int row, final int col, final Double value) {
		List<Double> rowL = data.get(row);
		for (int c = rowL.size(); c < col + 1; c++) {
			rowL.add(null);
		}
		rowL.set(col, new Double(value));
	}

	/**
	 * @param spec
	 *            the spec to set
	 */
	protected final void setSpec(final MatrixSpecI spec) {
		this.spec = spec;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#sizes()
	 */
	@Override
	public final int[] sizes() {
		int[] result = new int[2];
		result[0] = data.size();
		result[1] = spec.getNumberOfColumns(true);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#toList()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * @see org.nees.illinois.replay.data.DoubleMatrixI#value(int, int)
	 */
	@Override
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

	@Override
	public final void append(final List<Double> row) {
		data.add(row);
	}

	@Override
	public final void clear() {
		data.clear();
	}
}
