package edu.scripps.p3.prefilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.scripps.yates.utilities.files.FileUtils;

/**
 * Class that uses the output from two control experiment filters (
 * {@link FilterMockExperiment} ) files and the output of a
 * {@link FilterRealExperiment} and filters the real experiment creating a final
 * file with the real interactors.<br>
 * Originally, the class was called RemoveNonSpecificv2
 *
 * @author Salva
 *
 */
public class RemoveNonSpecificInteractors {
	private final static Logger log = Logger.getLogger(RemoveNonSpecificInteractors.class);
	private final File mock1FilterOut;
	private final File mock2FilterOut;
	private final File mock1Passed;
	private final File mock2Passed;
	private final File significativeRealExperiment;
	private File significativeInteractorsWithoutNonSpecific;

	public static void main(String[] args) {

		String inputFileNameNonSpecific1 = args[0];
		String inputFileNameNonSpecific2 = args[1];
		String inputFileNameSpecific1 = args[2];
		String inputFileNameSpecific2 = args[3];
		String realExperiment = args[4];
		final RemoveNonSpecificInteractors removeNonSpecificInteractors = new RemoveNonSpecificInteractors(
				new File(inputFileNameNonSpecific1), new File(inputFileNameNonSpecific2),
				new File(inputFileNameSpecific1), new File(inputFileNameSpecific2), new File(realExperiment));
		try {
			removeNonSpecificInteractors.filter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected RemoveNonSpecificInteractors(FilterMockExperiment filterMock1, FilterMockExperiment filterMock2,
			FilterRealExperiment filterRealExperiment) throws IOException {
		this(filterMock1.getFilterOutFile(), filterMock1.getPassedFile(), filterMock2.getFilterOutFile(),
				filterMock2.getPassedFile(), filterRealExperiment.getSignificativeInteractorsFile());
	}

	protected RemoveNonSpecificInteractors(File mock1FilterOut, File mock1Passed, File mock2FilterOut, File mock2Passed,
			File significativeRealExperiment) {
		this.mock1FilterOut = mock1FilterOut;
		this.mock1Passed = mock1Passed;
		this.mock2FilterOut = mock2FilterOut;
		this.mock2Passed = mock2Passed;
		this.significativeRealExperiment = significativeRealExperiment;
	}

	/**
	 *
	 * @param mock1FilterOut
	 * @param mock2FilterOut
	 * @param mock1Passed
	 * @param mock2Passed
	 * @param realExperiment
	 * @return
	 * @throws IOException
	 */
	protected File filter() throws IOException {

		log.info("Filtering non specific interactors from "
				+ FilenameUtils.getName(significativeRealExperiment.getAbsolutePath()) + " using the following files: "
				+ FilenameUtils.getName(mock1FilterOut.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock1Passed.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock2FilterOut.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock2Passed.getAbsolutePath()));
		BufferedReader inNon1 = new BufferedReader(new FileReader(mock1FilterOut));
		BufferedReader inNon2 = new BufferedReader(new FileReader(mock2FilterOut));
		BufferedReader in1 = new BufferedReader(new FileReader(mock1Passed));
		BufferedReader in2 = new BufferedReader(new FileReader(mock2Passed));

		Set<String> inclusionSet1 = new HashSet<String>();
		Set<String> inclusionSet2 = new HashSet<String>();

		Set<String> exclusionSet1 = new HashSet<String>();
		Set<String> exclusionSet2 = new HashSet<String>();

		String line1 = in1.readLine();
		line1 = in1.readLine();

		while (line1 != null) {
			String[] split = line1.split("\t");
			inclusionSet1.add(split[0]);
			line1 = in1.readLine();
		}

		String line2 = in2.readLine();
		line2 = in2.readLine();

		while (line2 != null) {
			String[] split = line2.split("\t");
			inclusionSet2.add(split[0]);
			line2 = in2.readLine();
		}

		String lineNon1 = inNon1.readLine();
		lineNon1 = inNon1.readLine();

		while (lineNon1 != null) {
			String[] split = lineNon1.split("\t");
			exclusionSet1.add(split[0]);
			lineNon1 = inNon1.readLine();
		}

		String lineNon2 = inNon2.readLine();
		lineNon2 = inNon2.readLine();

		while (lineNon2 != null) {
			String[] split = lineNon2.split("\t");
			exclusionSet2.add(split[0]);
			lineNon2 = inNon2.readLine();
		}

		BufferedReader in3 = new BufferedReader(new FileReader(significativeRealExperiment));

		significativeInteractorsWithoutNonSpecific = FileUtils.appendToFileName(significativeRealExperiment, null,
				"_WithoutNonSpecific");
		BufferedWriter out = new BufferedWriter(new FileWriter(significativeInteractorsWithoutNonSpecific));

		String line3 = in3.readLine();
		out.write(line3 + "\n");
		out.flush();
		line3 = in3.readLine();

		while (line3 != null) {
			int numTimesInInclusionList = 0;
			String[] split = line3.split("\t");
			boolean include;
			if (inclusionSet1.contains(split[0]) || inclusionSet2.contains(split[0])) {
				include = true;
				if (inclusionSet1.contains(split[0]) && inclusionSet2.contains(split[0])) {
					numTimesInInclusionList = 2;
				} else {
					numTimesInInclusionList = 1;
				}
			} else if (exclusionSet1.contains(split[0]) || exclusionSet2.contains(split[0])) {
				include = false;
			} else {
				include = true;
			}
			if (include) {
				out.write(line3 + "\t" + numTimesInInclusionList + "\n");
				out.flush();
			}
			line3 = in3.readLine();
		}
		in1.close();
		in2.close();
		in3.close();
		inNon1.close();
		inNon2.close();
		out.close();
		log.info("Execution terminated.");
		return significativeInteractorsWithoutNonSpecific;

	}

	/**
	 * @return the significativeInteractorsWithoutNonSpecific
	 * @throws IOException
	 */
	protected File getSignificativeInteractorsWithoutNonSpecific() throws IOException {
		if (significativeInteractorsWithoutNonSpecific == null) {
			filter();
		}
		return significativeInteractorsWithoutNonSpecific;
	}
}
