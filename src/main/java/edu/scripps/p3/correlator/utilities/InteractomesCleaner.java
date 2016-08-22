/**
 * diego
 * Nov 12, 2013
 */
package edu.scripps.p3.correlator.utilities;

import java.util.List;

import edu.scripps.p3.experimentallist.Interactome;

/**
 * @author diego
 *
 */
public class InteractomesCleaner {

	List<List<Interactome>> interactomes;
	String[] baits;
	
	/**
	 * @param interactomes
	 * @param baits
	 */
	public InteractomesCleaner(List<List<Interactome>> interactomes,
			String[] baits) {
		this.interactomes = interactomes;
		this.baits = baits;
	}

	/**
	 * 
	 */
	public void run() {
		
		String [] expbaits;
		
		for (int k=0; k < interactomes.size(); k++) {
			
			expbaits = baits[k].split("-");
			
			boolean keeper = false;
			
			for (int i=0; i < interactomes.get(k).size(); i++) {
				
				List<String> nodes = interactomes.get(k).get(i).getNetlist();
				
				for (String node : nodes) {
					
					keeper = false;
					
					for (int j=0; j < expbaits.length; j++) {
						
						if (node.equalsIgnoreCase(expbaits[j])) {
							keeper = true;
							break;
						}
						
					}
					
					if (!keeper) {
						interactomes.get(k).get(i).getSystem().remove(node);
					}
					
					
				}
				
				
			}
			
		}
		
	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
