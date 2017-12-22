/**
 * diego Jun 14, 2013
 */
package edu.scripps.p3.calculators;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class ConfidenceCalculator {

	private final List<List<Interactome>> interactomes;
	private final String[] baits;
	private final File logdir;

	private StringBuffer log;
	private StringBuffer row;
	private StringBuffer extras;

	private double internal;
	private double physical;
	private double genetic;

	private double corr;
	private double clus;
	private double quant;

	private boolean bonus;

	private double confidenceT;
	private double confidenceOrtoT;

	private String current_bait;

	private List<List<Hashtable<String, String>>> maps;
	private List<List<String>> maps_names;

	/**
	 * @param interactomes
	 * @param baits
	 * @param logdir
	 */
	public ConfidenceCalculator(List<List<Interactome>> interactomes, String[] baits, File logdir) {
		this.interactomes = interactomes;
		this.baits = baits;
		this.logdir = logdir;
	}

	/**
	 * @param internalW
	 * @param physicalW
	 * @param geneticW
	 */
	public void setCoeff(double internalW, double physicalW, double geneticW) {
		internal = internalW;
		physical = physicalW;
		genetic = geneticW;

	}

	/**
	 * @param correlationW
	 * @param clusterW
	 * @param quantitativeW
	 */
	public void setInternalCoeff(double correlationW, double clusterW, double quantitativeW) {
		corr = correlationW;
		clus = clusterW;
		quant = quantitativeW;

	}

	/**
	 * @param bonus
	 */
	public void setBonus(boolean bonus) {
		this.bonus = bonus;

	}

	/**
	 *
	 */
	public void run() {

		log = new StringBuffer();

		log.append("Coefficients\n");
		log.append("-Internal\n");
		log.append("Correlation:\t" + corr + "\n");
		log.append("Cluster:\t" + clus + "\n");
		log.append("Quant:\t" + quant + "\n");
		log.append("-Categories\n");
		log.append("Internal Metric:\t" + internal + "\n");
		log.append("Physical:\t" + physical + "\n");
		log.append("Genetic:\t" + genetic + "\n");
		log.append("Score Bonus:\t" + bonus + "\n");
		log.append("\n");

		double i_score;
		double p_score;
		double g_score;
		double score;
		int lys;
		int qbeh;

		double threshold;

		maps = new ArrayList<List<Hashtable<String, String>>>();
		maps_names = new ArrayList<List<String>>();

		for (int k = 0; k < interactomes.size(); k++) {

			String[] expbaits = baits[k].split("-");

			List<Hashtable<String, String>> maps_i = new ArrayList<Hashtable<String, String>>();
			List<String> maps_names_i = new ArrayList<String>();

			maps.add(maps_i);
			maps_names.add(maps_names_i);

			for (int i = 0; i < interactomes.get(k).size(); i++) {

				final Interactome interactome = interactomes.get(k).get(i);
				log.append("Working on\t" + interactome.getConditionName() + "\n");

				for (int j = 0; j < expbaits.length; j++) {

					log.append("Working on bait:\t" + expbaits[j] + "\n");
					log.append(
							"Protein\tInternal\t[Correlation\tCluster\tQuantitative]\tPhysical\tGenetic\tComposite\tLysate Trend\tQuant Trend\n");

					current_bait = expbaits[j];

					if (interactome.getNetworksByProteinName().isEmpty()) {
						continue;
					}

					Network bait = interactome.getNetwork(current_bait);

					if (bait.getInteractorsNames().size() > 0) {
						List<String> interactors = bait.getInteractorsNames();

						Hashtable<String, String> map = new Hashtable<String, String>();

						maps.get(k).add(map);
						maps_names.get(k).add(expbaits[j] + "_" + interactome.getConditionName());

						List<String> interactionsToRemove = new ArrayList<String>();
						Map<String, Double> interactionMap = new HashMap<String, Double>();
						for (String interactor : interactors) {
							Interaction inter = bait.getInteractionByInteractorName(interactor);

							i_score = getInternalScore(inter, false);
							p_score = getPhysicalScore(inter, k, i, false);
							g_score = getGeneticScore(inter, k, i, false);
							score = getFinalScore(i_score, p_score, g_score, false);
							interactionMap.put(interactor, score);
						}
						// sort by the composite score
						Collections.sort(interactors, new Comparator<String>() {

							@Override
							public int compare(String o1, String o2) {
								return Double.compare(interactionMap.get(o2), interactionMap.get(o1));
							}

						});
						for (String interactor : interactors) {

							Interaction inter = bait.getInteractionByInteractorName(interactor);

							row = new StringBuffer();

							row.append(interactor + "\t");

							extras = new StringBuffer();

							i_score = getInternalScore(inter, true);
							p_score = getPhysicalScore(inter, k, i, true);
							g_score = getGeneticScore(inter, k, i, true);
							score = getFinalScore(i_score, p_score, g_score, true);
							lys = checkLysateBehavior(inter);
							qbeh = checkQuantBehavior(inter);

							inter.setInternal_score(i_score);
							inter.setPhysical_score(p_score);
							inter.setGenetic_score(g_score);
							inter.setConfidence_score(score);
							inter.setLysate_behavior(lys);
							inter.setQuant_behvior(qbeh);

							/*
							 * if (p_score==0 && g_score==0) { threshold = 0.25;
							 * //was 0.2 // was 0.5 } else { threshold = 0.1; }
							 */

							threshold = confidenceT;// 0.5;//0.5; //0.25

							if (p_score >= 0.1) {
								threshold = confidenceOrtoT;// 0.1;
							}

							if (score > threshold) {

								map.put(interactor,
										score + "_" + i_score + "_" + p_score + "_" + g_score + "_" + lys + "_" + qbeh);
								log.append(row.toString() + "\t" + extras.toString() + "\n");

							} else {
								log.append(row.toString() + "discarded\t" + extras.toString() + "\n");
								interactionsToRemove.add(interactor);
							}
						}

						for (String interactor : interactionsToRemove) {
							bait.removeInteraction(interactor);
						}

					}

				}

			}
		}

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "ConfidenceScore");

	}

	private int checkLysateBehavior(Interaction inter) {
		if (inter.getLysateLike()) {
			row.append("Y\t");
			return 1;
		} else {
			row.append("\t");
			return 0;
		}
	}

	private int checkQuantBehavior(Interaction inter) {

		if (inter.getName().equals(current_bait)) {
			row.append("\t");
			return 0;
		} else {
			double qval = inter.getQuant_score();
			if (qval != 0) {
				row.append("Y\t");
				return 1;
			} else {
				row.append("\t");
				return 0;
			}
		}

	}

	private double getFinalScore(double iscore, double pscore, double gscore, boolean appendToLog) {

		double score;
		// double norm=0;

		score = internal * iscore + physical * pscore + genetic * gscore;

		if (bonus) {
			if (pscore > 0.1) {
				score = score + 0.1;
			}
		}

		if (score > 1) {
			score = 1;
		}

		/*
		 * if (iscore!=0) { norm += internal; } if (pscore!=0) { norm +=
		 * physical; } if (gscore!=0) { norm += genetic; } if (norm==0) {
		 * norm=1; } score /= norm;
		 */

		/*
		 * if (bonus) { // if (pscore>0 && gscore>0) { //was 0.1, 0.1 //0.01 //
		 * score = score + 0.1; if (pscore> 0.1) { //was 0.9 //0.01 score =
		 * score + 0.1; //was 0.05 } // if (gscore>0.01) { //was 0.9 // score =
		 * score + 0.1; //was 0.05 // } if (score>1) { score = 1.0; } // } }
		 */
		if (appendToLog) {
			row.append(score + "\t");
		}
		return score;
	}

	private double getGeneticScore(Interaction inter, int id1, int id2, boolean appendToLog) {
		double score = 0;

		if (inter.isGscores()) {
			List<Double> scores = inter.getGenetical_scores();
			List<Double> coeff = inter.getG_coefficient();
			List<String> names = interactomes.get(id1).get(id2).getGenetical_names();

			double norm = 0;

			double max = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < scores.size(); i++) {
				score = scores.get(i) * coeff.get(i);

				if (score > max) {
					max = score;
				}

				if (score > 0) {
					if (appendToLog) {
						extras.append(names.get(i) + " ");
					}
				}
			}

			score = max;

			for (int i = 0; i < scores.size(); i++) {

				if ((scores.get(i) * coeff.get(i)) != max) {
					if (scores.get(i) != 0.0) {
						score += (coeff.get(i) / 10.0);
					}
				}

			}

			/*
			 * for (int i=0; i < scores.size(); i++) { score += scores.get(i) *
			 * coeff.get(i); if (scores.get(i)!=0) { norm += coeff.get(i); } }
			 */

			if (norm == 0) {
				norm = 1;
			}

			/* score /= norm; */

			if (score > 1) {
				score = 1.0;
			}

		}
		if (appendToLog) {
			row.append(score + "\t");
			extras.append("\t");
		}
		return score;
	}

	private double getPhysicalScore(Interaction inter, int id1, int id2, boolean appendToLog) {

		double score = 0;

		if (inter.isPscores()) {
			List<Double> scores = inter.getPhysical_scores();
			List<Double> coeff = inter.getP_coefficient();
			List<String> names = interactomes.get(id1).get(id2).getPhysical_names();

			double norm = 0;

			double max = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < scores.size(); i++) {
				score = scores.get(i) * coeff.get(i);

				if (score > max) {
					max = score;
				}

				if (score > 0) {
					if (appendToLog) {
						extras.append(names.get(i) + " ");
					}
				}

			}

			score = max;

			for (int i = 0; i < scores.size(); i++) {

				if ((scores.get(i) * coeff.get(i)) != max) {
					if (scores.get(i) != 0.0) {
						score += (coeff.get(i) / 10.0);
					}
				}

			}

			/*
			 * for (int i=0; i < scores.size(); i++) { score += scores.get(i) *
			 * coeff.get(i); if (scores.get(i)!=0) { norm += coeff.get(i); } }
			 */

			if (norm == 0) {
				norm = 1;
			}

			// score /= norm;

			if (score > 1) {
				score = 1.0;
			}

		}
		if (appendToLog) {
			row.append(score + "\t");
			extras.append("\t");
		}
		return score;
	}

	private double getInternalScore(Interaction inter, boolean appendToLog) {

		double corr_s = inter.getCorrelation_score();
		double clu_s = inter.getCluster_score();
		double qua_s = inter.getQuant_score();

		// double norm=corr + clus + quant;

		double score = corr * corr_s + clus * clu_s + quant * qua_s;
		// score = score / norm;
		if (appendToLog) {
			row.append(score + "\t" + corr_s + "\t" + clu_s + "\t" + qua_s + "\t");
		}
		return score;
	}

	/**
	 * @return the maps
	 */
	public List<List<Hashtable<String, String>>> getMaps() {
		return maps;
	}

	/**
	 * @return the maps_names
	 */
	public List<List<String>> getMaps_names() {
		return maps_names;
	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

	/**
	 * @param confidenceT2
	 * @param confidenceOrtoT2
	 */
	public void setFinalConfidences(double confidenceT, double confidenceOrtoT) {
		this.confidenceT = confidenceT;
		this.confidenceOrtoT = confidenceOrtoT;

	}

}
