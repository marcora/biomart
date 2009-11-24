package org.biomart.common.general.utils;

import java.util.ArrayList;
import java.util.List;

public class Average {
		
	public double average;
	public double standardDeviation;
	public double total;
	public double squaredTotal;
	public int length;
	public double squarredAverage;
	public boolean upToDate;
	
	public Average () {
		total = 0;
		squaredTotal = 0;
		upToDate = false;
		length=0;
	}
	
	@Override
	public String toString () {
		return "average = " + average +
		", standardDeviation = " + standardDeviation; 
	}

	public String toDetailedString () {
		return "average = " + average +
		", standardDeviation = " + standardDeviation +
		", total = " + total +
		", squaredTotal = " + squaredTotal +
		", length = " + length +
		", squarredAverage = " + squarredAverage +
		", upToDate = " + upToDate;
	}
	
	public void addToAverage (double d) {
		addToAverage(d, false);
	}
	
	public void addToAverage (double d, boolean update) {
		upToDate=false;
		total+=d;
		squaredTotal+=Math.pow(d, 2);		
		length++;
		
		// Must be done in this order
		if (update) {
			update();
		}
	}
	
	public void update () {
		if (!upToDate) {
			average = total / length;
			squarredAverage = squaredTotal / length;
			standardDeviation = Math.sqrt(
					squarredAverage - Math.pow(average, 2));
			upToDate=true;
		}
	}
	
	public static Average computeAverageFromList(List<Double> list) {
		Average average = new Average ();
		for (double d : list) {
			average.total+=d;
			average.squaredTotal+=Math.pow(d, 2);
		}
		average.length = list.size();
		average.average = average.total / average.length;
		average.squarredAverage = average.squaredTotal / average.length;
		average.standardDeviation = Math.sqrt(
				average.squarredAverage - Math.pow(average.average, 2));
		average.upToDate = true;
		return average;
	}
	
	public static void main(String[] args) {
		List<Double> list = new ArrayList<Double> ();
		list.add (10.0);
		list.add (12.0);
		list.add (8.0);
		list.add (10.0);
		
		Average average = new Average ();
		for (double d : list) {
			average.addToAverage(d);
		}
		average.update();
		System.out.println(average.toDetailedString());
		
		Average average2 = Average.computeAverageFromList (list);
		System.out.println(average2.toDetailedString());		
	}
}