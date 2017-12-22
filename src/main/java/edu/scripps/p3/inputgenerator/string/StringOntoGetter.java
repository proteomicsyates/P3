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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;

import edu.scripps.yates.utilities.dates.DatesUtil;
import edu.scripps.yates.utilities.fasta.FastaParser;

/**
 *
 */

/**
 * @author diegoc
 *
 */
public class StringOntoGetter {

	private Set<String> baits;
	private Set<String> preys;
	private String specie;

	public static final String MOUSE = "10090";
	public static final String HUMAN = "9606";
	public static final String RAT = "10116";
	public static final String YEAST = "4932";

	private final static String baitsFile = "Z:\\share\\Salva\\data\\Ben's CRISPR AMPK\\yeast dataset\\bait.txt";
	private final static String inputFolder = "Z:\\share\\Salva\\data\\Ben's CRISPR AMPK\\yeast dataset\\DTASelects";
	private final static String outputFolder = "Z:\\share\\Salva\\data\\Ben's CRISPR AMPK\\yeast dataset\\orthogonal\\new";

	private static final boolean test = true;

	private void setSpecies(String specie) {
		this.specie = specie;
	}

	public void run() throws IOException {

		baits = new HashSet<String>();
		preys = new HashSet<String>();

		// load baits
		getData(baits);
		// load plist;
		// getData(preys);
		// Salva addition
		getDataFromInputFiles(preys);
		File out = chooseOutDir();
		FileWriter scores = new FileWriter(new File(out, "String_" + specie + ".txt"));
		FileWriter escores = new FileWriter(new File(out, "String_exp_" + specie + ".txt"));

		List<String> totalToQuery = new ArrayList<String>();
		totalToQuery.addAll(baits);
		totalToQuery.addAll(preys);
		// get interactions for baits
		int numBaits = 0;
		Map<String, Protein> proteinMap = getInteractors(totalToQuery);
		for (Protein bait : proteinMap.values()) {
			System.out.println(numBaits++ + " bait: " + bait.pname);

			scores.write(bait.getInteractions());
			escores.write(bait.getExpInteractions());

		}
		scores.close();
		escores.close();

		// writeOut(scores.toString(), "String_");
		// writeOut(escores.toString(), "String_exp_");

	}

	private void getDataFromInputFiles(Set<String> preys2) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		if (!test) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnval = fc.showOpenDialog(null);

			if (returnval == JFileChooser.APPROVE_OPTION) {
				f = fc.getSelectedFile();
			}
		} else {
			f = new File(inputFolder);
		}
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {

			f = files[i];

			FileInputStream fis;

			try {
				fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

				String dataline;
				boolean dataStarts = false;
				while ((dataline = dis.readLine()) != null) {
					if (dataline.startsWith("Unique")) {
						dataStarts = true;
						continue;
					}
					if (dataStarts && dataline.contains("\t") && "Proteins".equals(dataline.split("\t")[1])) {
						break;
					}
					if (dataStarts && dataline.contains("\t") && !"".equals(dataline.split("\t")[0])
							&& !"*".equals(dataline.split("\t")[0])) {

						String prey = getProtein(dataline);

						preys2.add(prey);

					}

				}

			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}

		}

	}

	private String getProtein(String s) {
		String[] tmp = s.split("\t");

		String proteinDescription = tmp[8];
		String name = FastaParser.getGeneFromFastaHeader(proteinDescription);
		if (name == null) {
			// take the first word of the description
			name = proteinDescription.substring(0, proteinDescription.indexOf(" "));

		}
		return name;
	}

	private Map<String, Protein> getInteractors(Collection<String> prots) {
		Map<String, Protein> ret = new HashMap<String, Protein>();
		System.out.println("retrieving from string interactions for " + prots.size() + " proteins");
		StringBuilder sb = new StringBuilder();
		for (String prey : prots) {
			if (!"".equals(sb.toString())) {
				sb.append("%0d");
			}
			sb.append(prey);

		}
		String urlStr = "http://string-db.org/api/psi-mi-tab/interaction_partners?identifiers=" + sb.toString()
				+ "&species=" + specie + "&required_score=0&caller_identity=YatesLab";
		try {
			System.out.println(urlStr);
			long t1 = System.currentTimeMillis();
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			String prot1;
			String prot2;
			String score;
			String expscore;
			boolean exp = false;
			System.out.println("Response received in "
					+ DatesUtil.getDescriptiveTimeFromMillisecs(System.currentTimeMillis() - t1));
			while ((line = rd.readLine()) != null) {
				if ("".equals(line.trim())) {
					continue;
				}
				exp = false;
				expscore = null;
				String[] elements = line.split("\t");

				prot1 = elements[2];
				Protein protein1 = null;
				if (ret.containsKey(prot1)) {
					protein1 = ret.get(prot1);
				} else {
					protein1 = new Protein(prot1);
					ret.put(prot1, protein1);
				}
				prot2 = elements[3];
				Protein protein2 = null;
				if (ret.containsKey(prot2)) {
					protein2 = ret.get(prot2);
				} else {
					protein2 = new Protein(prot2);
					ret.put(prot2, protein2);
				}
				String[] scores = elements[14].split("\\|");

				score = scores[0].split(":")[1];

				for (int i = 1; i < scores.length; i++) {

					if (scores[i].startsWith("e")) {

						expscore = scores[i].split(":")[1];
						exp = true;
						break;

					}

				}

				// if (prot.equals(prot1)) {
				protein1.addInter(prot2, score, expscore, exp);
				// }

				// if (prot.equals(prot2)) {
				protein2.addInter(prot1, score, expscore, exp);
				// }

			}
			rd.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(ret.size() + " interactors found");
		return ret;
	}

	private void getData(Set<String> data) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		if (!test) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnval = fc.showOpenDialog(null);

			if (returnval == JFileChooser.APPROVE_OPTION) {
				f = fc.getSelectedFile();
			}
		} else {
			f = new File(baitsFile);
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
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		StringOntoGetter sog = new StringOntoGetter();
		sog.setSpecies(StringOntoGetter.YEAST);
		sog.run();

	}

	private class Protein {

		String pname;
		Set<String> pinter;
		Map<String, String> scores;
		Map<String, String> escores;

		public Protein(String name) {
			pname = name;
			scores = new HashMap<String, String>();
			escores = new HashMap<String, String>();
			pinter = new HashSet<String>();
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

		File f = null;
		if (!test) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnval = fc.showSaveDialog(null);
			if (returnval == JFileChooser.APPROVE_OPTION) {
				f = fc.getSelectedFile();
				System.out.println("directory selected: " + f.getAbsolutePath());
			}
		} else {
			f = new File(outputFolder);
		}
		return f;

	}

}
