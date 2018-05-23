/**
 * diego
 * Aug 13, 2014
 */
package edu.scripps.p3.utilities.optimizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFileChooser;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;

/**
 * tool to calculate the optimal coefficient of P3 It uses an output file and
 * then it test all the possible combinations to find the optimal It uses a
 * brute force approach because the search space is small For larger search
 * space something fancier like GA should be used
 * 
 * @author diego
 * 
 */
public class P3OptimalCoefficientFinder {

	Hashtable<String, Bait> baittable;
	File curdir;
	private double raw_range;

	public void run() {

		baittable = new Hashtable<String, Bait>();

		File inp = openFile("Select confidence file");
		parseInp(inp);
		File qfile = openFile("Select quant file");
		parseQuant(qfile);
		File report = openFile("Select report file");
		parseReport(report);

		Configuration conf = new DefaultConfiguration();

		FitnessFunction myFunc = new CoefficientFitness(baittable);
		try {
			conf.setFitnessFunction(myFunc);
			Gene[] genes = new Gene[7];

			genes[0] = new DoubleGene(conf, 0, 1);
			genes[1] = new DoubleGene(conf, 0, 1);
			genes[2] = new DoubleGene(conf, 0, 1);
			genes[3] = new DoubleGene(conf, 0, 0.6);
			genes[4] = new DoubleGene(conf, 0, 0.3);
			genes[5] = new DoubleGene(conf, 0, 1);
			genes[6] = new DoubleGene(conf, 0, 1);

			Chromosome chrome = new Chromosome(conf, genes);

			conf.setSampleChromosome(chrome);
			conf.setPopulationSize(3000);
			Genotype population = Genotype.randomInitialGenotype(conf);

			IChromosome bestSolutionSoFar = null;

			for (int i = 0; i < 100; i++) {
				population.evolve();
				bestSolutionSoFar = population.getFittestChromosome();

				System.out.println("Generation:\t" + i);
				System.out.println("Correlation Weight:\t" + bestSolutionSoFar.getGene(0).getAllele());
				System.out.println("Cluster Weight:\t" + bestSolutionSoFar.getGene(1).getAllele());
				System.out.println("Quant Weight:\t" + bestSolutionSoFar.getGene(2).getAllele());
				System.out.println("Phy Weight:\t" + bestSolutionSoFar.getGene(3).getAllele());
				System.out.println("Gen Weight:\t" + bestSolutionSoFar.getGene(4).getAllele());
				System.out.println("Confidence Thr:\t" + bestSolutionSoFar.getGene(5).getAllele());
				System.out.println("Confidence Onto Thr:\t" + bestSolutionSoFar.getGene(6).getAllele());

			}

			System.out.println("Optimal configuration:");
			System.out.println("Correlation Weight:\t" + bestSolutionSoFar.getGene(0).getAllele());
			System.out.println("Cluster Weight:\t" + bestSolutionSoFar.getGene(1).getAllele());
			System.out.println("Quant Weight:\t" + bestSolutionSoFar.getGene(2).getAllele());
			System.out.println("Phy Weight:\t" + bestSolutionSoFar.getGene(3).getAllele());
			System.out.println("Gen Weight:\t" + bestSolutionSoFar.getGene(4).getAllele());
			System.out.println("Confidence Thr:\t" + bestSolutionSoFar.getGene(5).getAllele());
			System.out.println("Confidence Onto Thr:\t" + bestSolutionSoFar.getGene(6).getAllele());

		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void parseQuant(File inp) {

		FileInputStream fis;

		double min_ratio = Double.POSITIVE_INFINITY;
		double max_ratio = Double.NEGATIVE_INFINITY;

		try {
			fis = new FileInputStream(inp);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String[] elements;
			String name;
			double qratio;

			Hashtable<String, Double> qtable = new Hashtable<String, Double>();

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("Condition")) {
					continue;
				}

				elements = dataline.split("\t");
				name = elements[0];
				qratio = Double.parseDouble(elements[1]);

				if (qratio > max_ratio) {
					max_ratio = qratio;
				}

				if (qratio < min_ratio) {
					min_ratio = qratio;
				}

				qtable.put(name, qratio);

			}

			this.raw_range = max_ratio - min_ratio;

			Enumeration<String> enumkeys = baittable.keys();
			String key;
			while (enumkeys.hasMoreElements()) {

				key = enumkeys.nextElement();

				baittable.get(key).setRange(this.raw_range);

				for (Prey p : baittable.get(key).getPreyList()) {

					if (qtable.containsKey(p.getName())) {
						p.setQratio(qtable.get(p.getName()));
					}

				}

			}

		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseReport(File inp) {

		FileInputStream fis;

		try {
			fis = new FileInputStream(inp);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String[] elements;
			String bait;
			int known;
			boolean go = false;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("Bait")) {
					go = true;
					continue;
				}

				if (go) {

					elements = dataline.split("\t");
					bait = elements[0].trim();
					known = Integer.parseInt(elements[2]);

					if (baittable.containsKey(bait)) {
						baittable.get(bait).setKnown(known);
					}

				}

			}

		} catch (IOException e) {
			System.err.println("unable to read file");
		}

	}

	private void parseInp(File inp) {

		FileInputStream fis;

		try {
			fis = new FileInputStream(inp);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

			String dataline;
			String exp = null;
			String bait = null;
			Bait b = null;

			String name;
			double corr;
			double clu;
			double quant;
			double phy;
			double gen;

			boolean go = false;
			String baitkey = null;

			while ((dataline = dis.readLine()) != null) {

				if (dataline.startsWith("Working on bait:")) {

					bait = dataline.split("\t")[1];

					b = new Bait(bait, exp);

					baitkey = bait + "_" + exp;

					baittable.put(baitkey, b);

					continue;

				}

				if (dataline.startsWith("Working on")) {

					exp = dataline.split("\t")[1];
					go = true;

					continue;

				}

				if (dataline.startsWith("Protein")) {
					continue;
				}

				if (go) {

					String[] elements = dataline.split("\t");

					name = elements[0];
					corr = Double.parseDouble(elements[2]);
					clu = Double.parseDouble(elements[3]);
					quant = Double.parseDouble(elements[4]);
					phy = Double.parseDouble(elements[5]);
					gen = Double.parseDouble(elements[6]);

					Prey p = new Prey(name, corr, clu, quant, phy, gen);
					baittable.get(baitkey).addPrey(p);

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

		P3OptimalCoefficientFinder p3ocf = new P3OptimalCoefficientFinder();
		p3ocf.run();

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

}
