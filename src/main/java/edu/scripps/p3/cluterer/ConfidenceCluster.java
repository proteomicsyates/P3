/**
 * diego Jun 14, 2013
 */
package edu.scripps.p3.cluterer;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.cluterer.cluster.Kmeans;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class ConfidenceCluster {

	private final List<List<Interactome>> interactomes;
	private final File lout;
	private StringBuffer log;
	private int iterations;
	private Hashtable<String, Integer> presencelist;

	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	/**
	 * @param interactomes
	 * @param logdir
	 */
	public ConfidenceCluster(List<List<Interactome>> interactomes, File logdir) {
		this.interactomes = interactomes;
		lout = logdir;
	}

	private int getFullSize() {
		int size = 0;

		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {
				final Interactome interactome = interactomes.get(k).get(i);
				size += interactome.getProteinsHavingANetwork().size();
			}
		}

		return size;
	}

	/**
	 *
	 */
	public void run() {

		log = new StringBuffer();
		iterations = 50;

		int fullsize = getFullSize();

		progressMonitor = new ProgressMonitor(null, "Calculating Cluster  Scores", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {

				final Interactome interactome = interactomes.get(k).get(i);
				List<String> baits = interactome.getProteinsHavingANetwork();

				for (int baitIndex = 0; baitIndex < baits.size(); baitIndex++) {

					Network net = interactome.getNetwork(baits.get(baitIndex));
					List<String> interactors = net.getInteractorsNames();
					Hashtable<String, List<Double>> featuresTable = net.getFeaturesValues();

					log.append("Working on " + net.getBait() + "\n");
					progressMonitor.setNote("Working on " + net.getBait());

					if (interactors.size() > 2) {
						for (int q = 0; q < iterations; q++) {
							// presencelist counts how many times a protein
							// appears in the cluster created by kmeans
							if (q == 0) {
								presencelist = new Hashtable<String, Integer>();
							}

							Kmeans kmean = new Kmeans(interactors, featuresTable, 2);
							kmean.setBait(net.getBait());
							kmean.run();
							List<String> clust = kmean.getClusters();
							// update presencelist
							setStatistics(clust);

						}

						createClusters(interactors, featuresTable, net);
					} else {

						presencelist = new Hashtable<String, Integer>();
						setStatistics(interactors);
						createClusters(interactors, null, net);
					}

					progress++;
					progressMonitor.setProgress(progress);

				}

			}
		}

		progressMonitor.close();

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(lout, log, "ClusterLog");

	}

	private void createClusters(List<String> plist, Hashtable<String, List<Double>> pdata, Network net) {

		double distance;

		String bait = net.getBait();

		for (String pname : plist) {

			distance = 0;

			if (!pname.equals(bait)) {
				if (presencelist.containsKey(pname)) {
					int presence = presencelist.get(pname);
					// Salva change Jan 10th 2017: get the distance not only
					// when the protein is clustered with the bait 5 out 5
					// clusters (when iterations was 5, because I changed it to
					// 50), but at least half of them?
					// if (presence == iterations) {
					if (presence >= Double.valueOf(iterations / 2.0)) {
						distance = getDifference(pdata.get(bait), pdata.get(pname));

						distance = 1 - distance;
						if (distance < 0) {
							distance = 0;
						}

					}
				}
				log.append("Confidence between:\t" + bait + "\t" + pname + "\t" + distance + "\n");
				net.getInteractionByInteractorName(pname).setCluster_score(distance);
			} else {
				net.getInteractionByInteractorName(pname).setCluster_score(1.0);
			}

		}

	}

	private double getDifference(List<Double> bait, List<Double> prey) {

		double score = 0;

		if (prey != null) {
			for (int i = 0; i < bait.size(); i++) {
				score += Math.pow(bait.get(i) - prey.get(i), 2);
			}
			score = Math.sqrt(score);
		} else {
			score = Double.MAX_VALUE;
		}

		return score;
	}

	private void setStatistics(List<String> clust) {

		for (int i = 0; i < clust.size(); i++) {

			String protein = clust.get(i);
			if (presencelist.containsKey(protein)) {
				int presence = presencelist.get(protein);
				presence++;
				presencelist.put(protein, presence);
			} else {
				int presence = 1;
				presencelist.put(protein, presence);
			}

		}

	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
