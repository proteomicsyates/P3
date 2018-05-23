/**
 * diego Jun 13, 2013
 */
package edu.scripps.p3.correlator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ProgressMonitor;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.log4j.Logger;

import edu.scripps.p3.correlator.utilities.Complex;
import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.io.MyFileChooser;
import edu.scripps.p3.parsers.inputs.utilities.Protein;

/**
 * @author diego
 *
 */
public class ComplexCorrelator {
	private final static Logger log4j = Logger.getLogger(Correlator.class);
	private static int correlatorsRunning = 0;

	private final List<Experiment> elist;
	private List<List<Double>> matrix;
	private final File lout;
	private StringBuffer log;
	private List<List<Complex>> experimentComplexes;
	private final boolean rapidCorrelation;
	private final double rSquaredThreshold;
	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	public ComplexCorrelator(List<Experiment> elist, File logdir, boolean rapid, double rSquaredThreshold) {
		this.elist = elist;
		lout = logdir;
		rapidCorrelation = rapid;
		this.rSquaredThreshold = rSquaredThreshold;
	}

	public void run() {

		log = new StringBuffer();
		experimentComplexes = new ArrayList<List<Complex>>();

		int fullsize = getFullSize();

		progressMonitor = new ProgressMonitor(null, "Calculating Correlation Complexes", "Initialization...", 0,
				fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int baitID = 0; baitID < elist.size(); baitID++) {

			List<Complex> complexGroup = new ArrayList<Complex>();
			experimentComplexes.add(complexGroup);

			final Experiment experiment = elist.get(baitID);
			for (int conditionID = 0; conditionID < experiment.getNumberofConditions(); conditionID++) {

				final Condition condition = experiment.getCondition(conditionID);
				log.append("Working on " + experiment.getName() + "_" + condition.getName() + "\n");
				log.append("SPC Correlation threshold for R^2=" + this.rSquaredThreshold);
				initializeMatrix(condition.getNumberOfProteins());

				fillMatrix(baitID, conditionID);

				getCorrelation(baitID, conditionID);

			}
		}

		progressMonitor.close();

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(lout, log, "ComplexCorrelatorLog");

	}

	private HashSet<String> baits;

	public void setBaits(String[] baits) {

		this.baits = new HashSet<String>();

		for (String s : baits) {
			this.baits.add(s);
		}

	}

	private void getCorrelation(int bait_id, int condition_id) {

		final Experiment experiment = elist.get(bait_id);
		final Condition condition = experiment.getCondition(condition_id);
		Complex cmpx = new Complex(experiment.getName(), condition.getName());

		experimentComplexes.get(bait_id).add(cmpx);

		progressMonitor.setNote("Processing " + experiment.getName() + " for " + condition.getName());

		Runtime r = Runtime.getRuntime();
		long keepAlive = 50000;

		final int availableProcessors = r.availableProcessors();
		final int maximumPoolSize = availableProcessors * 2;
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, maximumPoolSize, keepAlive, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(100));
		executor.allowCoreThreadTimeOut(true);

		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (r instanceof Correlator) {
					final Correlator correlator = (Correlator) r;
					log4j.info("bait " + correlator.myBait + " condition " + correlator.myCondition + " id "
							+ correlator.proteinIndexInMatrix + " has been rejected " + correlatorsRunning
							+ " correlators running");
				}
				executor.execute(r);
			}
		});

		for (int i = 0; i < condition.getNumberOfProteins(); i++) {

			Runnable worker = new Correlator(bait_id, condition_id, i, rSquaredThreshold);
			executor.execute(worker);

		}
		executor.shutdown();

		try {
			executor.awaitTermination(2, TimeUnit.HOURS);
			log4j.info("Executor done with bait " + bait_id + " and condition " + condition_id + " "
					+ correlatorsRunning + " correlators running");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private double correlationCalculator(List<Double> preyx, List<Double> preyy) {

		PearsonsCorrelation pc = new PearsonsCorrelation();

		double c_pc;
		double c_sc;

		double correlation;
		double[] array1 = new double[preyx.size()];
		double[] array2 = new double[preyx.size()];
		final Double[] array1_TMP = preyx.toArray(new Double[0]);
		for (int k = 0; k < array1_TMP.length; k++) {
			array1[k] = array1_TMP[k];
		}
		final Double[] array2_TMP = preyy.toArray(new Double[0]);
		for (int k = 0; k < array2_TMP.length; k++) {
			array2[k] = array2_TMP[k];
		}

		if (rapidCorrelation) {
			c_pc = pc.correlation(array1, array2);
			correlation = c_pc;
		} else {
			c_pc = pc.correlation(array1, array2);
			SpearmansCorrelation sc = new SpearmansCorrelation();
			c_sc = sc.correlation(array1, array2);
			correlation = (c_pc + c_sc) / 2;
		}

		correlation = Math.pow(correlation, 2);
		// 0.5 equivalent to +-0.7 with R (i.e. 0.7 R -->
		// 0.49 R2) //was 0.5
		return correlation;
		// if (correlation > 0.5) {
		//
		// return true;
		//
		// } else {
		// return false;
		// }

	}

	private void fillMatrix(int bait_id, int condition_id) {

		List<String> pnames = elist.get(bait_id).getCondition(condition_id).getPnames();

		double value_i;
		double value_j;

		List<Double> column;

		for (int i = 0; i < pnames.size(); i++) {

			value_i = getValue(bait_id, condition_id, pnames.get(i));

			column = new ArrayList<Double>();

			for (int j = 0; j < pnames.size(); j++) {

				value_j = getValue(bait_id, condition_id, pnames.get(j));

				final double distance = getDistance(value_i, value_j);
				column.add(distance);

			}

			addColumn(column);
		}

	}

	private void addColumn(List<Double> column) {
		matrix.add(column);
	}

	private double getDistance(double v1, double v2) {

		double distance;

		distance = Math.abs(v1 - v2) / (v1 + v2);

		distance = 1 - distance;

		return distance;

	}

	private double getValue(int bait_id, int condition_id, String pname) {

		double value = 0;

		Protein p = elist.get(bait_id).getCondition(condition_id).getProtein(pname);

		value = (p.getApv() * p.getLength()) / p.getMw();

		return value;
	}

	public List<List<Complex>> getComplexList() {
		return experimentComplexes;
	}

	private void initializeMatrix(int size) {

		matrix = new ArrayList<List<Double>>();

	}

	private int getFullSize() {

		int size = 0;

		for (int i = 0; i < elist.size(); i++) {
			for (int j = 0; j < elist.get(i).getNumberofConditions(); j++) {
				size += elist.get(i).getCondition(j).getNumberOfProteins();
			}
		}

		return size;
	}

	private class Correlator implements Runnable {
		int myBait;
		int myCondition;
		int proteinIndexInMatrix;
		double rSquaredThreshold;

		public Correlator(int bait_id, int condition_id, int id, double rSquaredThreshold) {
			myBait = bait_id;
			myCondition = condition_id;
			proteinIndexInMatrix = id;
			this.rSquaredThreshold = rSquaredThreshold;
		}

		@Override
		public void run() {
			++correlatorsRunning;
			List<Double> preyx;
			List<Double> preyy;

			List<String> trending_complex;

			preyx = new ArrayList<Double>();
			trending_complex = new ArrayList<String>();
			final Experiment experiment = elist.get(myBait);
			final Condition condition = experiment.getCondition(myCondition);
			final String pname = condition.getPnames().get(proteinIndexInMatrix);
			trending_complex.add(pname);

			// get the values of the matrix for that protein
			// which are a transformation of the difference of SPC between the
			// protein and all the others
			for (int s = 0; s < matrix.get(proteinIndexInMatrix).size(); s++) {

				preyx.add(matrix.get(proteinIndexInMatrix).get(s));

			}
			// remove the value in the matrix corresponding to the protein with
			// itself:
			preyx.remove(proteinIndexInMatrix);

			// for each other protein, get its array of values
			for (int j = 0; j < matrix.size(); j++) {
				if (j != proteinIndexInMatrix) { // skip the diagonal

					preyy = new ArrayList<Double>();

					for (int s = 0; s < matrix.get(j).size(); s++) {

						preyy.add(matrix.get(j).get(s));

					}

					preyy.remove(j);
					final String pname2 = condition.getPnames().get(j);
					if ((pname.equals("DOCK7") || pname2.equals("DOCK7"))
							&& (pname.equals("PRKAA2") || pname2.equals("PRKAA2"))) {
						System.out.println(pname2);
					}
					// calculates the correlation
					final double correlation = correlationCalculator(preyx, preyy);
					if (correlation >= rSquaredThreshold) {

						trending_complex.add(pname2);
						log.append("ACEPTED:\t" + pname + " - " + pname2 + "\tR^2=" + correlation + "\n");
					} else {
						log.append("REJECTED:\t" + pname + " - " + pname2 + "\tR^2=" + correlation + "\n");
					}
				}
			}

			final Complex experimentConditionComplex = experimentComplexes.get(myBait).get(myCondition);
			experimentConditionComplex.addComplex(trending_complex);
			for (int j = 0; j < trending_complex.size(); j++) {
				log.append(trending_complex.get(j) + "\t");
			}
			log.append("\n\n");

			progress++;
			progressMonitor.setProgress(progress);
			--correlatorsRunning;

		}

	}

}
