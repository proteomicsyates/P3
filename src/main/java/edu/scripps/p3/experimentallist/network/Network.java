/**
 * diego
 * May 10, 2013
 */
package edu.scripps.p3.experimentallist.network;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.cluterer.utilities.Features;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;

/**
 * @author diego
 *
 */
public class Network {

	private String bait;
	private Hashtable<String, Interaction> interactionsByInteractorName;
	private Features feats;

	/**
	 * @return the interaction_names
	 */
	public List<String> getInteractorsNames() {
		return interactorNames;
	}

	private List<String> interactorNames;
	private boolean empty;

	public Network(String proteinName) {
		this.bait = proteinName;
		this.empty = true;
	}

	public void addInteraction(String interactorName, Interaction inter) {
		if (empty) {
			interactionsByInteractorName = new Hashtable<String, Interaction>();
			interactorNames = new ArrayList<String>();
			empty = false;
		}

		if (!interactionsByInteractorName.containsKey(interactorName)) {
			interactionsByInteractorName.put(interactorName, inter);
			interactorNames.add(interactorName);
		}

	}

	public Interaction getInteractionByInteractorName(String proteinName) {
		return interactionsByInteractorName.get(proteinName);
	}

	/**
	 * Gets the name of the protein of this network. This protein contains some
	 * interactors in this network. To obtain them, call getInteracorsNames()
	 * 
	 * @return
	 */
	public String getBait() {
		return bait;
	}

	public void setFeatures(Features feats) {
		this.feats = feats;
	}

	public Hashtable<String, List<Double>> getFeaturesValues() {
		return feats.getFeaturesByPrey();
	}

	public boolean isInNetwork(String interactorName) {
		if (interactionsByInteractorName.containsKey(interactorName)) {
			return true;
		} else {
			return false;
		}
	}

	public void removeInteraction(String interactorName) {
		if (isInNetwork(interactorName)) {
			interactionsByInteractorName.remove(interactorName);
			interactorNames.remove(interactorName);
		}
	}

}
