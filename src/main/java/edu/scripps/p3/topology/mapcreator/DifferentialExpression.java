/**
 * diego
 * Oct 1, 2013
 */
package edu.scripps.p3.topology.mapcreator;

import java.util.ArrayList;
import java.util.List;

import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Interactome;

/**
 * @author diego
 * 
 */
public class DifferentialExpression {

	List<List<Interactome>> interactomes;
	String[] baits;
	List<Experiment> elist;
	List<Differential> qlist;

	/**
	 * @param interactomes
	 * @param baits
	 * @param elist
	 * @param qlist
	 */
	public DifferentialExpression(List<List<Interactome>> interactomes, String[] baits, List<Experiment> elist,
			List<Differential> qlist) {
		this.interactomes = interactomes;
		this.baits = baits;
		this.elist = elist;
		this.qlist = qlist;
	}

	public void run() {

		qlist = new ArrayList<Differential>();

		for (int i = 0; i < baits.length; i++) {

			String bait = baits[i].split("-")[0];

			Differential diff = new Differential(bait);

			qlist.add(diff);

			for (int j = 0; j < interactomes.size(); j++) {
				for (int k = 0; k < interactomes.get(j).size(); k++) {
					final Interactome interactome = interactomes.get(j).get(k);
					if (interactome.getBait_name().equals(bait)) {

						List<String> netlist = interactome.getProteinsHavingANetwork();

						String exp = interactome.getConditionName();

						for (int z = 0; z < elist.size(); z++) {
							final Experiment experiment = elist.get(z);
							for (int q = 0; q < experiment.getNumberofConditions(); q++) {
								final Condition condition = experiment.getCondition(q);
								if (condition.getName().equals(exp)) {

									for (String element : netlist) {

										double spcount;

										if (condition.proteinInTable(element)) {
											spcount = condition.getProtein(element).getScount();
										} else {
											spcount = 0;
										}

										if (diff.getData().containsKey(element)) {

											double value = diff.getDiffValue(element);

											if (spcount != 0) {
												value = value / spcount;
											} else {
												value = 10;
											}

										} else {
											diff.addValue(element, spcount);
										}

									}

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
	public List<Differential> getQlist() {
		return qlist;
	}

}
