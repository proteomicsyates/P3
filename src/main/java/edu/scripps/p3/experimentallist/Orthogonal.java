/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.experimentallist;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author diego
 *
 */
public class Orthogonal {

	public static final String PHYSICAL = "PHYSICAL";
	public static final String GENETIC = "GENETIC";

	TObjectDoubleHashMap<String> table;
	String name;
	double coeff;
	String type;

	public Orthogonal(String name) {
		this.name = name;
		table = new TObjectDoubleHashMap<String>();
	}

	public void setCoefficient(double d) {
		coeff = d;
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

		final List<String> list = new ArrayList<String>();

		list.addAll(table.keySet());
		return list;

	}

}
