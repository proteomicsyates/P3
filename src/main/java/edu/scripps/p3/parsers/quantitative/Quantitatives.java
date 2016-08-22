/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.parsers.quantitative;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.scripps.p3.experimentallist.Differential;

/**
 * @author diego
 *
 */
public class Quantitatives {

	protected String[] files;
	protected String[] baits;
	
	protected File inputdir;
	
	protected List<Differential> dlist;
	protected List<Integer> assignments;
	
	private JFrame frame;
	protected String title;
	
	InputPanel ip;
	
	/**
	 * @param quantitativefilelist
	 * @param quantitativedir
	 * @param baits
	 * @param dlist
	 */
	public Quantitatives(String[] quantitativefilelist, File quantitativedir,
			String[] baits, List<Differential> dlist) {
		this.files = quantitativefilelist;
		this.inputdir = quantitativedir;
		this.baits = baits;
		this.dlist = dlist;
		
	}

	/**
	 * 
	 */
	public void run() {

		assignments = new ArrayList<Integer>();

		for (int i = 0; i < files.length; i++) {
			assignments.add(0);
		}

		createAndShowGUI();
	}

	protected void setTitle() {
		title = "Quantitative Resolver";
	}
	
	private void createAndShowGUI() {

		setTitle();
		
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		ip = new InputPanel();

		panel.add(ip);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL));

		JButton process = new JButton("Done");
		process.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				parseFiles();
				frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				frame.dispose();
			}
		});
		panel.add(process);

		frame.add(panel);

		// Display the window.
		frame.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_w = frame.getSize().width;
		int frame_h = frame.getSize().height;
		frame.setLocation((dim.width - frame_w) / 2, (dim.height - frame_h) / 2);

		frame.setVisible(true);

	}
	
	private JComboBox<String> getBox(String[] elements) {
		JComboBox<String> box = new JComboBox<String>();
		for (String element : elements) {
			box.addItem(element);
		}
		box.setSelectedIndex(0);
		return box;
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

				while ((dataline = dis.readLine()) != null) {

					if (dataline.startsWith("P")) {
						String [] element = dataline.split("\t");
						
						String pname;
						double value;
						
						if (element[1].startsWith("sp")) {
							
							String [] tmp2 = element[14].split("=");
							tmp2[2] = tmp2[2].replace(" PE", "");
						
							pname = tmp2[2].trim();
						} else {
							
							pname = element[14].split(" ")[0];
						}
						
						
						if (!element[6].equals("NA")) {
							value = Double.parseDouble(element[6]);
							if (value<0.1) value = 0;
						} else {
							value = 0;
						}
						
						dlist.get(baitindex).addValue(pname, value);
												
					}

				}

			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}
			
		}
		
	}
	
	private class InputPanel extends JPanel implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6808471768071049370L;
		List<JTextField> texts;
		List<JComboBox<String>> baitslist;
		
		public InputPanel() {

			texts = new ArrayList<JTextField>();
			baitslist = new ArrayList<JComboBox<String>>();
			
			setLayout(new GridLayout(0, 2));

			for (int i = 0; i < files.length; i++) {
				JTextField tfield = new JTextField(files[i]);
				tfield.setEditable(false);
				texts.add(tfield);

				JComboBox<String> box = getBox(baits);
				box.addActionListener(this);
				baitslist.add(box);

				add(texts.get(i));
				add(baitslist.get(i));
				
			}

		}

		public void actionPerformed(ActionEvent e) {

			int index;

			for (int i = 0; i < baitslist.size(); i++) {

				index = baitslist.get(i).getSelectedIndex();
								
				assignments.set(i, index);

			}

		}
	
	}
}
