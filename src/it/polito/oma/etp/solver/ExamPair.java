package it.polito.oma.etp.solver;

/**
 * Wrapper key-class used for mapping exam pairs to conflict coefficients
 */
public class ExamPair implements Comparable<ExamPair> {
	private final int exam1, exam2;
	private final Float conflictCoefficient;

	public ExamPair(int exam1, int exam2, float conflictCoefficient) {
		if(exam1 == exam2) {
			System.err.println("Exam pair not valid");
			System.exit(1);
		}
		
		this.exam1 = exam1;
		this.exam2 = exam2;
		this.conflictCoefficient = conflictCoefficient;
	}

	/**
	 * Constructor useful whenever the conflict coefficient makes
	 * no sense.
	 * @param exam1	First exam belonging to this pair.
	 * @param exam2	Second exam belonging to this pair.
	 */
	public ExamPair(int exam1, int exam2) {
		this(exam1, exam2, -1);
	}
	
	/**
	 * Copy constructor.
	 * @param otherPair	Exam pair to be copied.
	 */
	public ExamPair(ExamPair otherPair) {
		exam1 = otherPair.exam1;
		exam2 = otherPair.exam2;
		conflictCoefficient = otherPair.conflictCoefficient;
	}
	
//	// Searching in Lists
//	@Override
//	public boolean equals(Object o) {
//		if(this == o) return true;
//		if(!(o instanceof ExamPair)) return false;
//		
//		ExamPair otherPair = (ExamPair)o;
//		
//		// Exams order does not matter
//		return	(exam1 == otherPair.exam1 && exam2 == otherPair.exam2)
//					||
//				(exam1 == otherPair.exam2 && exam2 == otherPair.exam1);
//	}
	
	// Ordering
	@Override
	public int compareTo(ExamPair otherExamPair) {
		return Float.valueOf(otherExamPair.conflictCoefficient).compareTo(conflictCoefficient);
	}

//	/**
//	 * Hashing function used by the java.util.Map's get() function
//	 */
//	@Override
//	public int hashCode() {
//		return exam1 + exam2;
//	}
//

	/**
	 * Pretty-printing
	 */
	@Override
	public String toString() {
		// Using notation from 0 to E
		return	"<e" + exam1 + ", e" + exam2 + ", " + conflictCoefficient + ">";
	}
	
	public int getExam1() {
		return exam1;
	}

	public int getExam2() {
		return exam2;
	}

	public float getConflictCoefficient() {
		return conflictCoefficient;
	}
}
