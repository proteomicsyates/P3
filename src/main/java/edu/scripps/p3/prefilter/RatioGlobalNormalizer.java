package edu.scripps.p3.prefilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.scripps.yates.utilities.maths.Maths;
import gnu.trove.list.array.TDoubleArrayList;

public class RatioGlobalNormalizer {
	private final static Logger log = Logger.getLogger(RatioGlobalNormalizer.class);

	public static Double getGlobalLog2RatioMean(File inputFile, boolean ip2ConditionLight) {

		BufferedReader in = null;
		try {

			in = new BufferedReader(new FileReader(inputFile));
			String line = in.readLine();
			String[] split = line.split("\t");

			while (!split[0].equals("locus")) {

				line = in.readLine();

				split = line.split("\t");
			} // The reader is now at the first line of data

			final Map<String, Integer> indexesByHeader = PreFilterUtils.getIndexesByHeaders(split);
			final Map<Integer, Integer> indexByReplicate = PreFilterUtils.getRatioIndexesByReplicate(split,
					PreFilterUtils.area_ratio_x_regexp_new_format);
			line = in.readLine();

			final TDoubleArrayList ratios = new TDoubleArrayList();
			int counter = 0;
			while (line != null) {
				split = line.split("\t");
				final String uniProt_Acc = split[indexesByHeader.get(PreFilterUtils.LOCUS)];
				if (uniProt_Acc.equals("P54646")) {
					log.info(uniProt_Acc + " in file " + FilenameUtils.getName(inputFile.getAbsolutePath()));
				}
				final String proteinDescription = split[indexesByHeader.get(PreFilterUtils.DESCRIPTION_LOWER_CASE)];

				final String oldRatioString = PreFilterUtils.getOldRatioString(indexByReplicate, split);
				if (proteinDescription.contains("PRKAA2")) {
//					log.info(uniProt_Acc + "\t" + proteinDescription + "\t" + oldRatioString);
				}
				final String[] splitRatios2 = oldRatioString.split(";");

				for (int i = 0; i < splitRatios2.length; i++) {
					final String[] subSplitRatios2 = splitRatios2[i].split(",");
					for (int j = 0; j < subSplitRatios2.length; j++) {
						if (!subSplitRatios2[j].equals("X") && !(subSplitRatios2[j].length() < 1)) {
							if (ip2ConditionLight) {
								final double ratio = Maths.log(Double.parseDouble(subSplitRatios2[j]), 2);
								ratios.add(ratio);
							} else {
								final double ratio = Maths.log(1 / Double.parseDouble(subSplitRatios2[j]), 2);
								ratios.add(ratio);
							}
							counter++;
						}
					}
				}
				line = in.readLine();

			}

			final double mean = Maths.mean(ratios);
			log.info(counter + " ratios in file " + inputFile.getAbsolutePath() + " offset = " + mean);
			final double offset = mean;
			return offset;
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
