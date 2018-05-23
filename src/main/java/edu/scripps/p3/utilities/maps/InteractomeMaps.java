/**
 * diego
 * Aug 14, 2014
 */
package edu.scripps.p3.utilities.maps;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.JFileChooser;

/**
 * 
 * class to create a system view of the full interactome
 * 
 * @author diego
 *
 */
public class InteractomeMaps {

	File curdir;
	StringBuilder edges;
	Hashtable<String, Nodes> nodes;

	public void run() {

		File inp = openDir("Select topology maps directory");
		File quant = openFile("Select quant values file");
		parseInp(inp);

		parseQuant(quant);

		StringBuilder log = new StringBuilder();
		log.append("*edges");
		log.append(edges.toString());
		log.append("*nodes");

		Enumeration<String> enumkeys = nodes.keys();
		String key;
		while (enumkeys.hasMoreElements()) {
			key = enumkeys.nextElement();

			nodes.get(key).calculateColor();

			log.append(nodes.get(key).getNode());

		}

		File out = new File(inp, "Interactome.dat");
		writeOut(out, log.toString());

	}

	private void parseQuant(File inp) {

		FileInputStream fis;

		try {
			fis = new FileInputStream(inp);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String[] elements;
			String name;
			double qratio;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("Condition")) {
					continue;
				}

				elements = dataline.split("\t");
				name = elements[0];
				qratio = Double.parseDouble(elements[1]);

				if (nodes.containsKey(name)) {
					nodes.get(name).setQuant(qratio);
				}

			}

		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseInp(File inpdir) {

		edges = new StringBuilder();
		nodes = new Hashtable<String, Nodes>();

		List<String> baits = new ArrayList<String>();

		String[] list = inpdir.list();

		String bait;

		for (String element : list) {

			bait = element.split("_")[0];

			if (!baits.contains(bait)) {
				baits.add(bait);
			}

			File inp = new File(inpdir, element);

			if (inp.getName().contains("differential")) {

				FileInputStream fis;

				try {
					fis = new FileInputStream(inp);
					BufferedInputStream bis = new BufferedInputStream(fis);
					BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

					String dataline;
					String[] elements;
					String name;
					int known;
					boolean edges = false;
					boolean nodes = false;

					while ((dataline = dis.readLine()) != null) {

						if (dataline.startsWith("*edges")) {
							edges = true;
							nodes = false;
							continue;
						}

						if (dataline.startsWith("*nodes")) {
							nodes = true;
							edges = false;
							continue;
						}

						if (edges) {

							this.edges.append(dataline);
							this.edges.append("\n");

						}

						if (nodes) {

							name = dataline.split("\t")[0];

							if (!this.nodes.containsKey(name)) {
								Nodes node = new Nodes(name);
								this.nodes.put(name, node);
							}

						}

					}

				} catch (IOException e) {
					System.err.println("unable to read file");
				}

			}

		}

		for (String b : baits) {

			nodes.get(b).setBait(true);

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InteractomeMaps im = new InteractomeMaps();
		im.run();

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

	public File openDir(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			curdir = f.getParentFile();
		}

		return f;
	}

	private class Nodes {

		String name;
		double x;
		double y;
		double ratio;
		int red;
		int blue;
		int green;
		boolean bait;
		int shape;

		public Nodes(String name) {
			this.name = name;
			this.bait = false;
		}

		public void setQuant(double ratio) {
			this.ratio = ratio;
		}

		public void setBait(boolean b) {
			this.bait = b;
		}

		public void calculateColor() {

			if (bait) {
				Color c = Color.YELLOW;
				red = c.getRed();
				green = c.getGreen();
				blue = c.getBlue();
				shape = 0;
			} else {
				shape = 1;

				if (ratio > 0.9 && ratio < 1.1) {
					Color c = Color.GREEN;
					red = c.getRed();
					green = c.getGreen();
					blue = c.getBlue();

				} else {

					if (ratio > 3) {
						Color c = Color.RED;
						red = c.getRed();
						green = c.getGreen();
						blue = c.getBlue();

					} else {

						if (ratio < 0.33) {
							Color c = Color.BLUE;
							red = c.getRed();
							green = c.getGreen();
							blue = c.getBlue();

						} else {

							double position = this.ratio / 3;
							red = (int) (255 * position);
							green = 0;
							blue = (int) (255 * (1 - position));

						}

					}

				}

			}

			Random r = new Random();
			x = r.nextDouble();
			y = r.nextDouble();

		}

		public String getNode() {

			StringBuilder node = new StringBuilder();

			node.append(name);
			node.append("\t");
			node.append(x);
			node.append("\t");
			node.append(y);
			node.append("\tc ");
			node.append(red);
			node.append(",");
			node.append(green);
			node.append(",");
			node.append(blue);
			node.append("\ts ");
			node.append(shape);
			node.append("\ta \"");
			node.append(name);
			node.append("\"\n");

			return node.toString();

		}

	}

	private void writeOut(File out, String log) {

		try {
			Writer wout = new BufferedWriter(new FileWriter(out));
			wout.write(log);
			wout.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}

}
