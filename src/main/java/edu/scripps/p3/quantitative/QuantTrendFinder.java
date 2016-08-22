/**
 * diego
 * Jun 14, 2013
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
import edu.scripps.p3.io.dataIO;

/**
 * @author diego
 * 
 */
public class QuantTrendFinder {

	private List<List<Interactome>> interactomes;
	private List<Differential> qlist;
	private File logdir;
	private StringBuffer log;
	private double tlevel;
	private List<List<Double>> matrix;
	
	private ProgressMonitor progressMonitor;
	private static int progress=0;

	/**
	 * @param qlist
	 * @param interactomes
	 * @param logdir
	 */
	public QuantTrendFinder(List<Differential> qlist,
			List<List<Interactome>> interactomes, File logdir) {
		this.interactomes = interactomes;
		this.qlist = qlist;
		this.logdir = logdir;
	}

	/**
	 * @param quantitativeLevel
	 */
	public void setLevel(double quantitativeLevel) {
		this.tlevel = quantitativeLevel;

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
		
		progressMonitor = new ProgressMonitor(null,"Calculating Quantitative Trends","Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);
		
		for (int k = 0; k < qlist.size(); k++) {

			initializeMatrix(qlist.get(k).getQlist().size());

			for (int i = 0; i < qlist.get(k).getQlist().size(); i++) {
			
				fillMatrix(i, k);

			}

			getTrends(k);
			
			qlog.append("Condition " + k + "\n");
			
			for (String qname : qlist.get(k).getQlist()) {
				
				qlog.append(qname + "\t" + qlist.get(k).getDiffValue(qname) + "\n");
				
			}
			

		}

		progressMonitor.close();
		
		dataIO dIO = new dataIO();
		dIO.writeLog(logdir, log, "QuantTrend");
		
		dIO.writeLog(logdir, qlog, "QuantValues");
		
		
		
		
	}

	private void getTrends(int bait_id) {
	
		double val;

		String bait, prey;

		for (int i = 0; i < qlist.get(bait_id).getQlist().size(); i++) {

			bait = qlist.get(bait_id).getQlist().get(i);
			
			progressMonitor.setNote("Working on " + bait);

			for (int j = 0; j < qlist.get(bait_id).getQlist().size(); j++) {

				prey = qlist.get(bait_id).getQlist().get(j);

				val = matrix.get(i).get(j);
				val = Math.pow(val, tlevel);

				if (val > 0.1) {

					log.append(bait + "_" + prey + "\t" + val + "\n");

					for (Interactome interactome : interactomes.get(bait_id)) {

						if (interactome.isNetworkinSystem(bait)) {

							Network net = interactome.getNetwork(bait);

							if (net.getInteraction_names().contains(prey)) {

								Interaction inter = net.getInteraction(prey);
								inter.setQuant_score(val);
							} else {

								Interaction inter = new Interaction(prey);
								inter.setQuant_score(val);
								net.addInteraction(prey, inter);

							}

						}

					}
					
				} else {

					log.append(bait + "_" + prey + "\t" + val + "\tdiscarded\n");
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

		v1 = getValues(id, bait_id);

		//System.out.println(qlist.get(bait_id).getQlist().get(id) + "\t" + v1);
		
		for (int j = 0; j < size; j++) {

			v2 = getValues(j, bait_id);

			column.add(getDistance(v1, v2));

		}

		addColumn(column);

	}

	private void addColumn(List<Double> column) {

		matrix.add(column);

	}

	private double getDistance(double v1, double v2) {

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

	private double getValues(int id, int bait_id) {

		String key = qlist.get(bait_id).getQlist().get(id);

		double val = qlist.get(bait_id).getData().get(key);

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
