package org.nees.illinois.uisimcor.fem_executor.config;

/**
 * Class to handle String elements. No parsing needed obviously.
 * @author Michael Bletzinger
 */
public class StringDecoder extends ParseElement<String> {

	@Override
	public final String parse(final String str) {
		return str;
	}
}
