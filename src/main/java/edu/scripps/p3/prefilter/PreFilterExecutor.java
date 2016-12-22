package edu.scripps.p3.prefilter;

import java.io.File;
import java.io.IOException;

public class PreFilterExecutor {
	private final File controlAFile;
	private final File controlBFile;
	private final File realAoverB;
	private final double pValueThreshold;
	private final double avgRatioThreshold;

	public PreFilterExecutor(File controlAFile, File controlBFile, File realAoverB, double pValueThreshold,
			double avgRatioThreshold) {
		this.controlAFile = controlAFile;
		this.controlBFile = controlBFile;
		this.realAoverB = realAoverB;
		this.pValueThreshold = pValueThreshold;
		this.avgRatioThreshold = avgRatioThreshold;
	}

	public static void main(String[] args) {
		File controlAFile = getFileFromArgument(args[0]);

		File controlBFile = getFileFromArgument(args[1]);

		File realAOverB = getFileFromArgument(args[2]);
		double pValueThreshold = Double.valueOf(args[3]);
		double avgRatioThreshold = Double.valueOf(args[4]);
		PreFilterExecutor executor = new PreFilterExecutor(controlAFile, controlBFile, realAOverB, pValueThreshold,
				avgRatioThreshold);
		File run;
		try {
			run = executor.run();
			System.out.println("Final file created at: " + run.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static File getFileFromArgument(String argument) {
		File file = new File(argument);
		if (file.getParentFile() == null) {
			return new File(System.getProperty("user.dir") + File.separator + argument);
		} else {
			return file;
		}
	}

	public File run() throws IOException {
		final FilterMockExperiment filterMockExperiment1 = new FilterMockExperiment(controlAFile, controlBFile,
				pValueThreshold, avgRatioThreshold);
		final FilterMockExperiment filterMockExperiment2 = new FilterMockExperiment(controlBFile, controlAFile,
				pValueThreshold, avgRatioThreshold);
		final FilterRealExperiment filterRealExperiment = new FilterRealExperiment(realAoverB, pValueThreshold);

		final RemoveNonSpecificInteractors removeNonSpecificInteractors = new RemoveNonSpecificInteractors(
				filterMockExperiment1, filterMockExperiment2, filterRealExperiment);
		return removeNonSpecificInteractors.getSignificativeInteractorsWithoutNonSpecific();
	}
}
