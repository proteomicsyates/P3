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

import javax.swing.JFileChooser;

/**
 * create the biogrid physical and gentic from the downloaded biogrid file
 * 
 * @author diego
 *
 */
public class Bioparser {

	Hashtable<String, interaction> ilist;

	public void run() {

		System.out.println("hello");
		File f = openFile();
		System.out.println("hello0.5");
		File out = chooseOutDir();
		System.out.println("hello1");
		run2(f, out, "physical");
		run2(f, out, "genetic");

	}

	public void run2(File f, File out, String filter) {

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

			String line = null;
			System.out.println("hello3");
			while ((line = dis.readLine()) != null) {

				if (!line.startsWith("#")) {
					String[] dline = line.split("\t");

					if (dline[12].equals(filter)) {// "physical")) {

						String key1 = dline[7] + "_" + dline[8];
						String key2 = dline[8] + "_" + dline[7];
						System.out.println(dline[7]);
						if (dline[12].equals("physical")) {
							if (!system_phy.containsKey(dline[11])) {
								system_phy.put(dline[11], dline[11]);
							}
						} else {
							if (!system_gen.containsKey(dline[11])) {
								system_gen.put(dline[11], dline[11]);
							}
						}

						if (!type.containsKey(dline[12])) {
							type.put(dline[12], dline[12]);
						}
						if (!tgh.containsKey(dline[17])) {
							tgh.put(dline[17], dline[17]);
						}

						if (!ilist.containsKey(key1) && !ilist.containsKey(key2)) {

							interaction it = new interaction(dline[7], dline[8]);
							it.addExpSystem(dline[11]);
							it.addExpType(dline[12]);
							it.addExpstudy(dline[13]);
							it.addExpThroughput(dline[17]);
							ilist.put(key1, it);
							keys.add(key1);

						} else {

							if (ilist.containsKey(key1)) {
								interaction it = ilist.get(key1);
								it.addExpSystem(dline[11]);
								it.addExpType(dline[12]);
								it.addExpstudy(dline[13]);
								it.addExpThroughput(dline[17]);
								ilist.put(key1, it);
							} else {
								interaction it = ilist.get(key2);
								it.addExpSystem(dline[11]);
								it.addExpType(dline[12]);
								it.addExpstudy(dline[13]);
								it.addExpThroughput(dline[17]);
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

		File fout = new File(out, filename + filter + ".txt");
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
		Bioparser bp = new Bioparser();
		bp.run();

	}

}
