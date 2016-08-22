/**
 * diego
 * Jun 13, 2013
 */
package edu.scripps.p3.correlator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ProgressMonitor;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;








import edu.scripps.p3.correlator.utilities.Complex;
import edu.scripps.p3.experimentallist.Condition;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.io.dataIO;
import edu.scripps.p3.parsers.inputs.utilities.Protein;

/**
 * @author diego
 *
 */
public class ComplexCorrelator {

	private List<Experiment> elist;
	private List<List<Double>> matrix;
	private File lout;
	private StringBuffer log;
	private List<List<Complex>> complex;
	private boolean rapidCorrelation;
	
	private ProgressMonitor progressMonitor;
	private static int progress=0;
	
	public ComplexCorrelator(List<Experiment> elist,
			File logdir, boolean rapid) {
		this.elist = elist;
		this.lout = logdir;
		this.rapidCorrelation = rapid;
		
	}
	
	public void run() {
		
		log = new StringBuffer();
		complex = new ArrayList<List<Complex>>();
		
		int fullsize = getFullSize();
		
		progressMonitor = new ProgressMonitor(null, "Calculating Correlation Complexes", "Initialization...", 0, fullsize-2);
		progress = 0;
		progressMonitor.setProgress(progress);
		
		for (int i=0; i < elist.size(); i++) {
			
			List<Complex> complexGroup = new ArrayList<Complex>();
			complex.add(complexGroup);
			
			for (int j=0; j < elist.get(i).getNumberofConditions(); j++) {
				
				log.append("Working on " + elist.get(i).getName() + "_" + elist.get(i).getCondition(j).getName() + "\n");
				
				initializeMatrix(elist.get(i).getCondition(j).getNumberOfProteins());
				
				fillMatrix(i,j);
				
				getCorrelation(i,j);
				
			}
		}
		
		progressMonitor.close();
		
		dataIO dIO = new dataIO();
		dIO.writeLog(lout,log, "ComplexCorrelatorLog");
		
	}
	
	private HashSet<String> baits;
	
	public void setBaits(String [] baits) {
		
		this.baits = new HashSet<String>();
		
		for (String s : baits) {
			this.baits.add(s);
		}
		
	}
	
	private void getCorrelation(int bait_id, int condition_id) {
		
		Complex cmpx = new Complex(elist.get(bait_id).getName(), elist.get(bait_id).getCondition(condition_id).getName());
		
		complex.get(bait_id).add(cmpx);
		
		progressMonitor.setNote("Processing " + elist.get(bait_id).getName() + " for " + elist.get(bait_id).getCondition(condition_id).getName());
		
		Runtime r = Runtime.getRuntime();
		long keepAlive = 5000;
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, r.availableProcessors()*2, keepAlive, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100));
		executor.allowCoreThreadTimeOut(true);
		
		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor.execute(r);
            }
        });
		
		for (int i=0; i < elist.get(bait_id).getCondition(condition_id).getNumberOfProteins(); i++) {
			
			
 			
				Runnable worker = new Correlator(bait_id, condition_id, i);
				executor.execute(worker);
			
		}
		executor.shutdown();
		
		try {
			executor.awaitTermination(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	private boolean correlationCalculator(List<Double> preyx, List<Double> preyy) {
		
		double [][] values;
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		SpearmansCorrelation sc = new SpearmansCorrelation();
		
		double c_pc;
		double c_sc;
		
		double correlation;
		
		values = new double[2][preyx.size()];
		
		for (int k=0; k < preyx.size(); k++) {
			values[0][k] = preyx.get(k);
			values[1][k] = preyy.get(k);
		}
		
		if (rapidCorrelation) {
			c_pc = pc.correlation(values[0], values[1]);
			correlation = c_pc;
		} else {
			c_pc = pc.correlation(values[0], values[1]);
			c_sc = sc.correlation(values[0], values[1]);
			correlation = (c_pc + c_sc) /2;
		}
				
		correlation = Math.pow(correlation, 2);
			
		if (correlation > 0.5) { //equivalent to +-0.7 with R (i.e. 0.7 R --> 0.49 R2) //was 0.5
			
			return true;
			
		} else {
			return false;
		}
		
	}
	
	private void fillMatrix(int bait_id, int condition_id) {
		
		List<String> pnames = elist.get(bait_id).getCondition(condition_id).getPnames();
		
		double value_i;
		double value_j;
		
		List<Double> column;
		
		for (int i=0; i < pnames.size(); i++) {
			
			value_i = getValue(bait_id, condition_id, pnames.get(i));
			
			column = new ArrayList<Double>();
			
			for (int j=0; j < pnames.size(); j++) {
				
				value_j = getValue(bait_id, condition_id, pnames.get(j));
				
				column.add(getDistance(value_i,value_j));
				
			}
			
			addColumn(column);
		}
		
	}
	
	private void addColumn(List<Double> column) {
		matrix.add(column);
	}
	
	private double getDistance(double v1, double v2) {
		
		double distance;
		
		distance = Math.abs(v1-v2)/(v1+v2);
		
		distance = 1 - distance;
		
		return distance;
		
	}
	
	private double getValue(int bait_id, int condition_id, String pname) {
		
		double value = 0;
		
		Protein p = elist.get(bait_id).getCondition(condition_id).getProtein(pname);
		
		value = (p.getApv() * p.getLength())/p.getMw();
		
		return value;
	}
	
	public List<List<Complex>> getComplexList() {
		return complex;
	}
	
	private void initializeMatrix(int size) {
		
		matrix = new ArrayList<List<Double>>();

	}
	
	private int getFullSize() {
		
		int size=0;
		
		for (int i=0; i < elist.size(); i++) {
			for (int j=0; j < elist.get(i).getNumberofConditions(); j++) {
				size += elist.get(i).getCondition(j).getNumberOfProteins();
			}
		}
		
		return size;
	}
	
	private class Correlator implements Runnable { 
		
		int myBait;
		int myCondition;
		int myId;
		
		public Correlator(int bait_id, int condition_id, int id) {
			this.myBait = bait_id;
			this.myCondition = condition_id;
			this.myId = id;
		}
		
		public void run() {
			
			List<Double> preyx;
			List<Double> preyy;
			
			List<String> trending_complex;
			
			preyx = new ArrayList<Double>();
			trending_complex = new ArrayList<String>();
			trending_complex.add(elist.get(myBait).getCondition(myCondition).getPnames().get(myId));
						
			for (int s=0; s < matrix.get(myId).size(); s++) {
				
				preyx.add(matrix.get(myId).get(s));
				
			}
			
			preyx.remove(myId);
			
			for (int j=0; j < matrix.size(); j++) {
				if (j!=myId) {
					
					preyy = new ArrayList<Double>();
					
					for (int s=0; s < matrix.get(j).size(); s++) {
						
						preyy.add(matrix.get(j).get(s));
						
					}
					
					preyy.remove(j);
								
					
					if (correlationCalculator(preyx,preyy)) {
						
						trending_complex.add(elist.get(myBait).getCondition(myCondition).getPnames().get(j));
						
					}
				}
			}
						
			complex.get(myBait).get(myCondition).addComplex(trending_complex);
			for (int j=0; j < trending_complex.size(); j++) {
				log.append(trending_complex.get(j) + "\t");
			}
			log.append("\n\n");
			
			progress++;
			progressMonitor.setProgress(progress);
			
		}
		
	}
	
}
