/**
 * diego
 * Jun 14, 2013
 */
package edu.scripps.p3.topology;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.io.dataIO;
import edu.scripps.p3.topology.mapcreator.DifferentialMapCreator;
import edu.scripps.p3.topology.mapcreator.MapCreator;

/**
 * @author diego
 *
 */
public class TopologyCreator {

	private List<List<Hashtable<String, String>>> maps;
	private List<List<String>> maps_names;
	private File topodir;
	private List<Differential> qlist;
	private List<Hashtable<String, String>> indirectEdges;
	private boolean indirect;
	
	/**
	 * @param maps
	 * @param maps_names
	 * @param topodir
	 * @param qlist
	 */
	public TopologyCreator(List<List<Hashtable<String, String>>> maps,
			List<List<String>> maps_names, File topodir,
			List<Differential> qlist) {
		this.maps = maps;
		this.maps_names = maps_names;
		this.topodir = topodir;
		this.qlist = qlist;
		this.indirect = false;
	}

	/**
	 * @param indirectEdges
	 */
	public void setIndirectEdges(List<Hashtable<String, String>> indirectEdges) {
		this.indirectEdges = indirectEdges;
		this.indirect = true;
	}

	/**
	 * 
	 */
	public void run() {
						
		dataIO dIO = new dataIO();
		
		
		MapCreator mc;
		DifferentialMapCreator dmc;
		String map;
		
		for (int k=0; k < maps_names.size(); k++) {
			
			for (int i=0; i < maps_names.get(k).size(); i++) {
				
				//static map
				
				mc = new MapCreator(maps_names.get(k).get(i), maps.get(k).get(i));
				if (indirect) {
					
					String key = maps_names.get(k).get(i);
					
					mc.setIndirect(indirectEdges.get(k).get(key));
				}
				mc.run();
				map = mc.getMap();
				dIO.saveTopology(topodir, map, maps_names.get(k).get(i));
				
				
				
			}
			
			//differential map
			List<int[]> pairs = getPairs(k);
			int index1;
			int index2;
			
			for (int i=0; i < pairs.size(); i++) {
				
				index1 = pairs.get(i)[0];
				index2 = pairs.get(i)[1];
				
				dmc = new DifferentialMapCreator(maps_names.get(k).get(index1), maps.get(k).get(index1), maps.get(k).get(index2), qlist.get(k));
				if (indirect) {
					
					String key = maps_names.get(k).get(i);
					
					dmc.setIndirect(indirectEdges.get(k).get(key));
				}
				dmc.run();
				map = dmc.getMap();
				dIO.saveTopology(topodir, map, maps_names.get(k).get(index1).split("_")[0] + "_differential.dat");
				
			}
		}
		
		
		
		
		
	}
	
	private List<int[]> getPairs(int id) {
		List<int[]> pairs = new ArrayList<int[]>();
		
		Hashtable<String,Integer> table = new Hashtable<String,Integer>();
		
		String name;
		int index;
		
		for (int i=0; i < maps_names.get(id).size(); i++) {
			name = maps_names.get(id).get(i).split("_")[0];
			
			if (table.containsKey(name)) {
				
				index = table.get(name);
				
				int[] pair = new int[2];
				pair[0] = index;
				pair[1] = i;
				pairs.add(pair);
				
			} else {
				table.put(name, i);
			}
			
		}
		
		
		return pairs;
	}

}
