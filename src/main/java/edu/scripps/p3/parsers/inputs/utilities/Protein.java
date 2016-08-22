package edu.scripps.p3.parsers.inputs.utilities;

public class Protein {

	String name;
	String locus;
	double pcount;
	double scount;
	double coverage;
	double length;
	double mw;
	double pi;
	double apv;
	double apv_lconf;
	double apv_hconf;
	double max_theoretical_coverage;

	/**
	 * @param name
	 * @param locus
	 * @param pcount
	 * @param scount
	 * @param coverage
	 * @param length
	 * @param mw
	 * @param pi
	 */
	public Protein(String name, String locus, double pcount, double scount,
			double coverage, double length, double mw, double pi) {
		super();
		this.name = name;
		this.locus = locus;
		this.pcount = pcount;
		this.scount = scount;
		this.coverage = coverage;
		this.length = length;
		this.mw = mw;
		this.pi = pi;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the locus
	 */
	public String getLocus() {
		return locus;
	}

	/**
	 * @param locus
	 *            the locus to set
	 */
	public void setLocus(String locus) {
		this.locus = locus;
	}

	/**
	 * @return the pcount
	 */
	public double getPcount() {
		return pcount;
	}

	/**
	 * @param pcount
	 *            the pcount to set
	 */
	public void setPcount(double pcount) {
		this.pcount = pcount;
	}

	/**
	 * @return the scount
	 */
	public double getScount() {
		return scount;
	}

	/**
	 * @param scount
	 *            the scount to set
	 */
	public void setScount(double scount) {
		this.scount = scount;
	}

	/**
	 * @return the coverage
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * @param coverage
	 *            the coverage to set
	 */
	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	/**
	 * @return the length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(double length) {
		this.length = length;
	}

	/**
	 * @return the mw
	 */
	public double getMw() {
		return mw;
	}

	/**
	 * @param mw
	 *            the mw to set
	 */
	public void setMw(double mw) {
		this.mw = mw;
	}

	/**
	 * @return the pi
	 */
	public double getPi() {
		return pi;
	}

	/**
	 * @param pi the pi to set
	 */
	public void setPi(double pi) {
		this.pi = pi;
	}

	/**
	 * @return the apv
	 */
	public double getApv() {
		return apv;
	}

	/**
	 * @param apv the apv to set
	 */
	public void setApv(double apv) {
		this.apv = apv;
	}
	
	public double getApvLow() {
		return apv_lconf;
	}
	
	public double getApvHigh() {
		return apv_hconf;
	}
	
	public void setMaxTheoCoverage(double mtc) {
		this.max_theoretical_coverage=mtc;
	}
	
	public double normalizeCoverage() {
			
		this.coverage = coverage/max_theoretical_coverage*100;
		if (coverage>100) coverage = 100;
		return coverage;
	}
	
	/**
	 * calculate APV
	 */
	public void calculateApv() {
		
		this.apv = this.scount/this.coverage*100;
		double confidence = (100-this.coverage)/100;
		this.apv_lconf = this.apv - this.apv*confidence;
		this.apv_hconf = this.apv + this.apv*confidence;
		
		
	}

}
