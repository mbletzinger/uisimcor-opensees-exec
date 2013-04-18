package org.nees.illinois.uisimcor.fem_executor.test;

public class Mtx2Str {
	
	public static String array2String(double[] array) {
		String result = "[";
		for (int i = 0; i < array.length; i++) {
			result += (i == 0 ? "":", ") + array[i];
		}
		result += "]";
		return result;
	}
	public static String iArray2String(int [] array) {
		String result = "[";
		for (int i = 0; i < array.length; i++) {
			result += (i == 0 ? "":", ") + array[i];
		}
		result += "]";
		return result;
	}
	
	public static String matrix2String( double [][] matrix) {
		String result = "\n";
		for (int i = 0; i < matrix.length; i++) {
			result += (i == 0 ? "":"\n") +array2String(matrix[i]);
		}
		return result;		
	}
	public static double [][] timeOffset(double [][] data) {
		double [][] result = new double[data.length][data[0].length];
		double offset = data[0][0];
		for (int r = 0; r < data.length; r++) {
			result[r][0] = data[r][0] - offset;
			for(int c = 1; c < data[0].length; c++) {
				result[r][c] = data[r][c];
			}
		}
		return result;
	}

}
