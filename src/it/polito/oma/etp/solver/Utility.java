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
	public static void printMatrix(int[][] m) {
		String result = "";
		for(int[] row: m) {
			result += Arrays.toString(row) + "\n";
		}
		System.out.println(result + "\n");
	}
	
	/**
	 * Generates a random number between the specified range
	 * @param from	
	 * @param to
	 * @return
	 */
	public static int getRandomInt(int from, int to) {
		return java.util.concurrent.ThreadLocalRandom.current().nextInt(from, to);
	}
	
	public static double getRandomDouble(int from, int to) {
		return from + Math.random() * (to - from);
	}
}
