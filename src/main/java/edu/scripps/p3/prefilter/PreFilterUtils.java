package edu.scripps.p3.prefilter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreFilterUtils {
	protected final static String replicateREGEXP = "AREA_RATIO_1_(\\d+)";
	public static final String ACC = "LOCUS";
	public static final String DESCRIPTION = "DESCRIPTION";
	private static final Pattern area_ratio_x_regexp = Pattern.compile(replicateREGEXP);

	public static Map<String, Integer> getIndexesByHeaders(String headerLine) {
		return getIndexesByHeaders(headerLine.split("\t"));
	}

	protected static Map<String, Integer> getIndexesByHeaders(String[] splitedHeaderLine) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int index = 0; index < splitedHeaderLine.length; index++) {
			map.put(splitedHeaderLine[index], index);
		}
		return map;
	}

	protected static String getOldRatioString(Map<Integer, Integer> indexByReplicate, String proteinLine) {
		return getOldRatioString(indexByReplicate, proteinLine.split("\t"));
	}

	protected static String getOldRatioString(Map<Integer, Integer> indexByReplicate, String[] split) {
		StringBuilder sb = new StringBuilder();
		for (int rep = 1; rep <= indexByReplicate.size(); rep++) {

			sb.append(split[indexByReplicate.get(rep)]).append(";");
		}

		return sb.toString();
	}

	public static Map<Integer, Integer> getRatioIndexesByReplicate(String headerLine) {
		return getRatioIndexesByReplicate(headerLine.split("\t"));
	}

	public static Map<Integer, Integer> getRatioIndexesByReplicate(String[] splitedHeaderLine) {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

		for (int index = 0; index < splitedHeaderLine.length; index++) {
			String headerName = splitedHeaderLine[index];
			final Matcher matcher = area_ratio_x_regexp.matcher(headerName);
			if (matcher.find()) {
				int numRep = Integer.valueOf(matcher.group(1));
				ret.put(numRep, index);
			}
		}
		if (ret.isEmpty()) {
			// try wwith ratios_1
			for (int index = 0; index < splitedHeaderLine.length; index++) {
				String headerName = splitedHeaderLine[index];
				final Matcher matcher = Pattern.compile("RATIOS_(\\d+)").matcher(headerName);
				if (matcher.find()) {
					int numRep = Integer.valueOf(matcher.group(1));
					ret.put(numRep, index);
				}
			}
		}
		return ret;
	}
}
