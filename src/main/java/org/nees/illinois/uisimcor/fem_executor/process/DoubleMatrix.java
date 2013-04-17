package org.nees.illinois.uisimcor.fem_executor.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleMatrix {
	protected final List<List<Double>> data;
	protected final int numberOfColumns;

	private final Logger log = LoggerFactory.getLogger(DoubleMatrix.class);

	public DoubleMatrix(List<List<Double>> idata) {
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

	public DoubleMatrix(double[][] idata) {
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
	 * @return the data
	 */
	public double[][] getData() {
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

	public boolean isNull(int row, int col) {
		List<Double> rowL = data.get(row);
		if (rowL.size() < col) {
			return true;
		}
		return rowL.get(col) == null;
	}

	public void set(int row, int col, double value) {
		List<Double> rowL = data.get(row);
		for (int c = rowL.size(); c < col + 1; c++) {
			rowL.add(null);
		}
		rowL.set(col, new Double(value));
	}

	public int[] sizes() {
		int[] result = new int[2];
		result[0] = data.size();
		result[1] = numberOfColumns;
		return result;
	}

	public List<List<Double>> toList() {
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
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
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

	public double value(int row, int col) {
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
