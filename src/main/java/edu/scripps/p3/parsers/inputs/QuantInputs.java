package edu.scripps.p3.parsers.inputs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.parsers.inputs.utilities.ApvCalculator;
import edu.scripps.p3.parsers.inputs.utilities.NewCoverageFixer;
import edu.scripps.p3.parsers.inputs.utilities.Protein;
import edu.scripps.p3.prefilter.PreFilterUtils;

public class QuantInputs extends Inputs {
	private final static Logger log = Logger.getLogger(QuantInputs.class);
	private final Set<String> validProteins = new HashSet<String>();

	/**
	 * This is an extension of {@link Inputs} where we have a set of proteins
	 * that are valid, so any protein that is not in that set is going to be
	 * discarded
	 *
	 * @param files
	 * @param inputdir
	 * @param baits
	 * @param exp
	 * @param elist
	 * @param validProteins
	 */
	public QuantInputs(String[] files, File inputdir, String[] baits, String[] exp, List<Experiment> elist,
			Set<String> validProteins) {
		super(files, inputdir, baits, exp, elist);
		if (validProteins != null) {
			this.validProteins.addAll(validProteins);
		}
	}

	@Override
	protected void parseFiles() {

		File f;
		int baitindex;
		int expindex;

		for (int i = 0; i < files.length; i++) {

			f = new File(inputdir, files[i]);
			log.info("Reading quant input file " + f.getAbsolutePath());

			baitindex = assignments.get(i)[0];
			expindex = assignments.get(i)[1];

			FileInputStream fis;
			int numValidProteins = 0;
			int numTotalProteins = 0;
			try {
				fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
				Map<String, Integer> indexesByHeaders = null;
				String dataline;
				boolean dataStarts = false;
				while ((dataline = dis.readLine()) != null) {
					if (dataline.startsWith("Unique")) {
						dataStarts = true;
						continue;
					}
					if (dataline.startsWith("Locus")) {
						indexesByHeaders = PreFilterUtils.getIndexesByHeaders(dataline);
					}
					if (dataStarts && dataline.contains("\t") && "Proteins".equals(dataline.split("\t")[1])) {
						break;
					}
					if (dataStarts && dataline.contains("\t") && !"".equals(dataline.split("\t")[0])
							&& !"*".equals(dataline.split("\t")[0])) {

						Protein p = getProtein(dataline, indexesByHeaders);
						numTotalProteins++;
						final String gene = p.getName();
						if (gene.length() > 1) {
							if (!validProteins.isEmpty() && !validProteins.contains(gene)) {
								continue;
							}
							numValidProteins++;
							if (elist.get(baitindex).getCondition(expindex).proteinInTable(gene)) {
								// merge proteins
								Protein old = elist.get(baitindex).getCondition(expindex).getProtein(gene);

								Protein merged = mergeP(old, p);
								elist.get(baitindex).getCondition(expindex).addProtein(old.getName(), merged, false);

							} else {
								// add protein
								elist.get(baitindex).getCondition(expindex).addProtein(gene, p, true);
							}
						}

					}

				}
				log.info(elist.get(baitindex).getCondition(expindex).getNumberOfProteins()
						+ " proteins readed for bait " + baitindex + " condition " + expindex);
			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}
			log.info(numValidProteins + " valid proteins out of " + numTotalProteins + " in file " + files[i]);
		}

		if (elist.get(0).getCondition(0).proteinInTable("ULK1")) {
			System.out.println("Ulk1 in table");
		}

		NewCoverageFixer cfix = new NewCoverageFixer(elist, rootdir);
		cfix.run();

		ApvCalculator acalc = new ApvCalculator(elist);
		acalc.run();

	}
}
