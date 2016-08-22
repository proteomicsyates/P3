/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.orthogonal;


import java.util.List;

import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;

/**
 * @author diego
 *
 */
public class OrthogonalInserter {

	private List<List<Interactome>> interactomes;
	private List<Orthogonal> olist;
	
	/**
	 * @param interactomes
	 * @param olist
	 */
	public OrthogonalInserter(List<List<Interactome>> interactomes,
			List<Orthogonal> olist) {
		this.interactomes = interactomes;
		this.olist = olist;
	}

	/**
	 * 
	 */
	public void run() {
		
		String key;
		String rkey;
		
		for (int i=0; i < olist.size(); i++) {
			
			for (int k=0; k < interactomes.size(); k++) {
				
				for (Interactome interactome : interactomes.get(k)) {
					
					if (olist.get(i).getType().equals(Orthogonal.PHYSICAL)) {
						interactome.addPterm(olist.get(i).getName().split("\\.")[0]);
					} else {
						interactome.addGterm(olist.get(i).getName().split("\\.")[0]);
					}
					
					List<String> nets = interactome.getNetlist();
					
					for (String nname : nets) {
						
						Network net = interactome.getNetwork(nname);

						List<String> interactions = net.getInteraction_names();

						for (String inter : interactions) {
							
							key = nname + "_" + inter;
							rkey = inter + "_" + nname;

							double value = 0;
							
							if (!nname.equals(inter)) {

								if (olist.get(i).isInTable(key)) {

									value = olist.get(i).getValues(key);

								} else {

									if (olist.get(i).isInTable(rkey)) {

										value = olist.get(i).getValues(rkey);

									} else {

										value = 0.0;

									}
								}
							}
							
							Interaction interaction = net.getInteraction(inter);
							if (olist.get(i).getType().equals(Orthogonal.PHYSICAL)) {
								interaction.addPhysical_score(value, olist.get(i).getCoefficient());
							} else {
								interaction.addGenetical_score(value, olist.get(i).getCoefficient());
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		
	}

	/**
	 * @param i
	 * @return
	 */
	public List<List<Interactome>> getInteractomes(int i) {
		return interactomes;
	}

}
