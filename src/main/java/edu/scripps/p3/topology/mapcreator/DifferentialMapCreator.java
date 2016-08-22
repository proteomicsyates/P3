/**
 * diego
 * May 14, 2013
 */
package edu.scripps.p3.topology.mapcreator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.scripps.p3.experimentallist.Differential;

/**
 * @author diego
 * 
 */
public class DifferentialMapCreator {

	StringBuffer map;
	Differential qlist;
	String bait;
	Hashtable<String, String> interactions1;
	Hashtable<String, String> interactions2;

	Hashtable<String, String> qratios;

	String indirect;
	boolean indirect_available;

	/**
	 * @param string
	 * @param hashtable
	 * @param hashtable2
	 * @param qlist
	 */
	public DifferentialMapCreator(String name,
			Hashtable<String, String> table1, Hashtable<String, String> table2,
			Differential qlist) {
		this.bait = name.split("_")[0];
		this.interactions1 = table1;
		this.interactions2 = table2;
		this.qlist = qlist;
		this.indirect_available = false;
	}

	/**
	 * 
	 */
	public void run() {

		map = new StringBuffer();

		String key;
		String[] values1;
		String[] values2;

		Hashtable<String, String> lysate = new Hashtable<String, String>();
		Hashtable<String, String> qtrend = new Hashtable<String, String>();
		Hashtable<String, String> upconfidence = new Hashtable<String, String>();
		Hashtable<String, String> downconfidence = new Hashtable<String, String>();
		Hashtable<String, Double> conf = new Hashtable<String, Double>();
		List<String> cnames = new ArrayList<String>();

		Enumeration<String> enumKey = interactions1.keys();

		double val;

		map.append("*edges\n");

		while (enumKey.hasMoreElements()) {
			key = enumKey.nextElement();

			values1 = interactions1.get(key).split("_");

			if (interactions2.containsKey(key)) {
				values2 = interactions2.get(key).split("_");

				for (int i = 0; i < values1.length - 2; i++) {

					val = (Double.parseDouble(values1[i]) + Double
							.parseDouble(values2[i])) / 2;

					if (val != 0) {
						map.append(bait + "\t" + key + "\ti " + (i + 1)
								+ "\tc " + val + "\n");
					}

				}

				if (Double.parseDouble(values1[4]) == 1) {
					lysate.put(key, key);
				}

				if (Double.parseDouble(values1[5]) == 1) {
					qtrend.put(key, key);
				}

				if (Double.parseDouble(values1[0]) > Double
						.parseDouble(values2[0])) {
					downconfidence.put(key, key);
				}

				if (Double.parseDouble(values1[0]) < Double
						.parseDouble(values2[0])) {
					upconfidence.put(key, key);
				}

				val = (Double.parseDouble(values1[0]) + Double
						.parseDouble(values2[0])) / 2;

				val = scaleVal(val);

				conf.put(key, val);
				cnames.add(key);

			} else {
				for (int i = 0; i < values1.length - 2; i++) {
					if (Double.parseDouble(values1[i]) != 0) {
						map.append(bait + "\t" + key + "\ti " + (i + 1)
								+ "\tc " + values1[i] + "\n");
					}

				}

				if (Double.parseDouble(values1[4]) == 1) {
					lysate.put(key, key);
				}

				if (Double.parseDouble(values1[5]) == 1) {
					qtrend.put(key, key);
				}

				val = Double.parseDouble(values1[0]);
				val = scaleVal(val);

				conf.put(key, val);
				cnames.add(key);
			}

		}

		enumKey = interactions2.keys();

		while (enumKey.hasMoreElements()) {

			key = enumKey.nextElement();

			if (!interactions1.containsKey(key)) {
				values1 = interactions2.get(key).split("_");

				for (int i = 0; i < values1.length - 2; i++) {
					if (Double.parseDouble(values1[i]) != 0) {
						map.append(bait + "\t" + key + "\ti " + (i + 1)
								+ "\tc " + values1[i] + "\n");
					}

				}

				if (Double.parseDouble(values1[4]) == 1) {
					lysate.put(key, key);
				}

				if (Double.parseDouble(values1[5]) == 1) {
					qtrend.put(key, key);
				}

				val = Double.parseDouble(values1[0]);

				val = scaleVal(val);

				conf.put(key, val);
				cnames.add(key);
			}

		}

		quantCorrection(conf, cnames);

		if (indirect_available) {
			map.append(indirect);
		}

		map.append("*nodes\n");

		if (indirect_available) {
			cnames = getCustomSort(cnames);
		} else {
			Collections.sort(cnames);
		}

		double angle_step = 360.0 / ((double) cnames.size() + 1.0);
		angle_step = Math.toRadians(angle_step);

		double hypot;
		double alpha;

		double x;
		double y;

		int R, G, B;
		int shape;

		for (int i = 0; i < cnames.size(); i++) {

			if (cnames.get(i).equals(bait)) {

				x = 0.5;
				y = 0.5;
				R = Color.YELLOW.getRed();
				G = Color.YELLOW.getGreen();
				B = Color.YELLOW.getBlue();
				shape = 0;
				
				map.append(cnames.get(i) + "\t" + (float) x + "\t"
						+ (float) y + "\tc " + R + "," + G + "," + B
						+ "\ts " + shape + "\ta \"" + cnames.get(i)
						+ "\"\n");

			} else {

				hypot = conf.get(cnames.get(i));
				alpha = angle_step * i;

				x = hypot * Math.cos(alpha);
				y = hypot * Math.sin(alpha);

				x = x / 2 + 0.5;
				y = y / 2 + 0.5;

				Color color = getColor(cnames.get(i));

				R = color.getRed();
				G = color.getGreen();
				B = color.getBlue();

				shape = 0;

				// if (upconfidence.containsKey(cnames.get(i))) {
				// shape = 2;
				// }
				// if (downconfidence.containsKey(cnames.get(i))) {
				// shape = 3;
				// }
				if (lysate.containsKey(cnames.get(i))) {
					shape = 1;
				}
				if (qtrend.containsKey(cnames.get(i))) {
					shape = 2;
				}

				if (qratios.containsKey(cnames.get(i))) {
					shape = 3;
					map.append(cnames.get(i) + "\t" + (float) x + "\t"
							+ (float) y + "\tc " + R + "," + G + "," + B
							+ "\ts " + shape + "\ta \"" + cnames.get(i)
							+ ", SR:" + qratios.get(cnames.get(i)) + "\"\n");
				} else {
					map.append(cnames.get(i) + "\t" + (float) x + "\t"
							+ (float) y + "\tc " + R + "," + G + "," + B
							+ "\ts " + shape + "\ta \"" + cnames.get(i)
							+ "\"\n");
				}

			}
		}

	}

	private Color getColor(String name) {

		int r = 0, g = 0, b = 0;
		Color color;

		double qval;

		double qbait = qlist.getDiffValue(bait);

		if (qlist.getData().containsKey(name)) {
			qval = qlist.getData().get(name);
		} else {
			qval = -1;
		}

		if (qval != -1) {

			qval = qval / qbait;

			/*
			 * if (qval<0.75) { b = (int)(255.0 * (1.0 - qval)); } else { if
			 * (qval>1.5) { r = (int)(255.0 * (1.0 - (1.0/qval))); } else { r =
			 * Color.GREEN.getRed(); b = Color.GREEN.getBlue(); g =
			 * Color.GREEN.getGreen(); } }
			 */

			if (qval < 0.66) {

				if (qval != 0) {
					qval = 1 / qval;
				} else {
					qval = 100;
				}

				// blue spectrum
				if (qval > 2) {
					// super blue
					qval = qval - 2.0;
					b = (int) (54 * qval) + 200;

					if (b > 254)
						b = 254;

				} else {

					qval = qval - 1.5;

					qval = qval / 0.5;

					b = (int) (200 * qval);

				}

			} else {

				if (qval > 1.5) {

					// red spectrum
					if (qval > 2) {
						// super red

						qval = qval - 2.0;
						r = (int) (54 * qval) + 200;

						if (r > 254)
							r = 254;

					} else {

						qval = qval - 1.5;

						qval = qval / 0.5;

						r = (int) (200 * qval);

					}

				} else {
					// green
					r = Color.GREEN.getRed();
					g = Color.GREEN.getGreen();
					b = Color.GREEN.getBlue();
				}

			}

			color = new Color(r, g, b);
		} else {
			color = Color.LIGHT_GRAY;
		}

		return color;
	}

	/**
	 * @return
	 */
	public String getMap() {
		return map.toString();
	}

	private double scaleVal(double val) {

		val = 1 - val;

		// val = val/0.9;

		// if (val>0.2) {
		val = Math.pow(val, 3) + 0.1;
		// }

		if (val >= 1)
			val = 0.99;

		return val;
	}

	private void quantCorrection(Hashtable<String, Double> table,
			List<String> names) {

		double qbait;
		double qprey;
		double score;

		qratios = new Hashtable<String, String>();

		qbait = qlist.getDiffValue(bait);
		if (qbait == -1) {
			qbait = 1;
		}

		boolean reverse = false;

		for (String name : names) {

			reverse = false;

			qprey = qlist.getDiffValue(name);

			if (qprey == -1) {
				qprey = 1;
			}

			if (qprey < 1) {
				qprey = 1.0 / qprey;
				reverse = true;
			}

			double ratio = qprey / qbait;

			double floor = Math.floor(ratio);
			double ceil = Math.ceil(ratio);

			double min = Math.min(ratio - floor, ceil - ratio);

			// ratio = ratio%floor;

			score = table.get(name);

			if (min < 0.2) {
				score = score / 2;

				if (reverse) {
					qratios.put(name, (int) floor + ":1");
				} else {
					qratios.put(name, "1:" + (int) floor);
				}

			} else {
				// ratio = ratio + 1;

				score = score * 1.5;// /ratio;

			}

			if (score > 1.0)
				score = 1.0;

		}

	}

	public void setIndirect(String indirect) {
		this.indirect_available = true;
		this.indirect = indirect;
	}

	private List<String> getCustomSort(List<String> cnames) {

		NodeSorter ns = new NodeSorter(cnames, indirect);
		ns.run();
		cnames = ns.getNodes();

		return cnames;
	}

}
