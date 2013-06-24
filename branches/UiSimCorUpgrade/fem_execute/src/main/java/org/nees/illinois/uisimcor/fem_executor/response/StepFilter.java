package org.nees.illinois.uisimcor.fem_executor.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to filter step number string from the response stream.
 * @author Michael Bletzinger
 */
public class StepFilter implements ResponseFilterI {
	/**
	 * Pattern for the observable function.
	 */
	private final Pattern regex = Pattern.compile("step\\s+([0-9]+)");
	/**
	 * Step number string which was extracted.
	 */
	private String extracted = null;

	@Override
	public final boolean filter(final String response) {
		if (response.contains("#:") == false) {
			return false;
		}
		Matcher match = regex.matcher(response);
		match.find();
		extracted = match.group(1);
		return true;
	}

	@Override
	public final String get() {
		return extracted;
	}

}
