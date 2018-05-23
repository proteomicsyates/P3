/**
 * diego
 * Aug 14, 2014
 */
package edu.scripps.p3.utilities.optimizer;

import java.util.Enumeration;
import java.util.Hashtable;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

/**
 * @author diego
 *
 */
public class CoefficientFitness extends FitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Hashtable<String, Bait> btable;

	public CoefficientFitness(Hashtable<String, Bait> btable) {
		this.btable = btable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgap.FitnessFunction#evaluate(org.jgap.IChromosome)
	 */
	@Override
	protected double evaluate(IChromosome chrome) {

		double current_combination = 0;

		double w_corr = (double) chrome.getGene(0).getAllele();
		double w_clu = (double) chrome.getGene(1).getAllele();
		;
		double w_quant = (double) chrome.getGene(2).getAllele();
		double w_phy = (double) chrome.getGene(3).getAllele();
		double w_gen = (double) chrome.getGene(4).getAllele();
		double t_conf = (double) chrome.getGene(5).getAllele();
		double t_confO = (double) chrome.getGene(6).getAllele();

		Enumeration<String> bkeys = btable.keys();
		String key;
		while (bkeys.hasMoreElements()) {

			key = bkeys.nextElement();

			btable.get(key).processProteins(w_corr, w_clu, w_quant, w_phy, w_gen, t_conf, t_confO);
			btable.get(key).BaitStats();

			current_combination = +btable.get(key).getScore();

		}

		return current_combination;

	}

}
