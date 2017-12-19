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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFileChooser;

public class PeptideCutterParser {

	ArrayList<String> plist;
	Hashtable<String, String> ptable;
	StringBuffer pep;

	public void run() {

		// loadData();
		plist = new ArrayList<String>();
		plist.add(
				"MDDREDLVYQAKLAEQAERYDEMVESMKKVAGMDVELTVEERNLLSVAYKNVIGARRASWRIISSIEQKEENKGGEDKLKMIREYRQMVETELKLICCDILDVLDKHLIPAANTGESKVFYYKMKGDYHRYLAEFATGNDRKEAAENSLVAYKAASDIAMTELPPTHPIRLGLALNFSVFYYEILNSPDRACRLAKAAFDDAIAELDTLSEESYKDSTLIMQLLRDNLTLWTSDMQGDGEEQNKEALQDVEDENQ");
		ptable = new Hashtable<String, String>();
		ptable.put(plist.get(0), "Ywhae");
		getPepData();
		writeOut();

	}

	private void writeOut() {

		File out = outDirSelector();

		String output = pep.toString();
		String name = "peptide_cutter.txt";

		writeOut(out, output, name);

	}

	private File outDirSelector() {

		File outdir;

		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select Output directory");
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			System.out.println("directory selected: " + f.getAbsolutePath());
		}
		outdir = f;
		return outdir;
	}

	private void writeOut(File out, String output, String name) {

		File fout = new File(out.getAbsoluteFile(), name);
		try {
			Writer wout = new BufferedWriter(new FileWriter(fout));
			wout.write(output);
			wout.close();

		} catch (IOException e) {
			System.err.println("probelm writing " + name);
			e.printStackTrace();
		}

	}

	private void loadData() {

		plist = new ArrayList<String>();
		ptable = new Hashtable<String, String>();

		// Diego modification 11/20/2014
		Hashtable<String, String> rptable = new Hashtable<String, String>();

		// Diego modification 11/19/2014
		// Hashtable<String, String> rptable = new Hashtable<String,String>();

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Diego modification 11/19/2014
		// fc.setDialogTitle("Select UniProt File");
		fc.setDialogTitle("Select Protein List");
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}
		FileInputStream fis;

		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String data = null;
			while ((data = dis.readLine()) != null) {

				String[] line = data.split("\t");

				// Diego modification 11/20/2014
				if (!rptable.containsKey(line[1])) {
					plist.add(line[0]);
					ptable.put(line[0], line[1]);
					rptable.put(line[1], line[0]);
				}

				//// Diego modification 11/19/2014
				// if (!rptable.containsKey(line[1])) {
				// plist.add(line[0]);
				// ptable.put(line[0], line[1]);
				// rptable.put(line[1], line[0]);
				// }

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void getPepData() {

		pep = new StringBuffer();
		pep.append("Protein\tPepCounted\tLenght\tCoverage\n");

		// Diego modification 11/19/2014
		// restoring the correct end of the loop
		// for (int i=0; i < 30; i++) {//plist.size(); i++) {
		for (int i = 0; i < plist.size(); i++) {

			System.out.println("working on...." + plist.get(i) + " (" + (i + 1) + "/" + plist.size() + ")");

			String urlStr = "http://web.expasy.org/cgi-bin/peptide_cutter/peptidecutter.pl?protein=" + plist.get(i)
					+ "&enzyme_number=less_enzymes&enzyme=Tryps&special_enzyme=Chym&min_prob=&block_size=60&alphtable=alphtable&cleave_number=all&cleave_exactly=&cleave_range_min=&cleave_range_max=";

			URL url;
			try {
				url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String line;
				String peptoinsert;
				int pcounter;

				while ((line = rd.readLine()) != null) {

					if (line.contains("<tr><td>")) {

						String[] buffer = line.split("<td>");
						String[] values = buffer[2].split(" ");
						values[values.length - 1] = values[values.length - 1].split("<")[0];

						int[] val = new int[values.length];
						pcounter = 0;

						val[0] = Integer.parseInt(values[0]);

						// if (val[0]-0>=6) pcounter++;
						if (val[0] - 0 >= 6)
							pcounter += (val[0] - 0);

						for (int j = 1; j < values.length; j++) {
							val[j] = Integer.parseInt(values[j]);
							if (val[j] - val[j - 1] >= 6) {
								// pcounter++;
								pcounter += (val[j] - val[j - 1]);
							}
						}

						// double coverage =
						// (double)((double)pcounter/(double)values.length);
						float coverage = (float) ((double) pcounter / (double) val[val.length - 1]);

						// Diego modification 11/20/2014
						peptoinsert = ptable.get(plist.get(i)) + "\t" + pcounter + "\t" + val[val.length - 1] + "\t"
								+ coverage + "\n";

						// Diego modification 11/19/2014
						// peptoinsert = ptable.get(plist.get(i)) + "\t" +
						// pcounter + "\t" + val[val.length-1] + "\t" + coverage
						// + "\n";
						// peptoinsert = plist.get(i) + "\t" + pcounter + "\t" +
						// val[val.length-1] + "\t" + coverage + "\n";

						// peptoinsert = ptable.get(plist.get(i)) + "\t";
						// for (int j=0; j < values.length; j++) {
						// peptoinsert += values[j] + "\t";
						// }
						// peptoinsert += "\n";
						pep.append(peptoinsert);

					}

				}
				rd.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out.println("done parsing PepCutter");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PeptideCutterParser pcp = new PeptideCutterParser();
		pcp.run();

	}

}
