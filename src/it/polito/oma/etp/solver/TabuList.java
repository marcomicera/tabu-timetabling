package it.polito.oma.etp.solver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class TabuList {
	private int	size,
				maxSize;
	
	private LinkedList<Neighbor> tabuList;
	
	public TabuList(int initialSize, int maxSize) {
		size = initialSize;
		this.maxSize = maxSize;
		tabuList = new LinkedList<Neighbor>();
	}

	public void add(Neighbor neighbor) {
		if(!isFull())
			tabuList.add(neighbor);
		else {
			tabuList.pop();
			tabuList.add(neighbor);
		}
	}
	
	public int find(Neighbor neighbor) {
		return tabuList.indexOf(neighbor);
	}
	
	private boolean isFull() {
		return tabuList.size() == size;
	}
	
	public boolean contains(int exam, int t) {
		boolean b = false;
		
		Iterator<Neighbor> i;
		Neighbor n;
		for(i = tabuList.iterator(); i.hasNext();) {
			n = i.next();
			if(n.getMovingExam() == exam && n.getNewTimeslot() == t)
				b = true;
		}
		
		return b;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if(size == this.size)
			return;
		
		// Decreasing size
		if(size < this.size) {
			int decrease = this.size - size;
			
			// Popping oldest Tabu List elements
			while(decrease > 0) {
				tabuList.pop();
				--decrease;
			}
			
			this.size = size;
		}
		else {
			int newSize = (this.size > maxSize) ? maxSize : size;
			
			this.size = newSize;
		}
	}
	
	public void increaseSize(int quantity) {
		int newSize = (size + quantity > maxSize) ? maxSize : size + quantity;
		
		size = newSize;
	}
	
	public Neighbor getLastEntry() {
		return tabuList.getLast();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(tabuList.toArray());
	}
}

