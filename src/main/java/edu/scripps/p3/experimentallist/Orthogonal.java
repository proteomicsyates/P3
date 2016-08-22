/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * @author diego
 *
 */
public class Orthogonal {

	public static final String PHYSICAL = "PHYSICAL";
	public static final String GENETIC = "GENETIC";
	
	Hashtable<String, Double> table;
	String name;
	double coeff;
	String type;
	
	public Orthogonal(String name) {
		this.name = name;
		table = new Hashtable<String,Double>();
	}
	
	public void setCoefficient(double d) {
		this.coeff = d;
	}
	
	public double getCoefficient() {
		return coeff;
	}
	
	public void addEntry(String name, double value) {
		table.put(name, value);
	}
	
	public double getValues(String name) {
		return table.get(name);
	}
	
	public boolean isInTable(String name) {
		if (table.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getElements() {
		
		List<String> list = new ArrayList<String>();
		
		Enumeration<String> enumkeys = table.keys();
		while (enumkeys.hasMoreElements()) {
			String element = enumkeys.nextElement();
			
			list.add(element);
			
		}
		
		return list;
		
	}
	
}
