package it.polito.oma.etp.solver;

@SuppressWarnings("serial")
public class InvalidMoveException extends Exception {
	public InvalidMoveException(String message) {
		super("Invalid move: " + message);
	}
	
	public InvalidMoveException() {
		super("Invalid move");
	} 
}
