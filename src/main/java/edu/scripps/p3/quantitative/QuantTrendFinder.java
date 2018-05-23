/**
 * diego Jun 14, 2013
 */
package edu.scripps.p3.quantitative;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class QuantTrendFinder {

	private final List<List<Interactome>> interactomes;
	private final List<Differential> qlist;
	private final File logdir;
	private StringBuffer log;
	private double tlevel;
	private List<List<Double>> matrix;

	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	/**
	 * @param qlist
	 * @param interactomes
	 * @param logdir
	 */
	public QuantTrendFinder(List<Differential> qlist, List<List<Interactome>> interactomes, File logdir) {
		this.interactomes = interactomes;
		this.qlist = qlist;
		this.logdir = logdir;
	}

	/**
	 * @param quantitativeLevel
	 */
	public void setLevel(double quantitativeLevel) {
		tlevel = quantitativeLevel;

		if (tlevel < 1 || tlevel > 5) {
			tlevel = 3;
		}

	}

	private int getFullSize() {

		int size = 0;

		for (int k = 0; k < qlist.size(); k++) {

			size += qlist.get(k).getQlist().size();
		}

		return size;
	}

	/**
	 *
	 */
	public void run() {

		log = new StringBuffer();
		log.append("Creating q complexes\n");

		int fullsize = getFullSize();

		StringBuilder qlog = new StringBuilder();

		progressMonitor = new ProgressMonitor(null, "Calculating Quantitative Trends", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int baitIndex = 0; baitIndex < qlist.size(); baitIndex++) {

			initializeMatrix(qlist.get(baitIndex).getQlist().size());

			for (int i = 0; i < qlist.get(baitIndex).getQlist().size(); i++) {

				fillMatrix(i, baitIndex);

			}

			getTrends(baitIndex);

			qlog.append("Condition " + baitIndex + "\n");

			for (String qname : qlist.get(baitIndex).getQlist()) {

				qlog.append(qname + "\t" + qlist.get(baitIndex).getDiffValue(qname) + "\n");

			}

		}

		progressMonitor.close();

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "QuantTrend");

		dIO.writeLog(logdir, qlog, "QuantValues");

	}

	private void getTrends(int bait_id) {

		double val;

		String protein1, protein2;

		Differential differential = qlist.get(bait_id);
		for (int i = 0; i < differential.getQlist().size(); i++) {

			protein1 = differential.getQlist().get(i);

			progressMonitor.setNote("Working on " + protein1);

			for (int j = 0; j < differential.getQlist().size(); j++) {

				protein2 = differential.getQlist().get(j);

				val = matrix.get(i).get(j);
				val = Math.pow(val, tlevel);

				final String proteinPairKey = protein1 + "_" + protein2;
				if (val > 0.1) {

					for (Interactome interactome : interactomes.get(bait_id)) {

						if (interactome.isNetworkinSystem(protein1)) {
							log.append(proteinPairKey + "\t" + val + "\n");
							if (protein2.equals("PRP43")) {
								System.out.println("ASDF");
							}
							Network net = interactome.getNetwork(protein1);

							if (net.getInteractorsNames().contains(protein2)) {

								Interaction inter = net.getInteractionByInteractorName(protein2);
								inter.setQuant_score(val);
							} else {

								Interaction inter = new Interaction(protein2);
								inter.setQuant_score(val);
								net.addInteraction(protein2, inter);

							}

						} else {
							log.append(proteinPairKey + "\t" + val + "\tno with bait\n");
						}

					}

				} else {

					log.append(proteinPairKey + "\t" + val + "\tdiscarded\n");
					//
				}
			}

			progress++;
			progressMonitor.setProgress(progress);

		}

	}

	private void fillMatrix(int id, int bait_id) {

		int size = qlist.get(bait_id).getQlist().size();

		double v1;
		double v2;

		List<Double> column = new ArrayList<Double>();

		v1 = getProteinRatio(id, bait_id);

		// System.out.println(qlist.get(bait_id).getQlist().get(id) + "\t" +
		// v1);

		for (int j = 0; j < size; j++) {

			v2 = getProteinRatio(j, bait_id);

			double proteinRatioDistance = getProteinRatioDistance(v1, v2);
			column.add(proteinRatioDistance);

		}

		addColumn(column);

	}

	private void addColumn(List<Double> column) {

		matrix.add(column);

	}

	private double getProteinRatioDistance(double v1, double v2) {

		double distance = 0;
		double ratio = 1;

		if (v1 < 1) {
			v1 = 1.0 / v1;
		}

		if (v2 < 1) {
			v2 = 1.0 / v2;
		}

		if (v1 > v2) {
			ratio = v1 / v2;
		} else {
			ratio = v2 / v1;
		}

		double floor = Math.floor(ratio);
		double ceil = Math.ceil(ratio);

		double min = Math.min(ratio - floor, ceil - ratio);

		distance = 1 - (min * 2);

		return distance;
	}

	private double getProteinRatio(int proteinIndex, int bait_id) {

		String proteinKey = qlist.get(bait_id).getQlist().get(proteinIndex);

		double val = qlist.get(bait_id).getData().get(proteinKey);

		return val;
	}

	private void initializeMatrix(int size) {

		matrix = new ArrayList<List<Double>>();

	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
