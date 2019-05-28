package edu.scripps.p3.prefilter.interactors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import gnu.trove.set.hash.THashSet;

public abstract class InteractionFilter {
	protected final List<AbstractInteraction> interactions = new ArrayList<AbstractInteraction>();

	protected void loadTable(File table) throws IOException {
		final Stream<String> linesStream = Files.lines(Paths.get(table.toURI()));
		linesStream.forEach(line -> processLine(line));
		linesStream.close();
	}

	protected abstract Object processLine(String line);

	protected List<AbstractInteraction> getConfidentInteractors(Float threshold, String bait) {
		final List<AbstractInteraction> ret = new ArrayList<AbstractInteraction>();
		final Set<AbstractInteraction> directInteractorsWithLowScore = new THashSet<AbstractInteraction>();
		// first round
		for (final AbstractInteraction interaction : interactions) {
			if (interaction.containsProtein(bait)) {
				// direct interaction
				if (threshold == null || interaction.hasGEScore(threshold)) {
					// high score
					ret.add(interaction);
				} else {
					// low score
					directInteractorsWithLowScore.add(interaction);
				}
			}
		}
		// second round
		// check if the counterparts interacting with low score, are interacting
		// with other proteins with high score and those proteins have high
		// score with the bait
		for (final AbstractInteraction lowScoreInteraction : directInteractorsWithLowScore) {
			final String interactor = lowScoreInteraction.getCounterPart(bait);

			final Set<AbstractInteraction> interactions = getInteractions(interactor, threshold);
			for (final AbstractInteraction AbstractInteraction : interactions) {
				final String interactor2 = AbstractInteraction.getCounterPart(interactor);
				if (interactor2.equals(bait)) {
					continue;
				}
				final List<AbstractInteraction> interactions2 = getInteractions(bait, interactor2, threshold);
				if (!interactions2.isEmpty()) {
					lowScoreInteraction.setNote(interactor + "-"
							+ getInteractions(interactor2, interactor, threshold).get(0).getScore() + "-" + interactor2
							+ " and " + interactor2 + "-" + interactions2.get(0).getScore() + "-" + bait);
					if (!ret.contains(lowScoreInteraction)) {
						ret.add(lowScoreInteraction);
					}
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
				return Float.compare(o2.getScore(), o1.getScore());
			}
		});
		return ret;
	}

	protected List<AbstractInteraction> getInteractions(String protein1, String protein2, Float threshold) {
		final List<AbstractInteraction> ret = new ArrayList<AbstractInteraction>();
		for (final AbstractInteraction AbstractInteraction : interactions) {
			if (AbstractInteraction.containsProtein(protein1) && AbstractInteraction.containsProtein(protein2)) {
				if (threshold == null || AbstractInteraction.hasGEScore(threshold)) {
					ret.add(AbstractInteraction);
				}
			}
		}
		return ret;
	}

	protected Set<AbstractInteraction> getInteractions(String counterPart, Float threshold) {
		final Set<AbstractInteraction> ret = new THashSet<AbstractInteraction>();
		for (final AbstractInteraction AbstractInteraction : interactions) {
			if (AbstractInteraction.containsProtein(counterPart)) {
				if (threshold == null || AbstractInteraction.hasGEScore(threshold)) {
					ret.add(AbstractInteraction);
				}
			}
		}
		return ret;
	}

	public Set<String> getUniqueConfidentInteractors(Float threshold, String bait) {
		final List<AbstractInteraction> confidentInteractors = getConfidentInteractors(threshold, bait);
		final Set<String> ret = new THashSet<String>();
		for (final AbstractInteraction interaction : confidentInteractors) {
			ret.add(interaction.getCounterPart(bait));
		}
		return ret;
	}
}
