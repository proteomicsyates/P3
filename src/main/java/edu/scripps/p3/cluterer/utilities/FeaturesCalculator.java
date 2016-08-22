/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.cluterer.utilities;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

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
	
	private Hashtable<String, List<Double>> element;
	private List<String> fplist;

	private Protein currentBait;
	
	private ProgressMonitor progressMonitor;
	private static int progress=0;

	/**
	 * @param elist
	 * @param interactomes
	 * @param quantitative
	 * @param qlist
	 * @param logdir
	 */
	public FeaturesCalculator(List<Experiment> elist,
			List<List<Interactome>> interactomes, boolean quantitative,
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
				size += interactomes.get(k).get(i).getNetlist().size();
			}
		}
				
		return size;
	}
	
	/**
	 * 
	 */
	public void run() {

		int fullsize = getFullSize();
		
		progressMonitor = new ProgressMonitor(null,"Calculating Features","Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);
		
		for (int k = 0; k < interactomes.size(); k++) {
			for (int i = 0; i < interactomes.get(k).size(); i++) {
				List<String> pnames = interactomes.get(k).get(i).getNetlist();
				
				
				
				for (int j = 0; j < pnames.size(); j++) {
					
	//				if (pnames.get(j).equals(interactomes.get(k).get(i).getBait_name())) {
						
						element = new Hashtable<String, List<Double>>();
						fplist = new ArrayList<String>();

						Features feat = new Features(pnames.get(j) + "_"
								+ interactomes.get(k).get(i).getExp_name(),
								element, fplist);

						List<Protein> plist = getProteinList(i, j, k);

						calculatefeat(plist,k);
						
						interactomes.get(k).get(i).getNetwork(pnames.get(j)).setFeatures(feat);

						
	//				} else {
	//					interactomes.get(k).get(i).getSystem().remove(pnames.get(j));
	//					pnames.remove(j);
	//					j--;
	//				}
					
/*					element = new Hashtable<String, List<Double>>();
					fplist = new ArrayList<String>();

					Features feat = new Features(pnames.get(j) + "_"
							+ interactomes.get(k).get(i).getExp_name(),
							element, fplist);

					List<Protein> plist = getProteinList(i, j, k);

					calculatefeat(plist,k);
					
					interactomes.get(k).get(i).getNetwork(pnames.get(j)).setFeatures(feat);
*/					
					progress++;
					progressMonitor.setProgress(progress);

				}
			}
		}
		
		progressMonitor.close();

	}

	private List<Protein> getProteinList(int cond_id, int prot_id, int bait_id) {

		List<Protein> plist = new ArrayList<Protein>();

		String bait = interactomes.get(bait_id).get(cond_id).getNetlist().get(prot_id);

		List<String> pnames = interactomes.get(bait_id).get(cond_id).getNetwork(bait)
				.getInteraction_names();

		for (String pname : elist.get(bait_id).getCondition(cond_id).getPnames()) {

			if (pnames.contains(pname)) {
				plist.add(elist.get(bait_id).getCondition(cond_id).getProtein(pname));
			}

			if (pname.equals(bait)) {
				currentBait = elist.get(bait_id).getCondition(cond_id)
						.getProtein(pname);
				
				progressMonitor.setNote("Calculating Features for " + pname + " in " + elist.get(bait_id).getName() + " " + elist.get(bait_id).getCondition(cond_id).getName());
				
			}

		}

		return plist;
	}

	private void calculatefeat(List<Protein> plist, int bait_id) {

		Protein Bait = currentBait;
		
		double p_pepcount;
		double p_spcount;
		double p_mw;
		double p_lenght;
		double p_quant;
		double p_coverage;

		double b_pepcount = Bait.getPcount();
		double b_spcount = Bait.getScount();
		double b_mw = Bait.getMw();
		double b_lenght = Bait.getLength();
		double b_coverage = Bait.getCoverage();
		double b_quant;

		if (quantitative) {
			b_quant = qlist.get(bait_id).getDiffValue(Bait.getName());
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
		
		for (int j = 0; j < plist.size(); j++) {

			String name = plist.get(j).getName();
			p_pepcount = plist.get(j).getPcount();
			p_spcount = plist.get(j).getScount();
			p_mw = plist.get(j).getMw();
			p_lenght = plist.get(j).getLength();
			p_coverage = plist.get(j).getCoverage();
			
			if (quantitative) {
				p_quant = qlist.get(bait_id).getDiffValue(name);
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

			if (element.containsKey(name)) {

				List<Double> oldfeat = element.get(name);
				for (int k = 0; k < feats.size(); k++) {
					oldfeat.add(feats.get(k));
				}
				element.put(name, oldfeat);

			} else {
				element.put(name, feats);
				fplist.add(name);
			}

		}

	}
	
	private double getLogLogistic(double s) {
		
		double val = Math.log(s);
		val = 1.0/(1.0 + Math.exp(-val));
		return val;
		
	}

}
