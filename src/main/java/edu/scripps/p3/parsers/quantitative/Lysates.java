/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.parsers.quantitative;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.scripps.p3.experimentallist.Differential;

/**
 * @author diego
 *
 */
public class Lysates extends Quantitatives {

	/**
	 * @param quantitativefilelist
	 * @param quantitativedir
	 * @param baits
	 * @param dlist
	 */
	public Lysates(String[] quantitativefilelist, File quantitativedir,
			String[] baits, List<Differential> dlist) {
		super(quantitativefilelist, quantitativedir, baits, dlist);
		
	}

	protected void setTitle() {
		title = "Lysate Resolver";
	}
	
	protected void parseFiles() {
		
		File f;
		int baitindex;
		
		for (int i = 0; i < files.length; i++) {
			
			f = new File(inputdir,files[i]);

			baitindex = assignments.get(i);
			
			FileInputStream fis;
			
			try {
				fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(
						bis));

				String dataline;
				double value;
				String pname;

				while ((dataline = dis.readLine()) != null) {
					
					if (dataline.startsWith("P")) {
						String [] elements = dataline.split("\t");

						if (!elements[6].equals("NA")) {
							value = Double.parseDouble(elements[2]);
							pname = elements[14].split(" ")[0];

							dlist.get(baitindex).addValue(pname, value);
						}

					}

			
				}

			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}
			
		}
		
	}
	
}
