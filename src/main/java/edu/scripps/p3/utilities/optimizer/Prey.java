/**
 * diego
 * Aug 14, 2014
 */
package edu.scripps.p3.utilities.optimizer;

/**
 * @author diego
 *
 */
public class Prey {

	private String name;
	private double correlation;
	private double cluster;
	private double quant;
	private double qratio;
	private double phy;
	private double gen;
	private boolean known;

	public Prey(String name, double corr, double clus, double quant, double phy, double gen) {
		this.name = name;
		this.correlation = corr;
		this.cluster = clus;
		this.quant = quant;
		this.phy = phy;
		this.gen = gen;
		this.known = false;
		if (this.phy > 0.1) {
			this.known = true;
		}

	}

	/**
	 * @return the qratio
	 */
	public double getQratio() {
		return qratio;
	}

	/**
	 * @param qratio
	 *            the qratio to set
	 */
	public void setQratio(double qratio) {
		this.qratio = qratio;
	}

	/**
	 * @return the known
	 */
	public boolean isKnown() {
		return known;
	}

	/**
	 * @param known
	 *            the known to set
	 */
	public void setKnown(boolean known) {
		this.known = known;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the correlation
	 */
	public double getCorrelation() {
		return correlation;
	}

	/**
	 * @return the cluster
	 */
	public double getCluster() {
		return cluster;
	}

	/**
	 * @return the quant
	 */
	public double getQuant() {
		return quant;
	}

	/**
	 * @return the phy
	 */
	public double getPhy() {
		return phy;
	}

	/**
	 * @return the gen
	 */
	public double getGen() {
		return gen;
	}

}
