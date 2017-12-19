/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.cluterer.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.parsers.inputs.utilities.Protein;

/**
 * @author diego
 * 
 */
public class FeaturesCalculator {

	private List<Experiment> elist;
	private List<List<Interactome>> interactomes;
	private boolean quantitative;
	private List<Differential> qlist;

	private Protein currentBait;

	private ProgressMonitor progressMonitor;
	private static int progress = 0;

	/**
	 * @param elist
	 * @param interactomes
	 * @param quantitative
	 * @param qlist
	 * @param logdir
	 */
	public FeaturesCalculator(List<Experiment> elist, List<List<Interactome>> interactomes, boolean quantitative,
			List<Differential> qlist) {

		this.elist = elist;
		this.interactomes = interactomes;
		this.quantitative = quantitative;
		if (quantitative) {
			this.qlist = qlist;
		}

	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

	private int getFullSize() {
		int size = 0;

		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {
				final Interactome interactome = interactomes.get(k).get(i);
				size += interactome.getProteinsHavingANetwork().size();
			}
		}

		return size;
	}

	/**
	 * 
	 */
	public void run() {

		int fullsize = getFullSize();

		progressMonitor = new ProgressMonitor(null, "Calculating Features", "Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);

		for (int experimentIndex = 0; experimentIndex < interactomes.size(); experimentIndex++) {
			final List<Interactome> experimentInteractomes = interactomes.get(experimentIndex);
			for (int conditionIndex = 0; conditionIndex < experimentInteractomes.size(); conditionIndex++) {
				final Interactome experimentConditionInteractome = experimentInteractomes.get(conditionIndex);
				List<String> baits = experimentConditionInteractome.getProteinsHavingANetwork();

				for (int baitIndex = 0; baitIndex < baits.size(); baitIndex++) {

					// if
					// (pnames.get(j).equals(interactomes.get(k).get(i).getBait_name()))
					// {

					final String bait = baits.get(baitIndex);
					final String baitConditionKey = bait + "_" + experimentConditionInteractome.getConditionName();
					Features feat = new Features(baitConditionKey);

					List<Protein> plist = getProteinList(conditionIndex, baitIndex, experimentIndex);

					Hashtable<String, List<Double>> featuresPerPrey = new Hashtable<String, List<Double>>();
					List<String> preysWithFeatures = new ArrayList<String>();
					calculatefeat(plist, experimentIndex, featuresPerPrey, preysWithFeatures);
					feat.setFeaturesPerPreyData(preysWithFeatures, featuresPerPrey);

					experimentConditionInteractome.getNetwork(bait).setFeatures(feat);

					// } else {
					// interactomes.get(k).get(i).getSystem().remove(pnames.get(j));
					// pnames.remove(j);
					// j--;
					// }

					/*
					 * element = new Hashtable<String, List<Double>>(); fplist =
					 * new ArrayList<String>();
					 * 
					 * Features feat = new Features(pnames.get(j) + "_" +
					 * interactomes.get(k).get(i).getExp_name(), element,
					 * fplist);
					 * 
					 * List<Protein> plist = getProteinList(i, j, k);
					 * 
					 * calculatefeat(plist,k);
					 * 
					 * interactomes.get(k).get(i).getNetwork(pnames.get(j)).
					 * setFeatures(feat);
					 */
					progress++;
					progressMonitor.setProgress(progress);

				}
			}
		}

		progressMonitor.close();

	}

	/**
	 * Gets all the proteins that interact with the bait that are present in the
	 * experiment+condition.<br>
	 * It also sets the currentBait ( {@link Protein} ) object
	 * 
	 * @param cond_id
	 * @param prot_id
	 * @param bait_id
	 * @return
	 */
	private List<Protein> getProteinList(int cond_id, int prot_id, int bait_id) {

		List<Protein> plist = new ArrayList<Protein>();

		final Interactome interactome = interactomes.get(bait_id).get(cond_id);
		String bait = interactome.getProteinsHavingANetwork().get(prot_id);

		List<String> pnames = interactome.getNetwork(bait).getInteractorsNames();

		final Condition condition = elist.get(bait_id).getCondition(cond_id);
		for (String pname : condition.getPnames()) {

			if (pnames.contains(pname)) {
				plist.add(condition.getProtein(pname));
			}

			if (pname.equals(bait)) {
				currentBait = condition.getProtein(pname);

				progressMonitor.setNote("Calculating Features for " + pname + " in " + elist.get(bait_id).getName()
						+ " " + condition.getName());

			}

		}

		return plist;
	}

	private void calculatefeat(List<Protein> preyList, int bait_id, Hashtable<String, List<Double>> featuresPerPrey,
			List<String> preysWithFeatures) {

		Protein bait = currentBait;

		double p_pepcount;
		double p_spcount;
		double p_mw;
		double p_lenght;
		double p_quant;
		double p_coverage;

		double b_pepcount = bait.getPcount();
		double b_spcount = bait.getScount();
		double b_mw = bait.getMw();
		double b_lenght = bait.getLength();
		double b_coverage = bait.getCoverage();
		double b_quant;

		if (quantitative) {
			b_quant = qlist.get(bait_id).getDiffValue(bait.getName());
			if (b_quant == -1) {
				b_quant = 1;
			}
		} else {
			b_quant = 1;
		}

		double pep_ratio;
		double sp_ratio;
		double mw_ratio;
		double len_ratio;
		double empai_ratio;
		double density_ratio;
		double apv_ratio;
		double avgSpPep_ratio;

		double quant_ratio;
		double qdensity_ratio;

		for (int j = 0; j < preyList.size(); j++) {

			final Protein prey = preyList.get(j);
			String preyName = prey.getName();
			p_pepcount = prey.getPcount();
			p_spcount = prey.getScount();
			p_mw = prey.getMw();
			p_lenght = prey.getLength();
			p_coverage = prey.getCoverage();

			if (quantitative) {
				p_quant = qlist.get(bait_id).getDiffValue(preyName);
				if (p_quant == -1) {
					p_quant = 1;
				}
			} else {
				p_quant = 1;
			}

			List<Double> feats = new ArrayList<Double>();

			pep_ratio = p_pepcount / b_pepcount;

			sp_ratio = p_spcount / b_spcount;

			mw_ratio = p_mw / b_mw;

			len_ratio = p_lenght / b_lenght;

			empai_ratio = p_coverage / b_coverage;

			density_ratio = sp_ratio * len_ratio * (b_mw / p_mw);

			apv_ratio = sp_ratio * (b_coverage / p_coverage);

			avgSpPep_ratio = sp_ratio * (b_pepcount / p_pepcount);

			if (quantitative) {

				quant_ratio = p_quant / b_quant;

				qdensity_ratio = len_ratio * quant_ratio * (b_mw / p_mw);

				pep_ratio = getLogLogistic(pep_ratio);
				sp_ratio = getLogLogistic(sp_ratio);
				mw_ratio = getLogLogistic(mw_ratio);
				len_ratio = getLogLogistic(len_ratio);
				empai_ratio = getLogLogistic(empai_ratio);
				density_ratio = getLogLogistic(density_ratio);
				apv_ratio = getLogLogistic(apv_ratio);
				avgSpPep_ratio = getLogLogistic(avgSpPep_ratio);

				quant_ratio = getLogLogistic(quant_ratio);
				qdensity_ratio = getLogLogistic(qdensity_ratio);

				if (Double.isNaN(quant_ratio)) {
					quant_ratio = 1;
				}
				if (Double.isNaN(qdensity_ratio)) {
					qdensity_ratio = 1;
				}

				feats.add(pep_ratio);
				feats.add(sp_ratio);
				feats.add(mw_ratio);
				feats.add(len_ratio);
				feats.add(empai_ratio);
				feats.add(density_ratio);
				feats.add(apv_ratio);
				feats.add(avgSpPep_ratio);

				feats.add(quant_ratio);
				feats.add(qdensity_ratio);

			} else {

				pep_ratio = getLogLogistic(pep_ratio);
				sp_ratio = getLogLogistic(sp_ratio);
				mw_ratio = getLogLogistic(mw_ratio);
				len_ratio = getLogLogistic(len_ratio);
				empai_ratio = getLogLogistic(empai_ratio);
				density_ratio = getLogLogistic(density_ratio);
				apv_ratio = getLogLogistic(apv_ratio);
				avgSpPep_ratio = getLogLogistic(avgSpPep_ratio);

				feats.add(pep_ratio);
				feats.add(sp_ratio);
				feats.add(mw_ratio);
				feats.add(len_ratio);
				feats.add(empai_ratio);
				feats.add(density_ratio);
				feats.add(apv_ratio);
				feats.add(avgSpPep_ratio);

			}

			if (featuresPerPrey.containsKey(preyName)) {

				List<Double> oldfeat = featuresPerPrey.get(preyName);
				for (int k = 0; k < feats.size(); k++) {
					oldfeat.add(feats.get(k));
				}
				featuresPerPrey.put(preyName, oldfeat);

			} else {
				featuresPerPrey.put(preyName, feats);
				preysWithFeatures.add(preyName);
			}

		}

	}

	private double getLogLogistic(double s) {

		double val = Math.log(s);
		val = 1.0 / (1.0 + Math.exp(-val));
		return val;

	}

}
