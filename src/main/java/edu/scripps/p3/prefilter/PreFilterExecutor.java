package edu.scripps.p3.prefilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.scripps.yates.utilities.fasta.FastaParser;
import gnu.trove.map.hash.TDoubleObjectHashMap;

public class PreFilterExecutor {
	private final File controlAFile;
	private final boolean ipConditionLightA; // is the condition light the real
												// IP in control A?
	private final File controlBFile;
	private final boolean ipConditionLightB; // is the condition light the real
												// IP in control B?
	private final File realAoverB;
	private final double pValueThreshold;
	private double avgRatioThreshold;
	private RemoveNonSpecificInteractors removeNonSpecificInteractors;
	private String name;
	private final TDoubleObjectHashMap<List<String>> confidenceInteractorsByFoldChangeThreshold = new TDoubleObjectHashMap<List<String>>();
	private final TDoubleObjectHashMap<List<String>> knownInteractorsByFoldChangeThreshold = new TDoubleObjectHashMap<List<String>>();

	public PreFilterExecutor(File controlAFile, File controlBFile, File realAoverB, double pValueThreshold,
			double avgRatioThreshold) {
		this(controlAFile, true, controlBFile, false, realAoverB, pValueThreshold, avgRatioThreshold);
	}

	public PreFilterExecutor(File controlAFile, boolean ipConditionLightA, File controlBFile, boolean ipConditionLightB,
			File realAoverB, double pValueThreshold, double avgRatioThreshold) {
		this.controlAFile = controlAFile;
		this.ipConditionLightA = ipConditionLightA;
		this.controlBFile = controlBFile;
		this.ipConditionLightB = ipConditionLightB;
		this.realAoverB = realAoverB;
		this.pValueThreshold = pValueThreshold;
		this.avgRatioThreshold = avgRatioThreshold;
	}

	public void setavgRatioThreshold(double avgRatioThreshold) {
		this.avgRatioThreshold = avgRatioThreshold;
	}

	public static void main(String[] args) {
		File controlAFile = null;

		File controlBFile = null;

		File realAOverB = null;
		double pValueThreshold;
		double avgRatioThreshold;
		PreFilterExecutor executor = null;
		if (args.length == 5) {
			controlAFile = getFileFromArgument(args[0]);
			controlBFile = getFileFromArgument(args[1]);
			realAOverB = getFileFromArgument(args[2]);
			pValueThreshold = Double.valueOf(args[3]);
			avgRatioThreshold = Double.valueOf(args[4]);
			executor = new PreFilterExecutor(controlAFile, controlBFile, realAOverB, pValueThreshold,
					avgRatioThreshold);
		} else if (args.length == 7) {
			controlAFile = getFileFromArgument(args[0]);
			final boolean ipConditionLightA = Boolean.valueOf(args[1]);
			controlBFile = getFileFromArgument(args[2]);
			final boolean ipConditionLightB = Boolean.valueOf(args[3]);
			realAOverB = getFileFromArgument(args[4]);
			pValueThreshold = Double.valueOf(args[5]);
			avgRatioThreshold = Double.valueOf(args[6]);
			executor = new PreFilterExecutor(controlAFile, ipConditionLightA, controlBFile, ipConditionLightB,
					realAOverB, pValueThreshold, avgRatioThreshold);
		}

		try {
			final RemoveNonSpecificInteractors specificInteractors = executor.run();
			final File significativeInteractorsWithoutNonSpecific = specificInteractors
					.getSpecificSignificativeInteractors();
			System.out.println(
					"File for specific interactors that are significantly different between the 2 conditions created at: "
							+ significativeInteractorsWithoutNonSpecific.getAbsolutePath());
			final File nonSignificativeInteractorsWithoutNonSpecific = specificInteractors
					.getSpecificNonSignificativeInteractors();
			System.out.println(
					"File for specific interactors that are NOT significantly different between the 2 conditions created at: "
							+ nonSignificativeInteractorsWithoutNonSpecific.getAbsolutePath());
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static File getFileFromArgument(String argument) {
		final File file = new File(argument);
		if (file.getParentFile() == null) {
			return new File(System.getProperty("user.dir") + File.separator + argument);
		} else {
			return file;
		}
	}

	public RemoveNonSpecificInteractors run() throws IOException {
		final FilterMockExperiment filterMockExperiment1 = new FilterMockExperiment(controlAFile, ipConditionLightA,
				controlBFile, ipConditionLightB, pValueThreshold, avgRatioThreshold);
		final FilterMockExperiment filterMockExperiment2 = new FilterMockExperiment(controlBFile, ipConditionLightB,
				controlAFile, ipConditionLightA, pValueThreshold, avgRatioThreshold);
		final FilterRealExperiment filterRealExperiment = new FilterRealExperiment(realAoverB, pValueThreshold);

		removeNonSpecificInteractors = new RemoveNonSpecificInteractors(filterMockExperiment1, filterMockExperiment2,
				filterRealExperiment);
		return removeNonSpecificInteractors;
	}

	public List<String> getConfidentInteractors() throws IOException {
		final File specificNonSignificativeInteractors = removeNonSpecificInteractors
				.getSpecificNonSignificativeInteractors();
		final List<String> nonSignificantInteractors = Files
				.readAllLines(Paths.get(specificNonSignificativeInteractors.toURI())).stream().skip(1)
				.map(line -> line.split("\t")[4]).map(fastaHeader -> FastaParser.getGeneFromFastaHeader(fastaHeader))
				.filter(gene -> Objects.nonNull(gene)).distinct().collect(Collectors.toList());
		final File specificSignificativeInteractors = removeNonSpecificInteractors
				.getSpecificSignificativeInteractors();
		final List<String> significantInteractors = Files
				.readAllLines(Paths.get(specificSignificativeInteractors.toURI())).stream().skip(1)
				.map(line -> line.split("\t")[6]).map(fastaHeader -> FastaParser.getGeneFromFastaHeader(fastaHeader))
				.filter(gene -> Objects.nonNull(gene)).distinct().collect(Collectors.toList());
		final List<String> ret = new ArrayList<String>();
		for (final String interactor : nonSignificantInteractors) {
			if (!ret.contains(interactor)) {
				ret.add(interactor);
			}
		}
		for (final String interactor : significantInteractors) {
			if (!ret.contains(interactor)) {
				ret.add(interactor);
			}
		}
		setConfidenceIteractors(ret);
		return ret;
	}

	private void setConfidenceIteractors(List<String> confidenceIteractors) {

		Collections.sort(confidenceIteractors);
		confidenceInteractorsByFoldChangeThreshold.put(this.avgRatioThreshold, confidenceIteractors);
	}

	void setKnownIteractors(List<String> knownIteractors) {

		Collections.sort(knownIteractors);
		knownInteractorsByFoldChangeThreshold.put(this.avgRatioThreshold, knownIteractors);
	}

	public TDoubleObjectHashMap<List<String>> getConfidenceInteractorsByFoldchangeThreshold() {
		return confidenceInteractorsByFoldChangeThreshold;
	}

	public TDoubleObjectHashMap<List<String>> getKnownInteractorsByFoldchangeThreshold() {
		return knownInteractorsByFoldChangeThreshold;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
