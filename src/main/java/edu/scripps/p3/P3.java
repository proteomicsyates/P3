/**
 * Protein Protein interaction Predictor (P3) is a framework to deconvolve
 * complex dataset and to build a full interaction network
 * 
 * @author diego Jun 11, 2013
 */
package edu.scripps.p3;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.gui.P3mainFrame;
import edu.scripps.p3.parsers.inputs.utilities.Configuration;
import edu.scripps.yates.utilities.dates.DatesUtil;

/**
 * Main class of P3, it invokes the GUI and then passes the collected values to
 * the P3Core
 * 
 * @author diego
 * @version 1.0
 */
public class P3 {

	private static Object lock = new Object();

	private List<Experiment> elist; // for input file data
	private List<Differential> qlist; // for quant file data
	private List<Differential> llist; // for lysate file data
	private List<Orthogonal> olist; // for orthogonal data

	private boolean lysate;
	private boolean quantitative;
	private boolean physical;
	private boolean genetic;

	private boolean bonus;
	private boolean indirect;
	private boolean advanced;

	private File outdir;
	private File logdir;
	private File topodir;

	private String[] baits;
	private String[] experiments;

	private Configuration configuration;

	private P3mainFrame ppp3gui;
	private P3Core p3core;

	/**
	 * invokes the P3mainFrame method to create the gui creates a separate
	 * thread to deal with it collects the results invokes the P3Core method
	 * shows time used by the core
	 */
	public void run() {

		ppp3gui = new P3mainFrame(elist, qlist, llist, olist, quantitative, lysate, physical, genetic, bonus, indirect,
				advanced, outdir, logdir, topodir, baits, experiments, configuration, lock);

		Thread t = new Thread() {
			@Override
			public void run() {

				ppp3gui.run();
				synchronized (lock) {
					while (ppp3gui.isVisible()) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			}

		};
		t.start();

		try {
			t.join();

			elist = ppp3gui.getElist();
			qlist = ppp3gui.getQlist();
			llist = ppp3gui.getLlist();
			olist = ppp3gui.getOlist();
			quantitative = ppp3gui.isQuantitative();
			lysate = ppp3gui.isLysate();
			physical = ppp3gui.isPhysical();
			genetic = ppp3gui.isGenetic();
			bonus = ppp3gui.isBonus();
			indirect = ppp3gui.isIndirect();
			advanced = ppp3gui.isAdvanced();
			outdir = ppp3gui.getOutdir();
			logdir = ppp3gui.getLogdir();
			topodir = ppp3gui.getTopodir();
			baits = ppp3gui.getBaits();
			experiments = ppp3gui.getExperiments();
			configuration = ppp3gui.getConfiguration();

			p3core = new P3Core(elist, qlist, llist, olist, quantitative, lysate, physical, genetic, bonus, indirect,
					advanced, outdir, logdir, topodir, baits, experiments, configuration);

			long start = System.currentTimeMillis();

			p3core.run();

			long end = System.currentTimeMillis();

			long elapsed = end - start;

			JOptionPane.showMessageDialog(null,
					"Execution Terminated in " + DatesUtil.getDescriptiveTimeFromMillisecs(elapsed));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		P3 p3 = new P3();
		p3.run();

	}

}
