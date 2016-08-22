/**
 * diego
 * Jun 6, 2013
 */
package edu.scripps.p3.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.scripps.p3.parsers.inputs.utilities.Configuration;

/**
 * @author diego
 *
 */
public class CheckPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6110129114895383565L;
	private JCheckBox indirectBox;
	private JCheckBox bonusBox;
	private JCheckBox advancedBox;
	
	private boolean indtopology;
	private boolean finalbonus;
	private boolean advanced;
	
	Configuration conf;
	
	public CheckPanel() {
		
		indtopology = false;
		finalbonus = true;
		advanced = false;
		conf = new Configuration();
		conf.setValid(false);
		
		indirectBox = new JCheckBox("Indirect Topology");
		indirectBox.setSelected(false);
		indirectBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					indtopology = true;
				} else {
					indtopology = false;
				}
			}
		} );
		
		add(indirectBox);
		
		bonusBox = new JCheckBox("Final Score Bonus");
		bonusBox.setSelected(true);
		bonusBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					finalbonus = true;
				} else {
					finalbonus = false;
				}
			}
		} );
		
		add(bonusBox);
		
		advancedBox = new JCheckBox("Advanced Mode");
		advancedBox.setSelected(false);
		advancedBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					advanced = true;
					AdvancedMode am = new AdvancedMode(conf);
				} else {
					advanced = false;
				}
			}
		} );
		
		add(advancedBox);
		
	}
	
	public void reset() {
		indirectBox.setSelected(false);
		bonusBox.setSelected(false);
	}
	
	public boolean getIndirectTopology() {
		return indtopology;
	}
	
	public boolean getFinalBonus() {
		return finalbonus;
	}
	
	public boolean getAdvanced() {
		return advanced;
	}
	
	public Configuration getConfiguration() {
		return conf;
	}
}
