package edu.scripps.p3.parsers.inputs.utilities;

import java.io.File;
import java.util.List;

import edu.scripps.p3.P3;
import edu.scripps.p3.experimentallist.Experiment;
import edu.scripps.p3.io.MyFileChooser;

public class NewCoverageFixer extends CoverageFixer {

	public NewCoverageFixer(List<Experiment> elist, File inputdir) {
		super(elist, inputdir);
	}

	@Override
	public void setCoverageTable() {
		File fastaFile = null;
		if (!P3.test) {
			MyFileChooser dIO = new MyFileChooser(cdir);
			fastaFile = dIO.openFile("Select FASTA file");
		} else {
			fastaFile = new File(P3.fastaFile);
		}

		NewPeptideCutter peptideCutter = new NewPeptideCutter(fastaFile);
		peptideCutter.run();
		coverages.putAll(peptideCutter.getMaxEffectiveCoverages());

	}
}
