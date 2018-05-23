/**
 * diego
 * Jun 12, 2013
 */
package edu.scripps.p3.parsers.orthogonals;

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

import org.apache.log4j.Logger;

import edu.scripps.p3.experimentallist.Orthogonal;

/**
 * @author diego
 * 
 */
public class Orthogonals {
	private static final Logger log = Logger.getLogger(Orthogonal.class);
	String[] files;
	File inputdir;

	boolean physical;
	boolean genetic;

	List<Orthogonal> olist;

	List<Integer> assignments;
	List<Double> coefficients;

	JFrame frame;
	String title;
	InputPanel ip;

	String[] categories;
	String[] conflevel;

	/**
	 * @param orthogonalfilelist
	 * @param orthogonaldir
	 * @param physical
	 * @param genetic
	 * @param olist
	 */
	public Orthogonals(String[] orthogonalfilelist, File orthogonaldir, boolean physical, boolean genetic,
			List<Orthogonal> olist) {
		this.files = orthogonalfilelist;
		this.inputdir = orthogonaldir;
		this.physical = physical;
		this.genetic = genetic;
		this.olist = olist;
	}

	/**
	 * 
	 */
	public void run() {

		assignments = new ArrayList<Integer>();
		coefficients = new ArrayList<Double>();

		categories = new String[2];
		categories[0] = Orthogonal.PHYSICAL;
		categories[1] = Orthogonal.GENETIC;

		conflevel = new String[4];
		conflevel[0] = "LOW";
		conflevel[1] = "MEDIUM";
		conflevel[2] = "HIGH";
		conflevel[3] = "CUSTOM";

		for (int i = 0; i < files.length; i++) {
			assignments.add(0);
			coefficients.add(1.0);
		}

		createAndShowGUI();
	}

	private void setTitle() {
		title = "Orthogonal Resolver";
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
			@Override
			public void actionPerformed(ActionEvent e) {

				ip.getUserSelections();
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

	private void parseFiles() {

		physical = false;
		genetic = false;
		log.info("Parsing orthogonal data files");
		File f;
		for (int i = 0; i < files.length; i++) {

			olist.get(i).setCoefficient(coefficients.get(i));
			String type = null;
			if (assignments.get(i) == 0) {
				olist.get(i).setType(Orthogonal.PHYSICAL);
				type = Orthogonal.PHYSICAL;
				physical = true;
			} else {
				olist.get(i).setType(Orthogonal.GENETIC);
				type = Orthogonal.GENETIC;
				genetic = true;
			}

			f = new File(inputdir, files[i]);
			log.info("Parsing file " + f.getAbsolutePath() + " as " + type + " and weight=" + coefficients.get(i));

			FileInputStream fis;

			try {
				fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

				String dataline;
				double value;
				int counter = 0;
				while ((dataline = dis.readLine()) != null) {

					String[] element = dataline.split("\t");
					String key = element[0] + "_" + element[1];

					value = Double.parseDouble(element[2]);
					olist.get(i).addEntry(key, value);
					counter++;
				}
				log.info(counter + " protein interactions readed. ");
			} catch (FileNotFoundException e) {
				System.err.println("file not found");
			} catch (IOException e) {
				System.err.println("unable to read file");
			}

		}

	}

	private JComboBox<String> getBox(String[] values) {
		JComboBox<String> box = new JComboBox<String>();

		for (int i = 0; i < values.length; i++) {
			box.addItem(values[i]);
		}

		box.setSelectedIndex(0);
		return box;
	}

	private class InputPanel extends JPanel implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2739760369341012098L;
		/**
		 * 
		 */

		List<JTextField> texts;
		List<JComboBox<String>> typelist;
		List<JComboBox<String>> conflist;
		List<JTextField> coeffs;

		public InputPanel() {

			texts = new ArrayList<JTextField>();
			typelist = new ArrayList<JComboBox<String>>();
			conflist = new ArrayList<JComboBox<String>>();
			coeffs = new ArrayList<JTextField>();

			setLayout(new GridLayout(0, 4));

			for (int i = 0; i < files.length; i++) {
				JTextField tfield = new JTextField(files[i]);
				tfield.setEditable(false);
				texts.add(tfield);

				JComboBox<String> box = getBox(categories);
				box.addActionListener(this);
				typelist.add(box);

				JComboBox<String> box1 = getBox(conflevel);
				box1.addActionListener(this);
				conflist.add(box1);

				JTextField cfield = new JTextField("0.5");
				cfield.setToolTipText("Type confidence [0-1]");
				cfield.setEditable(true);
				cfield.setEnabled(false);
				cfield.addActionListener(this);
				coeffs.add(cfield);

				add(texts.get(i));
				add(typelist.get(i));
				add(conflist.get(i));
				add(coeffs.get(i));

			}

		}

		public void getUserSelections() {

			int typeSelectedIndex;
			int confidenceSelectedIndex;
			String text;
			double value = 0;

			for (int i = 0; i < coeffs.size(); i++) {

				typeSelectedIndex = typelist.get(i).getSelectedIndex();
				assignments.set(i, typeSelectedIndex);

				confidenceSelectedIndex = conflist.get(i).getSelectedIndex();

				if (confidenceSelectedIndex == 3) {

					text = coeffs.get(i).getText();

					if (text.length() == 0) {
						value = 0.5;
					} else {
						value = Double.parseDouble(text);
					}

					coefficients.set(i, value);

				} else {

					switch (confidenceSelectedIndex) {
					case 0:
						value = 0.1;
						coefficients.set(i, 0.1);
						break;
					case 1:
						value = 0.5;
						coefficients.set(i, 0.5);
						break;
					case 2:
						value = 0.9;
						coefficients.set(i, 0.9);
						break;
					}

				}
				coeffs.get(i).setText(String.valueOf(value));
			}

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			int index;
			int index2;
			String text;
			double value = 0;

			for (int i = 0; i < coeffs.size(); i++) {

				index = typelist.get(i).getSelectedIndex();

				assignments.set(i, index);

				index2 = conflist.get(i).getSelectedIndex();

				if (index2 == 3) {
					coeffs.get(i).setEnabled(true);

					text = coeffs.get(i).getText();

					if (text.length() == 0) {
						value = 0.5;
					} else {
						value = Double.parseDouble(text);
					}

					coefficients.set(i, value);

				} else {

					switch (index2) {
					case 0:
						value = 0.1;
						coefficients.set(i, 0.1);
						break;
					case 1:
						value = 0.5;
						coefficients.set(i, 0.5);
						break;
					case 2:
						value = 0.9;
						coefficients.set(i, 0.9);
						break;
					}

				}
				coeffs.get(i).setText(String.valueOf(value));

			}

		}

	}

}
