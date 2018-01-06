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
			}
		
			
			// Crossover (generating new children)
			
			
			// Chromosomes substitution (which chromosome survives)
			
		//}
		
		// TODO complete
		return null;
	}
	
	
	/**
	 * Returns the CUMULATIVE relative probability of each element of the population.
	 * 
	 * EXAMPLE:
	 * 
	 * 			FITNESS		PERCENTAGE FIT  		     CRP
	 * 			   8	    (1/8) / (1/31)		    (1/8) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/7) / (1/31)      (1/8 + 1/7) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / (1/31)     (1/8 + 1/7 + 1/8) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / (1/31)				  1		
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
	
	/*
	 * in order to select a parent accordingly with a probability we
	 * use the range between consecutive values of CRP
	 */
	
	private Solution SelectRandomParent(double[] relativeFitness) {
		Solution selectedParent = null;
		
		// generate a random number in the range [0,1]
		Random rnd = new Random();
		double randomNum = rnd.nextDouble();
		
		for (int i = 0; i < relativeFitness.length; i++) {
			
		}
		
		return selectedParent;
	}
	
	
	
	
	
	
	
}
























