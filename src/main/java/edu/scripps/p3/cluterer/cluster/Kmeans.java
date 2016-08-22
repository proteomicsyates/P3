package edu.scripps.p3.cluterer.cluster;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class Kmeans {

	List<String> plist;
	Hashtable<String,List<Double>> data;
	int clusters;
	String bait;
	
	double [][] centers;
	List<List<String>> cluslist;
	
	public Kmeans(List<String> plist, Hashtable<String,List<Double>> data, int cluster) {
		this.plist = plist;
		this.data = data;
		clusters = cluster;
	}
	
	public void run() {
		
		centers = new double[clusters][data.get(plist.get(0)).size()];
		
		selectCenters();
		
		for (int i=0; i < 20; i++) {
			
			calculateDistances();
			calculateNewCenters();
			
		}
		
	}
	
	public void setBait(String b) {
		bait =b;
	}
	
	
	
	private void calculateNewCenters() {
		
		for (int i=0; i < clusters; i++) {
			getNewCenter(i);
		}
		
	}
	
	private void getNewCenter(int id) {
		
		centers[id] = new double[data.get(plist.get(0)).size()];
		
		
		for (int j=0; j < centers[id].length; j++) {
			for (int i=0; i < cluslist.get(id).size(); i++) {
				centers[id][j] += data.get(cluslist.get(id).get(i)).get(j);
			}
			centers[id][j] /= cluslist.get(id).size();
			
		}
		
	}
	
	private void calculateDistances() {
		
		cluslist = new ArrayList<List<String>>();
		
		for (int i=0; i < clusters; i++) {
			List<String> column = new ArrayList<String>();
			cluslist.add(column);
		}
		
		double [] distance;
		
		for (int i=0; i < plist.size(); i++) {
			
			if (!plist.get(i).equals(bait)) {
				if (data.containsKey(plist.get(i))) {
					
					List<Double> feats = data.get(plist.get(i));
					distance = new double[clusters];
					
					for (int j=0; j < clusters; j++) {
						
						distance[j] = getDistance(feats,centers[j]);	
					}
					
					int assigned = getMinDistance(distance);
					
					cluslist.get(assigned).add(plist.get(i));
					} else {
						System.out.println("Features not available for " + plist.get(i));
					}
			}
					
		}
		
	}
	
	private int getMinDistance(double [] distance) {
		double value=Double.MAX_VALUE;
		int position=-1;
		
		for (int i=0; i <distance.length; i++) {
			if (distance[i]<value) {
				value = distance[i];
				position = i;
			}
		}
		
		return position;
		
	}
	private double getDistance(List<Double> feats, double[] center) {
		
		double dist=0;
		
		for (int i=0; i < feats.size(); i++) {
			dist += Math.pow(feats.get(i)-center[i], 2);
		}
		dist = Math.sqrt(dist);
		
		return dist;
	}
	
	private void selectCenters() {
		
		Random r = new Random();
		int oldindex=-1;
		
		for (int i=0; i < clusters; i++) {
			int index = r.nextInt(plist.size());
			if ((index!=oldindex) && (!plist.get(index).equals(bait))) {
				
				oldindex = index; //TODO its ok for 2 clusters, it need to become more elaborate for more...
				
				for (int j=0; j < data.get(plist.get(index)).size(); j++) {
					centers[i][j] = data.get(plist.get(index)).get(j);
				}
								
			} else {
				i--;
			}
					
		}
			
	}

	private void assignBait() {
		
		double [] distance;
		List<Double> feats = data.get(bait);
		distance = new double[clusters];
		
		for (int j=0; j < clusters; j++) {
			
			distance[j] = getDistance(feats,centers[j]);//cluslist.get(j).size();//getDistance(feats,centers[j]);	
		}
		
		int assigned = getMinDistance(distance);
		
		cluslist.get(assigned).add(bait);
		
	}
	
	public List<String> getClusters() {
		
		assignBait();
				
		for (int i=0; i < clusters; i++) {
			if (cluslist.get(i).contains(bait)) {
				return cluslist.get(i);
			}
		}
		
		return null;
	}
	
}
