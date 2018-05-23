/**
 * diego Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.io.MyFileChooser;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author diego
 *
 */
public class CoverageFixer {

	List<Experiment> elist;
	File cdir;
	TObjectDoubleHashMap<String> coverages;

	/**
	 * @param elist
	 * @param inputdir
	 */
	public CoverageFixer(List<Experiment> elist, File inputdir) {
		this.elist = elist;
		cdir = inputdir;
	}

	/**
	 *
	 */
	public void run() {

		coverages = new TObjectDoubleHashMap<String>();
		setCoverageTable();

		double proteinCoverage;

		final Set<String> proteinNames = coverages.keySet();
		for (final String proteinName : proteinNames) {

			proteinCoverage = coverages.get(proteinName);

			for (int i = 0; i < elist.size(); i++) {
				final Experiment experiment = elist.get(i);
				for (int j = 0; j < experiment.getNumberofConditions(); j++) {
					final Condition condition = experiment.getCondition(j);
					if (condition.proteinInTable(proteinName)) {
						final Protein protein = condition.getProtein(proteinName);
						protein.setMaxTheoCoverage(proteinCoverage);
						protein.normalizeCoverage();
					}
				}
			}

		}

	}

	public void setCoverageTable() {

		final MyFileChooser dIO = new MyFileChooser(cdir);
		final File pepcutter = dIO.openFile("Select PeptideCutter file");
		FileInputStream fis;
		try {
			fis = new FileInputStream(pepcutter);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			double val;

			while ((dataline = dis.readLine()) != null) {

				final String[] element = dataline.split("\t");
				val = Double.parseDouble(element[1]);
				coverages.put(element[0], val);

			}

		} catch (final FileNotFoundException e) {
			System.err.println("file not found");
		} catch (final IOException e) {
			System.err.println("unable to read bait file");
		}

	}

}
