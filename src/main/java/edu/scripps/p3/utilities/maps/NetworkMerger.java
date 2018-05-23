package edu.scripps.p3.utilities.maps;

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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;

/**
 * diego
 * Feb 26, 2014
 */

/**
 * merge two networks, the node color code is red for nodes only in first
 * condition blue in the second and green in both It should be generalized on
 * many networks with different color schemas
 * 
 * @author diego
 *
 */
public class NetworkMerger {

	List<String> edges;
	Hashtable<String, String> nodes;

	public void run() {

		edges = new ArrayList<String>();
		nodes = new Hashtable<String, String>();

		// load first condition
		File first = openFile("Select first condition");
		// load second condition
		File second = openFile("Select second condition");
		// save merged
		File outdir = outDir("Choose where to save");

		parse(first, 0);
		parse(second, 1);

		File fout = new File(outdir, "cellcycle.dat");

		writeOut(fout);

	}

	/**
	 * @param fout
	 */
	private void writeOut(File fout) {

		StringBuffer log = new StringBuffer();

		log.append("*edges\n");

		for (String edge : edges) {
			log.append(edge + "\n");
		}

		log.append("*nodes\n");

		Enumeration<String> enumkeys = nodes.keys();

		String key;

		while (enumkeys.hasMoreElements()) {

			key = enumkeys.nextElement();

			log.append(nodes.get(key) + "\n");

		}

		appendOut(fout, log.toString());

	}

	/**
	 * @param first
	 */
	private void parse(File input, int value) {

		FileInputStream fis;

		try {
			fis = new FileInputStream(input);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			boolean edg = false;
			boolean nod = false;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.contains("*edges")) {
					edg = true;
					nod = false;
					continue;
				}
				if (dataline.contains("*nodes")) {
					nod = true;
					edg = false;
					continue;
				}
				if (edg) {

					if (!edges.contains(dataline)) {
						edges.add(dataline);
					}

				}
				if (nod) {

					String[] elements = dataline.split("\t");

					if (nodes.containsKey(elements[0])) {

						dataline = dataline.replaceAll(elements[3], "c 0,255,0");

						nodes.put(elements[0], dataline);

					} else {

						if (value == 0) {

							dataline = dataline.replaceAll(elements[3], "c 255,0,0");

							nodes.put(elements[0], dataline);

						} else {

							dataline = dataline.replaceAll(elements[3], "c 0,0,255");

							nodes.put(elements[0], dataline);

						}

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

		NetworkMerger nm = new NetworkMerger();
		nm.run();

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

	public void appendOut(File fout, String output) {

		try {
			Writer out = new BufferedWriter(new FileWriter(fout, true));
			out.write(output);
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}
}
