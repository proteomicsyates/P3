/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs.utilities;

import java.util.Enumeration;
import java.util.List;

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
			for (int j=0; j < elist.get(i).getNumberofConditions(); j++) {
				
				Enumeration<String> plist = elist.get(i).getCondition(j).getPlist();
				
				while (plist.hasMoreElements()) {
					String pname = plist.nextElement();
					
					elist.get(i).getCondition(j).getProtein(pname).calculateApv();
					
				}
				
				
			}
		}
		
		
	}

}
