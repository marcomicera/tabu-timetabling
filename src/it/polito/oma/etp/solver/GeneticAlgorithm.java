package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.metadata.IIOInvalidTreeException;

import it.polito.oma.etp.reader.InstanceData;

public class GeneticAlgorithm {
	protected Settings tbSettings;
	protected GaSettings gaSettings;
	protected InstanceData instance;
	
	
	public GeneticAlgorithm(Settings tbSettings, GaSettings gaSettings, InstanceData instance) {
		this.tbSettings = tbSettings;
		this.gaSettings = gaSettings;
		this.instance = instance;
	}

	public Solution solve() {
		// Population generation
		Population initialPopulation = new Population(instance, gaSettings, tbSettings);
		
		/*	it's very important that the population is orderd in order to 
		 * 	select the correct parents that will generate childrens.
		 */
		Collections.sort(initialPopulation.getPopulation());
		
		//while(...) {
			// ***** Parents selection (for children generation) *********
		
			/* prents : array list that contains the parents that will generate childrens.
			 * 			the size of the array depens on the numberOfReproductiveParents
			 * 			of the GASettings
			 */
			ArrayList<Solution> parents = new ArrayList();
			
			/*TODO debug*/ System.out.println("Population: "+ Arrays.toString(initialPopulation.getPopulation().toArray()));

			if(!gaSettings.selectParentsByRelativeFitness) { // parents selected by best fitness
				
				int count = 1;
				while (count <= gaSettings.numberOfReproductiveParents) {
					
					/*	because the population is ordered with decresent value of fitness,
					 *  we have to keep the last "numberOfReproductiveParents" elements in order to have
					 * 	parents that have the best fitness among all the population.
					 */
					
					parents.add(initialPopulation.getPopulation().get(initialPopulation.getPopulation().size()-count));
					count++;
				}
				/*TODO debug*/ //System.out.println("parents: "+ Arrays.toString(parents.toArray()));

			}
			else { // parents selected randomly with by best relative fitness probability
				
				double[] CRP = GenerateCRP(initialPopulation);
					for (int i = 0; i < gaSettings.numberOfReproductiveParents; i++)
						parents.add(SelectRandomParent(CRP, initialPopulation));
			}
			
			// Crossover (generating new children)
			
			// initialization of the children 1
			int[] children1Schedule = new int[instance.getE()];

			if(!gaSettings.randomCuttingPoint) { // in case we select a deterministic cutting points
				int[] parentSchedule1 = parents.get(0).schedule;
				int[] parentSchedule2 = parents.get(1).schedule;
				
				//setting the part of the children schedule that is equal to the parent schedule part
				for (int i = gaSettings.whereToCut[0]; i <= gaSettings.whereToCut[1]; i++) {
					children1Schedule[i] = parentSchedule1[i];
				}
				
				
				// the starting point is the first exam after the biggest cutting point.
				// the wheretoCut array must be ordered.
				
				//children 1 generation -> we use parent 2!
				int startingExam = gaSettings.whereToCut[1]+1; 
				int exam = startingExam;
				int childrenExamToSet = startingExam;
					do {
						   int parentTimeSlot = parentSchedule2[exam];
						   
						   if(!gaSettings.initializationProblem)
							  if(chechFeasibility(parentTimeSlot, exam, children1Schedule)) {
								  children1Schedule[childrenExamToSet] = parentTimeSlot; 
								  childrenExamToSet++;
								  if(childrenExamToSet>=instance.getE()) childrenExamToSet = 0;
						   }
						   else { // this is the case of INITIALIZATION PROBLEM
							   //simple crossover
						   }
						   exam++;
						   if(exam == instance.getE()) exam = 0;

					} while(exam != startingExam);
	
				if(childrenExamToSet<gaSettings.whereToCut[0]-1) {
					System.err.println("children not completely setted!!!");
					System.exit(0);
				}
				/*TODO debug*/else System.out.println("children1 schedule= "+Arrays.toString(children1Schedule));
						
			}
			else {
				// in case we select a random cutting points generation
			}
			
			
			// Chromosomes substitution (which chromosome survives)
			
		//}
		
		// TODO complete
		return null;
	}
	
	
	/*
	 * it's used to check if the exam that we wont to put in the time slot given as
	 * parameter is a feasible move considering the actual childrenschedule setted
	 * so far.
	 * 
	 * @param TimeSlot	the time slot we wont to use for the exam given by currentExam param
	 * @param currentExam we are setting the children schedule, and this is the current exam
	 * 		              we wonto to schedule
	 * @param childrenSchedule	the childrenSchedule that dynamically change by inserting 
	 * 						    exams in the order given by it's parent.
	 * 
	 * @return isFeasible true if we can put the currentExam in the TimeSlot given by param
	 */
	
	private boolean chechFeasibility(int TimeSlot, int currentExam, int[] childrenSchedule) {
		boolean isFeasible = true;
		
		int startingExam = gaSettings.whereToCut[0]; 
		int exam = startingExam;
		
			do {
				   if(instance.getN()[currentExam][exam]>0 && 
					  TimeSlot == childrenSchedule[exam])
					   isFeasible = false;
				   exam++;
				   if(exam == instance.getE()) exam = 0;

			} while(exam != startingExam && !isFeasible);
		
		return isFeasible;
	}

	
	/** (tested)
	 * Returns the CUMULATIVE relative probability of each element of the population.
	 * 
	 * EXAMPLE:
	 * 
	 * 			FITNESS		PERCENTAGE FIT  		     	  		  CRP
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8		    (1/8) / 1/8+1/7+1/8+1/8
	 * 			   7	    (1/7) / 1/8+1/7+1/8+1/8      (1/8 + 1/7) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8     (1/8 + 1/7 + 1/8) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8 				   1		
	 * 
	 * 
	 * @param 		the pupulation of which we'll calculate the relative fitness.
	 * @return		CUMULATIVE relative fitness of each element of the population.
	 */
	private double[] GenerateCRP(Population poulation) {
		double[] CRP = new double[gaSettings.PopulationSize];
		
		double cumulativeSum = 0;
		for (int i = 0; i < poulation.getPopulation().size(); i++) {
			cumulativeSum += 1/poulation.getPopulation().get(i).getFitness();
			CRP[i] = (cumulativeSum/(poulation.getTotalFitness()));
		}
		/*TODO debug*/ System.out.println("CRP: "+Arrays.toString(CRP));
		return CRP;
	}
	
	/*(tested)
	 * in order to select a parent accordingly with a probability we
	 * use the range between consecutive values of CRP
	 */
	
	private Solution SelectRandomParent(double[] relativeFitness, Population population) {
		Solution selectedParent = null;
		
		// generate a random number in the range [0,1]
		Random rnd = new Random();
		
		// Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 
		double randomNum = rnd.nextDouble();
		
		/*
		 * example:  CRP= [0,26; 0,48; 0,78; 1]
		 * 			 if the random value is 0,3
		 * 				 -> we select the second element of population
		 */
		if(randomNum <= relativeFitness[0]) {
			selectedParent = population.getPopulation().get(0);
			/* TODO debug */ System.out.println("Selected element: "+ 1);
		}
		else
			for (int i = 1; i < relativeFitness.length; i++) {
				if(randomNum <= relativeFitness[i] && randomNum>relativeFitness[i-1]) {
					selectedParent = population.getPopulation().get(i);	
					/* TODO debug */ System.out.println("Selected element: "+ (i+1) +
														" with fitness: " +selectedParent.getFitness());
				}
			}
		
		return selectedParent;
	}
	
	
	
	
	
	
	
}
























