/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.parsers.inputs.utilities.Protein;

/**
 * @author diego
 *
 */
public class Condition {

	private String name; // experimental condition
	Hashtable<String, Protein> ptable;
	List<String> pnames;
	
	public Condition(String name) {
		this.name = name;
		ptable = new Hashtable<String,Protein>();
		pnames = new ArrayList<String>();
	}
	
	public boolean proteinInTable(String name) {
		if (ptable.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Protein getProtein(String name) {
		return ptable.get(name);
	}
	
	public void addProtein(String name, Protein p, boolean novel) {
		ptable.put(name, p);
		if (novel) {
			pnames.add(name);
		}
		
	}
	
	public Enumeration<String> getPlist() {
		return ptable.keys();
	}
	
	public int getNumberOfProteins() {
		return ptable.size();
	}
	
	public void removeProtein(String name) {
		if (proteinInTable(name)) {
			ptable.remove(name);
			pnames.remove(name);
		}
	}
	
	public List<String> getPnames() {
		
		/*Enumeration<String> enumkeys = getPlist();
		List<String> pnames = new ArrayList<String>();
		String key;
		
		while ( enumkeys.hasMoreElements()) {
			key = enumkeys.nextElement();
			pnames.add(key);
		}*/
		
		return pnames;
		
	}
	
	public String getName() {
		return name;
	}
	
}
