/**
 * diego May 15, 2013
 */
package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.experimentallist.network.Network;

/**
 * @author diego
 *
 */
public class Interactome {

	Hashtable<String, Network> networksByProteinName;
	String conditionName;
	String bait_name;
	boolean empty;

	List<String> physical_names;
	List<String> genetical_names;

	boolean pempty;
	boolean gempty;

	public Interactome(String conditionName) {
		this.conditionName = conditionName;
		empty = true;
		pempty = true;
		gempty = true;
	}

	public Interactome(String bait_name, String conditionName) {
		this.bait_name = bait_name;
		this.conditionName = conditionName;
		empty = true;
		pempty = true;
		gempty = true;
	}

	public void addNetwork(Network net) {

		networksByProteinName.put(net.getBait(), net);

	}

	public boolean isEmpty() {
		if (empty) {
			if (empty) {
				networksByProteinName = new Hashtable<String, Network>();
				empty = false;
			}
		}

		return empty;
	}

	public Hashtable<String, Network> getNetworksByProteinName() {
		return networksByProteinName;
	}

	public Network getNetwork(String proteinName) {
		return networksByProteinName.get(proteinName);

	}

	public boolean isNetworkinSystem(String name) {
		if (networksByProteinName.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getProteinsHavingANetwork() {

		List<String> names = new ArrayList<String>();
		Enumeration<String> enumKey = networksByProteinName.keys();
		while (enumKey.hasMoreElements()) {
			String key = enumKey.nextElement();
			names.add(key);
		}

		return names;

	}

	public void addPterm(String term) {
		if (pempty) {
			physical_names = new ArrayList<String>();
		}
		physical_names.add(term);
		pempty = false;
	}

	public void addGterm(String term) {
		if (gempty) {
			genetical_names = new ArrayList<String>();
		}
		genetical_names.add(term);
		gempty = false;
	}

	/**
	 * @return the physical_names
	 */
	public List<String> getPhysical_names() {
		return physical_names;
	}

	/**
	 * @return the genetical_names
	 */
	public List<String> getGenetical_names() {
		return genetical_names;
	}

	/**
	 * @return the exp_name
	 */
	public String getConditionName() {
		return conditionName;
	}

	public String getBait_name() {
		return bait_name;
	}

}
