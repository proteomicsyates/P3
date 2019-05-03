package edu.scripps.p3.prefilter;

import java.io.File;
import java.io.IOException;

public class PreFilterExecutor {
	private final File controlAFile;
	private final boolean ipConditionLightA; // is the condition light the real
												// IP in control A?
	private final File controlBFile;
	private final boolean ipConditionLightB; // is the condition light the real
												// IP in control B?
	private final File realAoverB;
	private final double pValueThreshold;
	private final double avgRatioThreshold;

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

		final RemoveNonSpecificInteractors removeNonSpecificInteractors = new RemoveNonSpecificInteractors(
				filterMockExperiment1, filterMockExperiment2, filterRealExperiment);
		return removeNonSpecificInteractors;
	}
}
