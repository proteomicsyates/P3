/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers.inputs.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.io.dataIO;

/**
 * @author diego
 *
 */
public class CoverageFixer {

	List<Experiment> elist;
	File cdir;
	Hashtable<String, Double> coverages;
	
	/**
	 * @param elist
	 * @param inputdir
	 */
	public CoverageFixer(List<Experiment> elist, File inputdir) {
		this.elist = elist;
		this.cdir = inputdir;
	}

	/**
	 * 
	 */
	public void run() {
		
		coverages = new Hashtable<String,Double>();
		setCoverageTable();
		
		double val;
		
		Enumeration<String> enumkeys = coverages.keys();
		while (enumkeys.hasMoreElements()) {
			
			String key = enumkeys.nextElement();
			
			val = coverages.get(key);
			
			for (int i=0; i < elist.size(); i++) {
				for (int j=0; j < elist.get(i).getNumberofConditions(); j++) {
					if (elist.get(i).getCondition(j).proteinInTable(key)) {
						elist.get(i).getCondition(j).getProtein(key).setMaxTheoCoverage(val);
						elist.get(i).getCondition(j).getProtein(key).normalizeCoverage();
					}
				}
			}
			
			
			
		}
		
	}

	private void setCoverageTable() {
		
		dataIO dIO = new dataIO(cdir);
		File pepcutter = dIO.openFile("Select PeptideCutter file");
		FileInputStream fis;
		try {
			fis = new FileInputStream(pepcutter);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
			
			String dataline;
			double val;
						
			while ((dataline = dis.readLine())!=null) {
			
				String [] element = dataline.split("\t");
				val = Double.parseDouble(element[1]);
				coverages.put(element[0], val);
				
			}
					
		} catch (FileNotFoundException e) {
			System.err.println("file not found");
		} catch (IOException e) {
			System.err.println("unable to read bait file");
		}
		
	}
	
}
