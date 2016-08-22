/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.parsers;

import javax.swing.JOptionPane;

/**
 * @author diego
 *
 */
public class Experiments {

	private String [] experiments;
	
	public void run() {
		
		int size = Integer.parseInt(getCondition("Experimental Condition", "Type the number of experimental conditions", "2"));
		
		experiments = new String[size];
		
		for (int i=0; i < size; i++) {
			experiments[i] = getCondition("Name Condition", "Type the name for experimental condition: " + i, "");
		}
				
	}
	
	public String[] getExperiments() {
		return experiments;
	}
	
	private String getCondition(String title, String body, String def) {

		String s = null;

		s = (String) JOptionPane.showInputDialog(null, body, title,
				JOptionPane.PLAIN_MESSAGE, null, null, def);
		return s;

	}
	
}
