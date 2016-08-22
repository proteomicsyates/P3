/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class Experiment {
	
	private String name; // bait or bait pool
	private List<Condition> clist;

	public Experiment(String name) {
		this.name = name;
		clist = new ArrayList<Condition>();
	}
	
	public void addConditions(String [] exp) {
		
		for (int i=0; i < exp.length; i++) {
			Condition c = new Condition(exp[i]);
			clist.add(c);
		}
		
	}
	
	public Condition getCondition(int cindex) {
		return clist.get(cindex);
	}
	
	public int getNumberofConditions() {
		return clist.size();
	}
	
	public String getName() {
		return name;
	}
	
}
