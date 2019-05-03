package edu.scripps.p3.parsers.inputs.utilities;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.EnzymeLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import edu.scripps.p3.util.P3Constants;
import edu.scripps.yates.utilities.fasta.FastaParser;
import edu.scripps.yates.utilities.strings.StringUtils;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class NewPeptideCutter {
	private final Logger log = Logger.getLogger(NewPeptideCutter.class);
	private final File fastaFile;
	private String enzymeName;
	private final int missedCleavages;
	private final TObjectDoubleHashMap<String> maxCoverages = new TObjectDoubleHashMap<String>();

	public NewPeptideCutter(File fastaFile) {
		this(fastaFile, null, 0);
	}

	public NewPeptideCutter(File fastaFile, String enzymeName, int missedCleavages) {
		this.fastaFile = fastaFile;
		if (enzymeName != null) {
			this.enzymeName = enzymeName;
		} else {
			this.enzymeName = "Trypsin/P";
		}
		this.missedCleavages = missedCleavages;
	}

	public void run() {

		try {
			final DBLoader loader = DBLoaderLoader.loadDB(fastaFile);
			final Enzyme e = EnzymeLoader.loadEnzyme(enzymeName, String.valueOf(missedCleavages));
			Protein protein;
			while ((protein = loader.nextProtein()) != null) {
				final String proteinSequence = protein.getSequence().getSequence();
				final char[] sequenceCoverage = new char[proteinSequence.length()];
				final long originalLength = protein.getLength();
				final Protein[] peps = e.cleave(protein, P3Constants.MIN_PEP_LENGTH, 80);
				final int i = 0;
				for (final Protein peptide : peps) {
					final TIntArrayList allPositions = StringUtils.allPositionsOf(proteinSequence,
							peptide.getSequence().getSequence());
					for (final int position : allPositions.toArray()) {
						int index = position - 1;
						int lenth = 0;
						while (lenth < peptide.getSequence().getLength()) {
							sequenceCoverage[index] = 'C';// covered
							lenth++;
							index++;
						}
					}
				}
				int maxSequence = 0;
				for (int index = 0; index < sequenceCoverage.length; index++) {
					if (sequenceCoverage[index] == 'C') {
						maxSequence++;
					}
				}
				String geneName = FastaParser.getGeneFromFastaHeader(protein.getHeader().getRawHeader());
				if (geneName == null) {
					final String acc = FastaParser.getACC(protein.getHeader().getAccession()).getAccession();
					geneName = acc;
				}
				final Double efectiveCoverage = maxSequence * 100.0 / originalLength;

				// log.debug("Protein " + geneName + ": " + maxSequence + "/" +
				// originalLength + " (" + efectiveCoverage
				// + "%)");
				if (protein.getHeader().getFullHeaderWithAddenda().contains("Reverse")) {
					continue;
				}
				maxCoverages.put(geneName, efectiveCoverage);
			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw new IllegalAccessError(e.getMessage());
		}
	}

	public Double getMaxEffectiveCoverage(String acc) {
		return maxCoverages.get(acc);
	}

	public TObjectDoubleHashMap<String> getMaxEffectiveCoverages() {
		return maxCoverages;
	}
}
