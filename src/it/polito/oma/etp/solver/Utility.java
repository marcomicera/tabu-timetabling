package it.polito.oma.etp.solver;

public class Utility {
	
	/**
	 * Copy the data from an integer matrix to another integer matrix.
	 * @param matrix Source, matrix to be copied
	 * @return Destination, matrix copied
	 */
	public static int[][] cloneMatrix(int[][] matrix){
		int [][] cloned = new int[matrix.length][matrix[0].length];
				
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				cloned[i][j] = matrix[i][j];
			}
		}
		
		return cloned;
	}

}
