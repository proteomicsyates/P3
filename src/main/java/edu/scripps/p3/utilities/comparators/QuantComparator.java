package edu.scripps.p3.utilities.comparators;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFileChooser;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * diego
 * Jun 2, 2014
 */

/**
 * compare the interactor files create in compareInteractors.java and two
 * different versions of quant files
 * 
 * @author diego
 * 
 */
public class QuantComparator {

	File curdir;

	private void setCurrentDir(String dir) {
		this.curdir = new File(dir);
	}

	public void run() {

		// load old quant
		File olddir = openDir("Select Old quant files");
		File newdir = openDir("Select New quant files");

		File intfile = openFile("Select Interactor Comparison Analysis");

		// load new quant

		Hashtable<String, SummaryStatistics> oldtable;
		Hashtable<String, SummaryStatistics> newtable;

		oldtable = parseNew(olddir);
		newtable = parseNew(newdir);

		StringBuffer shared_concordant = new StringBuffer();
		StringBuffer shared_discordant = new StringBuffer();

		StringBuffer only_old = new StringBuffer();
		StringBuffer only_new = new StringBuffer();

		Hashtable<String, String[]> interactors = parseInteractors(intfile);

		Enumeration<String> enumkeys = newtable.keys();

		String key;
		double newvalue;
		double oldvalue;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();

			newvalue = newtable.get(key).getMean();

			if (oldtable.containsKey(key)) {

				oldvalue = oldtable.get(key).getMean();

				if ((newvalue > 1 && oldvalue > 1) || (newvalue < 1 && oldvalue < 1) || (newvalue == oldvalue)) {

					shared_concordant.append(key + "\t" + newvalue + "\t" + oldvalue + "\t");

					if (interactors.containsKey(key)) {
						shared_concordant.append(interactors.get(key)[0] + "\t" + interactors.get(key)[1] + "\n");
					} else {
						shared_concordant.append("\n");
					}

				} else {

					shared_discordant.append(key + "\t" + newvalue + "\t" + oldvalue + "\t");

					if (interactors.containsKey(key)) {
						shared_discordant.append(interactors.get(key)[0] + "\t" + interactors.get(key)[1] + "\n");
					} else {
						shared_discordant.append("\n");
					}

				}

			} else {

				only_new.append(key + "\t" + newvalue + "\t");

				if (interactors.containsKey(key)) {
					only_new.append(interactors.get(key)[0] + "\n");
				} else {
					only_new.append("\n");
				}

			}

		}

		enumkeys = oldtable.keys();

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			oldvalue = oldtable.get(key).getMean();

			if (!newtable.containsKey(key)) {

				only_old.append(key + "\t" + oldvalue + "\t");

				if (interactors.containsKey(key)) {
					only_old.append(interactors.get(key)[1] + "\n");
				} else {
					only_old.append("\n");
				}

			}

		}

		System.out.println(
				"Shared concordant\nProtein\tNew Value\tOld Value\tIn New Interactions\tIn Old Interactions\n");
		System.out.println(shared_concordant.toString() + "\n");

		System.out.println(
				"Shared discordant\nProtein\tNew Value\tOld Value\tIn New Interactions\tIn Old Interactions\n");
		System.out.println(shared_discordant.toString() + "\n");

		System.out.println("Only New\nProtein\tNew Value\tIn New Interactions\n");
		System.out.println(only_new.toString() + "\n");

		System.out.println("Only Old\nProtein\tOld Value\tIn Old Interactions\n");
		System.out.println(only_old.toString() + "\n");

	}

	/**
	 * @param intfile
	 * @return
	 */
	private Hashtable<String, String[]> parseInteractors(File intfile) {

		Hashtable<String, String[]> interactions = new Hashtable<String, String[]>();

		FileInputStream fis;

		try {
			fis = new FileInputStream(intfile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			boolean newexp = false;
			boolean oldexp = false;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.equals("Shared")) {
					newexp = true;
					oldexp = true;
					continue;
				}

				if (dataline.equals("Interactor in new but discarded in old")) {
					newexp = true;
					oldexp = false;
					continue;
				}

				if (dataline.equals("Interactor in new only, in the old raw files")) {
					newexp = true;
					oldexp = false;
					continue;
				}

				if (dataline.equals("Interactor in new only, not in the old raw files")) {
					newexp = true;
					oldexp = false;
					continue;
				}

				if (dataline.equals("Interactor in old but discarded in new")) {
					newexp = false;
					oldexp = true;
					continue;
				}

				if (dataline.equals("Interactor in old only, in the new raw files")) {
					newexp = false;
					oldexp = true;
					continue;
				}

				if (dataline.equals("Interactor in old only, not in the new raw files")) {
					newexp = false;
					oldexp = true;
					continue;
				}

				if (newexp || oldexp) {

					if (dataline.length() > 1) {

						if (!dataline.startsWith("Name")) {

							String name = dataline.split("\t")[0];
							name = name.split("_")[0];

							if (interactions.containsKey(name)) {

								String[] values = interactions.get(name);
								if (newexp) {
									values[0] = "*";
								}
								if (oldexp) {
									values[1] = "*";
								}

								interactions.put(name, values);

							} else {

								String[] values = new String[2];
								if (newexp) {
									values[0] = "*";
								} else {
									values[0] = "";
								}
								if (oldexp) {
									values[1] = "*";
								} else {
									values[1] = "";
								}

								interactions.put(name, values);

							}

						}

					}

				}

			}

		} catch (IOException e) {
			System.err.println("unable to read file");
		}

		return interactions;
	}

	/**
	 * @param newdir
	 * @return
	 */
	private Hashtable<String, SummaryStatistics> parseNew(File newdir) {
		Hashtable<String, SummaryStatistics> newtable = new Hashtable<String, SummaryStatistics>();

		String[] list = newdir.list();
		File inp;

		for (String element : list) {

			inp = new File(newdir, element);

			parseFile(inp, newtable);

		}

		return newtable;
	}

	/**
	 * @param olddir
	 * @return
	 */
	/*
	 * private Hashtable<String, Hashtable<String, SummaryStatistics>>
	 * parseOld(File olddir) { Hashtable<String, Hashtable<String,
	 * SummaryStatistics>> oldtable = new Hashtable<String, Hashtable<String,
	 * SummaryStatistics>>();
	 * 
	 * String [] list = olddir.list();
	 * 
	 * 
	 * return oldtable; }
	 */

	protected void parseFile(File f, Hashtable<String, SummaryStatistics> newtable) {

		FileInputStream fis;

		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("P")) {
					String[] element = dataline.split("\t");

					String pname;
					double value;

					if (element[1].startsWith("sp")) {

						String[] tmp2 = element[14].split("=");
						tmp2[2] = tmp2[2].replace(" PE", "");

						pname = tmp2[2].trim();
					} else {

						pname = element[14].split(" ")[0];
					}

					if (!element[6].equals("NA")) {
						value = Double.parseDouble(element[6]);
						if (value < 0.1)
							value = 0;
					} else {
						value = 0;
					}

					if (newtable.containsKey(pname)) {

						newtable.get(pname).addValue(value);

					} else {

						SummaryStatistics ss = new SummaryStatistics();
						ss.addValue(value);
						newtable.put(pname, ss);

					}

				}

			}

		} catch (FileNotFoundException e) {
			System.err.println("file not found");
		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		QuantComparator qc = new QuantComparator();

		if (args.length > 0) {
			qc.setCurrentDir(args[0]);
		}

		qc.run();

	}

	private File openDir(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		return f;
	}

	public File openFile(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			curdir = f.getParentFile();
		}

		return f;
	}

}
