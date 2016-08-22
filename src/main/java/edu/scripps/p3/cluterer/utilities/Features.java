package edu.scripps.p3.cluterer.utilities;

import java.util.Hashtable;
import java.util.List;

public class Features {

	Hashtable<String, List<Double>> data;
	List<String> plist;
	String name;
	double threshold;
	
	public Features(String n, Hashtable<String, List<Double>> d, List<String> p) {
		name = n;
		data = d;
		plist = p;
	}
	
	public void setData(Hashtable<String, List<Double>> d) {
		data = d;
	}
	
	public Hashtable<String,List<Double>> getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
		
	public List<String> getPlist() {
		return plist;
	}
	
	public void setThreshold(double t) {
		this.threshold = t;
	}
	
	public double getThreshold() {
		return this.threshold;
	}
	
}
