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
		
		final List<Double> list = data.get(plist.get(0));
		centers = new double[clusters][list.size()];
		
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
			
			final String prey = plist.get(i);
			if (!prey.equals(bait)) {
				if (data.containsKey(prey)) {
					
					List<Double> feats = data.get(prey);
					distance = new double[clusters];
					
					for (int j=0; j < clusters; j++) {
						
						distance[j] = getDistance(feats,centers[j]);	
					}
					
					int assigned = getMinDistance(distance);
					
					cluslist.get(assigned).add(prey);
					} else {
						System.out.println("Features not available for " + prey);
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
			final String prey = plist.get(index);
			if ((index!=oldindex) && (!prey.equals(bait))) {
				
				oldindex = index; //TODO its ok for 2 clusters, it need to become more elaborate for more...
				
				for (int j=0; j < data.get(prey).size(); j++) {
					centers[i][j] = data.get(prey).get(j);
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
