package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Differential {

	Hashtable<String, Double> data;
	String name;
	
	/**
	 * @param data
	 * @param name
	 */
	public Differential(Hashtable<String, Double> data, String name) {
		super();
		this.data = data;
		this.name = name;
	}
	
	public Differential(String name) {
		this.name = name;
		data = new Hashtable<String,Double>();
	}
	
	public void addValue(String name, double value) {
		
		if (data.containsKey(name)) {
			
			double oldvalue = data.get(name);
			value = (oldvalue + value)/2;
			data.put(name, value);
			
		} else {
			data.put(name, value);
		}
		
	}
	
	/**
	 * @return the data
	 */
	public Hashtable<String, Double> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(Hashtable<String, Double> data) {
		this.data = data;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public double getDiffValue(String prot) {
		if (data.containsKey(prot)) {
			return data.get(prot);
		} else {
			return -1;
		}
	}
	
	public List<String> getQlist() {
		
		List<String> qlist = new ArrayList<String>();
		
		Enumeration<String> enumkeys = data.keys();
		String key;
		
		while(enumkeys.hasMoreElements()) {
			
			key = enumkeys.nextElement();
			qlist.add(key);
						
		}
		
		return qlist;
		
	}
	
}
