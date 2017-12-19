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

	public static void main(String[] args) {

		final FilterMockExperiment filterMockExperiment = new FilterMockExperiment(new File(args[0]), new File(args[1]),
				Double.parseDouble(args[2]), Double.parseDouble(args[3]));
		try {
			filterMockExperiment.filter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected FilterMockExperiment(File ip1File, File ip2File, double pvalueThreshold, double avgRatioThreshold) {
		this.ip1File = ip1File;
		this.ip2File = ip2File;
		this.avgRatioThreshold = avgRatioThreshold;
		this.pvalueThreshold = pvalueThreshold;
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

		log.info("Filtering control experiment from files " + FilenameUtils.getName(ip1File.getAbsolutePath()) + " and "
				+ FilenameUtils.getName(ip2File.getAbsolutePath()) + " using pValueThreshold=" + pvalueThreshold
				+ " and avgRatioThreshold=" + avgRatioThreshold);
		// String inputFileName = "src/data/quant_stat_compare2732.txt";
		BufferedReader in1 = new BufferedReader(new FileReader(ip1File));
		// in1 has to be FM
		BufferedReader in2 = new BufferedReader(new FileReader(ip2File));
		// in2 has to be Phen

		passedFile = FileUtils.appendToFileName(ip1File, null, "_Passed");
		BufferedWriter outPassed = new BufferedWriter(new FileWriter(passedFile));
		filterOutFile = FileUtils.appendToFileName(ip1File, null, "_FilteredOut");
		BufferedWriter outFilteredOut = new BufferedWriter(new FileWriter(filterOutFile));

		outPassed.write(
				"UniProt_Accession\tt-test_p-value\tFold_Change\tPassed_With_Singleton_Criteria\tQuantified_In_More_Than_One_Replicate\tProtein_Info\tRatios\n");
		outFilteredOut.write(
				"UniProt_Accession\tt-test_p-value\tFold_Change\tRejected_With_Fold_Change_Criteria\tQuantified_In_More_Than_One_Replicate\tContaminant\tProtein_Info\tRatios\n");
		outPassed.flush();
		outFilteredOut.flush();

		log.info("Executing...");

		Set<String> ProteinInLightPhen = new HashSet<String>();

		String s2 = in2.readLine();
		String[] split2 = s2.split("\t");
		while (!split2[0].equals("PLINE")) {
			s2 = in2.readLine();

			split2 = s2.split("\t");
		}
		// The reader is now at the header line
		Map<Integer, Integer> indexByReplicate2 = PreFilterUtils.getRatioIndexesByReplicate(split2);
		Map<String, Integer> indexesByHeader2 = PreFilterUtils.getIndexesByHeaders(split2);
		s2 = in2.readLine(); // The reader is now at the first line of data
		split2 = s2.split("\t");

		while (s2 != null) {
			String UniProt_Acc = split2[indexesByHeader2.get(PreFilterUtils.ACC)];
			// String UniProt_Acc = split2[1];
			// String[] splitRatios2 = split2[8].split(";");
			String[] splitRatios2 = PreFilterUtils.getOldRatioString(indexByReplicate2, split2).split(";");
			List<Double> ratios = new ArrayList<Double>();
			double counter = 0;
			for (int i = 0; i < splitRatios2.length; i++) {
				String[] subSplitRatios2 = splitRatios2[i].split(",");
				for (int j = 0; j < subSplitRatios2.length; j++) {
					if (!subSplitRatios2[j].equals("X") && !(subSplitRatios2[j].length() < 1)) {
						ratios.add(Double.parseDouble(subSplitRatios2[j]));
						counter++;
					}
				}
			}
			double lowRatioCounter = 0.0;

			for (int i = 0; i < ratios.size(); i++) {
				if (ratios.get(i) <= (1.0 / singletonFilter)) {
					lowRatioCounter++;
				}
			}

			if ((lowRatioCounter >= counter / 2) && (counter > 1)) {
				ProteinInLightPhen.add(UniProt_Acc);
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
		Map<Integer, Integer> indexByReplicate1 = PreFilterUtils.getRatioIndexesByReplicate(split);

		Map<String, Integer> indexesByHeader1 = PreFilterUtils.getIndexesByHeaders(split);
		// The reader is now at the header line

		s = in1.readLine(); // The reader is now at the first line of data
		split = s.split("\t");

		double counterStatTest = 0.0;
		double counterStatTestPassed = 0.0;

		while (s != null) {

			boolean processed = false;
			String UniProt_Acc = split[indexesByHeader1.get(PreFilterUtils.ACC)];
			String ProteinInfo = split[indexesByHeader1.get(PreFilterUtils.DESCRIPTION)];
			// String UniProt_Acc = split[1];
			// String ProteinInfo = split[19];
			List<Double> ratios = new ArrayList<Double>();
			double counter = 0;
			// String ratiosString = split[8];
			String ratiosString = PreFilterUtils.getOldRatioString(indexByReplicate1, split);
			String[] splitRatios1 = ratiosString.split(";");
			boolean inReplicate1 = false;
			boolean inReplicate2 = false;
			boolean inReplicate3 = false;

			for (int i = 0; i < splitRatios1.length; i++) {

				String[] splitRatios2 = splitRatios1[i].split(",");
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
				outFilteredOut.write(UniProt_Acc + "\tN\\A\t" + ratios.get(0) + "\tFALSE\tFALSE\tFALSE\t" + ProteinInfo
						+ "\t" + ratiosString + "\n");
				outFilteredOut.flush();
				processed = true;
			}

			if (quantifiedInMoreThanOneRep) {
				int nonSingletonCounter = 0;
				for (int i = 0; i < ratios.size(); i++) {
					if (!(ratios.get(i) >= singletonFilter) && !(ratios.get(i) <= singletonFilter2nd)) {
						nonSingletonCounter++;
					}
				}

				double highRatioCounter = 0.0;

				for (int i = 0; i < ratios.size(); i++) {
					if (ratios.get(i) >= singletonFilter) {
						highRatioCounter++;
					}
				}
				if (highRatioCounter >= counter / 2) {
					if (ProteinInLightPhen.contains(UniProt_Acc)) {
						outFilteredOut.write(UniProt_Acc + "\tN\\A\tN\\A\tFALSE\tTRUE\tTRUE\t" + ProteinInfo + "\t"
								+ ratiosString + "\n");
						outFilteredOut.flush();
						processed = true;
					} else {
						outPassed.write(
								UniProt_Acc + "\tN\\A\tN\\A\tTRUE\tTRUE\t" + ProteinInfo + "\t" + ratiosString + "\n");
						outPassed.flush();
						processed = true;
					}
				}

				if (!processed) {

					double[] ratiosArray = new double[nonSingletonCounter];
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
						outFilteredOut.write(UniProt_Acc + "\tINSUFFICIENT_DATA\t" + avgRatio + "\tFALSE\tTRUE\tFALSE\t"
								+ ProteinInfo + "\t" + ratiosString + "\n");
						outFilteredOut.flush();
					} else {
						counterStatTest++;
						// t-test calculation to revisit

						// get t-statistic
						TTest tstat = new TTest();

						double t = tstat.tTest(1.0, ratiosArray);
						// TDistribution tdist = new
						// TDistribution(intensitiesAvg.length-2);
						// For each peptide compute t-test pvalue and store
						// This is not exactly right since the exact value
						// of t is not included in the p-value
						// double Ttest_pvalue =
						// tdist.cumulativeProbability(t);

						double pvalue = t / 2.0;

						if (pvalue < pvalueThreshold) {
							counterStatTestPassed++;
							processed = true;
							if (avgRatio > avgRatioThreshold) {
								outPassed.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio + "\tFALSE" + "\t"
										+ "TRUE" + "\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outPassed.flush();

							} else {
								outFilteredOut.write(UniProt_Acc + "\t" + pvalue + "\t" + avgRatio
										+ "\tTRUE\tTRUE\tFALSE\t" + ProteinInfo + "\t" + ratiosString + "\n");
								outFilteredOut.flush();
							}
						}

						if (!processed) {
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
