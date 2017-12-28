/**
 * diego Jan 14, 2013
 */
package edu.scripps.p3.inputgenerator.biogrid;

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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;

import edu.scripps.p3.prefilter.PreFilterUtils;

/**
 * create the biogrid physical and gentic from the downloaded biogrid file
 *
 * @author diego
 *
 */
public class BioGRIDparser {

	private static final String EXPERIMENTAL_SYSTEM_TYPE = "Experimental System Type";
	private static final String OFFICIAL_SYMBOL_INTERACTOR_A = "Official Symbol Interactor A";
	private static final String OFFICIAL_SYMBOL_INTERACTOR_B = "Official Symbol Interactor B";
	private static final String PHYSICAL = "physical";
	private static final String AFFINITY_CAPTURE_MS = "Affinity Capture-MS";
	private static final String AFFINITY_CAPTURE_Western = "Affinity Capture-Western";
	private static final String EXPERIMENTAL_SYSTEM = "Experimental System";
	private static final String AUTHOR = "Author";
	private static final String THROUGHPUT = "Throughput";
	Hashtable<String, interaction> ilist;

	public void run() {
		boolean onlyAffinityCapture = false;
		System.out.println("hello");
		File f = openFile();
		System.out.println("hello0.5");
		File out = chooseOutDir();
		System.out.println("hello1");
		run2(f, out, "physical", onlyAffinityCapture);
		run2(f, out, "genetic", onlyAffinityCapture);

	}

	public void run2(File f, File out, String filter, boolean onlyAffinityCapture) {

		ilist = new Hashtable<String, interaction>();
		List<String> keys = new ArrayList<String>();

		Hashtable<String, String> system_phy = new Hashtable<String, String>();
		Hashtable<String, String> system_gen = new Hashtable<String, String>();
		Hashtable<String, String> type = new Hashtable<String, String>();
		Hashtable<String, String> tgh = new Hashtable<String, String>();
		System.out.println("hello2");
		// load biogrid file
		// File f = openFile();
		// parse file
		FileInputStream fis;
		String filename = f.getAbsolutePath().split("/")[f.getAbsolutePath().split("/").length - 1];
		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
			Map<String, Integer> indexesByHeaders = null;
			String line = null;
			System.out.println("hello3");
			while ((line = dis.readLine()) != null) {

				if (!line.startsWith("#")) {
					String[] dline = line.split("\t");

					final Integer experimentalSystemTypeIndex = indexesByHeaders.get(EXPERIMENTAL_SYSTEM_TYPE);
					final String experimentalSystemType = dline[experimentalSystemTypeIndex];
					if (experimentalSystemType.equals(filter)) {// "physical"))
																// {
						final String experimentalSystem = dline[indexesByHeaders.get(EXPERIMENTAL_SYSTEM)];
						if (onlyAffinityCapture) {
							if (!experimentalSystem.equals(AFFINITY_CAPTURE_MS)
									&& !experimentalSystem.equals(AFFINITY_CAPTURE_Western)) {
								continue;
							}
						}
						final String interactorA = dline[indexesByHeaders.get(OFFICIAL_SYMBOL_INTERACTOR_A)];
						final String interactorB = dline[indexesByHeaders.get(OFFICIAL_SYMBOL_INTERACTOR_B)];
						String key1 = interactorA + "_" + interactorB;
						String key2 = interactorB + "_" + interactorA;
						System.out.println(interactorA);
						if (experimentalSystemType.equals(PHYSICAL)) {
							if (!system_phy.containsKey(experimentalSystem)) {
								system_phy.put(experimentalSystem, experimentalSystem);
							}
						} else {
							if (!system_gen.containsKey(experimentalSystem)) {
								system_gen.put(experimentalSystem, experimentalSystem);
							}
						}

						if (!type.containsKey(experimentalSystemType)) {
							type.put(experimentalSystemType, experimentalSystemType);
						}
						final String throughput = dline[indexesByHeaders.get(THROUGHPUT)];
						if (!tgh.containsKey(throughput)) {
							tgh.put(throughput, throughput);
						}

						final String author = dline[indexesByHeaders.get(AUTHOR)];
						if (!ilist.containsKey(key1) && !ilist.containsKey(key2)) {

							interaction it = new interaction(interactorA, interactorB);
							it.addExpSystem(experimentalSystem);
							it.addExpType(experimentalSystemType);
							it.addExpstudy(author);
							it.addExpThroughput(throughput);
							ilist.put(key1, it);
							keys.add(key1);

						} else {

							if (ilist.containsKey(key1)) {
								interaction it = ilist.get(key1);
								it.addExpSystem(experimentalSystem);
								it.addExpType(experimentalSystemType);
								it.addExpstudy(author);
								it.addExpThroughput(throughput);
								ilist.put(key1, it);
							} else {
								interaction it = ilist.get(key2);
								it.addExpSystem(experimentalSystem);
								it.addExpType(experimentalSystemType);
								it.addExpstudy(author);
								it.addExpThroughput(throughput);
								ilist.put(key2, it);
							}

						}

						/*
						 * if (ilist.containsKey(key1)) { interaction it =
						 * ilist.get(key1); it.addExpSystem(dline[11]);
						 * it.addExpType(dline[12]); it.addExpstudy(dline[13]);
						 * it.addExpThroughput(dline[17]); ilist.put(key1, it);
						 * } else { interaction it = new
						 * interaction(dline[7],dline[8]);
						 * it.addExpSystem(dline[11]); it.addExpType(dline[12]);
						 * it.addExpstudy(dline[13]);
						 * it.addExpThroughput(dline[17]); ilist.put(key1, it);
						 * keys.add(key1); } if (ilist.containsKey(key2)) {
						 * interaction it = ilist.get(key2);
						 * it.addExpSystem(dline[11]); it.addExpType(dline[12]);
						 * it.addExpstudy(dline[13]);
						 * it.addExpThroughput(dline[17]); ilist.put(key2, it);
						 * } else { interaction it = new
						 * interaction(dline[8],dline[7]);
						 * it.addExpSystem(dline[11]); it.addExpType(dline[12]);
						 * it.addExpstudy(dline[13]);
						 * it.addExpThroughput(dline[17]); ilist.put(key2, it);
						 * keys.add(key2); }
						 */
					}

				} else {
					if (line.startsWith("#BioGRID Interaction ID")) {
						indexesByHeaders = PreFilterUtils.getIndexesByHeaders(line);
					}
				}

			}
		} catch (FileNotFoundException e) {
			System.err.println("problem reading file");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("error in reading line");
			e.printStackTrace();
		}

		// create output file
		// File out = chooseOutDir();
		double score;
		StringBuffer log = new StringBuffer();

		int sys_phy_size = system_phy.size();
		int sys_gen_size = system_gen.size();

		for (int i = 0; i < keys.size(); i++) {
			interaction it = ilist.get(keys.get(i));
			score = it.getScore(sys_phy_size, sys_gen_size);
			String[] names = keys.get(i).split("_");
			log.append(names[0] + "\t" + names[1] + "\t" + score + "\n");
		}

		File fout = new File(out, FilenameUtils.getBaseName(filename) + "_" + filter + ".txt");
		try {
			Writer wout = new BufferedWriter(new FileWriter(fout));
			wout.write(log.toString());
			wout.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}

	private File chooseOutDir() {
		System.out.println("test");
		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		System.out.println("test0");
		int returnval = fc.showSaveDialog(null);
		System.out.println("test1");
		if (returnval == JFileChooser.APPROVE_OPTION) {
			System.out.println("test2");
			f = fc.getSelectedFile();
			System.out.println("directory selected: " + f.getAbsolutePath());
		}
		System.out.println("test3");
		return f;

	}

	private File openFile() {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
		BioGRIDparser bp = new BioGRIDparser();
		bp.run();

	}

}
