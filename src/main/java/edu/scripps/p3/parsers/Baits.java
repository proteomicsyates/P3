/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author diego
 *
 */
public class Baits {

	private File input;
	private List<String> baits;
	/**
	 * @param bait
	 */
	public Baits(File bait) {
		this.input = bait;
	}

	/**
	 * 
	 */
	public void run() {
		
		baits = new ArrayList<String>();
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(input);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
			
			String dataline;
			
			while ((dataline = dis.readLine())!=null) {
				
				baits.add(dataline);
				
			}
		} catch (FileNotFoundException e) {
			System.err.println("bait file not found");
		} catch (IOException e) {
			System.err.println("unable to read bait file");
		}
		
	}

	/**
	 * @return
	 */
	public String[] getBaits() {
		
		String [] b = new String[baits.size()];
		for (int i=0; i < baits.size(); i++) {
			b[i] = baits.get(i);
		}
		return b;
	}

}
