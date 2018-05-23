package edu.scripps.p3.prefilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.log4j.Logger;

import edu.scripps.yates.utilities.files.FileUtils;
import edu.scripps.yates.utilities.maths.Maths;

/**
 * Class to filter an IP real experiment, that is, an experiment in which a real
 * IP has been compared to another real IP in other condition.<br>
 * Originally, the name of this class was CRISPR_Filter_Diff_Expr_For_Benv2
 *
 * @author Salva
 *
 */
public class FilterRealExperiment {
	private static final Logger log = Logger.getLogger(FilterRealExperiment.class);
	public static final double singletonFilter = 4.0;
	public static final double singletonFilter2nd = 1.0 / singletonFilter;
	private final File controlExperimentFile;
	private final double pvalueThreshold;
	private File significativeInteractorsFile;
	private File noSignificativeInteractorsFile;
	private File notEnoughReplicatesFile;
	private double FDR;

	public static void main(String[] args) {
		final FilterRealExperiment filterRealExperiment = new FilterRealExperiment(new File(args[0]),
				Double.parseDouble(args[1]));
		try {
			filterRealExperiment.filter();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected FilterRealExperiment(File controlExperimentFile, double pvalueThreshold) {
		this.controlExperimentFile = controlExperimentFile;
		this.pvalueThreshold = pvalueThreshold;
	}

	/**
	 * It filters a IP control experiment, that is, an experiment in which a
	 * real IP has been compared to a Mock IP.<br>
	 * It creates 3 output files:
	 * <ul>
	 * <li>inputFileName_Sig</li>
	 * <li>inputFileName_Non_Sig</li>
	 * <li>inputFileName_Not_Enough_Replicates</li>
	 * </ul>
	 *
	 * @param controlExperimentFile
	 *            this is a QuantCompare file
	 * @param pvalueThreshold
	 *            a p-value threshold
	 * @throws IOException
	 */
	protected void filter() throws IOException {
		try {
			log.info("Filtering real experiment in file: "
					+ FilenameUtils.getName(controlExperimentFile.getAbsolutePath()) + " using pValueThreshold="
					+ pvalueThreshold);

			// String inputFileName = "src/data/quant_stat_compare2732.txt";
			final BufferedReader in = new BufferedReader(new FileReader(controlExperimentFile));

			significativeInteractorsFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Sig");
			final BufferedWriter outPassed = new BufferedWriter(new FileWriter(significativeInteractorsFile));
			noSignificativeInteractorsFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Non_Sig");
			final BufferedWriter outFilteredOut = new BufferedWriter(new FileWriter(noSignificativeInteractorsFile));
			notEnoughReplicatesFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Not_Enough_Replicates");
			final BufferedWriter outNotEnoughRep = new BufferedWriter(new FileWriter(notEnoughReplicatesFile));

			outPassed.write("UniProt_Accession\t" + "t-test_p-value\t" + "Passed_With_Singleton_Criteria\t"
					+ "Heavy_Light\t" + "Average_Ratio\t" + "Protein_Info\t" + "Ratios\n");
			outFilteredOut.write("UniProt_Accession\tt-test_p-value\tAverage_Ratio\tProtein_Info\tRatios\n");
			outNotEnoughRep.write("UniProt_Accession\tQuantified_In_More_Than_One_Replicate\tProtein_Info\tRatios\n");

			outPassed.flush();
			outFilteredOut.flush();
			outFilteredOut.flush();
			log.info("Executing...");

			String s = in.readLine();
			String[] split = s.split("\t");
			while (!split[0].equals("PLINE")) {
				s = in.readLine();

				split = s.split("\t");
			}
			// The reader is now at the header line
			final Map<String, Integer> indexesByHeaders = PreFilterUtils.getIndexesByHeaders(split);
			final Map<Integer, Integer> indexesByReplicates = PreFilterUtils.getRatioIndexesByReplicate(split,
					PreFilterUtils.area_ratio_x_regexp);
			s = in.readLine(); // The reader is now at the first line of data
			split = s.split("\t");

			double counterStatTest = 0.0;
			double counterStatTestPassed = 0.0;

			while (s != null) {

				boolean processed = false;
				final String UniProt_Acc = split[indexesByHeaders.get(PreFilterUtils.ACC)];
				final String ProteinInfo = split[indexesByHeaders.get(PreFilterUtils.DESCRIPTION)];
				final List<Double> ratios = new ArrayList<Double>();
				double counter = 0;
				final String ratiosString = PreFilterUtils.getOldRatioString(indexesByReplicates, s);

				boolean inReplicate1 = false;
				boolean inReplicate2 = false;
				boolean inReplicate3 = false;

				final String[] splitRatios1 = ratiosString.split(";");
				for (int i = 0; i < splitRatios1.length; i++) {
					final String[] splitRatios2 = splitRatios1[i].split(",");
					for (int j = 0; j < splitRatios2.length; j++) {
						if (!splitRatios2[j].equals("X") && !(splitRatios2[j].length() < 1)) {
							ratios.add(Double.parseDouble(splitRatios2[j]));
							counter++;

							if (i == 0) {
								inReplicate1 = true;

							}
							if (i == 1) {
								inReplicate2 = true;

							}
							if (i == 2) {
								inReplicate3 = true;

							}
						}
					}
				}

				boolean quantifiedInMoreThanOneRep = false;
				if ((inReplicate1 && inReplicate2) || (inReplicate1 && inReplicate3)
						|| (inReplicate2 && inReplicate3)) {
					quantifiedInMoreThanOneRep = true;
				} else {
					// discarded not enough replicates
					outNotEnoughRep.write(UniProt_Acc + "\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
					outNotEnoughRep.flush();
					processed = true;
				}

				if (quantifiedInMoreThanOneRep) {
					int nonSingletonCounter = 0;
					for (int i = 0; i < ratios.size(); i++) {
						if (ratios.get(i) < singletonFilter && ratios.get(i) > singletonFilter2nd) {
							nonSingletonCounter++;
						}
					}

					double highRatioCounter = 0.0;
					int numberBelowSingletonRatioThreshold = 0;
					for (int i = 0; i < ratios.size(); i++) {
						if (ratios.get(i) >= singletonFilter) {
							highRatioCounter++;
						} else {
							numberBelowSingletonRatioThreshold++;
						}
					}
					if (highRatioCounter >= counter / 2) {
						// protein has more than half peptides singleton
						outPassed.write(UniProt_Acc + "\tN\\A\tTRUE\t>4.0_Light\tN\\A\t" + ProteinInfo + "\t"
								+ ratiosString + "\n");
						outPassed.flush();
						processed = true;
					} else {
						double lowRatioCounter = 0.0;
						int numberAboveSingletonRatioThreshold2nd = 0;
						for (int i = 0; i < ratios.size(); i++) {
							if (ratios.get(i) <= singletonFilter2nd) {
								lowRatioCounter++;
							} else {
								numberAboveSingletonRatioThreshold2nd++;
							}
						}
						if (lowRatioCounter >= counter / 2) {
							// protein has more than half peptides singleton
							outPassed.write(UniProt_Acc + "\tN\\A\tTRUE\t<0.25_Heavy\tN\\A\t" + ProteinInfo + "\t"
									+ ratiosString + "\n");
							outPassed.flush();
							processed = true;
						}
					}

					if (!processed) {

						final double[] ratiosArray = new double[nonSingletonCounter];
						int indexInArray = 0;
						for (int i = 0; i < ratios.size(); i++) {
							if ((ratios.get(i) < singletonFilter) && (ratios.get(i) > singletonFilter2nd)) {
								ratiosArray[indexInArray] = ratios.get(i);
								indexInArray++;
							}
						}

						double avgRatio = 0.0;
						double counterDiv = 0.0;
						for (int i = 0; i < ratiosArray.length; i++) {
							avgRatio = avgRatio + ratiosArray[i];
							counterDiv++;
						}

						avgRatio = avgRatio / counterDiv;

						if (ratiosArray.length == 1) {
							// if only has one ratio, is discarded for not
							// having
							// sufficient data
							outFilteredOut.write(UniProt_Acc + "\tINSUFFICIENT_DATA\t" + avgRatio + "\t" + ProteinInfo
									+ "\t" + ratiosString + "\n");
							outFilteredOut.flush();
						} else {
							counterStatTest++;
							// t-test calculation to revisit

							// get t-statistic
							final TTest tstat = new TTest();
							double t = -1.0;
							if (avgRatio >= 1) {
								t = tstat.tTest(1.0, ratiosArray);
							} else {
								for (int i = 0; i < ratiosArray.length; i++) {
									ratiosArray[i] = 1.0 / ratiosArray[i];
								}
								t = tstat.tTest(1.0, ratiosArray);
							}

							// TDistribution tdist = new
							// TDistribution(intensitiesAvg.length-2);
							// For each peptide compute t-test pvalue and store
							// This is not exactly right since the exact value
							// of t is not included in the p-value
							// double Ttest_pvalue =
							// tdist.cumulativeProbability(t);

							final double pvalue = t / 2;

							if (pvalue < pvalueThreshold) {
								outPassed.write(UniProt_Acc + "\t" + pvalue + "\t" + "FALSE" + "\t" + "N/A" + "\t"
										+ avgRatio + "\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outPassed.flush();
								processed = true;
								counterStatTestPassed++;
							}

							if (!processed) {
								outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio + "\t" + ProteinInfo
										+ "\t" + ratiosString + "\n");
								outFilteredOut.flush();
							}
						}

					}
				}
				s = in.readLine();
				if (s != null) {
					split = s.split("\t");
				}

			}
			in.close();
			outFilteredOut.close();
			outNotEnoughRep.close();
			outPassed.close();
			FDR = (counterStatTest * pvalueThreshold) / counterStatTestPassed;

			log.info("Completed");
			log.info("p-value = " + pvalueThreshold + " FDR = " + FDR);
		} catch (final Exception e) {
			log.warn("Error parsing input file. Now trying in a different way");
			filter2();
		}

	}

	private void filter2() throws IOException {
		log.info("Filtering real experiment in file: " + FilenameUtils.getName(controlExperimentFile.getAbsolutePath())
				+ " using pValueThreshold=" + pvalueThreshold);

		// String inputFileName = "src/data/quant_stat_compare2732.txt";
		final BufferedReader in = new BufferedReader(new FileReader(controlExperimentFile));

		significativeInteractorsFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Sig");
		final BufferedWriter outPassed = new BufferedWriter(new FileWriter(significativeInteractorsFile));
		noSignificativeInteractorsFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Non_Sig");
		final BufferedWriter outFilteredOut = new BufferedWriter(new FileWriter(noSignificativeInteractorsFile));
		notEnoughReplicatesFile = FileUtils.appendToFileName(controlExperimentFile, null, "_Not_Enough_Replicates");
		final BufferedWriter outNotEnoughRep = new BufferedWriter(new FileWriter(notEnoughReplicatesFile));

		outPassed.write("UniProt_Accession\t" + "t-test_p-value\t" + "Passed_With_Singleton_Criteria\t"
				+ "Heavy_Light\t" + "Average_Ratio\t" + "Protein_Info\t" + "Ratios\n");
		outFilteredOut.write("UniProt_Accession\tt-test_p-value\tAverage_Ratio\tProtein_Info\tRatios\n");
		outNotEnoughRep.write("UniProt_Accession\tQuantified_In_More_Than_One_Replicate\tProtein_Info\tRatios\n");

		outPassed.flush();
		outFilteredOut.flush();
		outFilteredOut.flush();
		log.info("Executing...");

		String s = in.readLine();
		String[] split = s.split("\t");
		while (!split[0].equals("locus")) {
			s = in.readLine();

			split = s.split("\t");
		}
		// The reader is now at the header line
		final Map<String, Integer> indexesByHeaders = PreFilterUtils.getIndexesByHeaders(split);
		final Map<Integer, Integer> indexesByReplicates = PreFilterUtils.getRatioIndexesByReplicate(split,
				PreFilterUtils.area_ratio_x_regexp_new_format);
		s = in.readLine(); // The reader is now at the first line of data
		split = s.split("\t");

		double counterStatTest = 0.0;
		double counterStatTestPassed = 0.0;

		while (s != null) {

			boolean processed = false;
			final String UniProt_Acc = split[indexesByHeaders.get(PreFilterUtils.LOCUS)];
			final String ProteinInfo = split[indexesByHeaders.get(PreFilterUtils.DESCRIPTION_LOWER_CASE)];
			final List<Double> ratios = new ArrayList<Double>();
			double counter = 0;
			final String ratiosString = PreFilterUtils.getOldRatioString(indexesByReplicates, s);

			boolean inReplicate1 = false;
			boolean inReplicate2 = false;
			boolean inReplicate3 = false;

			final String[] splitRatios1 = ratiosString.split(";");
			for (int i = 0; i < splitRatios1.length; i++) {
				final String[] splitRatios2 = splitRatios1[i].split(",");
				for (int j = 0; j < splitRatios2.length; j++) {
					if (!splitRatios2[j].equals("X") && !(splitRatios2[j].length() < 1)) {
						ratios.add(Double.parseDouble(splitRatios2[j]));
						counter++;

						if (i == 0) {
							inReplicate1 = true;

						}
						if (i == 1) {
							inReplicate2 = true;

						}
						if (i == 2) {
							inReplicate3 = true;

						}
					}
				}
			}

			boolean quantifiedInMoreThanOneRep = false;
			if ((inReplicate1 && inReplicate2) || (inReplicate1 && inReplicate3) || (inReplicate2 && inReplicate3)) {
				quantifiedInMoreThanOneRep = true;
			} else {
				// discarded not enough replicates
				outNotEnoughRep.write(UniProt_Acc + "\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
				outNotEnoughRep.flush();
				processed = true;
			}

			if (quantifiedInMoreThanOneRep) {
				int nonSingletonCounter = 0;
				for (int i = 0; i < ratios.size(); i++) {
					if (ratios.get(i) < singletonFilter && ratios.get(i) > singletonFilter2nd) {
						nonSingletonCounter++;
					}
				}

				double highRatioCounter = 0.0;
				int numberBelowSingletonRatioThreshold = 0;
				for (int i = 0; i < ratios.size(); i++) {
					if (ratios.get(i) >= singletonFilter) {
						highRatioCounter++;
					} else {
						numberBelowSingletonRatioThreshold++;
					}
				}
				if (highRatioCounter >= counter / 2) {
					// protein has more than half peptides singleton
					outPassed.write(UniProt_Acc + "\tN\\A\tTRUE\t>4.0_Light\tN\\A\t" + ProteinInfo + "\t" + ratiosString
							+ "\n");
					outPassed.flush();
					processed = true;
				} else {
					double lowRatioCounter = 0.0;
					int numberAboveSingletonRatioThreshold2nd = 0;
					for (int i = 0; i < ratios.size(); i++) {
						if (ratios.get(i) <= singletonFilter2nd) {
							lowRatioCounter++;
						} else {
							numberAboveSingletonRatioThreshold2nd++;
						}
					}
					if (lowRatioCounter >= counter / 2) {
						// protein has more than half peptides singleton
						outPassed.write(UniProt_Acc + "\tN\\A\tTRUE\t<0.25_Heavy\tN\\A\t" + ProteinInfo + "\t"
								+ ratiosString + "\n");
						outPassed.flush();
						processed = true;
					}
				}

				if (!processed) {

					final double[] ratiosArray = new double[nonSingletonCounter];
					int indexInArray = 0;
					for (int i = 0; i < ratios.size(); i++) {
						if ((ratios.get(i) < singletonFilter) && (ratios.get(i) > singletonFilter2nd)) {
							ratiosArray[indexInArray] = ratios.get(i);
							indexInArray++;
						}
					}

					final double avgRatio = Maths.mean(ratiosArray);

					if (ratiosArray.length == 1) {
						// if only has one ratio, is discarded for not having
						// sufficient data
						outFilteredOut.write(UniProt_Acc + "\tINSUFFICIENT_DATA\t" + avgRatio + "\t" + ProteinInfo
								+ "\t" + ratiosString + "\n");
						outFilteredOut.flush();
					} else {
						counterStatTest++;
						// t-test calculation to revisit

						// get t-statistic
						final TTest tstat = new TTest();
						double t = -1.0;
						if (avgRatio >= 1) {
							t = tstat.tTest(1.0, ratiosArray);
						} else {
							for (int i = 0; i < ratiosArray.length; i++) {
								ratiosArray[i] = 1.0 / ratiosArray[i];
							}
							t = tstat.tTest(1.0, ratiosArray);
						}

						// TDistribution tdist = new
						// TDistribution(intensitiesAvg.length-2);
						// For each peptide compute t-test pvalue and store
						// This is not exactly right since the exact value
						// of t is not included in the p-value
						// double Ttest_pvalue =
						// tdist.cumulativeProbability(t);

						final double pvalue = t / 2;

						if (pvalue < pvalueThreshold) {
							outPassed.write(UniProt_Acc + "\t" + pvalue + "\t" + "FALSE" + "\t" + "N/A" + "\t"
									+ avgRatio + "\t" + ProteinInfo + "\t" + ratiosString + "\n");
							outPassed.flush();
							processed = true;
							counterStatTestPassed++;
						}

						if (!processed) {
							outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio + "\t" + ProteinInfo
									+ "\t" + ratiosString + "\n");
							outFilteredOut.flush();
						}
					}

				}
			}
			s = in.readLine();
			if (s != null) {
				split = s.split("\t");
			}

		}
		in.close();
		outFilteredOut.close();
		outNotEnoughRep.close();
		outPassed.close();
		FDR = (counterStatTest * pvalueThreshold) / counterStatTestPassed;

		log.info("Completed");
		log.info("p-value = " + pvalueThreshold + " FDR = " + FDR);
	}

	/**
	 * @return the fDR
	 */
	public double getFDR() {
		return FDR;
	}

	/**
	 * @return the significativeInteractorsFile
	 * @throws IOException
	 */
	protected File getSignificativeInteractorsFile() throws IOException {
		if (significativeInteractorsFile == null) {
			filter();
		}
		return significativeInteractorsFile;
	}

	/**
	 * @return the noSignificativeInteractorsFile
	 * @throws IOException
	 */
	protected File getNoSignificativeInteractorsFile() throws IOException {
		if (noSignificativeInteractorsFile == null) {
			filter();
		}
		return noSignificativeInteractorsFile;
	}

	/**
	 * @return the notEnoughReplicatesFile
	 * @throws IOException
	 */
	protected File getNotEnoughReplicatesFile() throws IOException {
		if (notEnoughReplicatesFile == null) {
			filter();
		}
		return notEnoughReplicatesFile;
	}
}
