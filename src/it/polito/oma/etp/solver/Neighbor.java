package it.polito.oma.etp.solver;

import java.util.Arrays;

public class Neighbor {
	private int movingExam;
	private int newTimeslot;
	private float fitness;
	private int[] schedule;
	private int[][] te;
 	
	public Neighbor() {
	}
	
	public Neighbor(int movingExam, int newTimeslot, float fitness, int[] schedule) {
		this.movingExam = movingExam;
		this.newTimeslot = newTimeslot;
		this.fitness = fitness;
		this.schedule = schedule;
	}
	
	public Neighbor(int movingExam, int newTimeslot, float fitness, int[][] tematrix) {
		this.movingExam = movingExam;
		this.newTimeslot = newTimeslot;
		this.fitness = fitness;
		te = tematrix;
	}
	
	public void update(Neighbor neighbor) {
		movingExam = neighbor.movingExam;
		newTimeslot = neighbor.newTimeslot;
		fitness = neighbor.fitness;
		schedule = neighbor.schedule;
	}

	@Override
	public String toString() {
		return	"[Neighbor] movingExam = " + movingExam +
				"; newTimeslot = " + newTimeslot +
				"; fitness = " + fitness +
				"; schedule = " + Arrays.toString(schedule);
	}
	
	public float getFitness() {
		return fitness;
	}

	public int getMovingExam() {
		return movingExam;
	}

	public int getNewTimeslot() {
		return newTimeslot;
	}
}