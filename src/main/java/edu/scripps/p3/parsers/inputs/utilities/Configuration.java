/**
 * diego Jun 7, 2013
 */
package edu.scripps.p3.parsers.inputs.utilities;

import edu.scripps.p3.gui.AdvancedMode;

/**
 * @author diego
 *
 */
public class Configuration {

	private double correlationT;
	private double quantitativeLevel;
	private double correlationW;
	private double clusterW;
	private double quantitativeW;
	private double internalW;
	private double physicalW;
	private double geneticW;
	private boolean valid;
	private double confidenceT;
	private double confidenceOrtoT;
	private boolean rapidCorrelation;
	private boolean quantFeatures;
	private double spcCorrelationT;

	public Configuration() {
		defaultState();
	}

	public void defaultState() {
		setValid(true);
		setCorrelationT(AdvancedMode.CORRT);// 0.01);
		setQuantitativeLevel(AdvancedMode.QUANTT);// 2);
		setCorrelationW(AdvancedMode.CORR);// 0.1);
		setClusterW(AdvancedMode.CLUS);// 0.2);
		setQuantitativeW(AdvancedMode.QUANT);// 0.3);
		setInternalW(AdvancedMode.INTER);// 1.0);
		setPhysicalW(AdvancedMode.PHY);// 0.6);
		setGeneticW(AdvancedMode.GEN);// 0.2);
		setConfidenceT(AdvancedMode.CONF);// 0.5);
		setConfidenceOrtoT(AdvancedMode.CONFORTO);// 0.1);
		setRapidCorrelation(AdvancedMode.RAPID.equals("Y") ? true : false);// true);
		setQuantFeatures(AdvancedMode.QCLU.equals("Y") ? true : false);// false);
		// low complexity
		setSpcCorrelationT(0.25); // rSquaredThreshold
		// high complexity
		// setSpcCorrelationT(0.5);
	}

	/**
	 * @return the confidenceT
	 */
	public double getConfidenceT() {
		return confidenceT;
	}

	/**
	 * @param confidenceT
	 *            the confidenceT to set
	 */
	public void setConfidenceT(double confidenceT) {
		this.confidenceT = confidenceT;
	}

	/**
	 * @return the confidenceOrtoT
	 */
	public double getConfidenceOrtoT() {
		return confidenceOrtoT;
	}

	/**
	 * @param confidenceOrtoT
	 *            the confidenceOrtoT to set
	 */
	public void setConfidenceOrtoT(double confidenceOrtoT) {
		this.confidenceOrtoT = confidenceOrtoT;
	}

	/**
	 * @return the rapidCorrelation
	 */
	public boolean isRapidCorrelation() {
		return rapidCorrelation;
	}

	/**
	 * @param rapidCorrelation
	 *            the rapidCorrelation to set
	 */
	public void setRapidCorrelation(boolean rapidCorrelation) {
		this.rapidCorrelation = rapidCorrelation;
	}

	/**
	 * @return the quantFeatures
	 */
	public boolean isQuantFeatures() {
		return quantFeatures;
	}

	/**
	 * @param quantFeatures
	 *            the quantFeatures to set
	 */
	public void setQuantFeatures(boolean quantFeatures) {
		this.quantFeatures = quantFeatures;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @param valid
	 *            the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the correlationT
	 */
	public double getCorrelationT() {
		return correlationT;
	}

	/**
	 * @param correlationT
	 *            the correlationT to set
	 */
	public void setCorrelationT(double correlationT) {
		this.correlationT = correlationT;
	}

	/**
	 * @return the quantitativeLevel
	 */
	public double getQuantitativeLevel() {
		return quantitativeLevel;
	}

	/**
	 * @param quantitativeLevel
	 *            the quantitativeLevel to set
	 */
	public void setQuantitativeLevel(double quantitativeLevel) {
		this.quantitativeLevel = quantitativeLevel;
	}

	/**
	 * @return the correlationW
	 */
	public double getCorrelationW() {
		return correlationW;
	}

	/**
	 * @param correlationW
	 *            the correlationW to set
	 */
	public void setCorrelationW(double correlationW) {
		this.correlationW = correlationW;
	}

	/**
	 * @return the clusterW
	 */
	public double getClusterW() {
		return clusterW;
	}

	/**
	 * @param clusterW
	 *            the clusterW to set
	 */
	public void setClusterW(double clusterW) {
		this.clusterW = clusterW;
	}

	/**
	 * @return the quantitativeW
	 */
	public double getQuantitativeW() {
		return quantitativeW;
	}

	/**
	 * @param quantitativeW
	 *            the quantitativeW to set
	 */
	public void setQuantitativeW(double quantitativeW) {
		this.quantitativeW = quantitativeW;
	}

	/**
	 * @return the internalW
	 */
	public double getInternalW() {
		return internalW;
	}

	/**
	 * @param internalW
	 *            the internalW to set
	 */
	public void setInternalW(double internalW) {
		this.internalW = internalW;
	}

	/**
	 * @return the physicalW
	 */
	public double getPhysicalW() {
		return physicalW;
	}

	/**
	 * @param physicalW
	 *            the physicalW to set
	 */
	public void setPhysicalW(double physicalW) {
		this.physicalW = physicalW;
	}

	/**
	 * @return the geneticW
	 */
	public double getGeneticW() {
		return geneticW;
	}

	/**
	 * @param geneticW
	 *            the geneticW to set
	 */
	public void setGeneticW(double geneticW) {
		this.geneticW = geneticW;
	}

	public double getSpcCorrelationT() {
		return spcCorrelationT;
	}

	/**
	 * @param spcCorrelationT
	 *            the spcCorrelationT to set
	 */
	public void setSpcCorrelationT(double spcCorrelationT) {
		this.spcCorrelationT = spcCorrelationT;
	}

}
