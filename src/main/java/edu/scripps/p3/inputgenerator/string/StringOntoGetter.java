package edu.scripps.p3.inputgenerator.string;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;

/**
 *
 */

/**
 * @author diegoc
 *
 */
public class StringOntoGetter {

	private List<String> baits;
	private List<String> preys;
	private String specie;

	public static final String MOUSE = "10090";
	public static final String HUMAN = "9606";
	public static final String RAT = "10116";

	private void setSpecies(String specie) {
		this.specie = specie;
	}

	public void run() {

		baits = new ArrayList<String>();
		preys = new ArrayList<String>();

		// load baits
		getData(baits);
		// load plist;
		getData(preys);

		StringBuilder scores = new StringBuilder();
		StringBuilder escores = new StringBuilder();

		// get interactions for baits
		int numBaits = 0;
		for (String bait : baits) {
			System.out.println(numBaits++ + " bait: " + bait);
			Protein p = getInteractors(bait);

			scores.append(p.getInteractions());
			escores.append(p.getExpInteractions());

		}
		int numPreys = 0;
		for (String prey : preys) {
			System.out.println(numPreys++ + " prey: " + prey);
			Protein p = getInteractors(prey);

			scores.append(p.getInteractions());
			escores.append(p.getExpInteractions());

		}

		writeOut(scores.toString(), "String_");
		writeOut(escores.toString(), "String_exp_");

	}

	private Protein getInteractors(String prot) {

		Protein p = new Protein(prot);

		String urlStr = "http://string-db.org/api/psi-mi-tab/interactionsList?identifiers=" + prot + "&species="
				+ specie + "&required_score=000";
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			System.out.println(urlStr);
			String line;
			String prot1;
			String prot2;
			String score;
			String expscore;
			boolean exp = false;

			while ((line = rd.readLine()) != null) {
				if ("".equals(line.trim())) {
					continue;
				}
				exp = false;
				expscore = null;
				String[] elements = line.split("\t");

				prot1 = elements[2];
				prot2 = elements[3];

				String[] scores = elements[14].split("\\|");

				score = scores[0].split(":")[1];

				for (int i = 1; i < scores.length; i++) {

					if (scores[i].startsWith("e")) {

						expscore = scores[i].split(":")[1];
						exp = true;
						break;

					}

				}

				if (prot.equals(prot1)) {
					p.addInter(prot2, score, expscore, exp);
				}

				if (prot.equals(prot2)) {
					p.addInter(prot1, score, expscore, exp);
				}

			}
			rd.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return p;
	}

	private void getData(List<String> data) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;

			while ((dataline = dis.readLine()) != null) {

				data.add(dataline);

			}

			dis.close();

		} catch (FileNotFoundException e) {
			System.err.println("bait file not found");
		} catch (IOException e) {
			System.err.println("unable to read bait file");
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StringOntoGetter sog = new StringOntoGetter();
		sog.setSpecies(StringOntoGetter.RAT);
		sog.run();

	}

	private class Protein {

		String pname;
		List<String> pinter;
		Hashtable<String, String> scores;
		Hashtable<String, String> escores;

		public Protein(String name) {
			pname = name;
			scores = new Hashtable<String, String>();
			escores = new Hashtable<String, String>();
			pinter = new ArrayList<String>();
		}

		public void addInter(String prot, String score, String escore, boolean exp) {

			pinter.add(prot);
			scores.put(prot, score);
			if (exp) {
				escores.put(prot, escore);
			}

		}

		public String getInteractions() {

			StringBuilder log = new StringBuilder();

			for (String s : pinter) {

				if (scores.containsKey(s)) {

					log.append(pname + "\t" + s + "\t" + scores.get(s) + "\n");

				}

			}

			return log.toString();

		}

		public String getExpInteractions() {

			StringBuilder log = new StringBuilder();

			for (String s : pinter) {

				if (escores.containsKey(s)) {

					log.append(pname + "\t" + s + "\t" + escores.get(s) + "\n");

				}

			}

			return log.toString();
		}

	}

	private void writeOut(String output, String prefix) {

		File out = chooseOutDir();
		File fout = new File(out, prefix + specie + ".txt");
		try {
			Writer wout = new BufferedWriter(new FileWriter(fout));
			wout.write(output);
			wout.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}

	private File chooseOutDir() {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			System.out.println("directory selected: " + f.getAbsolutePath());
		}
		return f;

	}

}
