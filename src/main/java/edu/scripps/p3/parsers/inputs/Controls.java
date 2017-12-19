/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JComboBox;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.parsers.inputs.utilities.Protein;

/**
 * @author diego
 * 
 */
public class Controls extends Inputs {

	private double lbound;
	private double hbound;
	private boolean twotails;
	private boolean method;

	/**
	 * @param files
	 * @param inputdir
	 * @param baits
	 * @param exp
	 * @param elist
	 */
	public Controls(String[] files, File inputdir, String[] baits, String[] exp, List<Experiment> elist) {
		super(files, inputdir, baits, exp, elist);

	}

	@Override
	protected void parseFiles() {

		File f;
		int baitindex;
		int expindex;

		boolean allbait = false;
		boolean allexp = false;

		for (int i = 0; i < files.length; i++) {

			f = new File(inputdir, files[i]);

			baitindex = assignments.get(i)[0];
			expindex = assignments.get(i)[1];

			if (baitindex == 0) {
				allbait = true;
			} else {
				baitindex--;
			}
			if (expindex == 0) {
				allexp = true;
			} else {
				expindex--;
			}

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

						Protein p = getProtein(dataline);

						if (p.getScount() > 0) {
							if (allbait && allexp) {
								fullFilter(p);
							} else {
								if (allbait) {
									fullbaitFilter(p, expindex);
								} else {
									if (allexp) {
										fullexpFilter(p, baitindex);
									} else {
										singleFilter(p, expindex, baitindex);
									}
								}
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

	}

	public void setFilterBounds(double lbound, double hbound, boolean twotails) {
		this.lbound = lbound;
		this.hbound = hbound;
		this.twotails = twotails;
	}

	public void setFilterMethod(boolean method) {
		if (method) {
			this.method = true;
		} else {
			this.method = false;
		}
	}

	private void fullFilter(Protein p) {

		for (int i = 0; i < elist.size(); i++) {
			fullexpFilter(p, i);
		}

	}

	private void fullbaitFilter(Protein p, int expindex) {

		for (int i = 0; i < elist.size(); i++) {
			singleFilter(p, expindex, i);
		}

	}

	private void fullexpFilter(Protein p, int baitindex) {

		for (int i = 0; i < elist.get(baitindex).getNumberofConditions(); i++) {
			singleFilter(p, i, baitindex);
		}

	}

	private void singleFilter(Protein p, int expindex, int baitindex) {

		double controlSC = p.getScount();
		double expSC;
		double ratio;

		final Experiment experiment = elist.get(baitindex);
		final Condition condition = experiment.getCondition(expindex);
		if (condition.proteinInTable(p.getName())) {

			final Protein ipProtein = condition.getProtein(p.getName());
			expSC = ipProtein.getScount();

			ratio = expSC / controlSC;

			if (twotails) {

				if (ratio > lbound && ratio < hbound) {
					condition.removeProtein(p.getName());
				}

			} else {

				if (ratio < hbound) {

					condition.removeProtein(p.getName());

				}

			}

		}

	}

	@Override
	protected void setTitle() {
		title = "Controls Resolver";
	}

	@Override
	protected JComboBox<String> getBox(String[] elements) {
		JComboBox<String> box = new JComboBox<String>();

		box.addItem("All");

		for (String element : elements) {
			box.addItem(element);
		}
		box.setSelectedIndex(0);
		return box;
	}

}
