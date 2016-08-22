/**
 * diego
 * Jun 13, 2013
 */
package edu.scripps.p3.correlator;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.scripps.p3.correlator.utilities.Complex;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.network.Network;
import edu.scripps.p3.experimentallist.network.interaction.Interaction;
import edu.scripps.p3.io.dataIO;

/**
 * @author diego
 *
 */
public class ComplexFilter {

	private List<List<Complex>> complex_list;
	private List<List<Interactome>> interactomes;
	private File logdir;
	private StringBuffer log;

	private double correlation_threshold = 0.01;
	
	private ProgressMonitor progressMonitor;
	private static int progress=0;
	
	public ComplexFilter(List<List<Complex>> complex_list, File logdir) {
		
		this.complex_list = complex_list;
		this.logdir = logdir;
		
	}
	
	public void run() {
		
		log = new StringBuffer();
		interactomes = new ArrayList<List<Interactome>>();
		
		int fullsize = getFullSize();
		List<String> element;
		
		progressMonitor = new ProgressMonitor(null,"Calculating Correlation Scores","Initializing", 0, fullsize);
		progress = 0;
		progressMonitor.setProgress(progress);
		
		for (int cl=0; cl < complex_list.size(); cl++) {
			
			List<Interactome> inters = new ArrayList<Interactome>();
			interactomes.add(inters);
			
			Hashtable<String,Integer> frequency = new Hashtable<String,Integer>();
			Hashtable<String,Integer> couple_frequency = new Hashtable<String,Integer>();
			List<String> names;
			List<String> cnames;
			
			double ratio;
			
			for (int i=0; i < complex_list.get(cl).size(); i++) {
				
				log.append("Computing Frequencies for " + complex_list.get(cl).get(i).getBait() + "\t" + complex_list.get(cl).get(i).getCondition() + "\n");
				progressMonitor.setNote("Processing " + complex_list.get(cl).get(i).getBait() + " for " + complex_list.get(cl).get(i).getCondition());
				
				frequency = new Hashtable<String,Integer>();
				couple_frequency = new Hashtable<String,Integer>();
				names = new ArrayList<String>();
				cnames = new ArrayList<String>();
				
				String key;
				String rkey;
				String [] val;
				
				List<List<String>> cmpx = complex_list.get(cl).get(i).getComplexes();
				
				for (int j=0; j < cmpx.size(); j++) {
					
					element = cmpx.get(j);
					
					if (element!=null) {
						for (int k=0; k < element.size(); k++) {
							
							if (frequency.containsKey(element.get(k))) {
								
								int freq = frequency.get(element.get(k));
								freq++;
								frequency.put(element.get(k), freq);						
							} else {
								int freq = 1;
								frequency.put(element.get(k), freq);
								names.add(element.get(k));
							}
							
							
						}
						
						
						List<String> couples = getCouples(element);
						
						for (int k=0; k < couples.size(); k++) {
							
							val = couples.get(k).split("_");
							key = val[0] + "_" + val[1];
							rkey = val[1] + "_" + val[0];
							
							if (!couple_frequency.containsKey(key) && !couple_frequency.containsKey(rkey)) {
								
								int cfreq = 1;
								couple_frequency.put(key, cfreq);
								cnames.add(key);
								
							} else {
								
								if (couple_frequency.containsKey(key)) {
									
									int cfreq = couple_frequency.get(key);
									cfreq++;
									couple_frequency.put(key, cfreq);
									
								} else {
									
									int cfreq = couple_frequency.get(rkey);
									cfreq++;
									couple_frequency.put(rkey, cfreq);
								}
								
							}
							
							
						}
					}
					
					
					
					progress++;
					progressMonitor.setProgress(progress);
				}
				
				Interactome interactome = new Interactome(complex_list.get(cl).get(i).getBait(), complex_list.get(cl).get(i).getCondition()); 
				interactome.isEmpty();
				
				interactomes.get(cl).add(interactome);
				
				for (int j=0; j < cnames.size(); j++) {
					
					double cf = couple_frequency.get(cnames.get(j));
					
					String [] n = cnames.get(j).split("_");
					
					double f,f1;
					
					if (frequency.containsKey(n[0])) {
						f = frequency.get(n[0]);
					} else {
						f = 1;
					}
					if (frequency.containsKey(n[1])) {
						f1 = frequency.get(n[1]);
					} else {
						f1 = 1;
					}
					 				
					double frq = Math.min(f, f1);
					
					ratio = cf/frq;
								
					if (ratio>this.correlation_threshold) {	//it was 0.01	
								
					
						log.append(cnames.get(j) + "\t" + ratio + "\n");
						
						if (interactome.isNetworkinSystem(n[0])) {
							
							Network net = interactome.getNetwork(n[0]);
							
							Interaction inter = new Interaction(n[1]);
							inter.setCorrelation_score(ratio);
							
							net.addInteraction(n[1], inter);
							
							
						} else {
							
							Network net = new Network(n[0]);
							Interaction inter = new Interaction(n[1]);
							inter.setCorrelation_score(ratio);
							net.addInteraction(n[1], inter);
							interactome.addNetwork(net);
							
						}
						
						if (interactome.isNetworkinSystem(n[1])) {
							
							Network net = interactome.getNetwork(n[1]);
							Interaction inter = new Interaction(n[0]);
							inter.setCorrelation_score(ratio);
							net.addInteraction(n[0], inter);
							
							
						} else {
							
							Network net = new Network(n[1]);
							Interaction inter = new Interaction(n[0]);
							inter.setCorrelation_score(ratio);
							net.addInteraction(n[0], inter);
							interactome.addNetwork(net);
							
						}
								
					} else {
						log.append(cnames.get(j) + "\t" + ratio + "\tdiscarded\n");
					}
						
				}
				
			}
			
		}
		
		progressMonitor.close();
		
		dataIO dIO = new dataIO();
		dIO.writeLog(logdir, log, "CorrelationScoreLog");
		
		
	}
	
	private int getFullSize() {
		int size = 0;
		
		for (int i=0; i < complex_list.size(); i++) {
			for (int j=0; j < complex_list.get(i).size(); j++) {
				size += complex_list.get(i).get(j).getComplexes().size();
			}
		}
				
		return size;
	}
	
	private List<String> couples = new ArrayList<String>();
	
	private List<String> getCouples(List<String> element) {
		
		String key;
		
		couples.clear();
		
		for (int i=0; i < element.size(); i++) {
			for (int j=i; j < element.size(); j++) {
				
				key = element.get(i) + "_" + element.get(j);
				
				couples.add(key);
								
			}
		}
				
		return couples;
	}
	
	public List<List<Interactome>> getInteractomes() {
		return interactomes;
	}

	/**
	 * @param correlationT
	 */
	public void setCorrelationThreshold(double correlationT) {
		this.correlation_threshold = correlationT;
		
	}
	
}
