/**
 * diego
 * Jun 7, 2013
 */
package edu.scripps.p3.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.scripps.p3.parsers.inputs.utilities.Configuration;

/**
 * @author diego
 *
 */
public class AdvancedMode {
	public static final Double CORRT = 0.01;
	public static final Double QUANTT = 2.0;
	public static final Double CORR = 0.2;
	public static final Double CLUS = 0.7;
	public static final Double QUANT = 0.01;
	public static final Double INTER = 1.0;
	public static final Double PHY = 0.6;
	public static final Double GEN = 0.2;
	public static final Double CONF = 0.5;
	public static final Double CONFORTO = 0.1;
	public static final String RAPID = "Y";
	public static final String QCLU = "N";

	AdvancedPanel ap = new AdvancedPanel();

	JFrame frame;
	JButton process;

	Configuration conf;

	public AdvancedMode(Configuration conf) {

		this.conf = conf;

		frame = new JFrame();
		frame.setTitle("Advanced Mode");

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Add content to the window.
		frame.add(ap);

		// Display the window.
		frame.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_w = frame.getSize().width;
		int frame_h = frame.getSize().height;
		frame.setLocation((dim.width - frame_w) / 2, (dim.height - frame_h) / 2);

		frame.setVisible(true);

	}

	public void getUserSelectedValues() {

		if (ap.isChanged()) {

			conf.setValid(true);

			conf.setCorrelationT(ap.getCorrelationT());
			conf.setQuantitativeLevel(ap.getQuantitativeT());
			conf.setCorrelationW(ap.getCorrelation());
			conf.setClusterW(ap.getCluster());
			conf.setQuantitativeW(ap.getQuantitative());
			conf.setInternalW(ap.getInternal());
			conf.setPhysicalW(ap.getPhysical());
			conf.setGeneticW(ap.getGenetic());
			conf.setConfidenceT(ap.getConfidence());
			conf.setConfidenceOrtoT(ap.getConfidenceOrto());
			conf.setRapidCorrelation(ap.getRapidCorrelation());
			conf.setQuantFeatures(ap.getQuantFeatures());

		} else {

			conf.setValid(false);
		}

	}

	private class AdvancedPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5914799000235165384L;

		private boolean changes;

		private static final String textFieldCorrelationThreshold = "Correlation Threshold [0-1]:";
		private static final String textFieldQuantitativeThreshold = "Quantitative Strictness Level [1-5]:";
		private static final String textFieldInternalCorrelation = "Internal Score - Correlation Weight:";
		private static final String textFieldInternalCluster = "Internal Score - Cluster Weight:";
		private static final String textFieldInternalQuantitative = "Internal Score - Quantitative Weight:";
		private static final String textFieldInternal = "Internal Score Weight:";
		private static final String textFieldPhysical = "Physical Score Weight:";
		private static final String textFieldGenetic = "Genetic Score Weight:";
		private static final String textFieldConfidence = "Confidence Threshold [0-1]:";
		private static final String textFieldConfidenceOrto = "Confidence Threshold Known[0-1]:";
		private static final String textFieldRapidCorrelation = "Use Rapid Correlation[Y/N]:";
		private static final String textFieldQuantFeatures = "Use Cluster Quant Features [Y/N]:";

		JButton reset;

		JTextField correlationTField;
		JTextField quantitativeTField;
		JTextField correlationField;
		JTextField clusterField;
		JTextField quantitativeField;
		JTextField internalField;
		JTextField physicalField;
		JTextField geneticField;
		JTextField confidenceField;
		JTextField confidenceOrtoField;
		JTextField rapidCorrelationField;
		JTextField quantFeaturesField;

		public AdvancedPanel() {

			changes = false;

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			GridBagLayout gridbag = new GridBagLayout();

			JPanel options = new JPanel();
			options.setLayout(gridbag);

			correlationTField = new JTextField(5);
			correlationTField.setActionCommand(textFieldCorrelationThreshold);
			correlationTField.setText(CORRT.toString());

			JLabel correlationTLabel = new JLabel(textFieldCorrelationThreshold);
			correlationTLabel.setLabelFor(correlationTField);

			quantitativeTField = new JTextField(5);
			quantitativeTField.setActionCommand(textFieldQuantitativeThreshold);
			quantitativeTField.setText(QUANTT.toString());

			JLabel quantitativeTLabel = new JLabel(textFieldQuantitativeThreshold);
			quantitativeTLabel.setLabelFor(quantitativeTField);

			correlationField = new JTextField(5);
			correlationField.setActionCommand(textFieldInternalCorrelation);
			correlationField.setText(CORR.toString());
			correlationField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel correlationLabel = new JLabel(textFieldInternalCorrelation);
			correlationLabel.setLabelFor(correlationField);

			clusterField = new JTextField(5);
			clusterField.setActionCommand(textFieldInternalCluster);
			clusterField.setText(CLUS.toString());
			clusterField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel clusterLabel = new JLabel(textFieldInternalCluster);
			clusterLabel.setLabelFor(clusterField);

			quantitativeField = new JTextField(5);
			quantitativeField.setActionCommand(textFieldInternalQuantitative);
			quantitativeField.setText(QUANT.toString());
			quantitativeField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel quantitativeLabel = new JLabel(textFieldInternalQuantitative);
			quantitativeLabel.setLabelFor(quantitativeField);

			internalField = new JTextField(5);
			internalField.setActionCommand(textFieldInternal);
			internalField.setText(INTER.toString());
			internalField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel internalLabel = new JLabel(textFieldInternal);
			internalLabel.setLabelFor(internalField);

			physicalField = new JTextField(5);
			physicalField.setActionCommand(textFieldPhysical);
			physicalField.setText(PHY.toString());
			physicalField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel physicalLabel = new JLabel(textFieldPhysical);
			physicalLabel.setLabelFor(physicalField);

			geneticField = new JTextField(5);
			geneticField.setActionCommand(textFieldGenetic);
			geneticField.setText(GEN.toString());
			geneticField.setToolTipText(
					"Enter any positive value, the best range is [0-1] but it will work for any value above or equal to zero");

			JLabel geneticLabel = new JLabel(textFieldGenetic);
			geneticLabel.setLabelFor(geneticField);

			confidenceField = new JTextField(5);
			confidenceField.setActionCommand(textFieldConfidence);
			confidenceField.setText(CONF.toString());
			confidenceField.setToolTipText("Enter any value in the [0-1] range");

			JLabel confidenceLabel = new JLabel(textFieldConfidence);
			confidenceLabel.setLabelFor(confidenceField);

			confidenceOrtoField = new JTextField(5);
			confidenceOrtoField.setActionCommand(textFieldConfidenceOrto);
			confidenceOrtoField.setText(CONFORTO.toString());
			confidenceOrtoField.setToolTipText(
					"Confidence for preys that have external interaction informations, range [0-1]. Usually it should be lower than the confidence threshold");

			JLabel confidenceOrtoLabel = new JLabel(textFieldConfidenceOrto);
			confidenceOrtoLabel.setLabelFor(confidenceOrtoField);

			rapidCorrelationField = new JTextField(5);
			rapidCorrelationField.setActionCommand(textFieldRapidCorrelation);
			rapidCorrelationField.setText(RAPID);
			rapidCorrelationField.setToolTipText("Run Rapid Correlation Yes/No [Y/N]");

			JLabel rapidCorrelationLabel = new JLabel(textFieldRapidCorrelation);
			rapidCorrelationLabel.setLabelFor(rapidCorrelationField);

			quantFeaturesField = new JTextField(5);
			quantFeaturesField.setActionCommand(textFieldQuantFeatures);
			quantFeaturesField.setText(QCLU);
			quantFeaturesField.setToolTipText("Use Quantitative Features for Clustering Yes/No [Y/N]");

			JLabel quantFeaturesLabel = new JLabel(textFieldQuantFeatures);
			quantFeaturesLabel.setLabelFor(quantFeaturesField);

			JLabel[] labels = { correlationTLabel, quantitativeTLabel, correlationLabel, clusterLabel,
					quantitativeLabel, internalLabel, physicalLabel, geneticLabel, confidenceLabel, confidenceOrtoLabel,
					rapidCorrelationLabel, quantFeaturesLabel };
			JTextField[] textFields = { correlationTField, quantitativeTField, correlationField, clusterField,
					quantitativeField, internalField, physicalField, geneticField, confidenceField, confidenceOrtoField,
					rapidCorrelationField, quantFeaturesField };

			addLabelTextRows(labels, textFields, gridbag, options);

			add(options);

			add(new JSeparator(SwingConstants.HORIZONTAL));

			process = new JButton("Confirm");
			process.addActionListener(new Listener());
			/*
			 * process.addActionListener(new ActionListener() { public void
			 * actionPerformed(ActionEvent e) {
			 * 
			 * } } );
			 */

			reset = new JButton("Reset");
			reset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					quantitativeTField.setText(QUANTT.toString());
					correlationTField.setText(CORRT.toString());
					correlationField.setText(CORR.toString());
					clusterField.setText(CLUS.toString());
					quantitativeField.setText(QUANT.toString());
					internalField.setText(INTER.toString());
					physicalField.setText(PHY.toString());
					geneticField.setText(GEN.toString());
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(reset);
			buttonPanel.add(process);

			add(buttonPanel);

		}

		private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, GridBagLayout gridbag,
				Container container) {
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.EAST;
			int numLabels = labels.length;

			for (int i = 0; i < numLabels; i++) {
				c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
				c.fill = GridBagConstraints.NONE; // reset to default
				c.weightx = 0.0; // reset to default
				container.add(labels[i], c);

				c.gridwidth = GridBagConstraints.REMAINDER; // end row
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1.0;
				container.add(textFields[i], c);
			}
		}

		public boolean isChanged() {
			return changes;
		}

		public void setisChanged(boolean b) {
			changes = b;
		}

		public double getCorrelationT() {
			double correlationT = 0;

			String value;

			try {
				value = correlationTField.getText();
			} catch (NullPointerException e) {
				value = CORRT.toString();
			}

			if (value.length() == 0) {
				value = CORRT.toString();
			}

			correlationT = Double.parseDouble(value);

			return correlationT;
		}

		public double getQuantitativeT() {
			double quantT = 0;

			String value;

			try {
				value = quantitativeTField.getText();
			} catch (NullPointerException e) {
				value = QUANTT.toString();
			}

			if (value.length() == 0) {
				value = QUANTT.toString();
			}

			quantT = Double.parseDouble(value);

			return quantT;
		}

		public double getCorrelation() {
			double corr = 0;

			String value;

			try {
				value = correlationField.getText();
			} catch (NullPointerException e) {
				value = CORR.toString();
			}

			if (value.length() == 0) {
				value = CORR.toString();
			}

			corr = Double.parseDouble(value);

			return corr;
		}

		public double getQuantitative() {
			double quant = 0;

			String value;

			try {
				value = quantitativeField.getText();
			} catch (NullPointerException e) {
				value = QUANT.toString();
			}

			if (value.length() == 0) {
				value = QUANT.toString();
			}

			quant = Double.parseDouble(value);

			return quant;
		}

		public double getCluster() {
			double clus = 0;

			String value;

			try {
				value = clusterField.getText();
			} catch (NullPointerException e) {
				value = CLUS.toString();
			}

			if (value.length() == 0) {
				value = CLUS.toString();
			}

			clus = Double.parseDouble(value);

			return clus;
		}

		public double getInternal() {
			double inter = 0;

			String value;

			try {
				value = internalField.getText();
			} catch (NullPointerException e) {
				value = INTER.toString();
			}

			if (value.length() == 0) {
				value = INTER.toString();
			}

			inter = Double.parseDouble(value);

			return inter;
		}

		public double getPhysical() {
			double phy = 0;

			String value;

			try {
				value = physicalField.getText();
			} catch (NullPointerException e) {
				value = PHY.toString();
			}

			if (value.length() == 0) {
				value = PHY.toString();
			}

			phy = Double.parseDouble(value);

			return phy;
		}

		public double getGenetic() {
			double gen = 0;

			String value;

			try {
				value = geneticField.getText();
			} catch (NullPointerException e) {
				value = GEN.toString();
			}

			if (value.length() == 0) {
				value = GEN.toString();
			}

			gen = Double.parseDouble(value);

			return gen;
		}

		public double getConfidence() {

			double confidence = 0;
			String value;

			try {
				value = confidenceField.getText();
			} catch (NullPointerException e) {
				value = CONF.toString();
			}

			if (value.length() == 0) {
				value = CONF.toString();
			}

			confidence = Double.parseDouble(value);

			return confidence;

		}

		public double getConfidenceOrto() {

			double confidence = 0;
			String value;

			try {
				value = confidenceOrtoField.getText();
			} catch (NullPointerException e) {
				value = CONFORTO.toString();
			}

			if (value.length() == 0) {
				value = CONFORTO.toString();
			}

			confidence = Double.parseDouble(value);

			return confidence;

		}

		public boolean getRapidCorrelation() {

			boolean rapid = false;
			String value;

			try {
				value = rapidCorrelationField.getText();
			} catch (NullPointerException e) {
				value = RAPID;
			}

			if (value.length() == 0) {
				value = RAPID;
			}

			if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("YES")) {
				rapid = true;
			} else {
				rapid = false;
			}

			return rapid;

		}

		public boolean getQuantFeatures() {

			boolean qfeatures = false;
			String value;

			try {
				value = quantFeaturesField.getText();
			} catch (NullPointerException e) {
				value = QCLU;
			}

			if (value.length() == 0) {
				value = QCLU;
			}

			if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("YES")) {
				qfeatures = true;
			} else {
				qfeatures = false;
			}

			return qfeatures;

		}

	}

	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {

			Object source = event.getSource();

			if (source instanceof Component) {
				Component c = (Component) source;
				Frame frame = JOptionPane.getFrameForComponent(c);

				ap.setisChanged(true);

				getUserSelectedValues();

				if (frame != null) {
					frame.dispose();
				}
			}

		}

	}

}
