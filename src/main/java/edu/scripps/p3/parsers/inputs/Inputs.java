/**
 * diego Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.parsers.inputs.utilities.ApvCalculator;
import edu.scripps.p3.parsers.inputs.utilities.NewCoverageFixer;
import edu.scripps.p3.parsers.inputs.utilities.Protein;
import edu.scripps.p3.prefilter.PreFilterUtils;
import edu.scripps.yates.utilities.fasta.FastaParser;

/**
 * @author diego
 *
 */
public class Inputs {
	private final static Logger log = Logger.getLogger(Inputs.class);
	String[] files;
	String[] baits;
	String[] exp;

	JFrame frame;

	List<Experiment> elist;
	InputPanel ip;

	List<int[]> assignments;

	File inputdir;
	String title;

	public File rootdir;

	/**
	 *
	 * @param files
	 * @param inputdir
	 * @param baits
	 * @param exp
	 * @param elist
	 */
	public Inputs(String[] files, File inputdir, String[] baits, String[] exp, List<Experiment> elist) {

		this.files = files;
		this.baits = baits;
		this.exp = exp;
		this.elist = elist;
		this.inputdir = inputdir;

	}

	public void run() {

		assignments = new ArrayList<int[]>();

		for (int i = 0; i < files.length; i++) {
			int[] values = new int[2];
			assignments.add(values);
		}

		createAndShowGUI();
	}

	protected void setTitle() {
		title = "Input Resolver";
	}

	public void setRootDir(File rootdir) {
		this.rootdir = rootdir;
	}

	private void createAndShowGUI() {

		setTitle();

		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		ip = new InputPanel();

		panel.add(ip);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL));

		JButton process = new JButton("Done");
		process.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				parseFiles();
				frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				frame.dispose();
			}
		});
		panel.add(process);

		frame.add(panel);

		// Display the window.
		frame.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_w = frame.getSize().width;
		int frame_h = frame.getSize().height;
		frame.setLocation((dim.width - frame_w) / 2, (dim.height - frame_h) / 2);

		if (files.length > 20) {
			mockassign();
		} else {
			frame.setVisible(true);
		}

	}

	private void mockassign() {

		String cbait;

		for (int i = 0; i < files.length; i++) {

			for (int j = 0; j < baits.length; j++) {

				cbait = files[i].split("_")[1];

				if (cbait.equals(baits[j])) {

					// int [] val = new int[2];
					// val[0] = j;

					assignments.get(i)[0] = j;

					break;

				}

			}

		}

		parseFiles();

		frame.dispose();

	}

	public List<Experiment> getExperiments() {
		return elist;
	}

	protected void parseFiles() {

		File f;
		int baitindex;
		int expindex;

		for (int i = 0; i < files.length; i++) {

			f = new File(inputdir, files[i]);
			log.info("Reading input file " + f.getAbsolutePath());
			baitindex = assignments.get(i)[0];
			expindex = assignments.get(i)[1];
			final Experiment experiment = elist.get(baitindex);
			final Condition conditionFromExperiment = experiment.getCondition(expindex);

			FileInputStream fis;

			try {
				fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
				Map<String, Integer> indexesByHeaders = null;
				String dataline;
				boolean dataStarts = false;
				while ((dataline = dis.readLine()) != null) {
					if (dataline.startsWith("Unique")) {
						dataStarts = true;
						continue;
					}
					if (dataline.startsWith("Locus")) {
						indexesByHeaders = PreFilterUtils.getIndexesByHeaders(dataline);
					}
					if (dataStarts && dataline.contains("\t") && "Proteins".equals(dataline.split("\t")[1])) {
						break;
					}

					if (dataStarts && dataline.contains("\t") && !"".equals(dataline.split("\t")[0])
							&& !"*".equals(dataline.split("\t")[0])) {

						Protein p = getProtein(dataline, indexesByHeaders);

						if (p.getName().length() > 1) {

							if (conditionFromExperiment.proteinInTable(p.getName())) {
								// merge proteins
								Protein old = conditionFromExperiment.getProtein(p.getName());

								Protein merged = mergeP(old, p);
								conditionFromExperiment.addProtein(old.getName(), merged, false);

							} else {
								// add protein
								conditionFromExperiment.addProtein(p.getName(), p, true);
							}

						}

					}

				}
				log.info(conditionFromExperiment.getNumberOfProteins() + " proteins readed for bait " + baitindex
						+ " condition " + expindex);
			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}

		}

		// CoverageFixer cfix = new CoverageFixer(elist, rootdir);
		NewCoverageFixer cfix = new NewCoverageFixer(elist, rootdir);
		cfix.run();

		ApvCalculator acalc = new ApvCalculator(elist);
		acalc.run();

	}

	protected Protein mergeP(Protein p1, Protein p2) {

		Protein p3 = null;

		String name = p1.getName();
		String locus = p1.getLocus();
		double sequenceCount = (p1.getPcount() + p2.getPcount()) / 2;
		double specCount = (p1.getScount() + p2.getScount()) / 2;
		double coverage = (p1.getCoverage() + p2.getCoverage()) / 2;
		double lenght = (p1.getLength() + p2.getLength()) / 2;
		double molweight = (p1.getMw() + p2.getMw()) / 2;
		double pi = (p1.getPi() + p2.getPi()) / 2;

		p3 = new Protein(name, locus, sequenceCount, specCount, coverage, lenght, molweight, pi);

		return p3;
	}

	protected Protein getProtein(String s, Map<String, Integer> indexesByHeaders) {
		String[] tmp = s.split("\t");

		double sequenceCount = Double.parseDouble(tmp[indexesByHeaders.get("Sequence Count")]);
		double specCount = Double.parseDouble(tmp[indexesByHeaders.get("Spectrum Count")]);
		String sequenceCoverageString = tmp[indexesByHeaders.get("Sequence Coverage")];
		if (sequenceCoverageString.endsWith("%")) {
			sequenceCoverageString = sequenceCoverageString.substring(0, sequenceCoverageString.length() - 1);
		}
		double coverage = Double.parseDouble(sequenceCoverageString);
		double lenght = Double.parseDouble(tmp[indexesByHeaders.get("Length")]);
		double molweight = Double.parseDouble(tmp[indexesByHeaders.get("MolWt")]);
		double pi = Double.parseDouble(tmp[indexesByHeaders.get("pI")]);
		String proteinDescription = tmp[indexesByHeaders.get("Descriptive Name")];
		String fullAccession = tmp[indexesByHeaders.get("Locus")];

		// disabled by Salva
		// String locus = " ", name = " ";
		// if (tmp[0].startsWith("sp")) {
		//
		// String[] tmp1 = tmp[0].split("\\|");
		// locus = tmp1[1].trim();
		//
		// String[] tmp2 = tmp[9].split("=");
		// tmp2[2] = tmp2[2].replace(" PE", "");
		//
		// name = tmp2[2].trim();
		// // name = tmp1[2].split("_")[0];
		//
		// if (tmp[10].equals("Y")) {
		// name = "";
		// }
		//
		// } else {
		//
		// if (!tmp[0].startsWith("contaminant") &&
		// !tmp[0].startsWith("Reverse")) {
		//
		// if (tmp[0].startsWith("IPI")) {
		//
		// String[] tmp2 = tmp[7].split(" ");
		// tmp2[1] = tmp2[1].split("=")[1];
		//
		// name = tmp2[1].trim();
		// name = name.toUpperCase();
		// locus = tmp[0];
		//
		// } else {
		// locus = tmp[0];
		// name = tmp[7].split(" ")[0];
		// }
		// }
		//
		// }
		String locus = FastaParser.getUniProtACC(fullAccession);
		if (locus == null) {
			locus = fullAccession;
		}
		String name = FastaParser.getGeneFromFastaHeader(proteinDescription);
		if (name == null) {

			// take the first word of the description
			name = proteinDescription.substring(0, proteinDescription.indexOf(" "));

			if (name == null) {
				name = locus;
			}
		}
		Protein p = new Protein(name, locus, sequenceCount, specCount, coverage, lenght, molweight, pi);

		return p;

	}

	protected JComboBox<String> getBox(String[] elements) {
		JComboBox<String> box = new JComboBox<String>();
		for (String element : elements) {
			box.addItem(element);
		}
		box.setSelectedIndex(0);
		return box;
	}

	private class InputPanel extends JPanel implements ActionListener {

		List<JTextField> texts;
		List<JComboBox<String>> baitslist;
		List<JComboBox<String>> conditions;

		public InputPanel() {

			texts = new ArrayList<JTextField>();
			baitslist = new ArrayList<JComboBox<String>>();
			conditions = new ArrayList<JComboBox<String>>();

			setLayout(new GridLayout(0, 3));

			setAutoscrolls(getAutoscrolls());

			for (int i = 0; i < files.length; i++) {
				JTextField tfield = new JTextField(files[i]);
				tfield.setEditable(false);
				texts.add(tfield);

				JComboBox<String> box = getBox(baits);
				box.addActionListener(this);
				baitslist.add(box);

				JComboBox<String> cond = getBox(exp);
				cond.addActionListener(this);
				conditions.add(cond);

				add(texts.get(i));
				add(baitslist.get(i));
				add(conditions.get(i));

			}

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			int index1, index2;

			for (int i = 0; i < baitslist.size(); i++) {

				index1 = baitslist.get(i).getSelectedIndex();
				index2 = conditions.get(i).getSelectedIndex();

				int[] values = assignments.get(i);
				values[0] = index1;
				values[1] = index2;
				assignments.set(i, values);

			}

		}

	}

}
