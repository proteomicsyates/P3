package edu.scripps.p3.prefilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.scripps.p3.prefilter.interactors.BioGRIDFilter;
import edu.scripps.p3.prefilter.interactors.StringFilter;
import edu.scripps.yates.utilities.maths.Maths;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.set.hash.THashSet;

/**
 * It will look for the fold change threshold in which we have maximized the
 * recall of known interactors from BioGrid and String across a set of
 * experiments
 * 
 * @author salvador
 *
 */
public class IterativeMethod {
	private final static Logger log = Logger.getLogger(IterativeMethod.class);

	public static void main(String[] args) {
		final IterativeMethod tm = new IterativeMethod();
		try {
			tm.run();
			System.out.println("Everything is fine");
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private final File stringFile = new File(
			"Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\String\\string_v11.0_interactions.tsv");
	private final File biogridFile = new File(
			"Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\Biogrid\\BIOGRID-GENE-111550-3.5.172.tab2.txt");
	private final Float stringConfidenceThreshold = 0.4f;
	private final double pValueThreshold = 0.01;
	private static final String BAIT = "PRKAA2";

	private void run() throws IOException {

		// load STRING and bioGRID interactors
		final StringFilter string = new StringFilter(stringFile);
		final BioGRIDFilter biogrid = new BioGRIDFilter(biogridFile);
		final Set<String> stringConfidentInteractors = string.getUniqueConfidentInteractors(stringConfidenceThreshold,
				BAIT);
		System.out.println(stringConfidentInteractors.size() + " confident interactors from String");
		final Set<String> biogridConfidentInteractors = biogrid.getUniqueConfidentInteractors(null, BAIT);
		System.out.println(biogridConfidentInteractors.size() + " confident interactors from BioGRID");

		final Set<String> allConfidentInteractors = new THashSet<String>();
		allConfidentInteractors.addAll(biogridConfidentInteractors);
		allConfidentInteractors.addAll(stringConfidentInteractors);
		System.out.println(allConfidentInteractors.size() + " confidence interactors in TOTAL");

		double foldChangeThreshold = 1;
		final double maxFoldChange = 4.1;

		final double foldChangeDelta = 0.1;
		// create a preFilterExecutor per experiment
		final List<PreFilterExecutor> experiments = createPreFilterExecutors(pValueThreshold, foldChangeThreshold);

		// initiate the iterative method

		// define parameters
		final FileWriter outputLog = new FileWriter(
				new File("Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\iterativeMethod.log"));

		double previousFoldChangeThreshold = 0;
		double averageNumberOfKnownInteractors = 0;
		double previousAverageNumberOfKnownInteractors = 0;
		final TDoubleDoubleHashMap calledKnownInteractorsByFoldChange = new TDoubleDoubleHashMap();
		String message = "";
		while (foldChangeThreshold <= maxFoldChange) {
			try {
				// run filters
				for (final PreFilterExecutor preFilterExecutor : experiments) {
					preFilterExecutor.run();
				}
				message = "Getting number of known interactors with fold change threshold = " + foldChangeThreshold;
				log.info(message);
				outputLog.write(message + "\n");
				averageNumberOfKnownInteractors = getAverageNumberOfKnownInteractors(experiments,
						allConfidentInteractors, foldChangeThreshold, outputLog);
				message = "Average\t" + foldChangeThreshold + "\t\t" + averageNumberOfKnownInteractors;
				log.info(message);
				outputLog.write(message + "\n");
				outputLog.flush();
				calledKnownInteractorsByFoldChange.put(foldChangeThreshold, averageNumberOfKnownInteractors);
			} finally {
				final double tmp = foldChangeThreshold;
//				if (averageNumberOfKnownInteractors > previousAverageNumberOfKnownInteractors) {
//					foldChangeThreshold = increaseFoldChange(foldChangeThreshold, previousFoldChangeThreshold);
//				} else {
//					foldChangeThreshold = decreaseFoldChange(foldChangeThreshold, previousFoldChangeThreshold);
//				}
				foldChangeThreshold += foldChangeDelta;

				// set new foldchange
				for (final PreFilterExecutor preFilterExecutor : experiments) {
					preFilterExecutor.setavgRatioThreshold(foldChangeThreshold);
				}
				previousFoldChangeThreshold = tmp;

				previousAverageNumberOfKnownInteractors = averageNumberOfKnownInteractors;
//				final double delta = Math.abs(foldChangeThreshold - previousFoldChangeThreshold);
				message = "Setting new fold change threshold from " + previousFoldChangeThreshold + " to "
						+ foldChangeThreshold;
//						+ " (delta=" + delta + ")";
				log.info(message);
				outputLog.write(message + "\n\n");
//				if (delta < 0.05) {
//					break;
//				}

			}
		}

		message = "The optimal threshold is " + foldChangeThreshold + " which is able to call: "
				+ averageNumberOfKnownInteractors + " known interactors";
		System.out.println(message);
		outputLog.write(message + "\n\n");

		final TDoubleArrayList list = new TDoubleArrayList();
		list.addAll(calledKnownInteractorsByFoldChange.keySet());
		list.sort();

		outputLog.write("\t" + "\t");
		for (final PreFilterExecutor preFilterExecutor : experiments) {
			outputLog.write(preFilterExecutor.getName() + "\t\t");
		}
		outputLog.write("\n");
		outputLog.write("FOLD change\t" + "avg called known interactors\t");
		for (final PreFilterExecutor preFilterExecutor : experiments) {
			outputLog.write("num confident interactors" + "\t" + "num called known interactors\t");
		}
		outputLog.write("\n");
		for (final double foldChange : list.toArray()) {
			outputLog.write(foldChange + "\t" + calledKnownInteractorsByFoldChange.get(foldChange) + "\t");
			for (final PreFilterExecutor preFilterExecutor : experiments) {
				outputLog.write(preFilterExecutor.getConfidenceInteractorsByFoldchangeThreshold().get(foldChange).size()
						+ "\t");
				outputLog.write(
						preFilterExecutor.getKnownInteractorsByFoldchangeThreshold().get(foldChange).size() + "\t");
			}
			outputLog.write("\n");
		}
		outputLog.close();

		for (final PreFilterExecutor preFilterExecutor : experiments) {
			FileWriter outputTable = new FileWriter(
					new File("Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\iterativeMethod"
							+ preFilterExecutor.getName() + "_known_interactors.txt"));
			outputTable.write(preFilterExecutor.getName() + "\n");
			for (final double foldChange : list.toArray()) {
				outputTable.write(foldChange + "\t");
			}
			outputTable.write("\n");
			int row = 0;
			boolean foundInSome = true;
			while (foundInSome) {
				foundInSome = false;
				for (final double foldChange : list.toArray()) {
					final List<String> knownInteractors = preFilterExecutor.getKnownInteractorsByFoldchangeThreshold()
							.get(foldChange);
					if (knownInteractors.size() > row) {
						foundInSome = true;
						outputTable.write(knownInteractors.get(row));
					}
					outputTable.write("\t");
				}
				outputTable.write("\n");
				row++;
			}
			outputTable.close();

			// now all the confident interactors, not only the known
			outputTable = new FileWriter(
					new File("Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\iterativeMethod"
							+ preFilterExecutor.getName() + "_all_interactors.txt"));
			outputTable.write(preFilterExecutor.getName() + "\n");
			for (final double foldChange : list.toArray()) {
				outputTable.write(foldChange + "\t");
			}
			outputTable.write("\n");
			row = 0;
			foundInSome = true;
			while (foundInSome) {
				foundInSome = false;
				for (final double foldChange : list.toArray()) {
					final List<String> confidentInteractors = preFilterExecutor
							.getConfidenceInteractorsByFoldchangeThreshold().get(foldChange);
					if (confidentInteractors.size() > row) {
						foundInSome = true;
						outputTable.write(confidentInteractors.get(row));
					}
					outputTable.write("\t");
				}
				outputTable.write("\n");
				row++;
			}
			outputTable.close();
		}

	}

	private double getAverageNumberOfKnownInteractors(List<PreFilterExecutor> experiments,
			Set<String> allConfidentInteractors, double usedFoldChangeThreshold, FileWriter logFile)
			throws IOException {
		final TIntArrayList toAverage = new TIntArrayList();

		for (final PreFilterExecutor filter : experiments) {
			final List<String> knownIterators = new ArrayList<String>();
			final List<String> confidentInteractors = filter.getConfidentInteractors();
			int numKnownInteractors = 0;
			for (final String interactor : confidentInteractors) {
				if (allConfidentInteractors.contains(interactor)) {
					numKnownInteractors++;
					if (!knownIterators.contains(interactor)) {
						knownIterators.add(interactor);
					}
				}
			}
			filter.setKnownIteractors(knownIterators);
			final String message = filter.getName() + "\t" + usedFoldChangeThreshold + "\t"
					+ confidentInteractors.size() + "\t" + numKnownInteractors;
			log.info(message);
			logFile.write(message + "\n");
			toAverage.add(numKnownInteractors);
		}
		final double mean = Maths.mean(toAverage);

		return mean;
	}

	private double decreaseFoldChange(double foldChangeThreshold, double previousFoldChangeThreshold) {
		final double delta = Math.abs(previousFoldChangeThreshold - foldChangeThreshold);
		final double newFoldChange = foldChangeThreshold - delta / 2;
		return newFoldChange;
	}

	private double increaseFoldChange(double foldChangeThreshold, double previousFoldChangeThreshold) {
		final double delta = Math.abs(previousFoldChangeThreshold - foldChangeThreshold);
		final double newFoldChange = foldChangeThreshold + delta / 2;
		return newFoldChange;
	}

	private List<PreFilterExecutor> createPreFilterExecutors(double pValueThreshold, double avgRatioThreshold) {
		final List<PreFilterExecutor> ret = new ArrayList<PreFilterExecutor>();
		String pathPrefix = "";
		//
		// MudPIT Flag
		pathPrefix = "Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\newData_toNormalize\\OLD_Flag\\";
		final PreFilterExecutor ex1 = new PreFilterExecutor(new File(pathPrefix + "Crispr51L_WildtypeH_basal.txt"),
				new File(pathPrefix + "Crispr51H_WildtypeL_phen.txt"),
				new File(pathPrefix + "Crispr51L_basal_Crispr51H_phen.txt"), pValueThreshold, avgRatioThreshold);
		ex1.setName("MudPIT Flag");
		ret.add(ex1);
		//
		// MudPIT pAb wt 293T
		pathPrefix = "Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\newData_toNormalize\\OLD_mAb\\";
		final PreFilterExecutor ex2 = new PreFilterExecutor(new File(pathPrefix + "Higg_Lalpha2_Phen.txt"),
				new File(pathPrefix + "Halpha2_Ligg_basal.txt"),
				new File(pathPrefix + "Alpha2_End_IP_Hphen_Lbasal.txt"), pValueThreshold, avgRatioThreshold);
		ex2.setName("MudPIT pAb wt 293T");
		ret.add(ex2);
		//
		// High pH RP Flag
		pathPrefix = "Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\newData_toNormalize\\new_Flag\\";
		final PreFilterExecutor ex3 = new PreFilterExecutor(new File(pathPrefix + "293T_51_L_WT_H_FM_Flag_REAL.txt"),
				new File(pathPrefix + "293T_51_H_WT_L_Phen_Flag_REAL.txt"), new File(pathPrefix + "Flag_REAL.txt"),
				pValueThreshold, avgRatioThreshold);
		ex3.setName("High pH RP Flag");
		ret.add(ex3);
		//
		// High pH RP mAb CRISPR line
		pathPrefix = "Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\newData_toNormalize\\new_mAb\\";
		final PreFilterExecutor ex4 = new PreFilterExecutor(new File(pathPrefix + "293T_51L_mAb_51H_msIgG_FM_REAL.txt"),
				new File(pathPrefix + "293T_51H_mAb_51L_msIgG_Phen_REAL.txt"), new File(pathPrefix + "mAb_REAL.txt"),
				pValueThreshold, avgRatioThreshold);
		ex4.setName("High pH RP mAb CRISPR line");
		ret.add(ex4);
		//
		// new pAb
		pathPrefix = "Z:\\share\\Salva\\data\\Ben\\CRISPR AMPK\\NewData2019\\newData_toNormalize\\new pAb\\";
		final PreFilterExecutor ex5 = new PreFilterExecutor(new File(pathPrefix + "293T_51L_pAb_51H_RbIgG_FM_REAL.txt"),
				new File(pathPrefix + "293T_51L_pAb_51H_RbIgG_Phen_REAL.txt"), new File(pathPrefix + "pAb_REAL.txt"),
				pValueThreshold, avgRatioThreshold);
		ex5.setName("new pAb");
		ret.add(ex5);
		return ret;
	}
}
