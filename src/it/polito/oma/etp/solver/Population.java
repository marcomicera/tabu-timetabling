package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;

public abstract class Population {
	protected InstanceData instance;
	
	protected ArrayList<Solution> chromosomes;
	protected float totalFitness;
	protected float totalInverseFitness;
	
	protected Solution bestSolution;
	protected Solution worstSolution;
	
	/**
	 * Population abstract class constructor, initializing
	 * properties common to all subclasses
	 * @param instance	the only common data available from the
	 * 					beginning is the one describing the instance.
	 */
	public Population(InstanceData instance) {
		this.instance = instance;
		
		// Population initialization
		chromosomes = new ArrayList<Solution>();
	}
	
	/**
	 * Adds a new chromosome to this population.
	 * Worst/best solution checks are updated accordingly.
	 * @param newChromosome		chromosome to be added in this population.
	 */
	public void add(Solution newChromosome) {
		chromosomes.add(newChromosome);
		totalInverseFitness += 1/newChromosome.getFitness();
		totalFitness += newChromosome.getFitness();
		
		if(bestSolution == null || newChromosome.getFitness() < bestSolution.getFitness())
			// Updating best solution
			bestSolution = newChromosome;
		
		else if(worstSolution == null || newChromosome.getFitness() > worstSolution.getFitness())
			// Updating worst solution
			worstSolution = newChromosome;
	}
	
	/**
	 * Removes the specified chromosome from this population.
	 * Worst/best solution checks are updated accordingly.
	 * @param killedChromosome	chromosome to be removed from the this population.
	 */
	public void delete(Solution killedChromosome) {
		chromosomes.remove(killedChromosome);
		totalInverseFitness -= 1/killedChromosome.getFitness();
		totalFitness -= killedChromosome.getFitness();
		
		if(bestSolution == killedChromosome || worstSolution == killedChromosome)
			updateWorstAndBestSolution();
	}
	
	/**
	 * delete() method overload, allowing to specify a chromosome to-be-killed index
	 * instead of providing the corresponding Solution object.
	 * @param killedChromosomeIndex		the chromosome to-be-killed index.
	 */
	public void delete(int killedChromosomeIndex) {
		delete(getSolution(killedChromosomeIndex));
	}
	
	protected void updateWorstAndBestSolution() {
		// By now, there's not worst and better solution
		bestSolution = null;
		worstSolution = null;
		
		for(Solution chromosome: chromosomes) {
			// Initializing bestSolution
			if(bestSolution == null)
				bestSolution = chromosome;
			else if(bestSolution.getFitness() > chromosome.getFitness())
				bestSolution = chromosome;
			
			// Initializing worstSolution
			if(worstSolution == null)
				worstSolution = chromosome;
			else if(worstSolution.getFitness() < chromosome.getFitness())
				worstSolution = chromosome;
		}
	}

	public ArrayList<Solution> getChromosomes() {
		return chromosomes;
	}
	
	public int getSize() {
		return chromosomes.size();
	}
	
	/**
	 * Returns the solution mapped by the given index.
	 * @param index		interested solution index in the population.
	 * @return			the corresponding Solution object.
	 */
	public Solution getSolution(int index) {
		return chromosomes.get(index);
	}

	public float getTotalFitness() {
		return totalFitness;
	}

	public float getTotalInverseFitness() {
		return totalInverseFitness;
	}
	
	public Solution getBestSolution() {
		return bestSolution;
	}
	
	public Solution getWorstSolution() {
		return worstSolution;
	}
	
	@Override
	public String toString() {
		return	"Population infos: " + 
				chromosomes.size() + " chromosomes, " + 
				"totalFitness: " + totalFitness + ", " +
				"totalInverseFitness: " + totalInverseFitness + "\n" +
				"Chromosomes: " + Arrays.toString(chromosomes.toArray())
		;
	}
}
