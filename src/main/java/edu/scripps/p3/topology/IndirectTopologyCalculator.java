/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.topology;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;

/**
 * @author diego
 *
 */
public class IndirectTopologyCalculator {

	private List<List<Interactome>> interactomes;
	private String[] baits;
	private List<List<Hashtable<String, String>>> maps;
	private List<List<String>> maps_names;

	private List<Hashtable<String, String>> indirectedges;

	/**
	 * @param interactomes
	 * @param baits
	 * @param maps
	 * @param maps_names
	 */
	public IndirectTopologyCalculator(List<List<Interactome>> interactomes, String[] baits,
			List<List<Hashtable<String, String>>> maps, List<List<String>> maps_names) {
		this.interactomes = interactomes;
		this.baits = baits;
		this.maps = maps;
		this.maps_names = maps_names;
	}

	/**
	 * 
	 */
	public void run() {

		indirectedges = new ArrayList<Hashtable<String, String>>();

		String key;
		int index;
		List<String> pnames;
		List<String> interactions;
		List<Double> p_int;
		List<Double> g_int;

		double c_score;

		String root;
		StringBuffer indirect;

		for (int k = 0; k < interactomes.size(); k++) {

			Hashtable<String, String> indirectedges_i = new Hashtable<String, String>();
			indirectedges.add(indirectedges_i);
			String[] expbaits = baits[k].split("-");

			for (Interactome interactome : interactomes.get(k)) {

				for (String bait : expbaits) {

					key = bait + "_" + interactome.getConditionName();

					index = getIndex(key, k);
					indirect = new StringBuffer();

					if (index != -1) {

						pnames = getPnames(index, k);

						for (String pname : pnames) {

							if (interactome.isNetworkinSystem(pname)) {

								Network net = interactome.getNetwork(pname);
								root = net.getBait();
								interactions = net.getInteractorsNames();

								for (String interaction : interactions) {

									if (pnames.contains(interaction)) {

										Interaction inter = net.getInteractionByInteractorName(interaction);

										c_score = inter.getCorrelation_score();
										if (c_score > 0) {
											indirect.append(root + "\t" + interaction + "\ti " + 9 + "\tc "
													+ (c_score * 0.2) + "\n");
										}

										if (inter.isPscores()) {
											p_int = inter.getPhysical_scores();

											for (int i = 0; i < p_int.size(); i++) {
												if (p_int.get(i) > 0) {
													indirect.append(root + "\t" + interaction + "\ti " + (i + 5)
															+ "\tc " + (p_int.get(i) * 0.2) + "\n");
												}
											}
										}

										if (inter.isGscores()) {
											g_int = inter.getGenetical_scores();

											for (int i = 0; i < g_int.size(); i++) {
												if (g_int.get(i) > 0) {
													indirect.append(root + "\t" + interaction + "\ti " + (i + 7)
															+ "\tc " + (g_int.get(i) * 0.2) + "\n");
												}
											}
										}

									}
								}

							}
						}

					}

					indirectedges.get(k).put(key, indirect.toString());

				}

			}

		}

	}

	private List<String> getPnames(int index, int id) {

		List<String> pnames = new ArrayList<String>();

		Hashtable<String, String> map = maps.get(id).get(index);

		Enumeration<String> enumKey = map.keys();
		String key;
		while (enumKey.hasMoreElements()) {
			key = enumKey.nextElement();
			pnames.add(key);
		}

		return pnames;
	}

	private int getIndex(String key, int id) {

		int index = -1;

		if (maps_names.get(id).contains(key)) {
			index = maps_names.get(id).indexOf(key);
		}
		return index;

	}

	/**
	 * @return
	 */
	public List<Hashtable<String, String>> getIndirectEdges() {
		return indirectedges;
	}

}
