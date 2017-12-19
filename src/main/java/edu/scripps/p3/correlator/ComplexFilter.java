/**
 * diego Jun 13, 2013
 */
package edu.scripps.p3.correlator;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.correlator.utilities.Complex;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class ComplexFilter {

	private final List<List<Complex>> complex_list;
	private List<List<Interactome>> interactomes;
	private final File logdir;
	private StringBuffer log;

	private double correlation_threshold = 0.01;

	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	public ComplexFilter(List<List<Complex>> complex_list, File logdir) {

		this.complex_list = complex_list;
		this.logdir = logdir;

	}

	public void run() {

		log = new StringBuffer();
		interactomes = new ArrayList<List<Interactome>>();

		int fullsize = getFullSize();
		List<String> element;

		progressMonitor = new ProgressMonitor(null, "Calculating Correlation Scores", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int experimentIndex = 0; experimentIndex < complex_list.size(); experimentIndex++) {

			List<Interactome> inters = new ArrayList<Interactome>();
			interactomes.add(inters);

			double ratio;

			final List<Complex> experimentComplexes = complex_list.get(experimentIndex);
			for (int conditionID = 0; conditionID < experimentComplexes.size(); conditionID++) {
				Hashtable<String, Integer> frequency = new Hashtable<String, Integer>();
				Hashtable<String, Integer> couple_frequency = new Hashtable<String, Integer>();

				List<String> proteinNames = new ArrayList<String>();
				List<String> couple_names = new ArrayList<String>();

				final Complex experimentConditionComplex = experimentComplexes.get(conditionID);
				log.append("Computing Frequencies for " + experimentConditionComplex.getBait() + "\t"
						+ experimentConditionComplex.getCondition() + " with correlation threshold:"
						+ correlation_threshold + "\n");
				log.append("Protein_pair\tProtein1_freq\tProtein2_freq\tPair_freq\tratio\tValid\n");
				progressMonitor.setNote("Processing " + experimentConditionComplex.getBait() + " for "
						+ experimentConditionComplex.getCondition());

				String pairKey;
				String reversePairKey;
				String[] split;

				List<List<String>> cmpxPerProtein = experimentConditionComplex.getComplexes();

				for (int proteinIndex = 0; proteinIndex < cmpxPerProtein.size(); proteinIndex++) {

					element = cmpxPerProtein.get(proteinIndex);

					if (element != null) {
						for (int otherProteinIndex = 0; otherProteinIndex < element.size(); otherProteinIndex++) {

							final String otherProteinName = element.get(otherProteinIndex);
							if (frequency.containsKey(otherProteinName)) {

								int freq = frequency.get(otherProteinName);
								freq++;
								frequency.put(otherProteinName, freq);
							} else {
								int freq = 1;
								frequency.put(otherProteinName, freq);
								proteinNames.add(otherProteinName);
							}

						}

						List<String> couples = getCouples(element);

						for (int k = 0; k < couples.size(); k++) {

							final String couple = couples.get(k);
							split = couple.split("_");
							pairKey = split[0] + "_" + split[1];
							reversePairKey = split[1] + "_" + split[0];

							if (!couple_frequency.containsKey(pairKey)
									&& !couple_frequency.containsKey(reversePairKey)) {

								int cfreq = 1;
								couple_frequency.put(pairKey, cfreq);
								couple_names.add(pairKey);

							} else {

								if (couple_frequency.containsKey(pairKey)) {

									int cfreq = couple_frequency.get(pairKey);
									cfreq++;
									couple_frequency.put(pairKey, cfreq);

								} else {

									int cfreq = couple_frequency.get(reversePairKey);
									cfreq++;
									couple_frequency.put(reversePairKey, cfreq);
								}

							}

						}
					}

					progress++;
					progressMonitor.setProgress(progress);
				}

				Interactome interactome = new Interactome(experimentConditionComplex.getBait(),
						experimentConditionComplex.getCondition());
				interactome.isEmpty();

				interactomes.get(experimentIndex).add(interactome);

				for (int coupleIndex = 0; coupleIndex < couple_names.size(); coupleIndex++) {

					final String coupleKey = couple_names.get(coupleIndex);
					double coupleFrecuency = couple_frequency.get(coupleKey);

					String[] n = coupleKey.split("_");
					final String coupleProtein1 = n[0];
					final String coupleProtein2 = n[1];

					double protein1Frecuency, protein2Frecuency;

					if (frequency.containsKey(coupleProtein1)) {
						protein1Frecuency = frequency.get(coupleProtein1);
					} else {
						protein1Frecuency = 1;
					}
					if (frequency.containsKey(coupleProtein2)) {
						protein2Frecuency = frequency.get(coupleProtein2);
					} else {
						protein2Frecuency = 1;
					}

					double minProteinFrecuency = Math.min(protein1Frecuency, protein2Frecuency);

					ratio = coupleFrecuency / minProteinFrecuency;

					if (ratio > correlation_threshold) { // it was 0.01

						log.append(coupleKey + "\t" + +protein1Frecuency + "\t" + protein2Frecuency + "\t"
								+ coupleFrecuency + "\t" + ratio + "\taccepted\n");

						if (interactome.isNetworkinSystem(coupleProtein1)) {

							Network net = interactome.getNetwork(coupleProtein1);

							Interaction inter = new Interaction(coupleProtein2);
							inter.setCorrelation_score(ratio);

							net.addInteraction(coupleProtein2, inter);

						} else {

							Network net = new Network(coupleProtein1);
							Interaction inter = new Interaction(coupleProtein2);
							inter.setCorrelation_score(ratio);
							net.addInteraction(coupleProtein2, inter);
							interactome.addNetwork(net);

						}

						if (interactome.isNetworkinSystem(coupleProtein2)) {

							Network net = interactome.getNetwork(coupleProtein2);
							Interaction inter = new Interaction(coupleProtein1);
							inter.setCorrelation_score(ratio);
							net.addInteraction(coupleProtein1, inter);

						} else {

							Network net = new Network(coupleProtein2);
							Interaction inter = new Interaction(coupleProtein1);
							inter.setCorrelation_score(ratio);
							net.addInteraction(coupleProtein1, inter);
							interactome.addNetwork(net);

						}

					} else {
						log.append(coupleKey + "\t" + protein1Frecuency + "\t" + protein2Frecuency + "\t"
								+ coupleFrecuency + "\t" + ratio + "\tdiscarded\n");
					}

				}

			}

		}

		progressMonitor.close();

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "CorrelationScoreLog");

	}

	private int getFullSize() {
		int size = 0;

		for (int i = 0; i < complex_list.size(); i++) {
			for (int j = 0; j < complex_list.get(i).size(); j++) {
				size += complex_list.get(i).get(j).getComplexes().size();
			}
		}

		return size;
	}

	private final List<String> couples = new ArrayList<String>();

	private List<String> getCouples(List<String> element) {

		String key;

		couples.clear();

		for (int i = 0; i < element.size(); i++) {
			for (int j = i; j < element.size(); j++) {

				key = element.get(i) + "_" + element.get(j);

				couples.add(key);

			}
		}

		return couples;
	}

	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

	/**
	 * @param correlationT
	 */
	public void setCorrelationThreshold(double correlationT) {
		correlation_threshold = correlationT;

	}

}
