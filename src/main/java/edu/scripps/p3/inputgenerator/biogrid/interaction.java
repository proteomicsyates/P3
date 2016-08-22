/**
 * diego Jan 14, 2013
 */
package edu.scripps.p3.inputgenerator.biogrid;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * method to compute the biogrid score. It was done in two modes, the current
 * one it gives more weight to affinity capture-MS method. The old method, left
 * in the comment was trying to give a more granular score.
 *
 * @author diego
 *
 */
public class interaction {

	String nodeA;
	String nodeB;
	List<String> expsystem;
	List<String> exptype;
	List<String> studies;
	List<String> throughput;

	double score;

	public interaction(String A, String B) {
		nodeA = A;
		nodeB = B;
		expsystem = new ArrayList<String>();
		exptype = new ArrayList<String>();
		studies = new ArrayList<String>();
		throughput = new ArrayList<String>();
	}

	public void addExpSystem(String s) {

		// if (!expsystem.contains(s)) {
		expsystem.add(s);
		// }
	}

	public void addExpType(String s) {

		// if (!exptype.contains(s)) {
		exptype.add(s);
		// }
	}

	public void addExpstudy(String s) {

		// if (!studies.contains(s)) {
		studies.add(s);
		// }
	}

	public void addExpThroughput(String s) {

		// if (!throughput.contains(s)) {
		throughput.add(s);
		// }
	}

	public double getScore(double psystem, double gsystem) {

		// double esystem=0;

		// if (exptype.size()==2) {
		// esystem = psystem + gsystem;
		// } else {
		// if (exptype.get(0).equals("physical")) {
		// esystem = psystem;
		// } else {
		// esystem = gsystem;
		// }
		// }

		// score = (double)expsystem.size()/esystem;
		score = 1;// 0;

		/*
		 * switch (expsystem.size()) { case 1: score += 0.3; break; case 2:
		 * score += 0.6; break; case 3: score += 0.9; break; default: score +=
		 * 1.0; break; } if (exptype.contains("physical")) { score += 0.7; } if
		 * (exptype.contains("genetic")) { score += 0.3; } // score +=
		 * (double)exptype.size()/etype; if
		 * (throughput.contains("Low Throughput")) { score += 0.6; } if
		 * (throughput.contains("High Throughput")) { score += 0.4; } // score
		 * += (double)throughput.size()/ethoughput; switch (studies.size()) {
		 * case 1: score += 0.3; break; case 2: score += 0.6; break; case 3:
		 * score += 0.9; break; default: score += 1.0; break; } score /= 4.0; if
		 * (score>1) { System.out.println(); } if (score<0) {
		 * System.out.println(); }
		 */
		for (int i = 0; i < expsystem.size(); i++) {

			if (expsystem.get(i).equals("Affinity Capture-MS")) {
				score = score * 0.7;
			} else {
				score = score * 0.9;
			}

		}

		score = 1 - score;

		return score;
	}

	public int getStudiesNumber() {
		return studies.size();
	}

}
