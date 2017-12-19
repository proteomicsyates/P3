/**
 * diego
 * Nov 12, 2013
 */
package edu.scripps.p3.correlator.utilities;

import java.util.List;

import org.apache.log4j.Logger;

import edu.scripps.p3.experimentallist.Interactome;

/**
 * @author diego
 *
 */
public class InteractomesCleaner {
	private final static Logger log4j = Logger.getLogger(InteractomesCleaner.class);
	List<List<Interactome>> interactomes;
	String[] baits;

	/**
	 * @param interactomes
	 * @param baits
	 */
	public InteractomesCleaner(List<List<Interactome>> interactomes, String[] baits) {
		this.interactomes = interactomes;
		this.baits = baits;
	}

	/**
	 * Removes the interactomes in which there is not any of the baits
	 */
	public void run() {

		String[] expbaits;
		int numRemoved = 0;
		for (int experimentID = 0; experimentID < interactomes.size(); experimentID++) {

			expbaits = baits[experimentID].split("-");

			boolean keeper = false;

			final List<Interactome> experimentInteractomes = interactomes.get(experimentID);
			for (int conditionID = 0; conditionID < experimentInteractomes.size(); conditionID++) {

				final Interactome interactomeInCondition = experimentInteractomes.get(conditionID);
				List<String> nodes = interactomeInCondition.getProteinsHavingANetwork();

				for (String node : nodes) {

					keeper = false;

					for (int baitIndex = 0; baitIndex < expbaits.length; baitIndex++) {

						final String bait = expbaits[baitIndex];
						if (node.equalsIgnoreCase(bait)) {
							keeper = true;
							break;
						}

					}

					if (!keeper) {
						interactomeInCondition.getNetworksByProteinName().remove(node);
						numRemoved++;
					}
				}
				log4j.info("Interactome for experiment " + experimentID + " and condition " + conditionID + " has now "
						+ interactomeInCondition.getNetworksByProteinName().size() + " networks");
			}
		}
		log4j.info(numRemoved + " nodes removed from the interactomes.");

	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
