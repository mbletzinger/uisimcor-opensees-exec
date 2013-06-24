package org.nees.illinois.uisimcor.fem_executor.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters out the silly OpenSees Banner from the rest of the error stream.
 * @author Michael Bletzinger
 */
public class OpenSeesErrorFilter implements ResponseFilterI {
	/**
	 * Pattern for non-whitespace.
	 */
	private final Pattern regex = Pattern.compile("\\S+");
	/**
	 * Step number string which was extracted.
	 */
	private String extracted = null;

	@Override
	public final boolean filter(final String response) {
		if (response
				.contains("Open System For Earthquake Engineering Simulation")) {
			return false;
		}
		if (response.contains("Pacific Earthquake Engineering Research Center")) {
			return false;
		}
		Matcher match = regex.matcher(response);
		if (match.matches()) {
			return false;
		}
		extracted = response;
		return true;
	}

	@Override
	public final String get() {
		return extracted;
	}

}
