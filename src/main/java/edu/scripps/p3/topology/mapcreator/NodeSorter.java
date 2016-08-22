/**
 * diego
 * May 20, 2013
 */
package edu.scripps.p3.topology.mapcreator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class NodeSorter {

	List<String> cnames;
	String indirect;
	List<List<Double>> matrix;
	List<String> names;
	
	/**
	 * @param cnames
	 * @param indirect
	 */
	public NodeSorter(List<String> cnames, String indirect) {
		this.cnames = cnames;
		this.indirect = indirect;
	}

	/**
	 * 
	 */
	public void run() {
		
		int [] maxids;
		
		initializeMatrix();
		fillMatrix();
		
		do {
			
			maxids = getMax();
			mergeMax(maxids);
			
			
		} while (names.size()!=1);
		 
		sortNames();
		
		
	}

	private void sortNames() {
		
		String [] snames = names.get(0).split("_");
		
		cnames = new ArrayList<String>();
		
		for (String sname : snames) {
			cnames.add(sname);
		}
		
	}
	
	private int[] getMax() {
		
		int[] ids = new int[2];
		ids[0]=0;
		ids[1]=1;
		
		double max = Double.MIN_VALUE;
		
		for (int i=0; i < matrix.size(); i++) {
			for (int j=0; j < matrix.get(i).size(); j++) {
				if (i!=j) {
					if (matrix.get(i).get(j)>max) {
						max = matrix.get(i).get(j);
						ids[0]=i;
						ids[1]=j;
					}
				}
				
			}
		}
		
		return ids;
	}
	
	private void mergeMax(int[] ids) {
		
		String newname = names.get(ids[0]) + "_" + names.get(ids[1]);
		
		int idmin = Math.min(ids[0], ids[1]);
		int idmax = Math.max(ids[0], ids[1]);
		
		List<Double> newcolumn = new ArrayList<Double>();
		
		double newval;
		
		int idmin_size = matrix.get(idmin).size();
		
		for (int i=0; i < idmin_size; i++) {
			
			newval = Math.max(matrix.get(idmin).get(i), matrix.get(idmax).get(i));
			
			newcolumn.add(newval);
			
			matrix.get(i).add(newval);
		}
		
		newcolumn.add(0.0);
		
		matrix.add(newcolumn);
		
		matrix.remove(idmin);
		matrix.remove(idmax-1);
		
		for (int i=0; i < matrix.size(); i++) {
			matrix.get(i).remove(idmin);
			matrix.get(i).remove(idmax-1);
		}
		
		names.remove(idmin);
		names.remove(idmax-1);
		names.add(newname);
		
		
	}
	
	private void initializeMatrix() {
		matrix = new ArrayList<List<Double>>();
		names = new ArrayList<String>();
		
		for (String name : cnames) {
			names.add(name);
			List<Double> column = new ArrayList<Double>();
			for (int i=0; i < cnames.size(); i++) {
				column.add(0.0);
			}
			matrix.add(column);
		}
	}
	
	private void fillMatrix() {
		
		String [] edges = indirect.split("\n");
		
		int index1, index2;
		double val;
		
		for (String edge : edges) {
			
			String [] element = edge.split("\t");
			index1 = names.indexOf(element[0]);
			index2 = names.indexOf(element[1]);
			
			element[3] = element[3].split(" ")[1];
			val = Double.parseDouble(element[3]);
			
			if (matrix.get(index1).get(index2)==0) {
				matrix.get(index1).set(index2, val);
				matrix.get(index2).set(index1, val);
			} else {
				val = val + matrix.get(index1).get(index2);
				matrix.get(index1).set(index2, val);
				matrix.get(index2).set(index1, val);
			}
						
			
		}
	}
	
	
	/**
	 * @return
	 */
	public List<String> getNodes() {
		return cnames;
	}

}
