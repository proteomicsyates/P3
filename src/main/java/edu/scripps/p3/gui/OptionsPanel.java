/**
 * diego Jun 6, 2013
 */
package edu.scripps.p3.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.scripps.p3.P3;
import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.io.MyFileChooser;
import edu.scripps.p3.parsers.Baits;
import edu.scripps.p3.parsers.Experiments;
import edu.scripps.p3.parsers.inputs.Controls;
import edu.scripps.p3.parsers.inputs.Inputs;
import edu.scripps.p3.parsers.orthogonals.Orthogonals;
import edu.scripps.p3.parsers.quantitative.Lysates;
import edu.scripps.p3.parsers.quantitative.Quantitatives;
import edu.scripps.yates.utilities.fasta.FastaParser;

/**
 * @author diego
 *
 */
public class OptionsPanel extends JPanel {
	private final static Logger log = Logger.getLogger(OptionsPanel.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 3876056666363359324L;

	private final JButton baitButton;
	private final JButton expButton;
	private final JButton inputButton;
	private final JButton outputButton;
	private final JButton controlButton;
	private final JButton lysateButton;
	private final JButton quantitativeButton;
	private final JButton externalButton;

	private File cdir;

	private String[] baits;
	private String[] experiments;
	private String[] inputfilelist;
	private String[] controlfilelist;
	private String[] quantitativefilelist;
	private String[] physicalfilelist;
	private String[] geneticfilelist;
	private String[] lysatefilelist;
	private String[] orthogonalfilelist;

	private double[] physicalcoeff;
	private double[] geneticcoeff;

	private File rootdir;
	private File outdir;
	private File logdir;
	private File topodir;
	private File inputdir;
	private File controldir;
	private File lysatedir;
	private File quantitativedir;
	private File physicaldir;
	private File geneticdir;
	private File orthogonaldir;

	private boolean control;
	private boolean lysate;
	private boolean quantitative;
	private boolean physical;
	private boolean genetic;
	private boolean external;

	private List<Experiment> elist; // input file data
	private List<Differential> qlist; // quant file data
	private List<Differential> llist; // lysate file data
	private List<Orthogonal> olist; // orthogonal data
	private final File workingFolder = new File("Z:\\share\\Salva\\data\\Ben's CRISPR AMPK\\yeast dataset");

	public OptionsPanel() {

		control = false;
		lysate = false;
		quantitative = false;
		physical = false;
		genetic = false;

		setLayout(new GridLayout(0, 2));

		baitButton = new JButton("Bait(s)");
		baitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectBaits();
			}
		});

		add(baitButton);

		expButton = new JButton("Experimental Conditions");
		expButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectExperiments();
			}
		});

		add(expButton);

		inputButton = new JButton("Inputs");
		inputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					selectInputs();
				} catch (IOException e1) {
					e1.printStackTrace();
					System.err.println(e);
				}
			}
		});

		inputButton.setEnabled(false);

		add(inputButton);

		outputButton = new JButton("Where to Save");
		outputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectOutput();
			}
		});

		add(outputButton);

		controlButton = new JButton("Controls");
		controlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectControls();
			}
		});

		controlButton.setEnabled(false);
		add(controlButton);

		quantitativeButton = new JButton("Quantitative data");
		quantitativeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectQuantitative();
			}
		});
		quantitativeButton.setEnabled(false);
		add(quantitativeButton);

		lysateButton = new JButton("Lysate");
		lysateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectLysate();
			}
		});
		lysateButton.setEnabled(false);
		add(lysateButton);

		externalButton = new JButton("Orthogonal data");
		externalButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectExternal();
			}
		});
		externalButton.setEnabled(false);
		add(externalButton);

	}

	private void selectBaits() {
		File bait = null;

		if (!P3.test) {
			// TODO check for name of baits, format them as gene name
			MyFileChooser dIO = new MyFileChooser(workingFolder);
			bait = dIO.openFile("Select Bait(s");
		} else {
			bait = new File(P3.baitFile);
		}
		cdir = bait;
		rootdir = bait;

		Baits bts = new Baits(bait);
		bts.run();
		baits = bts.getBaits();
		baitButton.setEnabled(false);

		if (!expButton.isEnabled()) {
			inputButton.setEnabled(true);
		}

	}

	private void selectExperiments() {

		Experiments exps = new Experiments();
		exps.run();
		experiments = exps.getExperiments();
		expButton.setEnabled(false);

		if (!baitButton.isEnabled()) {
			inputButton.setEnabled(true);
		}

	}

	private void selectInputs() throws IOException {

		// ask to the user whether he wants to use quant information or not
		// final int userResponse = JOptionPane.showConfirmDialog(this, "Do you
		// want to use quantitative data?",
		// "Type of data", JOptionPane.YES_NO_CANCEL_OPTION);
		// if (userResponse == JOptionPane.NO_OPTION) {
		File inputlist = null;
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(rootdir);
			inputlist = dIO.openDir("Select Input Files Directory");
		} else {
			inputlist = new File(P3.inputFolder);
		}
		inputfilelist = inputlist.list();
		inputdir = inputlist;

		elist = new ArrayList<Experiment>();
		for (int i = 0; i < baits.length; i++) {
			Experiment e = new Experiment(baits[i]);
			e.addConditions(experiments);
			elist.add(e);
		}

		java.util.Arrays.sort(inputfilelist);

		Inputs inp = new Inputs(inputfilelist, inputdir, baits, experiments, elist);
		inp.setRootDir(rootdir);
		inp.run();

		inputButton.setEnabled(false);

		controlButton.setEnabled(true);
		// salva disabled this:
		quantitativeButton.setEnabled(true);
		externalButton.setEnabled(true);
		// } else if (userResponse == JOptionPane.YES_OPTION) {
		// // ask for other input files
		// MyFileChooser dIO = new MyFileChooser(rootdir);
		// File inputlist = dIO.openDir("Select Input Files Directory");
		// inputfilelist = inputlist.list();
		// inputdir = dIO.getCurDir();
		//
		// elist = new ArrayList<Experiment>();
		// for (int i = 0; i < baits.length; i++) {
		// Experiment e = new Experiment(baits[i]);
		// e.addConditions(experiments);
		// elist.add(e);
		// }
		//
		// java.util.Arrays.sort(inputfilelist);
		//
		// MyFileChooser dIO2 = new MyFileChooser(rootdir);
		// final File filteredQuantFile = dIO2.openFile("Select filtered quant
		// file (from Mathieu's filters)");
		// Set<String> validProteinList = null;
		// if (filteredQuantFile == null) {
		// validProteinList = readFilteredQuantFile(filteredQuantFile);
		// log.info(validProteinList.size() + " proteins read from " +
		// filteredQuantFile.getAbsolutePath());
		// }
		// QuantInputs inp = new QuantInputs(inputfilelist, inputdir, baits,
		// experiments, elist, validProteinList);
		// inp.setRootDir(rootdir);
		// inp.run();
		//
		// inputButton.setEnabled(false);
		//
		// // salva disabled this:
		// // control button seems to be the one using the filters and we want
		// // to disable this if we already have the input filtered by the
		// // Mathieu's filter
		// // controlButton.setEnabled(true);
		//
		// quantitativeButton.setEnabled(true);
		// externalButton.setEnabled(true);
		// } else if (userResponse == JOptionPane.CANCEL_OPTION) {
		// // do nothing
		// }

	}

	/**
	 * Reads the first column of the text file, except the first line which
	 * starts with "Uniprot_Accession"
	 *
	 * @param filteredQuantFile
	 * @return
	 * @throws IOException
	 */
	public static Set<String> readFilteredQuantFile(File textFile) throws IOException {
		int proteinInfoIndex = getProteinInfoColumnIndex(textFile, "Protein_Info");
		int proteinAccessionIndex = getProteinInfoColumnIndex(textFile, "UniProt_Accession");
		return Files.lines(Paths.get(textFile.getAbsolutePath()), Charset.defaultCharset())
				.map(line -> FastaParser.getGeneFromFastaHeader(line.split("\t")[proteinInfoIndex]) != null
						? FastaParser.getGeneFromFastaHeader(line.split("\t")[proteinInfoIndex])
						: line.split("\t")[proteinAccessionIndex])
				.filter(gene -> !gene.isEmpty() && !"UniProt_Accession".equals(gene)).collect(Collectors.toSet());

	}

	private static int getProteinInfoColumnIndex(File textFile, String columnHeader) throws IOException {
		final String firstLine = Files.lines(Paths.get(textFile.getAbsolutePath()), Charset.defaultCharset())
				.findFirst().get();
		final String[] split = firstLine.split("\t");
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals(columnHeader)) {
				return i;
			}
		}
		return -1;
	}

	private void selectOutput() {
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(rootdir);
			outdir = dIO.getOutdir("Select Directory to save output files");
		} else {
			outdir = new File(P3.outputFolder);
		}
		String childdir = outdir + File.separator + "log";

		logdir = new File(childdir);
		logdir.mkdir();

		childdir = outdir + File.separator + "topology";

		topodir = new File(childdir);
		topodir.mkdir();

		outputButton.setEnabled(false);
	}

	private String getCondition(String title, String body, String def) {

		String s = null;

		s = (String) JOptionPane.showInputDialog(null, body, title, JOptionPane.PLAIN_MESSAGE, null, null, def);
		return s;

	}

	private boolean selectControls() {

		MyFileChooser dIO = new MyFileChooser(rootdir);
		File inputlist = dIO.openDir("Select Control Files Directory");

		String condition;

		boolean double_t;
		double lowerbound;
		double upperbound;

		condition = getCondition("Select Control Boundary",
				"Select if a single threshold (Single) or double threshold (Double)", "Single");

		if (condition.equals("Single")) {

			double_t = false;

		} else {
			double_t = true;
		}

		condition = getCondition("Select Upper threshold", "Enter the value for the upper threshold", "2");

		upperbound = Double.parseDouble(condition);

		if (double_t) {

			condition = getCondition("Select Lower threshold", "Enter the value for the lower threshold", "0.5");

			lowerbound = Double.parseDouble(condition);

		} else {
			lowerbound = -1;
		}

		if (inputlist != null) {
			controlfilelist = inputlist.list();
			controldir = dIO.getCurDir();
			control = true;

			java.util.Arrays.sort(controlfilelist);

			Controls con = new Controls(controlfilelist, controldir, baits, experiments, elist);
			con.setFilterBounds(lowerbound, upperbound, double_t);
			con.run();

		} else {
			control = false;
		}

		controlButton.setEnabled(false);
		return true;
	}

	private void selectLysate() {
		File inputlist = null;
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(rootdir);
			inputlist = dIO.openDir("Select Lysate Files Directory");
		} else {
			inputlist = new File(P3.lysateFolder);
		}
		llist = new ArrayList<Differential>();

		if (inputlist != null) {
			lysatefilelist = inputlist.list();
			lysatedir = inputlist;
			lysate = true;

			java.util.Arrays.sort(lysatefilelist);

			for (int i = 0; i < baits.length; i++) {
				Differential d = new Differential(baits[i]);
				llist.add(d);
			}
			Lysates quant = new Lysates(lysatefilelist, lysatedir, baits, llist);
			quant.run();

			lysateButton.setEnabled(false);

		} else {
			lysate = false;
		}

	}

	private void selectQuantitative() {
		File inputlist = null;
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(rootdir);
			inputlist = dIO.openDir("Select Quantitative Files Directory");
		} else {
			inputlist = new File(P3.quantFolder);
		}

		qlist = new ArrayList<Differential>();

		if (inputlist != null) {
			quantitativefilelist = inputlist.list();
			quantitativedir = inputlist;
			quantitative = true;

			java.util.Arrays.sort(quantitativefilelist);

			for (int i = 0; i < baits.length; i++) {
				Differential d = new Differential(baits[i]);
				qlist.add(d);
			}
			Quantitatives quant = new Quantitatives(quantitativefilelist, quantitativedir, baits, qlist);
			quant.run();

			quantitativeButton.setEnabled(false);
			lysateButton.setEnabled(true);

		} else {
			quantitative = false;
		}

	}

	private void selectExternal() {
		File inputlist = null;
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(rootdir);
			inputlist = dIO.openDir("Select Orthogonal Files Directory");
		} else {
			inputlist = new File(P3.orthogonalFolder);
		}
		olist = new ArrayList<Orthogonal>();

		if (inputlist != null) {

			orthogonalfilelist = inputlist.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (// name.startsWith("BIOGRID") &&
					name.endsWith("txt")) {
						return true;
					}
					return false;
				}
			});
			orthogonaldir = inputlist;
			external = true;
			for (int i = 0; i < orthogonalfilelist.length; i++) {
				Orthogonal o = new Orthogonal(orthogonalfilelist[i]);
				olist.add(o);
			}
			Orthogonals orto = new Orthogonals(orthogonalfilelist, orthogonaldir, physical, genetic, olist);
			orto.run();

			externalButton.setEnabled(false);

		} else {
			physical = false;
			genetic = false;
			external = false;
		}

	}

	/*
	 * private double getCoefficient(String title, String defvalue) { double
	 * value = 0; String s = (String) JOptionPane.showInputDialog(null,
	 * "0.1 (Low); 0.5 (Medium); 0.9 (Strong)", title,
	 * JOptionPane.PLAIN_MESSAGE, null, null, defvalue); value =
	 * Double.parseDouble(s); return value; }
	 */

	/**
	 * @return the baits
	 */
	public String[] getBaits() {
		return baits;
	}

	/**
	 * @return the experiments
	 */
	public String[] getExperiments() {
		return experiments;
	}

	/**
	 * @return the outdir
	 */
	public File getOutdir() {
		return outdir;
	}

	/**
	 * @return the logdir
	 */
	public File getLogdir() {
		return logdir;
	}

	/**
	 * @return the topodir
	 */
	public File getTopodir() {
		return topodir;
	}

	/**
	 * @return the lysate
	 */
	public boolean isLysate() {
		return lysate;
	}

	/**
	 * @return the quantitative
	 */
	public boolean isQuantitative() {
		return quantitative;
	}

	/**
	 * @return the phyisical
	 */
	public boolean isPhysical() {

		if (external) {
			for (int i = 0; i < olist.size(); i++) {
				if (olist.get(i).getType().equals(Orthogonal.PHYSICAL)) {
					physical = true;
					return physical;
				}
			}
		}

		return physical;
	}

	/**
	 * @return the genetic
	 */
	public boolean isGenetic() {

		if (external) {
			for (int i = 0; i < olist.size(); i++) {
				if (olist.get(i).getType().equals(Orthogonal.GENETIC)) {
					genetic = true;
					return genetic;
				}
			}
		}

		return genetic;
	}

	/**
	 * @return the elist
	 */
	public List<Experiment> getElist() {
		return elist;
	}

	/**
	 * @return the qlist
	 */
	public List<Differential> getQlist() {
		return qlist;
	}

	/**
	 * @return the llist
	 */
	public List<Differential> getLlist() {
		return llist;
	}

	/**
	 * @return the olist
	 */
	public List<Orthogonal> getOlist() {
		return olist;
	}

	public void reset() {
		baitButton.setEnabled(true);
		expButton.setEnabled(true);
		inputButton.setEnabled(false);
		outputButton.setEnabled(true);
		controlButton.setEnabled(false);
		lysateButton.setEnabled(false);
		quantitativeButton.setEnabled(false);
		externalButton.setEnabled(false);

	}

}
