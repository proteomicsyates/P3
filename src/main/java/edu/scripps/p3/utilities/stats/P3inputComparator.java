package edu.scripps.p3.utilities.stats;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFileChooser;

/**
 * calculate statistics on the input files
 * For each protein provides the average value, the APV value and the Density value
 * APV and Density are P3 features
 * diego
 * Dec 4, 2013
 */

/**
 * @author diego
 * 
 */
public class P3inputComparator {

	Hashtable<String, Protein> ptable;

	public void run() {

		File inpdir = openDir("Select input dir");

		parseFiles(inpdir);

		String log = calculateStats();

		writeOut(inpdir, log);

	}

	private void writeOut(File out, String log) {

		File fout = new File(out, "InputStats.txt");
		try {
			Writer wout = new BufferedWriter(new FileWriter(fout));
			wout.write(log);
			wout.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	private String calculateStats() {

		StringBuffer log = new StringBuffer();

		Enumeration<String> enumkeys = ptable.keys();

		String key;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();

			ptable.get(key).computeAvg();
			ptable.get(key).computeAPV();
			ptable.get(key).computeDensity();

			log.append(ptable.get(key).getProtein());

		}

		return log.toString();
	}

	/**
	 * @param inpdir
	 */
	private void parseFiles(File inpdir) {

		ptable = new Hashtable<String, Protein>();

		String[] list = inpdir.list();

		java.util.Arrays.sort(list);

		String name;
		int sp;
		double cov;
		double l;
		double mw;

		for (int i = 0; i < list.length; i++) {

			File inp = new File(inpdir, list[i]);

			FileInputStream fis;

			try {
				fis = new FileInputStream(inp);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

				String dataline;

				while ((dataline = dis.readLine()) != null) {

					if (!dataline.startsWith("Locus")) {

						String[] elements = dataline.split("\t");

						sp = Integer.parseInt(elements[2]);
						cov = Double.parseDouble(elements[3]);
						l = Double.parseDouble(elements[4]);
						mw = Double.parseDouble(elements[5]);

						name = elements[7].split(" ")[0];

						if (ptable.containsKey(name)) {

							ptable.get(name).addScount(sp, i);
							ptable.get(name).addCoverage(cov, i);

						} else {

							Protein p = new Protein(name, list.length);
							p.addScount(sp, i);
							p.addCoverage(cov, i);
							p.setLenghtMW(l, mw);

							ptable.put(name, p);

						}

					}

				}

			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}

		}

	}

	/**
	 * @param string
	 * @return
	 */
	private File openDir(String t) {
		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(t);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		return f;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		P3inputComparator p3c = new P3inputComparator();
		p3c.run();

	}

	private class Protein {

		String name;
		int[] scount;
		double[] coverage;
		double length;
		double mw;
		double sp;
		double cov;
		double apv;
		double density;

		public Protein(String name, int size) {
			this.name = name;
			this.scount = new int[size];
			this.coverage = new double[size];
		}

		public void addScount(int value, int id) {
			scount[id] = value;
		}

		public void addCoverage(double c, int id) {
			coverage[id] = c;
		}

		public void setLenghtMW(double l, double mw) {
			this.length = l;
			this.mw = mw;
		}

		public void computeAvg() {

			sp = 0;
			cov = 0;

			for (int i = 0; i < scount.length; i++) {

				sp += scount[i];
				cov += coverage[i];

			}

			sp /= scount.length;
			cov /= scount.length;

		}

		public void computeAPV() {

			apv = sp / cov * 100;

		}

		public void computeDensity() {

			density = apv * length / mw;

		}

		public String getProtein() {

			StringBuffer prot = new StringBuffer();

			prot.append(name + "\t");

			for (int i = 0; i < scount.length; i++) {
				prot.append(scount[i] + "\t");
			}
			for (int i = 0; i < coverage.length; i++) {
				prot.append(coverage[i] + "\t");
			}
			prot.append(length + "\t");
			prot.append(mw + "\t");
			prot.append(apv + "\t");
			prot.append(density + "\n");

			return prot.toString();

		}

		@Override
		public String toString() {

			return getProtein();

		}

	}

}
