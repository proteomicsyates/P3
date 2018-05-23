package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Differential {

	Hashtable<String, Double> data;
	String name;

	public Differential(String name) {
		this.name = name;
		data = new Hashtable<String, Double>();
	}

	/**
	 * Adds a ratio value associated with the protein. This value should be a
	 * median of individual peptide ratios. <br>
	 * if the protein already have a value, the new value will be the averaeg of
	 * both values.
	 * 
	 * @param name
	 * @param value
	 */
	public void addValue(String name, double value) {

		if (data.containsKey(name)) {

			double oldvalue = data.get(name);
			value = (oldvalue + value) / 2;
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
	 * @param data
	 *            the data to set
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the ratio value of the protein.<br>
	 * That ratio value is the median of the individual peptide ratios in a
	 * quantcompare file. If more than one quantcompare file is provided for the
	 * same bait, and the protein is present in both files, the value will
	 * correspond to the average of the two median values.
	 * 
	 * @param prot
	 * @return
	 */
	public double getDiffValue(String prot) {
		if (data.containsKey(prot)) {
			return data.get(prot);
		} else {
			return -1;
		}
	}

	/**
	 * Gets the name of the proteins
	 * 
	 * @return
	 */
	public List<String> getQlist() {

		List<String> qlist = new ArrayList<String>();

		Enumeration<String> enumkeys = data.keys();
		String key;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			qlist.add(key);

		}

		return qlist;

	}

}
