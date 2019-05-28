package edu.scripps.p3.prefilter.interactors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import gnu.trove.set.hash.THashSet;

public class BioGRIDFilter extends InteractionFilter {

	public BioGRIDFilter(File table) throws IOException {
		loadTable(table);
	}

	@Override
	protected Object processLine(String line) {
		if (!line.startsWith("#")) {
			final String[] split = line.split("\t");
			final String prot1 = split[7];
			final String prot2 = split[8];

			final BiogridInteraction interaction = new BiogridInteraction(prot1, prot2);
			interactions.add(interaction);
		}
		return null;
	}

	public static void main(String[] args) {
		BioGRIDFilter sf;
		try {
			int num = 1;
			sf = new BioGRIDFilter(new File(args[0]));
			final String bait = args[1];
			final List<AbstractInteraction> interactors = sf.getConfidentInteractors(null, bait);
			for (final AbstractInteraction interaction : interactors) {
				System.out.print(num++ + "\t" + interaction.getCounterPart(bait));
				if (!"".equals(interaction.getNote())) {
					System.out.println("\t" + interaction.getNote());
				} else {
					System.out.println();
				}
			}
			System.out.println(sf.getUniqueConfidentInteractors(null, bait).size() + " unique interactors");
			System.exit(0);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	@Override
	protected List<AbstractInteraction> getConfidentInteractors(Float threshold, String bait) {
		final List<AbstractInteraction> ret = new ArrayList<AbstractInteraction>();
		final Set<AbstractInteraction> indirectInteractors = new THashSet<AbstractInteraction>();
		// first round
		for (final AbstractInteraction interaction : interactions) {
			if (interaction.containsProtein(bait)) {
				// direct interaction
				if (threshold == null || interaction.hasGEScore(threshold)) {
					// high score
					ret.add(interaction);
				}
			} else {
				// low score
				indirectInteractors.add(interaction);
			}
		}
		// second round
		// check if the indirect interactors, are interacting
		// with other proteins with high score and those proteins have high
		// score with the bait
		for (final AbstractInteraction indirectInteraction : indirectInteractors) {
			final String interactor1 = indirectInteraction.getProt1();
			final String interactor2 = indirectInteraction.getProt2();

			final List<AbstractInteraction> interactions1 = getInteractions(bait, interactor1, threshold);
			if (!interactions1.isEmpty()) {
				indirectInteraction.setNote(interactor1 + "-" + interactions1.get(0).getScore() + "-" + bait);
				if (!ret.contains(indirectInteraction)) {
					ret.add(indirectInteraction);
				}
			}
			final List<AbstractInteraction> interactions2 = getInteractions(bait, interactor2, threshold);
			if (!interactions2.isEmpty()) {
				indirectInteraction.setNote(interactor2 + "-" + interactions2.get(0).getScore() + "-" + bait);
				if (!ret.contains(indirectInteraction)) {
					ret.add(indirectInteraction);
				}
			}

		}
		ret.sort(new Comparator<AbstractInteraction>() {

			@Override
			public int compare(AbstractInteraction o1, AbstractInteraction o2) {
				if (o1.getNote().equals("") && !o2.getNote().equals("")) {
					return -1;
				}
				if (!o1.getNote().equals("") && o2.getNote().equals("")) {
					return 1;
				}
				if (o1.getScore() != null && o2.getScore() != null) {
					return Float.compare(o2.getScore(), o1.getScore());
				}
				return o1.getCounterPart(bait).compareTo(o2.getCounterPart(bait));
			}
		});
		return ret;
	}

}
