package edu.scripps.p3.utilities.stats;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * diego
 * Jan 14, 2014
 */

/**
 * process the confidencescores.txt and the quantvalues.txt to create an excel
 * report with a table of bait/prey and for each the confidence score, the quant
 * ratio are reported. The cells are colored based if they are novel
 * interactions (orange) or previous studies reported them (blue) The first tabs
 * are the experimental conditions. At the bottom of the last experimental
 * condition is reported the quant range. The other tabs create list of unique
 * novel not trending as the lysate shared interactions. These are the protein
 * of interest to further study
 * 
 * @author diego
 * 
 */
public class p3stats {

	Hashtable<String, Protein> table;
	Hashtable<String, List<String>> pools;
	Hashtable<String, List<String>> baitlists;
	TObjectDoubleHashMap<String> qlist;
	List<String> craps;
	private File curdir;

	public void run() {

		// load input file
		final File stat = openFile("Select p3 final stat file");

		parseStat(stat);

		final File inp = openFile("Select confidence file");

		parseInp(inp);

		final File qfile = openFile("Select quant file");

		parseQuant(qfile);

		final File crapfile = openFile("Select crapome file");

		parseCrapome(crapfile);

		final File outdir = outDir("Select where to save");

		final File out = new File(outdir, "InteractionMatrix.xls");

		final WritableWorkbook obook = createWorkbook(out);

		try {
			createMatrix(obook);
		} catch (final WriteException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @param obook
	 * @throws WriteException
	 */
	private void createMatrix(WritableWorkbook obook) throws WriteException {

		final WritableSheet h = obook.createSheet("H", 0);
		final WritableSheet l = obook.createSheet("L", 1);

		final List<String> heavy = pools.get("H");
		final List<String> light = pools.get("L");

		writeLabel(obook, h, 0, 0, "Proteins", false, false);
		writeLabel(obook, l, 0, 0, "Proteins", false, false);

		Collections.sort(heavy);
		Collections.sort(light);

		final List<String> bheavy = baitlists.get("H");
		final List<String> blight = baitlists.get("L");

		Collections.sort(bheavy);
		Collections.sort(blight);

		for (int k = 0; k < heavy.size(); k++) {
			writeLabel(obook, h, 0, k + 1, heavy.get(k), false, false);
		}
		for (int k = 0; k < light.size(); k++) {
			writeLabel(obook, l, 0, k + 1, light.get(k), false, false);
		}

		final Hashtable<String, Integer> bhlut = new Hashtable<String, Integer>();
		final Hashtable<String, Integer> bllut = new Hashtable<String, Integer>();

		int index = 1;

		for (int k = 0; k < bheavy.size(); k++) {
			bhlut.put(bheavy.get(k), index);// k+1);
			index = index + 2;
		}

		index = 1;

		for (int k = 0; k < blight.size(); k++) {
			bllut.put(blight.get(k), index);// k+1);
			index = index + 2;
		}

		int column_h = 1;
		int column_l = 1;

		double min_quant = Double.POSITIVE_INFINITY;
		double max_quant = Double.NEGATIVE_INFINITY;
		double ratio;

		final DecimalFormat df = new DecimalFormat("#.###");

		final Hashtable<String, List<String>> protunk_h = new Hashtable<String, List<String>>();
		final Hashtable<String, List<String>> protunk_l = new Hashtable<String, List<String>>();

		Enumeration<String> enumkeys = table.keys();
		while (enumkeys.hasMoreElements()) {

			final String key = enumkeys.nextElement();

			final Protein p = table.get(key);

			final String bait = p.getBait();

			if (bait.endsWith("H")) {

				column_h = bhlut.get(bait);

				writeLabel(obook, h, column_h, 0, bait.split("_")[0], false, false);

				for (int i = 0; i < heavy.size(); i++) {

					if (p.KnownContainProt(heavy.get(i))) {

						final double val = p.getKnownValue(heavy.get(i));

						final String label = df.format(val);

						writeLabel(obook, h, column_h, i + 1, label, true, false);

						if (qlist.containsKey(heavy.get(i).split("_")[0])) {
							ratio = qlist.get(heavy.get(i).split("_")[0]);
							writeLabel(obook, h, column_h + 1, i + 1, df.format(ratio), true, false);
						} else {
							writeLabel(obook, h, column_h + 1, i + 1, "NA", true, false);
						}

					} else {
						if (p.UnknownContainProt(heavy.get(i))) {

							if (!protunk_h.containsKey(heavy.get(i))) {
								final List<String> bts = new ArrayList<String>();
								protunk_h.put(heavy.get(i), bts);
							}

							final double val = p.getUnknownValue(heavy.get(i));

							final String label = df.format(val);

							writeLabel(obook, h, column_h, i + 1, label, false, true);

							if (qlist.containsKey(heavy.get(i).split("_")[0])) {
								ratio = qlist.get(heavy.get(i).split("_")[0]);
								writeLabel(obook, h, column_h + 1, i + 1, df.format(ratio), false, true);

								if (ratio > max_quant) {
									max_quant = ratio;
								}

								if (ratio < min_quant) {
									min_quant = ratio;
								}

							} else {
								writeLabel(obook, h, column_h + 1, i + 1, "NA", false, true);
							}

							protunk_h.get(heavy.get(i)).add(bait);

						} else {

							// writeLabel(obook, h, column_h, i+1, "0.0",
							// false);

						}
					}

				}

				// column_h++;
			} else {

				column_l = bllut.get(bait);

				writeLabel(obook, l, column_l, 0, bait.split("_")[0], false, false);

				for (int i = 0; i < light.size(); i++) {

					if (p.KnownContainProt(light.get(i))) {

						final double val = p.getKnownValue(light.get(i));

						final String label = df.format(val);

						writeLabel(obook, l, column_l, i + 1, label, true, false);

						if (qlist.containsKey(light.get(i).split("_")[0])) {
							ratio = qlist.get(light.get(i).split("_")[0]);
							writeLabel(obook, l, column_l + 1, i + 1, df.format(ratio), true, false);
						} else {
							writeLabel(obook, l, column_l + 1, i + 1, "NA", true, false);
						}

					} else {
						if (p.UnknownContainProt(light.get(i))) {

							if (!protunk_l.containsKey(light.get(i))) {
								final List<String> bts = new ArrayList<String>();
								protunk_l.put(light.get(i), bts);
							}

							final double val = p.getUnknownValue(light.get(i));

							final String label = df.format(val);

							writeLabel(obook, l, column_l, i + 1, label, false, true);

							if (qlist.containsKey(light.get(i).split("_")[0])) {
								ratio = qlist.get(light.get(i).split("_")[0]);
								writeLabel(obook, l, column_l + 1, i + 1, df.format(ratio), false, true);

								if (ratio > max_quant) {
									max_quant = ratio;
								}

								if (ratio < min_quant) {
									min_quant = ratio;
								}
							} else {
								writeLabel(obook, l, column_l + 1, i + 1, "NA", false, true);
							}

							protunk_l.get(light.get(i)).add(bait);

						} else {

							// writeLabel(obook, l, column_l, i+1, "0.0",
							// false);

						}
					}

				}

				writeLabel(obook, l, 0, light.size() + 3, "Unknown Range Ratios", false, false);
				writeLabel(obook, l, 0, light.size() + 4, "MAX", false, false);
				writeLabel(obook, l, 1, light.size() + 4, df.format(max_quant), false, false);
				writeLabel(obook, l, 0, light.size() + 5, "MIN", false, false);
				writeLabel(obook, l, 1, light.size() + 5, df.format(min_quant), false, false);

				// column_l++;
			}

		}

		// write number of baits for prey

		writeLabel(obook, h, (bheavy.size() * 2 + 3), 0, "Unknown Interactions", false, false);
		writeLabel(obook, h, (blight.size() * 2 + 3), 0, "Unknown Interactions", false, false);

		int rowindex = 1;

		for (int i = 0; i < heavy.size(); i++) {

			int size;

			if (protunk_h.containsKey(heavy.get(i))) {
				size = protunk_h.get(heavy.get(i)).size();
			} else {
				size = 0;
			}

			if (size == 1) {
				writeLabel(obook, h, (bheavy.size() * 2 + 3), rowindex, df.format(size), true, true);
			} else {
				writeLabel(obook, h, (bheavy.size() * 2 + 3), rowindex, df.format(size), false, false);
			}

			rowindex++;
		}

		rowindex = 1;

		for (int i = 0; i < light.size(); i++) {

			int size;

			if (protunk_l.containsKey(light.get(i))) {
				size = protunk_l.get(light.get(i)).size();
			} else {
				size = 0;
			}

			if (size == 1) {
				writeLabel(obook, l, (blight.size() * 2 + 3), rowindex, df.format(size), true, true);
			} else {
				writeLabel(obook, l, (blight.size() * 2 + 3), rowindex, df.format(size), false, false);
			}

			rowindex++;
		}

		// writeExcel(obook);

		List<String> keysToRemove = new ArrayList<String>();

		enumkeys = protunk_h.keys();
		String key;
		while (enumkeys.hasMoreElements()) {
			key = enumkeys.nextElement();

			if (protunk_h.get(key).size() > 1) {
				keysToRemove.add(key);
				continue;
			}

			final List<String> baits = protunk_h.get(key);

			for (final String b : baits) {
				if (table.get(b).IsLysateTrend(key)) {
					keysToRemove.add(key);
				}
			}

		}

		for (final String k : keysToRemove) {
			protunk_h.remove(k);
		}

		keysToRemove = new ArrayList<String>();
		enumkeys = protunk_l.keys();

		while (enumkeys.hasMoreElements()) {
			key = enumkeys.nextElement();

			if (protunk_l.get(key).size() > 1) {
				keysToRemove.add(key);
				continue;
			}

			final List<String> baits = protunk_l.get(key);

			for (final String b : baits) {
				if (table.get(b).IsLysateTrend(key)) {
					keysToRemove.add(key);
				}
			}

		}

		for (final String k : keysToRemove) {
			protunk_l.remove(k);
		}

		final WritableSheet uh = obook.createSheet("Unknown Unique Not Lysate H", 2);
		final WritableSheet ul = obook.createSheet("Unknown Unique Not Lysate L", 3);

		writeLabel(obook, uh, 0, 0, "Protein", false, false);
		writeLabel(obook, ul, 0, 0, "Protein", false, false);

		writeLabel(obook, uh, 1, 0, "Bait", false, false);
		writeLabel(obook, ul, 1, 0, "Bait", false, false);

		writeLabel(obook, uh, 2, 0, "Score", false, false);
		writeLabel(obook, ul, 2, 0, "Score", false, false);

		final Hashtable<String, Integer> shared = new Hashtable<String, Integer>();

		enumkeys = protunk_h.keys();
		double val;
		rowindex = 1;

		String shared_key;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			final List<String> baits = protunk_h.get(key);

			for (final String b : baits) {

				val = table.get(b).getUnknownValue(key);

				writeLabel(obook, uh, 0, rowindex, key, false, false);
				writeLabel(obook, uh, 1, rowindex, b, false, false);
				writeLabel(obook, uh, 2, rowindex, df.format(val), false, false);

				shared_key = key + "@" + b;
				shared_key = shared_key.split("_")[0];

				if (shared.containsKey(shared_key)) {

					int value = shared.get(shared_key);
					value++;
					shared.put(shared_key, value);

				} else {
					shared.put(shared_key, 1);
				}

			}

			rowindex++;

		}

		enumkeys = protunk_l.keys();
		rowindex = 1;
		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			final List<String> baits = protunk_l.get(key);

			for (final String b : baits) {

				val = table.get(b).getUnknownValue(key);

				writeLabel(obook, ul, 0, rowindex, key, false, false);
				writeLabel(obook, ul, 1, rowindex, b, false, false);
				writeLabel(obook, ul, 2, rowindex, df.format(val), false, false);

				shared_key = key + "@" + b;
				shared_key = shared_key.split("_")[0];

				if (shared.containsKey(shared_key)) {

					int value = shared.get(shared_key);
					value++;
					shared.put(shared_key, value);

				} else {
					shared.put(shared_key, 1);
				}

			}

			rowindex++;

		}

		keysToRemove = new ArrayList<String>();
		enumkeys = shared.keys();

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();
			val = shared.get(key);

			if (val != 2) {
				keysToRemove.add(key);
			}

		}

		final WritableSheet s = obook.createSheet("Unknown Unique Not Lysate S", 4);
		final WritableSheet ns = obook.createSheet("Unknown Unique Not Lysate", 5);
		writeLabel(obook, ns, 0, 0, "Protein", false, false);
		writeLabel(obook, ns, 1, 0, "Bait", false, false);

		rowindex = 1;

		for (final String k : keysToRemove) {

			writeLabel(obook, ns, 0, rowindex, k.split("@")[0], false, false);
			writeLabel(obook, ns, 1, rowindex, k.split("@")[1], false, false);
			rowindex++;

			shared.remove(k);
		}

		writeLabel(obook, s, 0, 0, "Protein", false, false);
		writeLabel(obook, s, 1, 0, "Bait", false, false);
		writeLabel(obook, s, 2, 0, "Score", false, false);
		writeLabel(obook, s, 3, 0, "Quant Value", false, false);
		writeLabel(obook, s, 4, 0, "Quant Ratio", false, false);

		enumkeys = shared.keys();
		double bait_quant;
		double prot_quant;
		double ratio_quant;
		String b;
		String p;

		rowindex = 1;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();

			p = key.split("@")[0];
			b = key.split("@")[1];

			val = table.get(b + "_H").getUnknownValue(p) + table.get(b + "_L").getUnknownValue(p);
			val /= 2;

			if (qlist.containsKey(p)) {
				prot_quant = qlist.get(p);
				bait_quant = qlist.get(b);

				ratio_quant = prot_quant / bait_quant;

				writeLabel(obook, s, 0, rowindex, p, false, false);
				writeLabel(obook, s, 1, rowindex, b, false, false);
				writeLabel(obook, s, 2, rowindex, df.format(val), false, false);
				writeLabel(obook, s, 3, rowindex, df.format(prot_quant), false, false);
				writeLabel(obook, s, 4, rowindex, df.format(ratio_quant), false, false);

				rowindex++;
			} else {

				writeLabel(obook, s, 0, rowindex, p, false, false);
				writeLabel(obook, s, 1, rowindex, b, false, false);
				writeLabel(obook, s, 2, rowindex, df.format(val), false, false);
				writeLabel(obook, s, 3, rowindex, "NA", false, false);
				writeLabel(obook, s, 4, rowindex, "NA", false, false);

				rowindex++;
			}

		}

		final WritableSheet crap = obook.createSheet("Crapome", 6);
		writeLabel(obook, crap, 0, 0, "Bait", false, false);
		writeLabel(obook, crap, 1, 0, "Unknwon proteins", false, false);
		writeLabel(obook, crap, 2, 0, "In Crapome", false, false);

		rowindex = 1;

		enumkeys = table.keys();

		double belong = 0;
		double total = 0;

		final List<String> crapResult = new ArrayList<String>();

		while (enumkeys.hasMoreElements()) {
			key = enumkeys.nextElement();

			total = table.get(key).getUnknowns().size();
			belong = 0;

			for (final String prey : table.get(key).getUnknowns()) {

				if (craps.contains(prey)) {
					belong++;
				}

			}

			crapResult.add(key + "\t" + total + "\t" + belong);

		}

		Collections.sort(crapResult);
		String[] elements;
		for (final String prey : crapResult) {

			elements = prey.split("\t");

			writeLabel(obook, crap, 0, rowindex, elements[0], false, false);
			writeLabel(obook, crap, 1, rowindex, elements[1], false, false);
			writeLabel(obook, crap, 2, rowindex, elements[2], false, false);

			rowindex++;

		}

		writeExcel(obook);
	}

	private void parseCrapome(File cfile) {

		craps = new ArrayList<String>();

		FileInputStream fis;

		try {
			fis = new FileInputStream(cfile);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;

			while ((dataline = dis.readLine()) != null) {

				craps.add(dataline);

			}

		} catch (final IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseQuant(File qfile) {

		qlist = new TObjectDoubleHashMap<String>();

		FileInputStream fis;

		try {
			fis = new FileInputStream(qfile);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String[] elements;

			while ((dataline = dis.readLine()) != null) {

				if (!dataline.startsWith("Condition")) {
					elements = dataline.split("\t");

					qlist.put(elements[0], Double.parseDouble(elements[1]));

				}

			}

		} catch (final IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseStat(File stat) {

		table = new Hashtable<String, Protein>();
		pools = new Hashtable<String, List<String>>();
		baitlists = new Hashtable<String, List<String>>();

		FileInputStream fis;

		try {
			fis = new FileInputStream(stat);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String bait = null;
			String exp = null;

			Protein p = null;
			String[] elements;
			boolean skip = false;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("B")) {

					skip = false;

					bait = dataline.split("\t")[1];
					exp = bait.split("_")[1];

					if (bait.contains("pool")) {
						skip = true;
						continue;
					}

					p = new Protein(bait);

					if (!pools.containsKey(exp)) {
						pools.put(exp, new ArrayList<String>());
						baitlists.put(exp, new ArrayList<String>());
					}

					table.put(bait, p);
					baitlists.get(exp).add(bait);

				}

				if (!skip) {

					if (dataline.startsWith("K")) {

						elements = dataline.split("\t");

						for (int i = 1; i < elements.length; i++) {
							p.addKnown(elements[i], -1);
						}

					}

					if (dataline.startsWith("U")) {

						elements = dataline.split("\t");

						for (int i = 1; i < elements.length; i++) {

							if ((elements[i] + "_" + exp).equals(bait)) {
								p.addKnown(elements[i], -1);
							} else {
								p.addUnknown(elements[i], -1);
							}

						}

					}

				}

			}

		} catch (final IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseInp(File inp) {

		/*
		 * table = new Hashtable<String, Protein>(); pools = new
		 * Hashtable<String, List<String>>(); baitlists = new Hashtable<String,
		 * List<String>>(); List<Double> average = new ArrayList<Double>();
		 * List<Double> minaverage = new ArrayList<Double>();
		 */

		FileInputStream fis;

		try {
			fis = new FileInputStream(inp);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String exp = null;
			String name;
			String bait = null;
			String value;

			final Protein p = null;
			boolean known = false;

			boolean go = false;
			boolean towrite = true;
			boolean lysate = false;

			final double min = Double.POSITIVE_INFINITY;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("Working on bait:")) {

					bait = dataline.split("\t")[1];

					// p = new Protein(bait + "_" + exp);

					// table.put(bait + "_" + exp, p);
					// baitlists.get(exp).add(bait);

					// if (min != Double.POSITIVE_INFINITY) {
					// minaverage.add(min);
					// }

					// min = Double.POSITIVE_INFINITY;

					continue;

				}

				if (dataline.startsWith("Working on")) {

					// if (minaverage.size() != 0) {

					// getStats(exp, minaverage, average);

					// }

					exp = dataline.split("\t")[1];
					go = true;

					// average = new ArrayList<Double>();
					// minaverage = new ArrayList<Double>();

					// pools.put(exp, new ArrayList<String>());
					// baitlists.put(exp, new ArrayList<String>());
					continue;

				}

				if (dataline.startsWith("Protein")) {
					continue;
				}

				if (go) {

					final String[] elements = dataline.split("\t");

					name = elements[0];
					value = elements[7];

					known = false;
					towrite = true;

					if (elements.length >= 9) {
						if (elements[8].equals("Y")) {
							lysate = true;
						} else {
							lysate = false;
						}
					} else {
						lysate = false;
					}

					if (elements.length >= 11) {

						if (elements[10].equals("discarded")) {
							towrite = false;
						}
					}

					// if (elements.length > 11) {

					// if (dataline.split("\t")[11].contains("exp")
					// || dataline.split("\t")[11].contains("BioGrid")) {
					// known = true;
					// }
					// } else {
					// known = false;
					// }

					// if (name.equals(bait)) {
					// known = true;
					// }

					if (towrite) {

						if (!pools.get(exp).contains(name)) {
							pools.get(exp).add(name);
						}

						if (table.get(bait + "_" + exp).KnownContainProt(name)) {

							table.get(bait + "_" + exp).addKnown(name, Double.parseDouble(value));

						} else {
							table.get(bait + "_" + exp).addUnknown(name, Double.parseDouble(value));
						}

						table.get(bait + "_" + exp).addLysateTrend(name, lysate);

					}

					// if (known) {
					// average.add(Double.parseDouble(value));

					// if (Double.parseDouble(value) < min) {
					// min = Double.parseDouble(value);
					// }

					// }

				}

			}

			// getStats(exp, minaverage, average);

		} catch (final FileNotFoundException e) {
			System.err.println("file not found");
		} catch (final IOException e) {
			System.err.println("unable to read file");
		}

	}

	/**
	 * @param exp
	 * @param minaverage
	 * @param average
	 */
	private void getStats(String exp, List<Double> minaverage, List<Double> average) {

		System.out.print("Known average for " + exp + ":\t");
		double sum = 0;
		for (int i = 0; i < average.size(); i++) {
			sum += average.get(i);
		}
		sum = sum / average.size();
		System.out.println(sum);

		System.out.print("Lowest average for " + exp + ":\t");
		sum = 0;
		for (int i = 0; i < minaverage.size(); i++) {
			sum += minaverage.get(i);
		}
		sum = sum / minaverage.size();
		System.out.println(sum);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final p3stats p3s = new p3stats();
		p3s.run();

	}

	public File openFile(String t) {

		File f = null;

		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		final int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			curdir = f.getParentFile();
		}

		return f;
	}

	public File outDir(String title) {

		final JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(title);
		fc.setCurrentDirectory(curdir);
		final int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		} else {
			System.exit(-2);
		}
		return f;

	}

	public WritableWorkbook createWorkbook(File out) {

		WritableWorkbook workbook = null;

		try {
			workbook = Workbook.createWorkbook(out);
		} catch (final IOException e) {

			e.printStackTrace();
		}
		return workbook;
	}

	public void writeLabel(WritableWorkbook book, WritableSheet sheet, int column, int row, String text, boolean known,
			boolean unk) throws WriteException {

		final WritableCellFormat format = new WritableCellFormat();
		format.setBackground(Colour.LIGHT_BLUE); // format for known

		final WritableCellFormat format1 = new WritableCellFormat();
		format1.setBackground(Colour.LIGHT_ORANGE); // format for unknown

		final WritableCellFormat format2 = new WritableCellFormat();
		format2.setBackground(Colour.LIGHT_GREEN); // format for unique

		// if (isNumeric(text)) {
		// writeNumber(book, sheet, column, row, Double.parseDouble(text));
		// } else {

		Label label = null;

		if (known && unk) {

			label = new Label(column, row, text, format2);

		} else {

			if (known) {

				label = new Label(column, row, text, format);

			} else {

				if (unk) {

					label = new Label(column, row, text, format1);

				} else {

					label = new Label(column, row, text);
				}

			}
		}

		try {
			sheet.addCell(label);
		} catch (final RowsExceededException e) {

			e.printStackTrace();
		} catch (final WriteException e) {

			e.printStackTrace();
		}
		// }

	}

	public boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			final double d = Double.parseDouble(str);
		} catch (final NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void writeNumber(WritableWorkbook book, WritableSheet sheet, int column, int row, double val) {

		final Number number = new Number(column, row, val);
		try {
			sheet.addCell(number);
		} catch (final RowsExceededException e) {

			e.printStackTrace();
		} catch (final WriteException e) {

			e.printStackTrace();
		}
	}

	public void writeExcel(WritableWorkbook book) {
		try {
			book.write();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		try {
			book.close();
		} catch (final WriteException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private class Protein {

		String bait;
		Hashtable<String, Double> known;
		Hashtable<String, Double> unknown;
		Hashtable<String, Boolean> lysate;

		public Protein(String bait) {

			this.bait = bait;
			known = new Hashtable<String, Double>();
			unknown = new Hashtable<String, Double>();
			lysate = new Hashtable<String, Boolean>();

		}

		public void addKnown(String prot, double value) {
			known.put(prot, value);
		}

		public void addUnknown(String prot, double value) {
			unknown.put(prot, value);
		}

		public void addLysateTrend(String prot, boolean value) {
			lysate.put(prot, value);
		}

		public boolean KnownContainProt(String prot) {
			if (known.containsKey(prot)) {
				return true;
			} else {
				return false;
			}
		}

		public boolean UnknownContainProt(String prot) {
			if (unknown.containsKey(prot)) {
				return true;
			} else {
				return false;
			}
		}

		public double getKnownValue(String prot) {
			return known.get(prot);
		}

		public double getUnknownValue(String prot) {
			return unknown.get(prot);
		}

		public boolean IsLysateTrend(String prot) {

			return lysate.get(prot);
		}

		@SuppressWarnings("unused")
		public List<String> getKnowns() {

			final List<String> pnames = new ArrayList<String>();
			final Enumeration<String> enumkeys = known.keys();
			while (enumkeys.hasMoreElements()) {
				final String key = enumkeys.nextElement();

				pnames.add(key);

			}

			return pnames;

		}

		@SuppressWarnings("unused")
		public List<String> getUnknowns() {

			final List<String> pnames = new ArrayList<String>();
			final Enumeration<String> enumkeys = unknown.keys();
			while (enumkeys.hasMoreElements()) {
				final String key = enumkeys.nextElement();

				pnames.add(key);

			}

			return pnames;

		}

		public String getBait() {
			return bait;
		}

	}

}
