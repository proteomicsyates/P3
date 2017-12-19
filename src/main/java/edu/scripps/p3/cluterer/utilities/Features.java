package edu.scripps.p3.cluterer.utilities;

import java.util.Hashtable;
import java.util.List;

public class Features {

	Hashtable<String, List<Double>> featuresPerPrey;
	List<String> preyList;
	String name;
	double threshold;

	public Features(String n) {
		name = n;

	}

	public void setFeaturesPerPreyData(List<String> preysWithFeatures, Hashtable<String, List<Double>> d) {
		featuresPerPrey = d;
		this.preyList = preysWithFeatures;
	}

	public Hashtable<String, List<Double>> getFeaturesByPrey() {
		return featuresPerPrey;
	}

	public String getName() {
		return name;
	}

	public List<String> getPreylist() {
		return preyList;
	}

	public void setThreshold(double t) {
		this.threshold = t;
	}

	public double getThreshold() {
		return this.threshold;
	}

}
