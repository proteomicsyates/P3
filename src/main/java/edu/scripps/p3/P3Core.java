/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3;


import java.io.File;
import java.util.List;

import edu.scripps.p3.calculators.ConfidenceCalculator;
import edu.scripps.p3.calculators.ConfidenceStratifier;
import edu.scripps.p3.cluterer.ConfidenceCluster;
import edu.scripps.p3.cluterer.StratifyCluster;
import edu.scripps.p3.cluterer.utilities.FeaturesCalculator;
import edu.scripps.p3.correlator.ComplexCorrelator;
import edu.scripps.p3.correlator.ComplexFilter;
import edu.scripps.p3.correlator.utilities.Complex;
import edu.scripps.p3.correlator.utilities.InteractomesCleaner;
import edu.scripps.p3.experimentallist.Differential;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.experimentallist.Interactome;
import edu.scripps.p3.experimentallist.Orthogonal;
import edu.scripps.p3.orthogonal.OrthogonalInserter;
import edu.scripps.p3.orthogonal.OrthogonalRecall;
import edu.scripps.p3.parsers.inputs.utilities.Configuration;
import edu.scripps.p3.quantitative.LysateTrendFinder;
import edu.scripps.p3.quantitative.QuantTrendFinder;
import edu.scripps.p3.topology.IndirectTopologyCalculator;
import edu.scripps.p3.topology.TopologyCreator;
import edu.scripps.p3.topology.mapcreator.DifferentialExpression;

/**
 * This class invokes the methods to go from convoluted complex data to interaction networks
 * @author diego
 * @version 1.0
 */
public class P3Core {

	private String[] baits;

	private List<Experiment> elist; // input file data
	private List<Differential> qlist; // quant file data
	private List<Differential> llist; // lysate file data
	private List<Orthogonal> olist; // orthogonal data

	private File logdir;
	private File topodir;

	private List<List<Complex>> complex_list;
	private List<List<Interactome>> interactomes;

	private boolean quantitative;
	private boolean lysate;
	private boolean physical;
	private boolean genetic;
	private boolean indirect;
	private boolean bonus;
	
	private Configuration configuration;
	
	private boolean debug = true;

	/**
	 * Constructor that receives the input values from the gui
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
	 * @author diego
	 */
	public P3Core(List<Experiment> elist,
			List<Differential> qlist, List<Differential> llist,
			List<Orthogonal> olist, boolean quantitative, boolean lysate,
			boolean physical, boolean genetic, boolean bonus,
			boolean indirect, boolean advanced, File outdir, File logdir,
			File topodir, String[] baits, String[] experiments,
			Configuration configuration) {
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
		this.logdir = logdir;
		this.topodir = topodir;
		this.baits = baits;
		this.configuration = configuration;
	}

	/**
	 * @author diego
	 */
	public void run() {

		long start=0,end=0;
		
		OrthogonalRecall or = new OrthogonalRecall(baits, logdir);
		or.setOrto(olist);
		or.setPhase("PreP3");
		or.rawfilter(elist);
		or.rawstats(elist);
		
		//------------------------------------------------------------------------------------
		// CORRELATION
		//--> calculate trend between protein
		if (debug) {
			System.out.print("ComplexCorrelator:\t");
			start = System.currentTimeMillis();
		}
		ComplexCorrelator cc = new ComplexCorrelator(elist,logdir, configuration.isRapidCorrelation());
		cc.setBaits(baits);
		cc.run();
		complex_list = cc.getComplexList();
		System.gc();
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		
		//--> calculating complexes based on frequencies
		if (debug) {
			System.out.print("ComplexFilter:\t");
			start = System.currentTimeMillis();
		}
		ComplexFilter cf = new ComplexFilter(complex_list, logdir);
		cf.setCorrelationThreshold(configuration.getCorrelationT());
		cf.run();
		interactomes = cf.getInteractomes();
		complex_list = null;
		System.gc();
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		
		InteractomesCleaner ic = new InteractomesCleaner(interactomes, baits);
		ic.run();
		interactomes = ic.getInteractomes();
		System.gc();
		
		//------------------------------------------------------------------------------------
		
		or.setPhase("Correlation");
		or.calculate(interactomes);
		
		//------------------------------------------------------------------------------------
		// CLUSTER
		//--> features calculator
		if (debug) {
			System.out.print("FeaturesCalculator:\t");
			start = System.currentTimeMillis();
		}
		
		FeaturesCalculator fc = new FeaturesCalculator(elist, interactomes, configuration.isQuantFeatures(), qlist);
		fc.run();
		interactomes = fc.getInteractomes();
	//	elist = null;
		System.gc();
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		
		//--> cluster
		if (debug) {
			System.out.print("ConfidenceCluster:\t");
			start = System.currentTimeMillis();
		}
		ConfidenceCluster ccl = new ConfidenceCluster(interactomes, logdir);
		ccl.run();
		interactomes = ccl.getInteractomes();
		System.gc();
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		
		
	//I disabled this module because it messes up the clustering values normalizing them.
		
		 
		//--> stratify values
	//	if (debug) {
	//		System.out.print("StratifyCluster:\t");
	//		start = System.currentTimeMillis();
	//	}
	//	StratifyCluster sc = new StratifyCluster(interactomes, baits);
	//	sc.run();
	//	interactomes = sc.getInteractomes();
	//	System.gc();
	//	if (debug) {
	//		end = System.currentTimeMillis();
	//		System.out.println((end-start));
	//	}
		//------------------------------------------------------------------------------------ 
		
		or.setPhase("Cluster");
		or.calculate(interactomes);
		
		//------------------------------------------------------------------------------------
		// QUANTITATIVE
		//--> quantitative trends
		if (quantitative) {
			if (debug) {
				System.out.print("QuantTrendFinder:\t");
				start = System.currentTimeMillis();
			}
			QuantTrendFinder qtf = new QuantTrendFinder(qlist, interactomes, logdir);
			qtf.setLevel(configuration.getQuantitativeLevel());
			qtf.run();
			interactomes = qtf.getInteractomes();
			System.gc();
			if (debug) {
				end = System.currentTimeMillis();
				System.out.println((end-start));
			}
		}
		//------------------------------------------------------------------------------------

		or.setPhase("Quantitative");
		or.calculate(interactomes);
		
		//------------------------------------------------------------------------------------
		// ORTHOGONAL DATA
		//--> physical and genetic information from external databases
		if (physical || genetic) {
			
			if (debug) {
				System.out.print("OrthogonalInserter:\t");
				start = System.currentTimeMillis();
			}
			OrthogonalInserter oi = new OrthogonalInserter(interactomes, olist);
			oi.run();
			interactomes = oi.getInteractomes(0);
			System.gc();
			if (debug) {
				end = System.currentTimeMillis();
				System.out.println((end-start));
			}
		}
		//------------------------------------------------------------------------------------

		//------------------------------------------------------------------------------------
		// LYSATE
		//--> lysate trends
		if (lysate) {

			if (debug) {
				System.out.print("LysateTrendFinder:\t");
				start = System.currentTimeMillis();
			}
			LysateTrendFinder ltf = new LysateTrendFinder(qlist, llist, interactomes);
			ltf.setT(0.8, 1.25); //TODO add in the advanced panel? I don't think so, because we want only to characterize a trend
			ltf.run();
			interactomes = ltf.getInteractomes();
			if (debug) {
				end = System.currentTimeMillis();
				System.out.println((end-start));
			}
		}
		//------------------------------------------------------------------------------------

		//------------------------------------------------------------------------------------
		// CONFIDENCE SCORE
		//--> calculate scores
		if (debug) {
			System.out.print("ConfidenceCalculator:\t");
			start = System.currentTimeMillis();
		}
		ConfidenceCalculator ccal = new ConfidenceCalculator(interactomes, baits, logdir);
		ccal.setCoeff(configuration.getInternalW(), configuration.getPhysicalW(), configuration.getGeneticW());
		ccal.setInternalCoeff(configuration.getCorrelationW(), configuration.getClusterW(), configuration.getQuantitativeW());
		ccal.setBonus(bonus);
		ccal.setFinalConfidences(configuration.getConfidenceT(), configuration.getConfidenceOrtoT());
		ccal.run();
		interactomes = ccal.getInteractomes();
			
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		//------------------------------------------------------------------------------------

		or.setPhase("Confidence");
		or.calculate(interactomes);
		or.writeLog();
		
		or.finalStatistics(interactomes);
		
		
		//------------------------------------------------------------------------------------
		// TOPOLOGY
		//--> indirect edges calculator
		IndirectTopologyCalculator itc = null;
		if (indirect) {
			if (debug) {
				System.out.print("IndirectTopologyCalculator:\t");
				start = System.currentTimeMillis();
			}
			itc = new IndirectTopologyCalculator(interactomes, baits, ccal.getMaps(), ccal.getMaps_names());
			itc.run();
			if (debug) {
				end = System.currentTimeMillis();
				System.out.println((end-start));
			}
		}
		
		//--> maps creator
		if (debug) {
			System.out.print("TopologyCreator:\t");
			start = System.currentTimeMillis();
		}
		
		if (!quantitative) {
			DifferentialExpression de = new DifferentialExpression(interactomes, baits, elist, qlist);
			de.run();
			qlist = de.getQlist();
		}
		
		TopologyCreator tc = new TopologyCreator(ccal.getMaps(), ccal.getMaps_names(), topodir, qlist);
		if (indirect) {
			tc.setIndirectEdges(itc.getIndirectEdges());
		}
		tc.run();
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println((end-start));
		}
		//------------------------------------------------------------------------------------
	
	}

}
