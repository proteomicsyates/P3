/**
 * diego
 * Aug 14, 2014
 */
package edu.scripps.p3.utilities.optimizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class Bait {

	private String bait;
	private String condition;
	private List<Prey> preys;
	private int known;
	private double min_quant;
	private double max_quant;
	private int known_interactors;
	private int novel_interactors;
	private double recall;
	private double powerlaw;
	private double range;
	private double raw_range;

	public Bait(String bait, String condition) {
		this.bait = bait;
		this.condition = condition;
		preys = new ArrayList<Prey>();
	}

	public void setKnown(int known) {
		this.known = known;
	}

	public void addPrey(Prey prey) {
		preys.add(prey);
	}

	public List<Prey> getPreyList() {
		return preys;
	}

	public void BaitStats() {

		recall = (double) (this.known_interactors) / (double) (this.known);

		int interactors = (int) (0.2 * known);

		this.novel_interactors = Math.abs(this.novel_interactors - interactors);

		if (this.novel_interactors != 0) {

			powerlaw = Math.pow(this.novel_interactors, -2);

		} else {
			powerlaw = 1;
		}

		// powerlaw = 1 - Math.pow(novel_interactors, 0.5);

		// powerlaw = Math.max(powerlaw, 0);

		/*
		 * if (this.novel_interactors != 0 ) {
		 * 
		 * 
		 * novel_interactors = novel_interactors - interactors;
		 * 
		 * if (novel_interactors < 1) { novel_interactors = 1; }
		 * 
		 * powerlaw = Math.pow(this.novel_interactors, -0.3);//-2); } else {
		 * powerlaw = 0; }
		 */
		range = (this.max_quant - this.min_quant) / raw_range;

	}

	public void setRange(double r) {
		this.raw_range = r;
	}

	/**
	 * @return the recall
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * @return the powerlaw
	 */
	public double getPowerlaw() {
		return powerlaw;
	}

	/**
	 * @return the range
	 */
	public double getRange() {
		return range;
	}

	public double getScore() {

		double score = 0;
		score += getRecall();
		score += getPowerlaw();
		score += getRange();

		if (score < 0) {
			score = 0;
		}

		return score;

	}

	public void processProteins(double w_corr, double w_clu, double w_quant, double w_phy, double w_gen, double t_conf,
			double t_confO) {

		double score;
		this.known_interactors = 0;
		this.novel_interactors = 0;

		for (Prey prey : preys) {

			score = w_corr * prey.getCorrelation();
			score += w_clu * prey.getCluster();
			score += w_quant * prey.getQuant();
			score += w_phy * prey.getPhy();
			score += w_gen * prey.getGen();

			if (score == 0) {
				continue;
			}

			if (prey.getPhy() > 0.1) {
				score += 0.1;

				if (score > t_confO) {
					known_interactors++;

					if (prey.getQratio() > this.max_quant) {
						max_quant = prey.getQratio();
					}

					if (prey.getQratio() < this.min_quant) {
						min_quant = prey.getQratio();
					}

					continue;
				}

			}

			if (score > t_conf) {
				novel_interactors++;

				if (prey.getQratio() > this.max_quant) {
					max_quant = prey.getQratio();
				}

				if (prey.getQratio() < this.min_quant) {
					min_quant = prey.getQratio();
				}

				continue;
			}

		}

	}

}
