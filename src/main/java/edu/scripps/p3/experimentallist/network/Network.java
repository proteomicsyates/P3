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

	private String exp_name;
	private Hashtable<String,Interaction> interactions;
	private Features feats;
	/**
	 * @return the interaction_names
	 */
	public List<String> getInteraction_names() {
		return interaction_names;
	}

	private List<String> interaction_names;
	private boolean empty;
	
	public Network(String name) {
		this.exp_name = name;
		this.empty = true;
	}
	
	public void addInteraction(String inter_name, Interaction inter) {
		if (empty) {
			interactions = new Hashtable<String,Interaction>();
			interaction_names = new ArrayList<String>();
			empty = false;
		}
		
		if (!interactions.containsKey(inter_name)) {
			interactions.put(inter_name, inter);
			interaction_names.add(inter_name);
		}
			
	}
	
	public Interaction getInteraction(String name) {
		return interactions.get(name);
	}
	
	public String getExpName() {
		return exp_name;
	}
	
	public void setFeatures(Features feats) {
		this.feats = feats;
	}
	
	public Hashtable<String,List<Double>> getFeaturesValues() {
		return feats.getData();
	}
	
	public boolean isInNetwork(String name) {
		if (interactions.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void removeInteraction(String name) {
		if (isInNetwork(name)) {
			interactions.remove(name);
			interaction_names.remove(name);
		}
	}
	
}
