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
	private final File realExperimentSignificative;
	private final File realExperimentNonSignificative;
	private File specificSignificativeInteractors;
	private File specificNonsignificativeInteractors;

	protected RemoveNonSpecificInteractors(FilterMockExperiment filterMock1, FilterMockExperiment filterMock2,
			FilterRealExperiment filterRealExperiment) throws IOException {
		this(filterMock1.getFilterOutFile(), filterMock1.getPassedFile(), filterMock2.getFilterOutFile(),
				filterMock2.getPassedFile(), filterRealExperiment.getSignificativeInteractorsFile(),
				filterRealExperiment.getNoSignificativeInteractorsFile());
	}

	protected RemoveNonSpecificInteractors(File mock1FilterOut, File mock1Passed, File mock2FilterOut, File mock2Passed,
			File realExperimentSignificative, File realExperimentNonSignificative) {
		this.mock1FilterOut = mock1FilterOut;
		this.mock1Passed = mock1Passed;
		this.mock2FilterOut = mock2FilterOut;
		this.mock2Passed = mock2Passed;
		this.realExperimentSignificative = realExperimentSignificative;
		this.realExperimentNonSignificative = realExperimentNonSignificative;
	}

	private File filterFromSignificatives() throws IOException {
		return filter(realExperimentSignificative);
	}

	private File filterFromNonSignificatives() throws IOException {
		return filter(realExperimentNonSignificative);
	}

	private File filter(File toFilter) throws IOException {
		log.info("Filtering non specific interactors from " + FilenameUtils.getName(toFilter.getAbsolutePath())
				+ " using the following files: " + FilenameUtils.getName(mock1FilterOut.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock1Passed.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock2FilterOut.getAbsolutePath()) + ", "
				+ FilenameUtils.getName(mock2Passed.getAbsolutePath()));
		final BufferedReader inNon1 = new BufferedReader(new FileReader(mock1FilterOut));
		final BufferedReader inNon2 = new BufferedReader(new FileReader(mock2FilterOut));
		final BufferedReader in1 = new BufferedReader(new FileReader(mock1Passed));
		final BufferedReader in2 = new BufferedReader(new FileReader(mock2Passed));

		final Set<String> inclusionSet1 = new HashSet<String>();
		final Set<String> inclusionSet2 = new HashSet<String>();

		final Set<String> exclusionSet1 = new HashSet<String>();
		final Set<String> exclusionSet2 = new HashSet<String>();

		String line1 = in1.readLine();
		line1 = in1.readLine();

		while (line1 != null) {
			final String[] split = line1.split("\t");
			inclusionSet1.add(split[0]);
			line1 = in1.readLine();
		}

		String line2 = in2.readLine();
		line2 = in2.readLine();

		while (line2 != null) {
			final String[] split = line2.split("\t");
			inclusionSet2.add(split[0]);
			line2 = in2.readLine();
		}

		String lineNon1 = inNon1.readLine();
		lineNon1 = inNon1.readLine();

		while (lineNon1 != null) {
			final String[] split = lineNon1.split("\t");
			exclusionSet1.add(split[0]);
			lineNon1 = inNon1.readLine();
		}

		String lineNon2 = inNon2.readLine();
		lineNon2 = inNon2.readLine();

		while (lineNon2 != null) {
			final String[] split = lineNon2.split("\t");
			exclusionSet2.add(split[0]);
			lineNon2 = inNon2.readLine();
		}

		final BufferedReader in3 = new BufferedReader(new FileReader(toFilter));

		final File specificInteractors = FileUtils.appendToFileName(toFilter, null, "_WithoutNonSpecific");
		final BufferedWriter out = new BufferedWriter(new FileWriter(specificInteractors));

		String line3 = in3.readLine();
		out.write(line3 + "\n");
		out.flush();
		line3 = in3.readLine();

		while (line3 != null) {
			int numTimesInInclusionList = 0;
			final String[] split = line3.split("\t");
			if (split[0].equals("P35610")) {
				log.info("asdf");
			}
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
		return specificInteractors;

	}

	protected File getSpecificSignificativeInteractors() throws IOException {
		if (specificSignificativeInteractors == null) {
			specificSignificativeInteractors = filterFromSignificatives();
		}
		return specificSignificativeInteractors;
	}

	protected File getSpecificNonSignificativeInteractors() throws IOException {
		if (specificNonsignificativeInteractors == null) {
			specificNonsignificativeInteractors = filterFromNonSignificatives();
		}
		return specificNonsignificativeInteractors;
	}
}
