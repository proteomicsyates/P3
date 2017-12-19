/**
 * diego
 * Feb 7, 2013
 */
package edu.scripps.p3.correlator.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class Complex {
	// a list of proteins per protein in an eperiment+condition
	private List<List<String>> complexes;

	private String bait;
	private String condition;

	public Complex() {

		complexes = new ArrayList<List<String>>();
	}

	public Complex(String bait, String condition) {

		this.bait = bait;
		this.condition = condition;
		complexes = new ArrayList<List<String>>();

	}

	/**
	 * Adds a list of proteins that have certain correlation
	 * 
	 * @param element
	 */
	public void addComplex(List<String> element) {
		complexes.add(element);
	}

	public List<List<String>> getComplexes() {
		return complexes;
	}

	public String getBait() {
		return bait;
	}

	public String getCondition() {
		return condition;
	}
}
