/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs.utilities;

import java.util.Enumeration;
import java.util.List;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;

/**
 * @author diego
 *
 */
public class ApvCalculator {

	List<Experiment> elist;
	/**
	 * @param elist
	 */
	public ApvCalculator(List<Experiment> elist) {
		this.elist = elist;
	}

	/**
	 * 
	 */
	public void run() {
		
		for (int i=0; i < elist.size(); i++) {
			final Experiment experiment = elist.get(i);
			for (int j=0; j < experiment.getNumberofConditions(); j++) {
				
				final Condition condition = experiment.getCondition(j);
				Enumeration<String> plist = condition.getPlist();
				
				while (plist.hasMoreElements()) {
					String pname = plist.nextElement();
					
					condition.getProtein(pname).calculateApv();
					
				}
				
				
			}
		}
		
		
	}

}
