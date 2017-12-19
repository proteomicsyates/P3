/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.quantitative;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Interactome;

/**
 * @author diego
 *
 */
public class LysateTrendFinder {

	private List<Differential> qlist;
	private List<Differential> llist;
	private List<List<Interactome>> interactomes;

	private double lowT;
	private double highT;

	/**
	 * @param qlist
	 * @param llist
	 * @param interactomes
	 */
	public LysateTrendFinder(List<Differential> qlist, List<Differential> llist, List<List<Interactome>> interactomes) {
		this.qlist = qlist;
		this.llist = llist;
		this.interactomes = interactomes;
	}

	/**
	 * @param low
	 * @param high
	 */
	public void setT(double low, double high) {
		this.lowT = low;
		this.highT = high;

	}

	/**
	 * 
	 */
	public void run() {

		double quant;
		double lysate;
		double ratio;
		String key;

		for (int i = 0; i < qlist.size(); i++) {

			Hashtable<String, Double> qvalues = qlist.get(i).getData();
			Hashtable<String, Double> lvalues = llist.get(i).getData();

			Enumeration<String> enumKey = qvalues.keys();

			while (enumKey.hasMoreElements()) {

				key = enumKey.nextElement();

				quant = qvalues.get(key);
				if (lvalues.containsKey(key)) {

					lysate = lvalues.get(key);
					ratio = quant / lysate;

					if (ratio > lowT && ratio < highT) {

						for (int j = 0; j < interactomes.get(i).size(); j++) {

							Interactome interactome = interactomes.get(i).get(j);

							for (String bait : interactome.getProteinsHavingANetwork()) {

								if (interactome.getNetwork(bait).isInNetwork(key)) {

									interactome.getNetwork(bait).getInteractionByInteractorName(key)
											.setLysateLike(true);

								}

							}

						}

					}

				}

			}

		}

	}

	/**
	 * @return
	 */
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

}
