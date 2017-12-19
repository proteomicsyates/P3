/**
 * diego May 24, 2013
 */
package edu.scripps.p3.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.parsers.inputs.utilities.Configuration;

/**
 * @author diego
 *
 */
public class P3mainFrame {

	private static Object lock;

	public final static double version = 0.2;

	private MainPanel mp;
	private JFrame frame;

	private List<Experiment> elist; // input file data
	private List<Differential> qlist; // quant file data
	private List<Differential> llist; // lysate file data
	private List<Orthogonal> olist; // orthogonal data

	private boolean lysate;
	private boolean quantitative;
	private boolean physical;
	private boolean genetic;

	private boolean bonus;
	private boolean indirect;
	private boolean advanced;

	private File outdir;
	private File logdir;
	private File topodir;

	private String[] baits;
	private String[] experiments;

	private Configuration configuration;

	/**
	 * @param elist
	 * @param qlist
	 * @param llist
	 * @param olist
	 * @param quantitative
	 * @param lysate
	 * @param physical
	 * @param genetic
	 * @param bonus
	 * @param indirect
	 * @param advanced
	 * @param outdir
	 * @param logdir
	 * @param topodir
	 * @param baits
	 * @param experiments
	 * @param configuration
	 * @param lock
	 */
	public P3mainFrame(List<Experiment> elist, List<Differential> qlist, List<Differential> llist,
			List<Orthogonal> olist, boolean quantitative, boolean lysate, boolean physical, boolean genetic,
			boolean bonus, boolean indirect, boolean advanced, File outdir, File logdir, File topodir, String[] baits,
			String[] experiments, Configuration configuration, Object lock) {
		this.elist = elist;
		this.qlist = qlist;
		this.llist = llist;
		this.olist = olist;
		this.quantitative = quantitative;
		this.lysate = lysate;
		this.physical = physical;
		this.genetic = genetic;
		this.bonus = bonus;
		this.indirect = indirect;
		this.advanced = advanced;
		this.outdir = outdir;
		this.logdir = logdir;
		this.topodir = topodir;
		this.baits = baits;
		this.experiments = experiments;
		this.configuration = configuration;
		this.lock = lock;
	}

	public void run() {
		// Create and set up the window.
		frame = new JFrame("Protein-Protein Predictor  v" + version);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mp = new MainPanel();
		frame.add(mp);

		// Display the window.
		frame.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_w = frame.getSize().width;
		int frame_h = frame.getSize().height;
		frame.setLocation((dim.width - frame_w) / 2, (dim.height - frame_h) / 2);

		frame.setVisible(true);

	}

	public boolean isVisible() {
		return frame.isVisible();
	}

	/**
	 * @return the elist
	 */
	public List<Experiment> getElist() {
		return elist;
	}

	/**
	 * @return the qlist
	 */
	public List<Differential> getQlist() {
		return qlist;
	}

	/**
	 * @return the llist
	 */
	public List<Differential> getLlist() {
		return llist;
	}

	/**
	 * @return the olist
	 */
	public List<Orthogonal> getOlist() {
		return olist;
	}

	/**
	 * @return the lysate
	 */
	public boolean isLysate() {
		return lysate;
	}

	/**
	 * @return the quantitative
	 */
	public boolean isQuantitative() {
		return quantitative;
	}

	/**
	 * @return the physical
	 */
	public boolean isPhysical() {
		return physical;
	}

	/**
	 * @return the genetic
	 */
	public boolean isGenetic() {
		return genetic;
	}

	/**
	 * @return the bonus
	 */
	public boolean isBonus() {
		return bonus;
	}

	/**
	 * @return the indirect
	 */
	public boolean isIndirect() {
		return indirect;
	}

	/**
	 * @return the advanced
	 */
	public boolean isAdvanced() {
		return advanced;
	}

	/**
	 * @return the outdir
	 */
	public File getOutdir() {
		return outdir;
	}

	/**
	 * @return the logdir
	 */
	public File getLogdir() {
		return logdir;
	}

	/**
	 * @return the topodir
	 */
	public File getTopodir() {
		return topodir;
	}

	/**
	 * @return the baits
	 */
	public String[] getBaits() {
		return baits;
	}

	/**
	 * @return the experiments
	 */
	public String[] getExperiments() {
		return experiments;
	}

	/**
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	private class MainPanel extends JPanel {

		JButton process;
		JButton reset;

		OptionsPanel op;
		CheckPanel cp;

		public MainPanel() {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				e.printStackTrace();
			}
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			op = new OptionsPanel();
			add(op);

			add(new JSeparator(SwingConstants.HORIZONTAL));

			cp = new CheckPanel();
			add(cp);

			add(new JSeparator(SwingConstants.HORIZONTAL));

			process = new JButton("Run");
			process.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getValueFromGuiForP3();
					dispose();

				}
			});

			reset = new JButton("Reset");
			reset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					reset();
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(reset);
			buttonPanel.add(process);

			add(buttonPanel);
		}

		private void dispose() {
			synchronized (lock) {
				frame.dispose();
				lock.notify();
			}
		}

		private void getValueFromGuiForP3() {

			baits = op.getBaits();
			experiments = op.getExperiments();

			quantitative = op.isQuantitative();
			lysate = op.isLysate();
			physical = op.isPhysical();
			genetic = op.isGenetic();

			outdir = op.getOutdir();
			logdir = op.getLogdir();
			topodir = op.getTopodir();

			elist = op.getElist();
			if (quantitative) {
				qlist = op.getQlist();
			} else {
				qlist = null;
			}
			if (lysate) {
				llist = op.getLlist();
			} else {
				llist = null;
			}
			if (physical || genetic) {
				olist = op.getOlist();
			} else {
				olist = null;
			}

			indirect = cp.getIndirectTopology();
			bonus = cp.getFinalBonus();
			advanced = cp.getAdvanced();
			if (advanced) {
				configuration = cp.getConfiguration();
			} else {
				configuration = new Configuration();
				configuration.defaultState();
				configuration.setQuantFeatures(quantitative);
			}

		}

		private void reset() {
			op.reset();
			cp.reset();
		}

	}

}
