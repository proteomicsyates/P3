/**
 * diego Nov 19, 2013
 */
package edu.scripps.p3.orthogonal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.io.MyFileChooser;

/**
 * @author diego
 *
 */
public class OrthogonalRecall {

	List<String> baits;
	Hashtable<String, List<String>> reftable;
	File logdir;
	String phase;

	List<String> phases;
	Hashtable<String, ScoreTable> scoretables;
	Hashtable<String, PreyStat> preystats;

	/**
	 * @param baits
	 * @param logdir
	 */
	public OrthogonalRecall(String[] baits, File logdir) {

		this.baits = new ArrayList<String>();
		reftable = new Hashtable<String, List<String>>();

		phases = new ArrayList<String>();
		scoretables = new Hashtable<String, ScoreTable>();

		this.logdir = logdir;

		reftable.put("pool", new ArrayList<String>());

		for (int i = 0; i < baits.length; i++) {

			if (baits[i].contains("-")) {

				String[] elements = baits[i].split("-");

				for (String element : elements) {

					if (!this.baits.contains(element)) {
						this.baits.add(element);

						reftable.put(element, new ArrayList<String>());

					}

				}

			} else {

				if (!this.baits.contains(baits[i])) {
					this.baits.add(baits[i]);

					reftable.put(baits[i], new ArrayList<String>());

				}
			}

		}

	}

	public void writeLog() {

		StringBuffer log = new StringBuffer();

		log.append("\t\tRaw\t\t\t\t\tCorrelation\t\t\t\t\tCluster\t\t\t\t\tQuant\t\t\t\t\tConfidence\n");
		log.append("Bait\t\t");
		log.append("Know&Called\tKnow&MisCalled\tUnknown\tFilter\t\t");
		log.append("Know&Called\tKnow&MisCalled\tUnknown\tFilter\t\t");
		log.append("Know&Called\tKnow&MisCalled\tUnknown\tFilter\t\t");
		log.append("Know&Called\tKnow&MisCalled\tUnknown\tFilter\t\t");
		log.append("Know&Called\tKnow&MisCalled\tUnknown\tFilter\t\t");
		log.append("\n");

		Enumeration<String> enumkeys = scoretables.keys();

		while (enumkeys.hasMoreElements()) {

			String key = enumkeys.nextElement();

			log.append(key + "\t\t");

			for (String phase : phases) {

				log.append(scoretables.get(key).getKnownAndCalled(phase) + "\t");
				log.append(scoretables.get(key).getKnownMisCalled(phase) + "\t");
				log.append(scoretables.get(key).getUnKnown(phase) + "\t");
				log.append(scoretables.get(key).getLost(phase) + "\t");
				log.append("\t");

			}

			log.append("\n");

		}

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "RecallLog");

	}

	public void setPhase(String phase) {
		this.phase = phase;
		phases.add(phase);
	}

	/**
	 * @param elist
	 */
	public void rawfilter(List<Experiment> elist) {

		String bait;

		List<String> bnames;

		for (Experiment exp : elist) {

			bait = exp.getName();

			bnames = new ArrayList<String>();

			if (bait.contains("-")) {

				String[] elements = bait.split("-");

				for (String element : elements) {
					bnames.add(element);
				}

				bnames.add("pool");

			} else {
				bnames.add(bait);
			}

			for (int i = 0; i < exp.getNumberofConditions(); i++) {

				List<String> pnames = exp.getCondition(i).getPnames();

				for (String bname : bnames) {

					List<String> refnames = reftable.get(bname);

					for (int j = 0; j < refnames.size(); j++) {

						if (!pnames.contains(refnames.get(j))) {
							refnames.remove(j);
							j--;
						}

					}

					String st_name = bname + "_" + exp.getCondition(i).getName();

					ScoreTable st = new ScoreTable(st_name);
					st.addPhase(phase);

					scoretables.put(st_name, st);

					for (String refname : refnames) {
						st.addProtein(refname);
						st.addKnownAndCalled(phase);
					}

				}

			}

		}

		reftable.clear();
		reftable = null;

	}

	/**
	 * @param olist
	 */
	public void setOrto(List<Orthogonal> olist) {

		String bait;
		String prey;

		for (Orthogonal orto : olist) {
			if (orto.getType().equals(Orthogonal.PHYSICAL) && orto.getCoefficient() > 0.4) {
				List<String> elements = orto.getElements();

				for (String element : elements) {

					bait = element.split("_")[0];
					prey = element.split("_")[1];

					if (reftable.containsKey(bait)) {

						if (!reftable.get(bait).contains(prey)) {
							reftable.get(bait).add(prey);

							if (!reftable.get("pool").contains(prey)) {
								reftable.get("pool").add(prey);
							}

						}

					}

					if (reftable.containsKey(prey)) {
						if (!reftable.get(prey).contains(bait)) {
							reftable.get(prey).add(bait);

							if (!reftable.get("pool").contains(bait)) {
								reftable.get("pool").add(bait);
							}

						}
					}

				}
			}

		}

	}

	/**
	 * @param interactomes
	 */
	public void calculate(List<List<Interactome>> interactomes) {

		String bait;
		String condition;
		int overlap;

		List<String> bnames;
		List<String> pooled = null;

		boolean pool = false;

		for (int i = 0; i < interactomes.size(); i++) {

			for (int j = 0; j < interactomes.get(i).size(); j++) {

				final Interactome interactome = interactomes.get(i).get(j);
				bait = interactome.getBait_name();
				condition = interactome.getConditionName();

				bnames = new ArrayList<String>();

				if (bait.contains("-")) {

					String[] elements = bait.split("-");

					for (String element : elements) {
						bnames.add(element);
					}

					bnames.add("pool");
					pooled = new ArrayList<String>();
					pool = true;

				} else {
					bnames.add(bait);
				}

				for (String bname : bnames) {

					List<String> pnames;

					if (bname.equals("pool")) {
						pnames = pooled;
						pool = false;
					} else {
						pnames = interactome.getNetwork(bname).getInteractorsNames();
					}

					if (pool) {
						for (String pname : pnames) {
							if (!pooled.contains(pname)) {
								pooled.add(pname);
							}
						}
					}

					calculate(bname, condition, pnames);

				}

				findMis(bnames, condition);

			}

		}

	}

	/**
	 * For each individual bait, it removes all the lost interactors that are
	 * present as interactor of another bait, adding them to the set of "known
	 * and misscalled" interactors.
	 * 
	 * @param bnames
	 * @param condition
	 */
	private void findMis(List<String> bnames, String condition) {

		for (String bname1 : bnames) {

			final String bait1ConditionKey = bname1 + "_" + condition;
			ScoreTable scoreTable1 = scoretables.get(bait1ConditionKey);

			List<String> lost = scoreTable1.getLosts(phase);

			for (String bname2 : bnames) {

				for (int i = 0; i < lost.size(); i++) {

					final String bait2ConditionKey = bname2 + "_" + condition;
					final String lostInteractor = lost.get(i);
					final ScoreTable scoreTable2 = scoretables.get(bait2ConditionKey);
					if (scoreTable2.contains(lostInteractor)) {

						scoreTable1.addKnownMiscalled(phase);
						// lost.remove(i);
						// i--;
						lost.remove(lostInteractor);

					}

				}

			}

		}

	}

	private String stat(String bname, String exp, List<String> pnames) {

		StringBuilder blog = new StringBuilder();
		String name = bname + "_" + exp;
		ScoreTable st = scoretables.get(name);
		st.addPhase(phase);

		blog.append("B\t");
		blog.append(name);
		blog.append("\nK\t");

		StringBuilder known = new StringBuilder();
		StringBuilder novel = new StringBuilder();

		for (String pname : pnames) {
			if (st.ProtInRef(pname)) {

				known.append(pname);
				known.append("\t");

			} else {

				novel.append(pname);
				novel.append("\t");

			}

		}

		known.append("\n");
		novel.append("\n");

		blog.append(known.toString());
		blog.append("U\t");
		blog.append(novel.toString());

		return blog.toString();

	}

	/**
	 * @param bname
	 * @param exp
	 * @param p3Interactors
	 */
	private void calculate(String bname, String exp, List<String> p3Interactors) {

		String name = bname + "_" + exp;
		// score table of known interactors of bait name in experiment exp
		ScoreTable st = scoretables.get(name);
		st.addPhase(phase);

		int prots;

		List<String> unknowns = new ArrayList<String>();

		// add lost

		for (String p3Interactor : p3Interactors) {
			if (st.ProtInRef(p3Interactor)) {
				st.addKnownAndCalled(phase);
			} else {
				st.addUnknown(phase);
				st.addUnkProt(p3Interactor);
			}

		}

		for (String pname : st.getRef()) {
			if (!p3Interactors.contains(pname)) {
				st.addLost(pname);
			}
		}

		prots = st.getKnownAndCalled(phase);// + st.getUnKnown(phase);

		String oldphase = phases.get(phases.size() - 2);

		int difference = st.getKnownAndCalled(oldphase) - prots;// +
																// st.getKnownMisCalled(oldphase)
																// +
																// st.getUnKnown(oldphase)
																// - prots;

		st.setLost(difference);

	}

	private class ScoreTable {

		String name_exp;
		Hashtable<String, int[]> table;
		List<String> reference;
		List<String> lost;
		List<String> unknowns;

		public ScoreTable(String name_exp) {
			this.name_exp = name_exp;
			table = new Hashtable<String, int[]>();
			reference = new ArrayList<String>();
		}

		/**
		 * @param phase
		 * @return
		 */
		public List<String> getUnknowns(String phase) {
			return unknowns;
		}

		public List<String> getLosts(String phase) {
			return lost;
		}

		public void addLost(String name) {
			lost.add(name);
		}

		/**
		 * @param difference
		 */
		public void setLost(int difference) {
			table.get(phase)[3] = difference;

		}

		public int getLost(String phase) {
			return table.get(phase)[3];
		}

		/**
		 * Increases the number of proteins that are known and were called as an
		 * interactor in a certain phase of the algorithm
		 * 
		 * @param phase
		 */
		public void addKnownAndCalled(String phase) {

			table.get(phase)[0]++;

		}

		/**
		 * Adds the protein name to the list of proteins that were unknown to be
		 * an interactor
		 * 
		 * @param proteinName
		 */
		public void addUnkProt(String proteinName) {
			unknowns.add(proteinName);
		}

		/**
		 * Increases the number of proteins that are known and misscalled in a
		 * certain phase of the algorithm
		 * 
		 * @param phase
		 */
		public void addKnownMiscalled(String phase) {

			table.get(phase)[1]++;
		}

		/**
		 * Increases the number of proteins that are unknown in a certain phase
		 * of the algorithm
		 * 
		 * @param phase
		 */
		public void addUnknown(String phase) {

			table.get(phase)[2]++;
		}

		public void reduceUnknown(String phase) {
			table.get(phase)[2]--;
		}

		public void addPhase(String phase) {

			int[] values = new int[4];
			table.put(phase, values);
			lost = new ArrayList<String>();
			unknowns = new ArrayList<String>();

		}

		public void addProtein(String prot) {
			reference.add(prot);
		}

		public int getKnownAndCalled(String phase) {
			return table.get(phase)[0];
		}

		public int getKnownMisCalled(String phase) {
			return table.get(phase)[1];
		}

		public int getUnKnown(String phase) {
			return table.get(phase)[2];
		}

		public int getRefTableSize() {
			return reference.size();
		}

		public boolean ProtInRef(String prot) {
			if (reference.contains(prot)) {
				return true;
			} else {
				return false;
			}
		}

		public List<String> getRef() {
			return reference;
		}

		public boolean contains(String name) {

			if (unknowns.contains(name)) {
				return true;
			}

			if (reference.contains(name)) {

				if (!lost.contains(name)) {
					return true;
				}

			}

			return false;

		}

	}

	/**
	 * @param interactomes
	 */
	public void finalStatistics(List<List<Interactome>> interactomes) {

		String bait;
		String exp;

		List<String> bnames;
		List<String> pooled = null;

		boolean pool = false;

		StringBuilder log = new StringBuilder();

		for (int i = 0; i < interactomes.size(); i++) {

			for (int j = 0; j < interactomes.get(i).size(); j++) {

				final Interactome interactome = interactomes.get(i).get(j);
				bait = interactome.getBait_name();
				exp = interactome.getConditionName();

				bnames = new ArrayList<String>();

				if (bait.contains("-")) {

					String[] elements = bait.split("-");

					for (String element : elements) {
						bnames.add(element);
					}

					bnames.add("pool");
					pooled = new ArrayList<String>();
					pool = true;

				} else {
					bnames.add(bait);
				}

				for (String bname : bnames) {

					List<String> pnames;

					if (bname.equals("pool")) {
						pnames = pooled;
						pool = false;
					} else {
						pnames = interactome.getNetwork(bname).getInteractorsNames();
					}

					if (pool) {
						for (String pname : pnames) {
							if (!pooled.contains(pname)) {
								pooled.add(pname);
							}
						}
					}

					log.append(stat(bname, exp, pnames));

				}

			}

		}

		MyFileChooser dIO = new MyFileChooser();
		dIO.writeLog(logdir, log, "P3FinalStat");

	}

	/**
	 * @param elist
	 */
	public void rawstats(List<Experiment> elist) {
		String bait;

		preystats = new Hashtable<String, PreyStat>();

		String conditionName;

		List<String> bnames = null;

		Hashtable<String, List<String>> preysByCondition = new Hashtable<String, List<String>>();

		for (Experiment exp : elist) {

			bait = exp.getName();

			bnames = new ArrayList<String>();

			if (bait.contains("-")) {

				String[] elements = bait.split("-");

				for (String element : elements) {
					bnames.add(element);
				}

				bnames.add("pool");

			} else {
				bnames.add(bait);
			}

			for (int i = 0; i < exp.getNumberofConditions(); i++) {

				final Condition condition = exp.getCondition(i);
				List<String> pnames = condition.getPnames();

				conditionName = condition.getName();

				List<String> preyslist = new ArrayList<String>();
				preysByCondition.put(conditionName, preyslist);

				for (String pname : pnames) {

					final String proteinConditionKey = pname + "_" + conditionName;
					PreyStat ps = new PreyStat(proteinConditionKey);
					preystats.put(proteinConditionKey, ps);

					preysByCondition.get(conditionName).add(proteinConditionKey);

				}

				for (String bname : bnames) {

					String baitConditionKey = bname + "_" + conditionName;

					ScoreTable st = scoretables.get(baitConditionKey);

					List<String> preys = st.getRef();

					for (String prey : preys) {

						final String preyConditionKey = prey + "_" + conditionName;
						preystats.get(preyConditionKey).addBait(bname);

					}
				}

			}
		}

		MyFileChooser dIO = new MyFileChooser();

		StringBuffer log = new StringBuffer();

		Enumeration<String> conditionNames = preysByCondition.keys();

		while (conditionNames.hasMoreElements()) {

			conditionName = conditionNames.nextElement();

			log.append(conditionName.toUpperCase() + "\n");
			log.append("Prey\t");

			for (String bname : bnames) {
				log.append(bname + "\t");
			}

			log.append("\n");

			List<String> preys = preysByCondition.get(conditionName);
			Collections.sort(preys);

			for (String prey : preys) {

				log.append(prey + "\t");

				for (String bname : bnames) {

					if (preystats.get(prey).isBait(bname)) {
						log.append("Known\t");
					} else {
						log.append("\t");
					}

				}

				log.append("\n");

			}

			log.append("\n\n\n");

		}

		dIO.writeLog(logdir, log, "RawSharedLog");

	}

	private class PreyStat {

		String pname;
		List<String> baits;
		Hashtable<String, Double> interactions;

		public PreyStat(String pname) {
			this.pname = pname;
			baits = new ArrayList<String>();
			interactions = new Hashtable<String, Double>();
		}

		public void addBait(String bait) {
			baits.add(bait);
		}

		public void addInteraction(String bait, double val) {
			interactions.put(bait, val);
		}

		public boolean isBait(String bait) {
			if (baits.contains(bait)) {
				return true;
			} else {
				return false;
			}
		}

		public double getConfidence(String bait) {

			if (interactions.containsKey(bait)) {
				return interactions.get(bait);
			} else {
				return -1;
			}

		}

	}

}