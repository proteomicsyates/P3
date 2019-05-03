package edu.scripps.p3.prefilter.interactors;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This program takes a TSV table from String and reports the confident
 * interactors that pass a certain threshold, including the ones that directly
 * interact with a bait with a low score but that interact with high score with
 * a protein that interacts with high score with the bait
 * 
 * @author salvador
 *
 */
public class StringFilter extends InteractionFilter {

	public StringFilter(File table) throws IOException {

		loadTable(table);
	}

	@Override
	protected Object processLine(String line) {
		if (!line.startsWith("#")) {
			final String[] split = line.split("\t");
			final String prot1 = split[0];
			final String prot2 = split[1];
			final float score = Float.valueOf(split[14]);
			final StringInteraction interaction = new StringInteraction(prot1, prot2, score);
			interactions.add(interaction);
		}
		return null;
	}

	public static void main(String[] args) {
		final Float threshold = Float.valueOf(args[0]);
		StringFilter sf;
		try {
			int num = 1;
			sf = new StringFilter(new File(args[1]));
			final String bait = args[2];
			final List<AbstractInteraction> interactors = sf.getConfidentInteractors(threshold, bait);
			for (final AbstractInteraction interaction : interactors) {
				System.out.print(num++ + "\t" + interaction.getCounterPart(bait) + "\t" + interaction.getScore());
				if (!"".equals(interaction.getNote())) {
					System.out.println("\t" + interaction.getNote());
				} else {
					System.out.println();
				}
			}
			System.exit(0);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
