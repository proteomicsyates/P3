package edu.scripps.p3.utilities.stats;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * diego
 * Jan 27, 2014
 */

/**
 * calculate the correlation of the spec count of the prey with the bait
 * 
 * @author diego
 * 
 */
public class FilterTrends {

	Hashtable<String, Integer> lut;
	List<String> baits;
	double[] bvalues;
	Hashtable<String, double[]> preys;

	public void run() {
		// select bait file
		File bfile = openFile("Select bait file");
		// select input file for bait value
		File ifile = openFile("Select input file");
		// select output file for prey trends
		File ofile = openFile("Select score file");
		// select where to save
		// File outdir = outDir("Select where to save");

		// calculate for each prey the correlation with the bait
		try {
			extractBaits(bfile);
			getBaitsValue(ifile);
			extractPreys(ofile);
			calculateCorrelation();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param ifile
	 * @throws IOException
	 */
	private void getBaitsValue(File ifile) throws IOException {

		bvalues = new double[baits.size()];

		FileInputStream fis;

		fis = new FileInputStream(ifile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

		String dataline;

		while ((dataline = dis.readLine()) != null) {

			if (!dataline.startsWith("Locus")) {
				String[] elements = dataline.split("\t");

				String name = elements[7].split(" ")[0];

				if (baits.contains(name)) {

					int index = lut.get(name);
					double value = Double.parseDouble(elements[2]);

					bvalues[index] = value;

				}

			}

		}

	}

	/**
	 * 
	 */
	private void calculateCorrelation() {

		double correlation;
		int overlap;
		String key;
		PearsonsCorrelation pc = new PearsonsCorrelation();

		Enumeration<String> enumkeys = preys.keys();

		System.out.println("Prey\tOverlap\tCorrelation");

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			overlap = 0;

			double[] scores = preys.get(key);

			for (double s : scores) {
				if (s != 0.0) {
					overlap++;
				}
			}

			correlation = pc.correlation(bvalues, scores);

			System.out.println(key + "\t" + overlap + "\t" + correlation);

		}

	}

	/**
	 * @param ofile
	 */
	private void extractPreys(File ofile) throws IOException {

		preys = new Hashtable<String, double[]>();

		FileInputStream fis;

		fis = new FileInputStream(ofile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

		String dataline;

		int index = -1;
		String bait;
		boolean goon = false;
		String prey;
		double score;

		while ((dataline = dis.readLine()) != null) {

			if (dataline.startsWith("Working on bait:")) {
				bait = dataline.split("\t")[1];
				index = lut.get(bait);
				continue;
			}

			if (dataline.startsWith("Working on")) {
				if (dataline.split("\t")[1].equals("H")) {
					goon = true;
				} else {
					goon = false;
					return;
				}
				continue;
			}

			if (dataline.startsWith("Protein")) {
				continue;
			}

			if (goon) {

				String[] elements = dataline.split("\t");

				if (elements.length > 10) {
					if (elements[10].equals("discarded")) {
						continue;
					}
				}

				prey = elements[0];
				score = (Double.parseDouble(elements[2]) + Double.parseDouble(elements[3])) / 2.0;
				/*
				 * Double.parseDouble(elements[1]);/*(Double.parseDouble(
				 * elements[2]) + Double.parseDouble(elements[3])) / 2.0;
				 */

				if (!baits.contains(prey)) {
					if (preys.containsKey(prey)) {
						preys.get(prey)[index] = score;
					} else {
						double[] scores = new double[baits.size()];
						scores[index] = score;
						preys.put(prey, scores);
					}
				}

			}

		}

	}

	/**
	 * @param bfile
	 * @throws IOException
	 */
	private void extractBaits(File bfile) throws IOException {

		lut = new Hashtable<String, Integer>();
		baits = new ArrayList<String>();

		FileInputStream fis;

		fis = new FileInputStream(bfile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

		String dataline;

		while ((dataline = dis.readLine()) != null) {
			String[] elements = dataline.split("-");

			for (int i = 0; i < elements.length; i++) {

				lut.put(elements[i], i);
				baits.add(elements[i]);

			}

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FilterTrends ft = new FilterTrends();
		ft.run();

	}

	public File openFile(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(t);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		return f;
	}

	public File outDir(String title) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(title);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		} else {
			System.exit(-2);
		}
		return f;

	}

}
