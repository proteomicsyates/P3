/**
 * diego
 * May 10, 2013
 */
package edu.scripps.p3.experimentallist.network.interaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class Interaction {

	private String name;
	private double correlation_score;
	private double cluster_score;
	private double quant_score;
	private List<Double> physical_scores;
	private List<Double> genetical_scores;
	private List<Double> other_scores;
	private List<Double> p_coefficient;
	private List<Double> g_coefficient;
	private List<Double> o_coefficient;
	private boolean pscores;
	private boolean gscores;
	private boolean oscores;
	private boolean lysatelike;
	
	
	private double internal_score;
	private double physical_score;
	private double genetic_score;
	private double confidence_score;
	private int lysate_behavior;
	private int quant_behvior;
	
	
	public Interaction(String name) {
		this.name = name;
		correlation_score = 0;
		cluster_score = 0;
		quant_score = 0;
		pscores = false;
		gscores = false;
		oscores = false;
		lysatelike = false;
		
	}

	public void addPhysical_score(double physical_score, double p_coef) {
		
		if (isPscores()) {
			physical_scores.add(physical_score);
			p_coefficient.add(p_coef);
		} else {
			pscores = true;
			physical_scores = new ArrayList<Double>();
			p_coefficient = new ArrayList<Double>();
			physical_scores.add(physical_score);
			p_coefficient.add(p_coef);
		}
		
	}
	
	public void addGenetical_score(double genetical_score, double g_coef) {
		
		if (isGscores()) {
			genetical_scores.add(genetical_score);
			g_coefficient.add(g_coef);
		} else {
			gscores = true;
			genetical_scores = new ArrayList<Double>();
			g_coefficient = new ArrayList<Double>();
			genetical_scores.add(genetical_score);
			g_coefficient.add(g_coef);
		}
		
	}
	
	public void addOther_score(double other_score, double o_coef) {
		
		if (isOscores()) {
			other_scores.add(other_score);
		} else {
			oscores = true;
			other_scores = new ArrayList<Double>();
			o_coefficient = new ArrayList<Double>();
			other_scores.add(other_score);
			o_coefficient.add(o_coef);
		}
		
	}
	
	public void setLysateLike(boolean value) {
		lysatelike = value;
	}
	
	public boolean getLysateLike() {
		return lysatelike;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the correlation_score
	 */
	public double getCorrelation_score() {
		return correlation_score;
	}

	/**
	 * @param correlation_score the correlation_score to set
	 */
	public void setCorrelation_score(double correlation_score) {
		this.correlation_score = correlation_score;
	}

	/**
	 * @return the cluster_score
	 */
	public double getCluster_score() {
		return cluster_score;
	}

	/**
	 * @param cluster_score the cluster_score to set
	 */
	public void setCluster_score(double cluster_score) {
		this.cluster_score = cluster_score;
	}

	/**
	 * @return the quant_score
	 */
	public double getQuant_score() {
		return quant_score;
	}

	/**
	 * @param quant_score the quant_score to set
	 */
	public void setQuant_score(double quant_score) {
		this.quant_score = quant_score;
	}

	/**
	 * @return the pscores
	 */
	public boolean isPscores() {
		return pscores;
	}

	/**
	 * @return the gscores
	 */
	public boolean isGscores() {
		return gscores;
	}

	/**
	 * @return the oscores
	 */
	public boolean isOscores() {
		return oscores;
	}

	/**
	 * @return the physical_scores
	 */
	public List<Double> getPhysical_scores() {
		return physical_scores;
	}

	/**
	 * @return the genetical_scores
	 */
	public List<Double> getGenetical_scores() {
		return genetical_scores;
	}

	/**
	 * @return the other_scores
	 */
	public List<Double> getOther_scores() {
		return other_scores;
	}

	/**
	 * @return the p_coefficient
	 */
	public List<Double> getP_coefficient() {
		return p_coefficient;
	}

	/**
	 * @return the g_coefficient
	 */
	public List<Double> getG_coefficient() {
		return g_coefficient;
	}

	/**
	 * @return the internal_score
	 */
	public double getInternal_score() {
		return internal_score;
	}

	/**
	 * @param internal_score the internal_score to set
	 */
	public void setInternal_score(double internal_score) {
		this.internal_score = internal_score;
	}

	/**
	 * @return the physical_score
	 */
	public double getPhysical_score() {
		return physical_score;
	}

	/**
	 * @param physical_score the physical_score to set
	 */
	public void setPhysical_score(double physical_score) {
		this.physical_score = physical_score;
	}

	/**
	 * @return the genetic_score
	 */
	public double getGenetic_score() {
		return genetic_score;
	}

	/**
	 * @param genetic_score the genetic_score to set
	 */
	public void setGenetic_score(double genetic_score) {
		this.genetic_score = genetic_score;
	}

	/**
	 * @return the confidence_score
	 */
	public double getConfidence_score() {
		return confidence_score;
	}

	/**
	 * @param confidence_score the confidence_score to set
	 */
	public void setConfidence_score(double confidence_score) {
		this.confidence_score = confidence_score;
	}

	/**
	 * @return the lysate_behavior
	 */
	public int getLysate_behavior() {
		return lysate_behavior;
	}

	/**
	 * @param lysate_behavior the lysate_behavior to set
	 */
	public void setLysate_behavior(int lysate_behavior) {
		this.lysate_behavior = lysate_behavior;
	}

	/**
	 * @return the quant_behvior
	 */
	public int getQuant_behvior() {
		return quant_behvior;
	}

	/**
	 * @param quant_behvior the quant_behvior to set
	 */
	public void setQuant_behvior(int quant_behvior) {
		this.quant_behvior = quant_behvior;
	}
	
	
	
}
