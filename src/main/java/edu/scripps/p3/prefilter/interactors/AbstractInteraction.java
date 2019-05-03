package edu.scripps.p3.prefilter.interactors;

public abstract class AbstractInteraction {
	protected final String prot1;
	protected final String prot2;
	protected String note = "";
	private final Float score;

	public AbstractInteraction(String prot1, String prot2, Float score) {
		this.prot1 = prot1;
		this.prot2 = prot2;
		this.score = score;
	}

	public String getProt1() {
		return prot1;
	}

	public String getProt2() {
		return prot2;
	}

	public boolean containsProtein(String protein) {
		if (prot1.equals(protein) || prot2.equals(protein)) {
			return true;
		}
		return false;
	}

	public String getCounterPart(String protein1) {
		if (prot1.equals(protein1)) {
			return prot2;
		}
		if (prot2.equals(protein1)) {
			return prot1;
		}
		return null;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		if (!"".equals(this.note)) {
			this.note += ", ";
		}
		this.note += note;
	}

	public Float getScore() {
		return score;
	}

	/**
	 * Has greater or Equal score
	 * 
	 * @param score
	 * @return
	 */
	public boolean hasGEScore(float score) {
		if (this.score >= score) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String ret = "[" + prot1 + "-" + score + "-" + prot2;
		if (!note.equals("")) {
			ret += ", note=" + note;
		}
		ret += "]";
		return ret;
	}
}
