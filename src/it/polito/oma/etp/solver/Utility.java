package it.polito.oma.etp.solver;

import java.util.Arrays;

public class Utility {	
	/**
	 * Copy the data from an integer matrix to another integer matrix.
	 * @param matrix	Source, matrix to be copied
	 * @return			Destination, copied matrix
	 */
	public static int[][] cloneMatrix(int[][] matrix){
		int[][] clone = new int[matrix.length][];
		for(int i = 0; i < matrix.length; ++i) {
			int[] tempRow = matrix[i];
			int rowLength = tempRow.length;
			clone[i] = new int[rowLength];
			System.arraycopy(tempRow, 0, clone[i], 0, rowLength);
		}
		
		return clone;
	}
	
	/**
	 * Copy the data from an integer array to another integer array.
	 * @param matrix	Source, array to be copied
	 * @return			Destination, copied array
	 */
	public static int[] cloneArray(int[] array) {
		int[] clone = new int[array.length];
		
		System.arraycopy(array, 0, clone, 0, array.length);
		
		return clone;
	}

	/**
	 * General function which prints a matrix in a readable way.
	 * @param m		matrix to be printed.
	 * @return		a string showing the matrix in a readable way.
	 */
	public static String printMatrix(int[][] m) {
		String result = "";
		for(int[] row: m) {
			result += Arrays.toString(row) + "\n";
		}
		return result + "\n";
	}
}
