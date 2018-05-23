package edu.scripps.p3.utilities.comparators;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;

/**
 * diego
 * May 27, 2014
 */

/**
 * 
 * compare two runs of P3 Require confidencescore.txt files as input
 * 
 * @author diego
 * 
 */
public class CompareInteractors {

	File currentDir;
	Hashtable<String, List<Interactor>> ntable;
	Hashtable<String, List<Interactor>> otable;

	public void run() {

		File ointer = openFile("Select old interactor");
		File ninter = openFile("Select new interactor");

		File fnew = openDir("Select New input dir");
		File fold = openDir("Select Old input dir");
		List<String> nlist = parseInput(fnew);
		List<String> olist = parseInput(fold);

		ntable = new Hashtable<String, List<Interactor>>();
		otable = new Hashtable<String, List<Interactor>>();

		try {
			parse(ninter, true);
			parse(ointer, false);

			compare(nlist, olist);

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void compare(List<String> newlist, List<String> oldlist) {

		Enumeration<String> baits = ntable.keys();
		String bait;

		StringBuffer shared = new StringBuffer();
		shared.append("Name\tNew Confidecne\tOld Confidence\n");
		boolean found;

		int count_shared = 0;
		int old_disc = 0;
		int new_disc = 0;
		int old_filt = 0;
		int old_add = 0;
		int new_filt = 0;
		int new_add = 0;

		StringBuffer old_discarded = new StringBuffer();
		StringBuffer new_discarded = new StringBuffer();

		StringBuffer old_filtered = new StringBuffer();
		StringBuffer new_filtered = new StringBuffer();

		StringBuffer old_addition = new StringBuffer();
		StringBuffer new_addition = new StringBuffer();

		while (baits.hasMoreElements()) {

			bait = baits.nextElement();

			List<Interactor> nlist = ntable.get(bait);
			List<Interactor> olist = otable.get(bait);

			Interactor inter1;
			Interactor inter2;

			for (int i = 0; i < nlist.size(); i++) {

				inter1 = nlist.get(i);
				found = false;

				if (!inter1.isDiscarded()) {

					for (int j = 0; j < olist.size(); j++) {

						inter2 = olist.get(j);

						if (inter1.getName().equals(inter2.getName())) {

							if (!inter2.isDiscarded()) {

								shared.append(inter1.getName() + "\t" + inter1.getConfidence() + "\t"
										+ inter2.getConfidence() + "\n");
								found = true;
								count_shared++;
								break;

							} else {

								old_discarded.append(
										inter1.getName() + "\t" + inter1.getConfidence() + "\t" + inter2.getConfidence()
												+ "\t" + inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
								found = true;
								old_disc++;
								break;

							}

						}

					}

					if (!found) {

						if (oldlist.contains(inter1.getShortName())) {

							old_filtered.append(inter1.getName() + "\t" + inter1.getConfidence() + "\t"
									+ inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
							old_filt++;

						} else {

							new_addition.append(inter1.getName() + "\t" + inter1.getConfidence() + "\t"
									+ inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
							new_add++;

						}

					}

				}

			}

			for (int i = 0; i < olist.size(); i++) {

				inter1 = olist.get(i);
				found = false;

				if (!inter1.isDiscarded()) {

					for (int j = 0; j < nlist.size(); j++) {

						inter2 = nlist.get(j);

						if (inter1.getName().equals(inter2.getName())) {

							if (!inter2.isDiscarded()) {

								found = true;
								break;

							} else {

								new_discarded.append(
										inter1.getName() + "\t" + inter1.getConfidence() + "\t" + inter2.getConfidence()
												+ "\t" + inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
								found = true;
								new_disc++;
								break;

							}

						}

					}

					if (!found) {

						if (newlist.contains(inter1.getShortName())) {

							new_filtered.append(inter1.getName() + "\t" + inter1.getConfidence() + "\t"
									+ inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
							new_filt++;

						} else {

							old_addition.append(inter1.getName() + "\t" + inter1.getConfidence() + "\t"
									+ inter1.isPhysical() + "\t" + inter1.isGenetic() + "\n");
							old_add++;

						}

					}

				}

			}

		}

		System.out.println("Shared:\t" + count_shared);
		System.out.println("Interactor in new but discarded in old:\t" + old_disc);
		System.out.println("Interactor in new only, in the old raw files:\t" + old_filt);
		System.out.println("Interactor in new only, not in the old raw files:\t" + new_add);

		System.out.println("Interactor in old but discarded in new:\t" + new_disc);
		System.out.println("Interactor in old only, in the new raw files:\t" + new_filt);
		System.out.println("Interactor in old only, not in the new raw files:\t" + old_add);

		System.out.println("\nShared\n");
		System.out.println(shared.toString());

		System.out.println("\nInteractor in new but discarded in old\n");
		System.out.println(old_discarded.toString());

		System.out.println("\nInteractor in new only, in the old raw files\n");
		System.out.println(old_filtered.toString());

		System.out.println("\nInteractor in new only, not in the old raw files\n");
		System.out.println(new_addition.toString());

		System.out.println("\nInteractor in old but discarded in new\n");
		System.out.println(new_discarded.toString());

		System.out.println("\nInteractor in old only, in the new raw files\n");
		System.out.println(new_filtered.toString());

		System.out.println("\nInteractor in old only, not in the new raw files\n");
		System.out.println(old_addition.toString());

	}

	private void parse(File inp, boolean selector) throws IOException {

		FileInputStream fis = new FileInputStream(inp);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

		String dataline;
		String bait = null;
		boolean toread = false;
		String exp = null;
		String name = null;
		String confidence;
		boolean discarded;
		int physical;
		int genetic;

		while ((dataline = dis.readLine()) != null) {

			String[] elements = dataline.split("\t");

			if (elements[0].equals("Working on")) {
				exp = elements[1];
				continue;
			}

			if (elements[0].equals("Working on bait:")) {
				bait = elements[1];
				toread = true;

				if (selector) {
					if (!ntable.containsKey(bait)) {
						List<Interactor> pnames = new ArrayList<Interactor>();
						ntable.put(bait, pnames);
					}
				} else {
					if (!otable.containsKey(bait)) {
						List<Interactor> pnames = new ArrayList<Interactor>();
						otable.put(bait, pnames);
					}
				}

				continue;

			}

			if (toread) {

				if (elements[0].equals("Protein")) {

					continue;

				} else {

					name = elements[0] + "_" + exp;
					confidence = elements[7];
					discarded = false;
					if (elements.length > 10) {
						if (elements[10].equals("discarded")) {
							discarded = true;
						}
					}
					physical = -1;
					if (elements.length > 11) {
						if (elements[11].contains("StringYeast_exp")) {
							physical = 0;
						}
						if (elements[11].contains("BioGridYeast_physical")) {
							physical += 2;
						}
					}
					genetic = -1;
					if (elements.length > 12) {
						if (elements[12].contains("BioGridYeast_genetic")) {
							genetic = 0;
						}
						if (elements[12].contains("GOYeast")) {
							genetic += 2;
						}
					}

					Interactor inter = new Interactor(name, confidence, discarded, physical, genetic);

					if (selector) {
						ntable.get(bait).add(inter);
					} else {
						otable.get(bait).add(inter);
					}

				}

			}

		}

	}

	private List<String> parseInput(File inpdir) {

		String[] list = inpdir.list();
		List<String> nlist = new ArrayList<String>();

		for (String name : list) {

			File inp = new File(inpdir, name);

			FileInputStream fis;

			try {
				fis = new FileInputStream(inp);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

				String dataline;
				String proteinname;

				while ((dataline = dis.readLine()) != null) {

					if (!dataline.startsWith("Locus")) {

						String[] elements = dataline.split("\t");

						proteinname = elements[7].split(" ")[0];

						if (!nlist.contains(proteinname)) {
							nlist.add(proteinname);
						}

					}

				}

			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}

		}

		return nlist;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CompareInteractors ci = new CompareInteractors();
		ci.run();

	}

	public File openFile(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(currentDir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			currentDir = f.getParentFile();
		}

		return f;
	}

	public File outDir(String title) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(title);
		fc.setCurrentDirectory(currentDir);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		} else {
			System.exit(-2);
		}
		return f;

	}

	private File openDir(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(currentDir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		return f;
	}

	private class Interactor {

		String name;
		String confidence;
		boolean discarded;
		String shortname;
		int physical;
		int genetic;

		public Interactor(String name, String confidence, boolean discarded, int physical, int genetic) {
			this.name = name;
			this.confidence = confidence;
			this.discarded = discarded;
			this.shortname = name.split("_")[0];
			this.physical = physical;
			this.genetic = genetic;
		}

		public String getName() {
			return this.name;
		}

		public String getShortName() {
			return this.shortname;
		}

		public String getConfidence() {
			return this.confidence;
		}

		public boolean isDiscarded() {
			return this.discarded;
		}

		public String isPhysical() {

			switch (this.physical) {
			case 0:
				return "String";
			case 1:
				return "Biogrid";
			case 2:
				return "Physical";
			default:
				return "";
			}

		}

		public String isGenetic() {

			switch (this.genetic) {
			case 0:
				return "Biogrid";
			case 1:
				return "GO";
			case 2:
				return "Genetic";
			default:
				return "";
			}

		}

	}
}
