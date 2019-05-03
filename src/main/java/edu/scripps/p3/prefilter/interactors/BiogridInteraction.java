package edu.scripps.p3.prefilter.interactors;

public class BiogridInteraction extends AbstractInteraction {

	public BiogridInteraction(String prot1, String prot2) {
		super(prot1, prot2, null);
	}

	@Override
	public String toString() {
		String ret = "[" + prot1 + "-" + prot2;
		if (!note.equals("")) {
			ret += ", note=" + note;
		}
		ret += "]";
		return ret;
	}
}
