package edu.scripps.p3.inputgenerator;


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
import java.util.Hashtable;

import javax.swing.JFileChooser;

public class PeptideCutterNameNormalization {

	StringBuffer log;
	Hashtable<String, String> data;

	public void run() {

	//	loadonto();
		loadfile();
		writefile();

	}

	private void loadfile() {
		log = new StringBuffer();
		File f = null;
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		FileInputStream fis;
		String dataline;
		String name;
		double value;

		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			while ((dataline = dis.readLine()) != null) {

				if (!dataline.startsWith("Protein")) {
					String[] line = dataline.split("\t");
					name = line[0];
					value = Double.parseDouble(line[3]);
					value = value*100;
					//name = data.get(name);
					log.append(name + "\t" + value + "\n");
				}
			}

		} catch (FileNotFoundException e) {
			System.err.println("file not found");
		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void loadonto() {

		log = new StringBuffer();
		data = new Hashtable<String, String>();

		File f = null;
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		FileInputStream fis;
		String dataline;
		double val;

		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			while ((dataline = dis.readLine()) != null) {

				String[] line = dataline.split("\t");
				if (!data.containsKey(line[0])) {
					data.put(line[0], line[1]);
				}
				
			}

		} catch (FileNotFoundException e) {
			System.err.println("file not found");
		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	private File getOutDir() {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}
		return f;
	}

	private void writefile() {

		File logdir = getOutDir();

		File fout = new File(logdir, "PepCutterNameFixed.txt");

		try {
			Writer out = new BufferedWriter(new FileWriter(fout));
			out.write(log.toString());
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PeptideCutterNameNormalization pcnn = new PeptideCutterNameNormalization();
		pcnn.run();

	}

}
