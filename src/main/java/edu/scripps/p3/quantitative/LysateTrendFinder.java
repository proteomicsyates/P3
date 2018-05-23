/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.quantitative;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Interactome;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author diego
 *
 */
public class LysateTrendFinder {
	private final static Logger log = Logger.getLogger(LysateTrendFinder.class);
	private final List<Differential> qlist;
	private final List<Differential> llist;
	private final List<List<Interactome>> interactomes;

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
		lowT = low;
		highT = high;

	}

	/**
	 * 
	 */
	public void run() {

		double quant;
		double lysate;
		double ratio;

		for (int i = 0; i < qlist.size(); i++) {

			final Differential differential = qlist.get(i);
			final TObjectDoubleHashMap<String> qvalues = differential.getData();
			final Differential differential2 = llist.get(i);
			final TObjectDoubleHashMap<String> lvalues = differential2.getData();
			log.info("Looking for quantitative trends between the lysate " + lvalues.size() + " and the experiment "
					+ qvalues.size() + " in bait " + differential.getName());
			final Set<String> keySet = qvalues.keySet();

			for (final String key : keySet) {

				quant = qvalues.get(key);
				if (lvalues.containsKey(key)) {

					lysate = lvalues.get(key);
					if (key.contains("SNF4")) {
						log.info(quant + "/" + lysate);
					}
					ratio = quant / lysate;

					if (ratio > lowT && ratio < highT) {

						for (int j = 0; j < interactomes.get(i).size(); j++) {

							final Interactome interactome = interactomes.get(i).get(j);

							for (final String bait : interactome.getProteinsHavingANetwork()) {

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
