/**
 * diego Nov 21, 2013
 */
package edu.scripps.p3.calculators;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class ConfidenceStratifier {

	File logdir;
	List<List<Interactome>> interactomes;
	List<String> pooledInteractors;
	StringBuffer log;
	StringBuffer matrix;

	private List<List<Hashtable<String, String>>> maps;
	private List<List<String>> maps_names;

	/**
	 * @param interactomes
	 * @param logdir
	 */
	public ConfidenceStratifier(List<List<Interactome>> interactomes, File logdir) {
		this.logdir = logdir;
		this.interactomes = interactomes;
	}

	/**
	 *
	 */
	public void run() {

		/**
		 * create a list of the interactors for a condition for each find how
		 * many times it replicates in the baits stratify that values filter for
		 * 0.1
		 *
		 */

		stratify();
		filter();

	}

	private void stratify() {
		int w_size;
		Hashtable<String, double[]> values;

		matrix = new StringBuffer();

		SummaryStatistics stats = new SummaryStatistics();

		for (int i = 0; i < interactomes.size(); i++) {
			for (int j = 0; j < interactomes.get(i).size(); j++) {

				Interactome interactome = interactomes.get(i).get(j);

				w_size = interactome.getProteinsHavingANetwork().size();

				values = new Hashtable<String, double[]>();

				String[] expbaits = interactomes.get(i).get(j).getBait_name().split("-");

				matrix.append("\n\nProtein");

				for (String expbait : expbaits) {
					matrix.append("\t" + expbait);
				}

				matrix.append("\n");

				for (int k = 0; k < w_size; k++) {

					Network network = interactome.getNetwork(interactome.getProteinsHavingANetwork().get(k));

					List<String> interactors = network.getInteractorsNames();

					for (String interactor : interactors) {

						if (values.containsKey(interactor)) {

							double[] scores = values.get(interactor);
							scores[k] = network.getInteractionByInteractorName(interactor).getConfidence_score();
							values.put(interactor, scores);

						} else {

							double[] scores = new double[w_size];
							scores[k] = network.getInteractionByInteractorName(interactor).getConfidence_score();
							values.put(interactor, scores);

						}

					}

				}

				Enumeration<String> enumkeys = values.keys();

				double mean;
				double std;
				double ratio;

				while (enumkeys.hasMoreElements()) {

					String key = enumkeys.nextElement();

					stats = new SummaryStatistics();

					matrix.append(key);

					double[] scores = values.get(key);

					// sum=0.0;

					for (double score : scores) {

						matrix.append("\t" + score);

						stats.addValue(score);

						// sum += score;
					}

					mean = stats.getMean();
					std = stats.getStandardDeviation();

					ratio = std / mean;

					matrix.append("\n");

					for (int k = 0; k < scores.length; k++) {

						// if (ratio < 1 && scores[k]>0) {
						// scores[k] = 0.001;
						// } else {
						scores[k] = scores[k];// / sum;
						// }

						if (scores[k] > 0) {
							interactome.getNetwork(interactome.getProteinsHavingANetwork().get(k)).getInteractionByInteractorName(key)
									.setConfidence_score(scores[k]);
						}

					}

				}

			}
		}

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, matrix, "StratifiedMatrix");

	}

	private void filter() {

		log = new StringBuffer();
		String current_bait;

		double i_score;
		double p_score;
		double g_score;
		double score;
		int lys;
		int qbeh;

		StringBuffer row;

		maps = new ArrayList<List<Hashtable<String, String>>>();
		maps_names = new ArrayList<List<String>>();

		String[] expbaits;

		for (int k = 0; k < interactomes.size(); k++) {

			List<Hashtable<String, String>> maps_i = new ArrayList<Hashtable<String, String>>();
			List<String> maps_names_i = new ArrayList<String>();

			maps.add(maps_i);
			maps_names.add(maps_names_i);

			for (int i = 0; i < interactomes.get(k).size(); i++) {

				final Interactome interactome = interactomes.get(k).get(i);
				log.append("Working on\t" + interactome.getConditionName() + "\n");

				expbaits = interactome.getBait_name().split("-");

				for (int b = 0; b < expbaits.length; b++) {
					current_bait = expbaits[b];

					log.append("Working on bait:\t" + current_bait + "\n");
					log.append(
							"Protein\tInternal\t[Correlation\tCluster\tQuantitative]\tPhysical\tGenetic\tComposite\tLysate Trend\tQuant Trend\n");

					Network bait = interactome.getNetwork(current_bait);

					List<String> interactors = bait.getInteractorsNames();

					Hashtable<String, String> map = new Hashtable<String, String>();

					maps.get(k).add(map);
					maps_names.get(k).add(current_bait + "_" + interactome.getConditionName());

					List<String> interactionsToRemove = new ArrayList<String>();

					for (String interactor : interactors) {

						Interaction inter = bait.getInteractionByInteractorName(interactor);

						row = new StringBuffer();

						row.append(interactor + "\t");

						i_score = inter.getInternal_score();
						p_score = inter.getPhysical_score();
						g_score = inter.getGenetic_score();
						score = inter.getConfidence_score();
						lys = inter.getLysate_behavior();
						qbeh = inter.getQuant_behvior();

						row.append(i_score + "\t" + inter.getCorrelation_score() + "\t" + inter.getCluster_score()
								+ "\t" + inter.getQuant_score() + "\t" + p_score + "\t" + g_score + "\t" + score);
						if (lys == 1) {
							row.append("Y\t");
						} else {
							row.append("\t");
						}
						if (qbeh == 1) {
							row.append("Y\t");
						} else {
							row.append("\t");
						}

						if (score > 0.1) {

							map.put(interactor,
									score + "_" + i_score + "_" + p_score + "_" + g_score + "_" + lys + "_" + qbeh);
							log.append(row.toString() + "\n");

						} else {
							log.append(row.toString() + "discarded\n");
							interactionsToRemove.add(interactor);
						}

					}

					for (String interactor : interactionsToRemove) {
						bait.removeInteraction(interactor);
					}

				}

			}

		}

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "StratifiedConfidenceScore");

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

}
