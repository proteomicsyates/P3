/**
 * diego
 * May 15, 2013
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

	Hashtable<String, Network> system_table;
	String exp_name;
	String bait_name;
	boolean empty;
	
	List<String> physical_names;
	List<String> genetical_names;
	
	boolean pempty;
	boolean gempty;
	
	public Interactome(String name) {
		this.exp_name = name;
		empty = true;
		pempty = true;
		gempty = true;
	}
	
	public Interactome(String bait_name, String exp_name) {
		this.bait_name = bait_name;
		this.exp_name = exp_name;
		empty = true;
		pempty = true;
		gempty = true;
	}
	
	public void addNetwork(Network net) {
		
		system_table.put(net.getExpName(), net);
		
	}
	
	public boolean isEmpty() {
		if (empty) {
			if (empty) {
				system_table = new Hashtable<String,Network>();
				empty = false;
			}
		}
		
		return empty;
	}
	
	public Hashtable<String, Network> getSystem() {
		return system_table;
	}
	
	public Network getNetwork(String name) {
		return system_table.get(name);
		
	}
	
	public boolean isNetworkinSystem(String name) {
		if (system_table.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<String> getNetlist() {
		
		List<String> names = new ArrayList<String>();
		Enumeration<String> enumKey = system_table.keys();
		while(enumKey.hasMoreElements()) {
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
	public String getExp_name() {
		return exp_name;
	}
	
	public String getBait_name() {
		return bait_name;
	}
	
	
}
