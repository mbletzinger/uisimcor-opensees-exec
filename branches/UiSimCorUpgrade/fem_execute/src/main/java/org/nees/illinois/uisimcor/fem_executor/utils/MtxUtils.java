package org.nees.illinois.uisimcor.fem_executor.utils;

import java.util.List;

/**
 * Utility class to convert matrices and arrays to strings.
 * @author Michael Bletzinger
 */
public final class MtxUtils {
	/**
	 * Prevent class from being constructed.
	 */
	private MtxUtils() {
	}

	/**
	 * Converts double list to string.
	 * @param list
	 *            Double list.
	 * @return String.
	 */
	public static String list2String(final List<Double> list) {
		double[] array = list2Array(list);
		return array2String(array);
	}

	/**
	 * Converts double array to string.
	 * @param array
	 *            Double array.
	 * @return String.
	 */
	public static String array2String(final double[] array) {
		String result = "[";
		for (int i = 0; i < array.length; i++) {
			result += (i == 0 ? "" : ", ") + array[i];
		}
		result += "]";
		return result;
	}

	/**
	 * Converts integer array to string.
	 * @param array
	 *            Integer array.
	 * @return String.
	 */
	public static String array2String(final int[] array) {
		String result = "[";
		for (int i = 0; i < array.length; i++) {
			result += (i == 0 ? "" : ", ") + array[i];
		}
		result += "]";
		return result;
	}

	/**
	 * Converts double matrix to string.
	 * @param matrix
	 *            Double matrix.
	 * @return String.
	 */
	public static String matrix2String(final double[][] matrix) {
		String result = "\n";
		for (int i = 0; i < matrix.length; i++) {
			result += (i == 0 ? "" : "\n") + array2String(matrix[i]);
		}
		return result;
	}

	/**
	 * Converts integer matrix to string.
	 * @param matrix
	 *            Integer matrix.
	 * @return String.
	 */
	public static String matrix2String(final int[][] matrix) {
		String result = "\n";
		for (int i = 0; i < matrix.length; i++) {
			result += (i == 0 ? "" : "\n") + array2String(matrix[i]);
		}
		return result;
	}

	/**
	 * Offsets the value of the first column of every row by the value at
	 * [0][0].
	 * @param data
	 *            Double matrix with time as the first column.
	 * @return Matrix with first column data normalized.
	 */
	public static double[][] timeOffset(final double[][] data) {
		double[][] result = new double[data.length][data[0].length];
		double offset = data[0][0];
		for (int r = 0; r < data.length; r++) {
			result[r][0] = data[r][0] - offset;
			for (int c = 1; c < data[0].length; c++) {
				result[r][c] = data[r][c];
			}
		}
		return result;
	}

	/**
	 * Convert List<Double> to double[].
	 * @param list
	 *            List<Double>
	 * @return double[]
	 */
	public static double[] list2Array(final List<Double> list) {
		double[] result = new double[list.size()];
		int i = 0;
		for (Double d : list) {
			result[i++] = d;
		}
		return result;
	}
}
