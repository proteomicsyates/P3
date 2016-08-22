/**
 * diego
 * May 14, 2013
 */
package edu.scripps.p3.topology.mapcreator;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;


/**
 * @author diego
 *
 */
public class MapCreator {

	String bait;
	Hashtable<String,String> interactions;
	StringBuffer map;
	
	String indirect;
	boolean indirect_available;
	
	/**
	 * @param string
	 * @param hashtable
	 */
	public MapCreator(String name, Hashtable<String, String> interactions) {
		this.bait = name.split("_")[0];
		this.interactions = interactions;
		this.indirect_available = false;
	}

	/**
	 * 
	 */
	public void run() {
		
		String key;
		String [] values;
		
		map = new StringBuffer();
		
		map.append("*edges\n");
		
		Enumeration<String> enumKey = interactions.keys();
		
		Hashtable<String,String> lysate = new Hashtable<String,String>();
		Hashtable<String,Double> conf = new Hashtable<String,Double>();
		List<String> cnames = new ArrayList<String>();
				
		DecimalFormat df = new DecimalFormat("#.####");
				
		while(enumKey.hasMoreElements()) {
			key = enumKey.nextElement();
			values = interactions.get(key).split("_");
			
			for (int i=0; i < values.length-2; i++) {
				if (Double.parseDouble(values[i])!=0) {
					map.append(bait + "\t" + key + "\ti " + (i+1) + "\tc " + df.format(values[i]) + "\n");
				}
				
			}
			
			if (Double.parseDouble(values[4])==1) {
				lysate.put(key, key);
			}
					
			double val = Double.parseDouble(values[0]);
			val = scaleVal(val);
			
			conf.put(key, val);
			cnames.add(key);
			
		}
		
		if (indirect_available) {
			map.append(indirect);
		}
		
		map.append("*nodes\n");
		
		if (indirect_available) {
			cnames = getCustomSort(cnames);
		} else {
			Collections.sort(cnames);
		}
				
		double angle_step = 360.0/((double)cnames.size()+1.0);
		angle_step = Math.toRadians(angle_step);
		
		double hypot;
		double alpha;
		
		double x;
		double y;
		
		Random r = new Random();
		
		int R,G,B;
		int shape;
		
		for (int i=0; i < cnames.size(); i++) {
			
			if (cnames.get(i).equals(bait)) {
				
				x = 0.5;
				y = 0.5;
				R = Color.YELLOW.getRed();
				G = Color.YELLOW.getGreen();
				B = Color.YELLOW.getBlue();
				shape = 0;
				
 			} else {
 				hypot = conf.get(cnames.get(i));
 				alpha = angle_step * i;
 				
 				x = hypot * Math.cos(alpha);
 				y = hypot * Math.sin(alpha);
 				
 				x = x/2 + 0.5;
 				y = y/2 + 0.5;
 				
 				R = r.nextInt(255);
 				G = r.nextInt(255);
 				B = r.nextInt(255);
 				
 				if (lysate.containsKey(cnames.get(i))) {
 					shape=1;
 				} else {
 					shape=0;
 				}
 			}
			
			
			
			map.append(cnames.get(i) + "\t" + (float)x + "\t" + (float)y + "\tc " + R + "," + G + "," + B + "\ts " + shape + "\ta \"" + cnames.get(i) + "\"\n");
			
		}
		
		
	}

	private double scaleVal(double val) {
		
		val = 1 - val;
		
		//	val = val/0.9;
			
		//	if (val>0.2) {
				val = Math.pow(val, 3) + 0.1;
		//	}
					
			if (val>=1) val=0.99;
			
			return val;
	}
	
	/**
	 * @return
	 */
	public String getMap() {
		return map.toString();
	}
	
	public void setIndirect(String indirect) {
		this.indirect_available = true;
		this.indirect = indirect;
	}
	
	private List<String> getCustomSort(List<String> cnames) {
		
		NodeSorter ns = new NodeSorter(cnames, indirect);
		ns.run();
		cnames = ns.getNodes();
		
		return cnames;
	}

}
