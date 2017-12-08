package it.polito.oma.etp.reader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;

public class InputReader {
	
	private static FileReader fr;
	private static BufferedReader br;

	/**
	 * Reads .stu .exm .slo files relative to a specific instance to get
	 * a set of params that can be used to solve that problem instance.
	 * @param instanceName	Name of the files to read without extensions
	 * @return Benchmark	Object containing all needed params
	 */
	public static Benchmark getBenchmark(String instanceName) {
		int S = extractS(instanceName);
		int E = extractE(instanceName);
		int t = extractTmax(instanceName);
		int[][] n = extractN(instanceName, E);
		
		
		System.out.println("students: " + S + " exams: " + E + " slots: " + t);
		/*
		for(int i = 0; i < E; i++) {
			System.out.println(" ");
			for(int j = 0; j < E; j++) {
				System.out.print(n[i][j] + ", ");
			}
		}
		*/
		Benchmark bm = new Benchmark(instanceName, S, E, t, n);
		return bm;
	}
	
	/**
	 * Extract cardinality of students from the .stu file 
	 * @param iName	Instance name
	 */
	private static int extractS(String iName) {
		String currentLine, lastLine = "";

		try {
			fr = new FileReader(iName + ".stu");
			br = new BufferedReader(fr);
			
			/* Last line of the .stu file contains the number of the last student 
			 * The if condition ensure to not take in consideration an empty last string.*/
			while((currentLine = br.readLine()) != null) {
				if(currentLine.length() != 0)
					lastLine = currentLine;		
			}
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Extracting from the last line the number of the last student: 
		 * the format of the last line is "sNUM examnum"
		 * I cut off the examnum part and take just NUM from the sNUM part.
		 * Then I convert the string into an integer.
		 * */
		return Integer.parseInt(lastLine.split(" ")[0].replace("s", ""));
	}

	/**
	 * Extract cardinality of exams from the .exm file 
	 * @param iName	Instance name
	 */
	private static int extractE(String iName) {
		String currentLine, lastLine = "";
		
		try {
			fr = new FileReader(iName + ".exm");
			br = new BufferedReader(fr);
			
			/* Last line of the .stu file contains the number of the last student 
			 * The if condition ensure to not take in consideration an empty last string.*/
			while((currentLine = br.readLine()) != null) {
				if(currentLine.length() != 0)
					lastLine = currentLine;		
			}
			
		}  
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Extracting from the last line the number of the last exam: 
		 * the format of the last line is "examnum numstudent"
		 * I cut off the numstudent part and take just the examnum part.
		 * Then I convert the string into an integer.
		 * */
		return Integer.parseInt(lastLine.split(" ")[0]);
	}
	
	/**
	 * Extract number of available timeslots from .slo file
	 * @param iName	Instance name
	 */
	private static int extractTmax(String iName) {
		String currentLine = "";
		
		try {
			fr = new FileReader(iName + ".slo");
			br = new BufferedReader(fr);
			
			currentLine = br.readLine();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return Integer.parseInt(currentLine);
	}
	
	
//	commento prova
//  questo era fabrizio
	
	
	/**
	 * Extract the N matrix (number of students attending a given
	 * pair of exams) from the .stu file
	 * @param iName	Instance name
	 */
	private static int[][] extractN(String iName, int eNum){
		String currentLine, nextLine;
		boolean b = false;
		ArrayList<Integer> studentExams = new ArrayList<Integer>();
		int[][] n = new int[eNum][eNum];
		
		try {
			fr = new FileReader(iName + ".stu");
			br = new BufferedReader(fr);
			
			/* Looping through all valid lines.
			 * Format of line is sNUM examnum. */
			while((currentLine = br.readLine()) != null) {
				if(currentLine.length() != 0) {
					
					// Mark the buffer position.
					if (b == false) br.mark(10000);
					b = false;

					// Add to studentExams the first exam for that student.
					studentExams.add(Integer.parseInt(currentLine.split(" ")[1]));
					
					// Cycle until the lines have the same sNUM.
					while((nextLine = br.readLine()) != null && currentLine.split(" ")[0].equals(nextLine.split(" ")[0])) {
							b = true;
							/*Adding to the support structure studentExams every single exam
						 	* for that student. */
							studentExams.add(Integer.parseInt(nextLine.split(" ")[1]));
							// Mark the buffer position.
							br.mark(10000);
					}
				
					/* After the last loop, nextLine is the first line of the new student. I don't
					 * like this because when I will evaluate the condition of the outer loop I will
					 * move ahead the cursor, not considering the first line of the new student. So I
					 * will reset the cursor to the last mark which is the last line of the old student.*/
					br.reset();
					
					// Now that I have all the exams for a student x, I update N.
					updateNMatrix(n, studentExams);
					
				}
			} 
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return n;
	}
	
	/**
	 * Updates the N Matrix given a list of exams for a particular student.
	 * @param n			The N matrix.
	 * @param examList	ArrayList containing all the exams for a student.
	 */
	private static void updateNMatrix(int[][] n, ArrayList<Integer> examList) {
		/* I have to place in couple every exam in examList and then add 1 to the 
		 * specific element in the n matrix.
		 * Here i is used to mark the first element of the couple in the arraylist.*/
		for(int i = 0; i < examList.size(); i++) {
			// j is the second element of the couple and always starts one position after i.
			for(int j = i + 1; j < examList.size(); j++) {
				
				// N matrix is symmetric.
				n[examList.get(i)-1][examList.get(j)-1]++;
				n[examList.get(j)-1][examList.get(i)-1]++;
			}
		}
		// Prepare the list for the next student.
		examList.clear();
	}
}