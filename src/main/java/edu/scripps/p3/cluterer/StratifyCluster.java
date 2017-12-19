/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.cluterer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;

/**
 * @author diego
 *
 */
public class StratifyCluster {

	List<List<Interactome>> interactomes;
	String[] baits;

	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	/**
	 * @param interactomes
	 * @param baits
	 */
	public StratifyCluster(List<List<Interactome>> interactomes, String[] baits) {
		this.interactomes = interactomes;
		this.baits = baits;
	}

	private int getFullSize() {
		int size = 0;

		for (int k = 0; k < interactomes.size(); k++) {

			size += (baits[k].split("-").length * 2) + 1;
		}

		return size;
	}

	/**
	 * 
	 */
	public void run() {

		String[] expbaits;

		int fullsize = getFullSize();

		progressMonitor = new ProgressMonitor(null, "Cluster Scores Stratification", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int k = 0; k < interactomes.size(); k++) {

			expbaits = baits[k].split("-");

			// cleanInteractomes(k, expbaits);

			for (int i = 0; i < interactomes.get(k).size(); i++) {

				Interactome interactome = interactomes.get(k).get(i);

				Hashtable<String, double[]> vtable = new Hashtable<String, double[]>();
				List<String> vnames = new ArrayList<String>();

				for (int j = 0; j < expbaits.length; j++) {

					progressMonitor.setNote("Working on " + expbaits[j]);

					if (interactome.isNetworkinSystem(expbaits[j])) {
						Network net = interactome.getNetwork(expbaits[j]);

						List<String> interactors = net.getInteractorsNames();

						for (String interactor : interactors) {
							if (!interactor.equals(expbaits[j])) {
								final Interaction interaction = net.getInteractionByInteractorName(interactor);
								if (vtable.containsKey(interactor)) {

									double[] val = vtable.get(interactor);
									val[j] = interaction.getCluster_score();
									vtable.put(interactor, val);

								} else {

									double[] val = new double[expbaits.length];
									val[j] = interaction.getCluster_score();
									vtable.put(interactor, val);
									vnames.add(interactor);

								}

							}
						}

					}

					progress++;
					progressMonitor.setProgress(progress);

				}

				progressMonitor.setNote("Stratifying");

				vtable = stratify(vtable, vnames);

				progress++;
				progressMonitor.setProgress(progress);

				for (int j = 0; j < expbaits.length; j++) {

					progressMonitor.setNote("Working on " + expbaits[j]);

					Network net = interactome.getNetwork(expbaits[j]);
					List<String> interactors = net.getInteractorsNames();

					for (String interactor : interactors) {

						if (interactor.equals(expbaits[j])) {

						} else {
							if (vtable.containsKey(interactor)) {

								net.getInteractionByInteractorName(interactor)
										.setCluster_score(vtable.get(interactor)[j]);

							}
						}

					}

					progress++;
					progressMonitor.setProgress(progress);

				}

			}

		}

		progressMonitor.close();

	}

	private void cleanInteractomes(int id, String[] baits) {

		boolean keeper = false;

		for (int i = 0; i < interactomes.get(id).size(); i++) {

			final Interactome interactome = interactomes.get(id).get(i);
			List<String> nodes = interactome.getProteinsHavingANetwork();

			for (String node : nodes) {

				keeper = false;

				for (int j = 0; j < baits.length; j++) {

					if (node.equals(baits[j])) {
						keeper = true;
						break;
					}

				}

				if (!keeper) {
					interactome.getNetworksByProteinName().remove(node);
				}

			}

		}

	}

	private Hashtable<String, double[]> stratify(Hashtable<String, double[]> t, List<String> n) {

		double[] v;
		double sum;

		for (int i = 0; i < n.size(); i++) {

			v = t.get(n.get(i));

			sum = 0;

			for (int j = 0; j < v.length; j++) {
				sum += v[j];
			}

			if (sum == 0) {
				sum = 1;
			}

			for (int j = 0; j < v.length; j++) {

				v[j] = v[j] / sum;

			}

			t.put(n.get(i), v);

		}

		return t;
	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
