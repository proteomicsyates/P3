/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.cluterer;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.cluterer.cluster.Kmeans;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.io.dataIO;

/**
 * @author diego
 * 
 */
public class ConfidenceCluster {

	private List<List<Interactome>> interactomes;
	private File lout;
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
		this.lout = logdir;
	}

	private int getFullSize() {
		int size = 0;

		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {
				size += interactomes.get(k).get(i).getNetlist().size();
			}
		}

		return size;
	}

	/**
	 * 
	 */
	public void run() {

		log = new StringBuffer();
		iterations = 5;

		int fullsize = getFullSize();

		progressMonitor = new ProgressMonitor(null,
				"Calculating Cluster Scores", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {

				List<String> names = interactomes.get(k).get(i).getNetlist();

				for (int j = 0; j < names.size(); j++) {

					Network net = interactomes.get(k).get(i)
							.getNetwork(names.get(j));
					List<String> plist = net.getInteraction_names();
					Hashtable<String, List<Double>> pdata = net
							.getFeaturesValues();

					log.append("Working on " + net.getExpName() + "\n");
					progressMonitor.setNote("Working on " + net.getExpName());
					
					if (plist.size() > 2) {
						for (int q = 0; q < iterations; q++) {

							if (q == 0) {
								presencelist = new Hashtable<String, Integer>();
							}

							Kmeans kmean = new Kmeans(plist, pdata, 2);
							kmean.setBait(net.getExpName());
							kmean.run();
							List<String> clust = kmean.getClusters();
							setStatistics(clust);

						}

						createClusters(plist, pdata, net);
					} else {
						
						presencelist = new Hashtable<String, Integer>();
						setStatistics(plist);
						createClusters(plist, null, net);
					}

					progress++;
					progressMonitor.setProgress(progress);

				}

			}
		}

		progressMonitor.close();

		dataIO dIO = new dataIO();
		dIO.writeLog(lout, log, "ClusterLog");

	}

	private void createClusters(List<String> plist,
			Hashtable<String, List<Double>> pdata, Network net) {

		double distance;

		String bait = net.getExpName();

		for (String pname : plist) {

			distance = 0;

			if (!pname.equals(bait)) {
				if (presencelist.containsKey(pname)) {
					int presence = presencelist.get(pname);
					if (presence == iterations) {
						distance = getDifference(pdata.get(bait),
								pdata.get(pname));
												
						distance = 1 - distance;
						if (distance < 0) {
							distance = 0;
						}

					}
				}
				log.append("Confidence between:\t" + bait + "\t" + pname + "\t"
						+ distance + "\n");
				net.getInteraction(pname).setCluster_score(distance);
			} else {
				net.getInteraction(pname).setCluster_score(1.0);
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

			if (presencelist.containsKey(clust.get(i))) {
				int presence = presencelist.get(clust.get(i));
				presence++;
				presencelist.put(clust.get(i), presence);
			} else {
				int presence = 1;
				presencelist.put(clust.get(i), presence);
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
