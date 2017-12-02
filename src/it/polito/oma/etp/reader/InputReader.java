package it.polito.oma.etp.reader;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class InputReader {
	
	private static FileReader fr;
	private static BufferedReader br;

	public static Benchmark getBenchmark(String instanceName) {
		int S = extractS(instanceName);
		int E = extractE(instanceName);
		int t = extractTmax(instanceName);
		
		System.out.println("students: " + S + " exams: " + E + " slots: " + t);
		return null;
	}
	
	/**
	 * Extract cardinality of students from the .stu file 
	 * @param iName instance name
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
	 * @param iName instance name
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
	 * @param iName instance name
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
}