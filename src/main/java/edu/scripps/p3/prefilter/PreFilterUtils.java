package edu.scripps.p3.prefilter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreFilterUtils {
	protected final static String replicateREGEXP = "AREA_RATIO_1_(\\d+)";
	protected final static String replicateNEWFORMATREGEXP = "peptide ratio limited\\s*(\\d+)";
	public static final String ACC = "ACCESSION";
	public static final String LOCUS = "locus";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String DESCRIPTION_LOWER_CASE = "description";
	public static final Pattern area_ratio_x_regexp = Pattern.compile(replicateREGEXP);
	public static final Pattern area_ratio_x_regexp_new_format = Pattern.compile(replicateNEWFORMATREGEXP);

	public static Map<String, Integer> getIndexesByHeaders(String headerLine) {
		return getIndexesByHeaders(headerLine.split("\t"));
	}

	protected static Map<String, Integer> getIndexesByHeaders(String[] splitedHeaderLine) {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		for (int index = 0; index < splitedHeaderLine.length; index++) {
			map.put(splitedHeaderLine[index], index);
		}
		return map;
	}

	protected static String getOldRatioString(Map<Integer, Integer> indexByReplicate, String proteinLine) {
		return getOldRatioString(indexByReplicate, proteinLine.split("\t"));
	}

	protected static String getOldRatioString(Map<Integer, Integer> indexByReplicate, String[] split) {
		final StringBuilder sb = new StringBuilder();

		for (int rep = 0; rep <= indexByReplicate.size(); rep++) {
			if (indexByReplicate.containsKey(rep)) {
				String str = split[indexByReplicate.get(rep)];
				// remove quotes
				str = str.replace("\"", "");
				sb.append(str).append(";");

			}
		}

		return sb.toString();
	}

	public static Map<Integer, Integer> getRatioIndexesByReplicate(String headerLine, Pattern ratioRegexp) {
		return getRatioIndexesByReplicate(headerLine.split("\t"), ratioRegexp);
	}

	public static Map<Integer, Integer> getRatioIndexesByReplicate(String[] splitedHeaderLine, Pattern ratioRegexp) {
		final Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

		for (int index = 0; index < splitedHeaderLine.length; index++) {
			final String headerName = splitedHeaderLine[index];
			final Matcher matcher = ratioRegexp.matcher(headerName);
			if (matcher.find()) {
				final int numRep = Integer.valueOf(matcher.group(1));
				ret.put(numRep, index);
			}
		}
		if (ret.isEmpty()) {
			// try wwith ratios_1
			for (int index = 0; index < splitedHeaderLine.length; index++) {
				final String headerName = splitedHeaderLine[index];
				final Matcher matcher = Pattern.compile("RATIOS_(\\d+)").matcher(headerName);
				if (matcher.find()) {
					final int numRep = Integer.valueOf(matcher.group(1));
					ret.put(numRep, index);
				}
			}
		}
		return ret;
	}
}
