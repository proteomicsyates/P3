package edu.scripps.p3.prefilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.log4j.Logger;

import edu.scripps.yates.utilities.files.FileUtils;
import edu.scripps.yates.utilities.maths.Maths;

/**
 * Filter an mock experiment based on another mock experiment made in a
 * different experimental condition. <br>
 * Originally this class was called CRISPR_Filter_For_FM_Controlv2
 *
 * @author Salva
 *
 */
public class FilterMockExperiment {
	private static final Logger log = Logger.getLogger(FilterMockExperiment.class);

	public static final double singletonFilter = 4.0;
	public static final double singletonFilter2nd = 1.0 / singletonFilter;
	private final File ip1File;
	private final File ip2File;
	private final double pvalueThreshold;
	private final double avgRatioThreshold;
	private File passedFile;
	private File filterOutFile;

	private double FDR;

	private final boolean ip1ConditionLight;

	private final boolean ip2ConditionLight;

	protected FilterMockExperiment(File ip1File, boolean ip1ConditionLight, File ip2File, boolean ip2ConditionLight,
			double pvalueThreshold, double avgRatioThreshold) {
		this.ip1File = ip1File;
		this.ip2File = ip2File;
		this.avgRatioThreshold = avgRatioThreshold;
		this.pvalueThreshold = pvalueThreshold;
		this.ip1ConditionLight = ip1ConditionLight;
		this.ip2ConditionLight = ip2ConditionLight;
	}

	/**
	 * Filter an mock experiment based on another mock experiment made in a
	 * different experimental condition. It creates 2 output files:
	 * <ul>
	 * <li>inputFileName_Passed</li>
	 * <li>inputFileName_FilteredOut</li>
	 *
	 *
	 * @param ip1File
	 * @param ip2File
	 * @param pvalueThreshold
	 * @param avgRatioThreshold
	 * @throws IOException
	 */
	protected void filter() throws IOException {
		try {
			log.info("Filtering control experiment from files " + FilenameUtils.getName(ip1File.getAbsolutePath())
					+ " and " + FilenameUtils.getName(ip2File.getAbsolutePath()) + " using pValueThreshold="
					+ pvalueThreshold + " and avgRatioThreshold=" + avgRatioThreshold);
			// String inputFileName = "src/data/quant_stat_compare2732.txt";
			final BufferedReader in1 = new BufferedReader(new FileReader(ip1File));
			// in1 has to be FM
			final BufferedReader in2 = new BufferedReader(new FileReader(ip2File));
			// in2 has to be Phen

			passedFile = FileUtils.appendToFileName(ip1File, null, "_Passed");
			final BufferedWriter outPassed = new BufferedWriter(new FileWriter(passedFile));
			filterOutFile = FileUtils.appendToFileName(ip1File, null, "_FilteredOut");
			final BufferedWriter outFilteredOut = new BufferedWriter(new FileWriter(filterOutFile));

			outPassed.write(
					"UniProt_Accession\t" + "t-test_p-value\t" + "Fold_Change\t" + "Passed_With_Singleton_Criteria\t"
							+ "Quantified_In_More_Than_One_Replicate\t" + "Protein_Info\t" + "Ratios\n");
			outFilteredOut.write("UniProt_Accession\t" + "t-test_p-value\t" + "Fold_Change\t"
					+ "Rejected_With_Fold_Change_Criteria\t" + "Quantified_In_More_Than_One_Replicate\t"
					+ "Contaminant\t" + "Protein_Info\t" + "Ratios\n");
			outPassed.flush();
			outFilteredOut.flush();

			log.info("Executing...");

			final Set<String> proteinInLightPhen = new HashSet<String>();

			String s2 = in2.readLine();
			String[] split2 = s2.split("\t");
			while (!split2[0].equals("PLINE")) {
				s2 = in2.readLine();

				split2 = s2.split("\t");
			}
			// The reader is now at the header line
			final Map<Integer, Integer> indexByReplicate2 = PreFilterUtils.getRatioIndexesByReplicate(split2,
					PreFilterUtils.area_ratio_x_regexp);
			final Map<String, Integer> indexesByHeader2 = PreFilterUtils.getIndexesByHeaders(split2);
			s2 = in2.readLine(); // The reader is now at the first line of data
			split2 = s2.split("\t");

			while (s2 != null) {
				final String uniProt_Acc = split2[indexesByHeader2.get(PreFilterUtils.ACC)];
				if (uniProt_Acc.equals("P54646")) {
					log.info("ASDF");
				}
				// String UniProt_Acc = split2[1];
				// String[] splitRatios2 = split2[8].split(";");
				final String[] splitRatios2 = PreFilterUtils.getOldRatioString(indexByReplicate2, split2).split(";");
				final List<Double> ratios = new ArrayList<Double>();
				double counter = 0;
				for (int i = 0; i < splitRatios2.length; i++) {
					final String[] subSplitRatios2 = splitRatios2[i].split(",");
					for (int j = 0; j < subSplitRatios2.length; j++) {
						if (!subSplitRatios2[j].equals("X") && !(subSplitRatios2[j].length() < 1)) {
							if (ip2ConditionLight) {
								ratios.add(Double.parseDouble(subSplitRatios2[j]));
							} else {
								ratios.add(1 / Double.parseDouble(subSplitRatios2[j]));
							}

							counter++;
						}
					}
				}
				double singletonRatioCounter = 0.0;

				for (int i = 0; i < ratios.size(); i++) {
					if (ratios.get(i) <= (1.0 / singletonFilter)) {
						singletonRatioCounter++;
					}
				}
				// at least half of the peptides of this protein are singleton
				if ((singletonRatioCounter >= counter / 2) && (counter > 1)) {
					proteinInLightPhen.add(uniProt_Acc);
				}

				s2 = in2.readLine();
				if (s2 != null) {
					split2 = s2.split("\t");
				}
			}

			String s = in1.readLine();
			String[] split = s.split("\t");
			while (!split[0].equals("PLINE")) {
				s = in1.readLine();

				split = s.split("\t");
			}
			final Map<Integer, Integer> indexByReplicate1 = PreFilterUtils.getRatioIndexesByReplicate(split,
					PreFilterUtils.area_ratio_x_regexp);

			final Map<String, Integer> indexesByHeader1 = PreFilterUtils.getIndexesByHeaders(split);
			// The reader is now at the header line

			s = in1.readLine(); // The reader is now at the first line of data
			split = s.split("\t");

			double counterStatTest = 0.0;
			double counterStatTestPassed = 0.0;

			while (s != null) {

				boolean processed = false;
				final String UniProt_Acc = split[indexesByHeader1.get(PreFilterUtils.ACC)];
				final String ProteinInfo = split[indexesByHeader1.get(PreFilterUtils.DESCRIPTION)];
				// String UniProt_Acc = split[1];
				// String ProteinInfo = split[19];
				final List<Double> ratios = new ArrayList<Double>();
				double counter = 0;
				// String ratiosString = split[8];
				final String ratiosString = PreFilterUtils.getOldRatioString(indexByReplicate1, split);
				final String[] splitRatios1 = ratiosString.split(";");
				boolean inReplicate1 = false;
				boolean inReplicate2 = false;
				boolean inReplicate3 = false;

				for (int i = 0; i < splitRatios1.length; i++) {

					final String[] splitRatios2 = splitRatios1[i].split(",");
					for (int j = 0; j < splitRatios2.length; j++) {
						if (!splitRatios2[j].equals("X") && !(splitRatios2[j].length() < 1)) {
							if (ip1ConditionLight) {
								ratios.add(Double.parseDouble(splitRatios2[j]));
							} else {
								ratios.add(1 / Double.parseDouble(splitRatios2[j]));
							}
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
					// proteins quantified in less than 2 replicates are
					// discarded.
					// use outFilteredOut
					outFilteredOut.write(UniProt_Acc + "\tN\\A\t" + ratios.get(0) + "\tFALSE\tFALSE\tFALSE\t"
							+ ProteinInfo + "\t" + ratiosString + "\n");
					outFilteredOut.flush();
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

					for (int i = 0; i < ratios.size(); i++) {
						if (ratios.get(i) >= singletonFilter) {
							highRatioCounter++;
						}
					}
					// more than half peptides are singleton
					if (highRatioCounter >= counter / 2) {
						if (proteinInLightPhen.contains(UniProt_Acc)) {
							// proteins that have more than half peptides
							// singleton
							// in one control and the same in the other control,
							// are
							// discarded for not being an specific interaction.
							outFilteredOut.write(UniProt_Acc + "\tN\\A\tN\\A\tFALSE\tTRUE\tTRUE\t" + ProteinInfo + "\t"
									+ ratiosString + "\n");
							outFilteredOut.flush();
							processed = true;
						} else {
							// proteins that have more than half peptides
							// singleton
							// in one control but not in the other
							outPassed.write(UniProt_Acc + "\tN\\A\tN\\A\tTRUE\tTRUE\t" + ProteinInfo + "\t"
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

						if (ratiosArray.length <= 1) {
							// proteins with only 1 ratio are discarded for not
							// having enough data
							outFilteredOut.write(UniProt_Acc + "\tINSUFFICIENT_DATA\t" + avgRatio
									+ "\tFALSE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
							outFilteredOut.flush();
						} else {
							counterStatTest++;
							// t-test calculation to revisit

							// get t-statistic
							final TTest tstat = new TTest();

							final double t = tstat.tTest(1.0, ratiosArray);
							// TDistribution tdist = new
							// TDistribution(intensitiesAvg.length-2);
							// For each peptide compute t-test pvalue and store
							// This is not exactly right since the exact value
							// of t is not included in the p-value
							// double Ttest_pvalue =
							// tdist.cumulativeProbability(t);

							final double pvalue = t / 2.0;

							if (pvalue < pvalueThreshold) {
								counterStatTestPassed++;
								processed = true;
								if (avgRatio > avgRatioThreshold) {
									// discarded because ratio threshold
									outPassed.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio + "\tFALSE" + "\t"
											+ "TRUE" + "\t" + ProteinInfo + "\t" + ratiosString + "\n");
									outPassed.flush();

								} else {
									// passed filters because pvalue and ratios
									// pass thresholds
									outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio
											+ "\tTRUE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
									outFilteredOut.flush();
								}
							}

							if (!processed) {
								// discarded because pvalue
								outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio
										+ "\tFALSE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outFilteredOut.flush();
							}
						}
					}
				}

				s = in1.readLine();
				if (s != null) {
					split = s.split("\t");
				}
			}
			in1.close();
			in2.close();
			FDR = (counterStatTest * pvalueThreshold) / counterStatTestPassed;

			log.info("Completed");
			log.info("p-value = " + pvalueThreshold + " FDR = " + FDR);
		} catch (final Exception e) {
			log.warn("Error parsing input file. Now trying in a different way");
			filter2();
		}
	}

	/**
	 * This will read the file the same way, but taking into account another format
	 * of census quant compare files
	 * 
	 * @throws IOException
	 */
	private void filter2() throws IOException {
		log.info("Filtering control experiment from files " + FilenameUtils.getName(ip1File.getAbsolutePath()) + " and "
				+ FilenameUtils.getName(ip2File.getAbsolutePath()) + " using pValueThreshold=" + pvalueThreshold
				+ " and avgRatioThreshold=" + avgRatioThreshold);
		// String inputFileName = "src/data/quant_stat_compare2732.txt";
		final BufferedReader in1 = new BufferedReader(new FileReader(ip1File));
		// in1 has to be FM
		final BufferedReader in2 = new BufferedReader(new FileReader(ip2File));
		// in2 has to be Phen

		passedFile = FileUtils.appendToFileName(ip1File, null, "_Passed");
		final BufferedWriter outPassed = new BufferedWriter(new FileWriter(passedFile));
		filterOutFile = FileUtils.appendToFileName(ip1File, null, "_FilteredOut");
		final BufferedWriter outFilteredOut = new BufferedWriter(new FileWriter(filterOutFile));

		outPassed
				.write("UniProt_Accession\t" + "t-test_p-value\t" + "Fold_Change\t" + "Passed_With_Singleton_Criteria\t"
						+ "Quantified_In_More_Than_One_Replicate\t" + "Protein_Info\t" + "Ratios\n");
		outFilteredOut.write(
				"UniProt_Accession\t" + "t-test_p-value\t" + "Fold_Change\t" + "Rejected_With_Fold_Change_Criteria\t"
						+ "Quantified_In_More_Than_One_Replicate\t" + "Contaminant\t" + "Protein_Info\t" + "Ratios\n");
		outPassed.flush();
		outFilteredOut.flush();

		log.info("Executing...");

		final Set<String> proteinWithSingletonsInControl2 = new HashSet<String>();

		String s2 = in2.readLine();
		String[] split2 = s2.split("\t");

		while (!split2[0].equals("locus")) {

			s2 = in2.readLine();

			split2 = s2.split("\t");
		}
		// The reader is now at the header line
		final Map<Integer, Integer> indexByReplicate2 = PreFilterUtils.getRatioIndexesByReplicate(split2,
				PreFilterUtils.area_ratio_x_regexp_new_format);
		final Map<String, Integer> indexesByHeader2 = PreFilterUtils.getIndexesByHeaders(split2);
		s2 = in2.readLine(); // The reader is now at the first line of data
		split2 = s2.split("\t");

		while (s2 != null) {
			final String uniProt_Acc = split2[indexesByHeader2.get(PreFilterUtils.LOCUS)];
			if (uniProt_Acc.equals("P54646")) {
				log.info(uniProt_Acc + " in file " + FilenameUtils.getName(ip2File.getAbsolutePath()));
			}
			final String proteinDescription = split2[indexesByHeader2.get(PreFilterUtils.DESCRIPTION_LOWER_CASE)];
			// String UniProt_Acc = split2[1];
			// String[] splitRatios2 = split2[8].split(";");
			final String oldRatioString = PreFilterUtils.getOldRatioString(indexByReplicate2, split2);
			if (proteinDescription.contains("PRKAA2")) {
				log.info(uniProt_Acc + "\t" + proteinDescription + "\t" + oldRatioString);
			}
			final String[] splitRatios2 = oldRatioString.split(";");
			final List<Double> ratios = new ArrayList<Double>();
			int counter = 0;
			for (int i = 0; i < splitRatios2.length; i++) {
				final String[] subSplitRatios2 = splitRatios2[i].split(",");
				for (int j = 0; j < subSplitRatios2.length; j++) {
					if (!subSplitRatios2[j].equals("X") && !(subSplitRatios2[j].length() < 1)) {
						if (ip2ConditionLight) {
							ratios.add(Double.parseDouble(subSplitRatios2[j]));
						} else {
							ratios.add(1 / Double.parseDouble(subSplitRatios2[j]));
						}
						counter++;
					}
				}
			}
			int singletonRatioCounter = 0;

			for (int i = 0; i < ratios.size(); i++) {
				if (ratios.get(i) <= singletonFilter2nd) {
					singletonRatioCounter++;
				}
			}
			// at least half of the peptides of this protein are singleton
			if ((singletonRatioCounter >= counter / 2) && (counter > 1)) {
				proteinWithSingletonsInControl2.add(uniProt_Acc);
			}

			s2 = in2.readLine();
			if (s2 != null) {
				split2 = s2.split("\t");
			}
		}
		System.out.println(proteinWithSingletonsInControl2.size()
				+ " proteins with more than half peptides as singletons in control 2 ("
				+ FilenameUtils.getBaseName(ip2File.getAbsolutePath()) + ")");
		String s = in1.readLine();
		String[] split = s.split("\t");
		while (!split[0].equals("locus")) {
			s = in1.readLine();

			split = s.split("\t");
		}
		final Map<Integer, Integer> indexByReplicate1 = PreFilterUtils.getRatioIndexesByReplicate(split,
				PreFilterUtils.area_ratio_x_regexp_new_format);

		final Map<String, Integer> indexesByHeader1 = PreFilterUtils.getIndexesByHeaders(split);
		// The reader is now at the header line

		s = in1.readLine(); // The reader is now at the first line of data
		split = s.split("\t");

		double counterStatTest = 0.0;
		double counterStatTestPassed = 0.0;

		while (s != null) {

			boolean processed = false;
			final String UniProt_Acc = split[indexesByHeader1.get(PreFilterUtils.LOCUS)];
			final String ProteinInfo = split[indexesByHeader1.get(PreFilterUtils.DESCRIPTION_LOWER_CASE)];
			// String UniProt_Acc = split[1];
			// String ProteinInfo = split[19];
			final List<Double> ratios = new ArrayList<Double>();
			double counter = 0;
			// String ratiosString = split[8];
			final String ratiosString = PreFilterUtils.getOldRatioString(indexByReplicate1, split);
			if (ProteinInfo.contains("PRKAA2")) {
				log.info(ProteinInfo + "\t" + ratiosString);
			}
			final String[] splitRatios1 = ratiosString.split(";");
			boolean inReplicate1 = false;
			boolean inReplicate2 = false;
			boolean inReplicate3 = false;

			for (int i = 0; i < splitRatios1.length; i++) {

				final String[] splitRatios2 = splitRatios1[i].split(",");
				for (int j = 0; j < splitRatios2.length; j++) {
					if (!splitRatios2[j].equals("X") && !(splitRatios2[j].length() < 1)) {
						if (ip1ConditionLight) {
							ratios.add(Double.parseDouble(splitRatios2[j]));
						} else {
							ratios.add(1 / Double.parseDouble(splitRatios2[j]));

						}
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
				// proteins quantified in less than 2 replicates are discarded.
				// use outFilteredOut
				outFilteredOut.write(UniProt_Acc + "\tN\\A\t" + ratios.get(0) + "\tFALSE\tFALSE\tFALSE\t" + ProteinInfo
						+ "\t" + ratiosString + "\n");
				outFilteredOut.flush();
				processed = true;
			}

			if (quantifiedInMoreThanOneRep) {
				int nonSingletonCounter = 0;
				int highRatioCounter = 0;
				for (int i = 0; i < ratios.size(); i++) {
					if (ratios.get(i) < singletonFilter && ratios.get(i) > singletonFilter2nd) {
						nonSingletonCounter++;
					}
					if (ratios.get(i) >= singletonFilter) {
						highRatioCounter++;
					}
				}

				// at least half peptides are singleton
				if (highRatioCounter >= counter / 2) {
					if (proteinWithSingletonsInControl2.contains(UniProt_Acc)) {
						// proteins that have at least half peptides singleton
						// in one control and the same in the other control, are
						// discarded for not being an specific interaction.
						outFilteredOut.write(UniProt_Acc + "\tN\\A\tN\\A\tFALSE\tTRUE\tTRUE\t" + ProteinInfo + "\t"
								+ ratiosString + "\n");
						outFilteredOut.flush();
						processed = true;
					} else {
						// proteins that have more than half peptides singleton
						// in one control but not in the other
						outPassed.write(
								UniProt_Acc + "\tN\\A\tN\\A\tTRUE\tTRUE\t" + ProteinInfo + "\t" + ratiosString + "\n");
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

					if (ratiosArray.length <= 1) {
						// proteins with only 1 ratio are discarded for not
						// having enough data
						outFilteredOut.write(UniProt_Acc + "\tINSUFFICIENT_DATA\t" + avgRatio + "\tFALSE\tTRUE\tFALSE\t"
								+ ProteinInfo + "\t" + ratiosString + "\n");
						outFilteredOut.flush();
					} else {
						counterStatTest++;
						// t-test calculation to revisit

						// get t-statistic
						final TTest tstat = new TTest();

						final double t = tstat.tTest(1.0, ratiosArray);
						// TDistribution tdist = new
						// TDistribution(intensitiesAvg.length-2);
						// For each peptide compute t-test pvalue and store
						// This is not exactly right since the exact value
						// of t is not included in the p-value
						// double Ttest_pvalue =
						// tdist.cumulativeProbability(t);

						final double pvalue = t / 2.0;

						if (pvalue < pvalueThreshold) {
							counterStatTestPassed++;
							processed = true;
							if (avgRatio > avgRatioThreshold) {
								// passed filters becasue pvalue and ratios pass
								// thresholds
								outPassed.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio + "\tFALSE" + "\t"
										+ "TRUE" + "\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outPassed.flush();

							} else {
								// discarded because ratio threshold

								outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio
										+ "\tTRUE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outFilteredOut.flush();
							}
						}

						if (!processed) {
							// discarded because pvalue
							outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio
									+ "\tFALSE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
							outFilteredOut.flush();
						}
					}
				}
			}

			s = in1.readLine();
			if (s != null) {
				split = s.split("\t");
			}
		}
		in1.close();
		in2.close();
		FDR = (counterStatTest * pvalueThreshold) / counterStatTestPassed;

		log.info("Completed");
		log.info("p-value = " + pvalueThreshold + " FDR = " + FDR);
	}

	/**
	 * @return the fDR
	 */
	protected double getFDR() {
		return FDR;
	}

	/**
	 * @return the passedFile
	 * @throws IOException
	 */
	protected File getPassedFile() throws IOException {
		if (passedFile == null) {
			filter();
		}
		return passedFile;
	}

	/**
	 * @return the filterOutFile
	 * @throws IOException
	 */
	protected File getFilterOutFile() throws IOException {
		if (filterOutFile == null) {
			filter();
		}
		return filterOutFile;
	}

}
