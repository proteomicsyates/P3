/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.parsers.quantitative;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.prefilter.PreFilterUtils;
import edu.scripps.yates.utilities.fasta.FastaParser;

/**
 * @author diego
 *
 */
public class Lysates extends Quantitatives {

	/**
	 * @param quantitativefilelist
	 * @param quantitativedir
	 * @param baits
	 * @param dlist
	 */
	public Lysates(String[] quantitativefilelist, File quantitativedir, String[] baits, List<Differential> dlist) {
		super(quantitativefilelist, quantitativedir, baits, dlist);

	}

	@Override
	protected void setTitle() {
		title = "Lysate Resolver";
	}

	@Override
	protected void parseFiles() {
		log.info("Parsing quant lysate files...");

		File f;
		int baitindex;
		final Median median = new Median();

		for (int i = 0; i < files.length; i++) {

			f = new File(inputdir, files[i]);
			log.info("Parsing lysate quant file '" + f.getAbsolutePath() + "'...");

			baitindex = assignments.get(i);

			FileInputStream fis;

			try {
				fis = new FileInputStream(f);
				final BufferedInputStream bis = new BufferedInputStream(fis);
				final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
				Map<String, Integer> indexesByHeaders = null;
				Map<Integer, Integer> ratioIndexesByReplicate = null;
				String dataline;
				final boolean dataStarts = false;
				while ((dataline = dis.readLine()) != null) {
					if (dataline.startsWith("PLINE")) {
						// header
						indexesByHeaders = PreFilterUtils.getIndexesByHeaders(dataline);
						ratioIndexesByReplicate = PreFilterUtils.getRatioIndexesByReplicate(dataline,
								PreFilterUtils.area_ratio_x_regexp);
					}
					if (dataline.startsWith("P\t")) {

						final String[] elements = dataline.split("\t");
						for (final Integer index : ratioIndexesByReplicate.values()) {
							final String ratioForReplicateString = elements[index];
							final List<Double> peptideRatios = new ArrayList<Double>();
							String[] split = null;
							if (ratioForReplicateString.contains(",")) {
								split = ratioForReplicateString.split(",");
							} else {
								split = new String[1];
								split[0] = ratioForReplicateString;
							}
							for (final String individualRatioString : split) {
								try {
									final Double ratioValue = Double.valueOf(individualRatioString);
									peptideRatios.add(ratioValue);
								} catch (final NumberFormatException e) {

								}
							}
							final double[] ratioArray = new double[peptideRatios.size()];
							int index2 = 0;
							for (final double d : peptideRatios) {
								ratioArray[index2++] = d;
							}
							// make the median
							final double value = median.evaluate(ratioArray);
							final String description = elements[indexesByHeaders.get(PreFilterUtils.DESCRIPTION)];

							String pname = FastaParser.getGeneFromFastaHeader(description);
							if (pname == null) {
								// get the gene name from the first word of the
								// description
								if (description.contains(" ")) {
									pname = description.split(" ")[0];
								}

							}
							if (pname == null) {
								pname = elements[indexesByHeaders.get(PreFilterUtils.ACC)];
							}
							dlist.get(baitindex).addValue(pname, value);

						}

					}

				}

			} catch (final FileNotFoundException e) {
				System.err.println("file not found");
			} catch (final IOException e) {
				System.err.println("unable to read file");
			}
			log.info(dlist.get(baitindex).getData().size() + " proteins read");
		}

	}

}
