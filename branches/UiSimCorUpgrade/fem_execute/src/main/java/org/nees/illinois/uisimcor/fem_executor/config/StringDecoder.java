package org.nees.illinois.uisimcor.fem_executor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle String elements. No parsing needed obviously.
 * @author Michael Bletzinger
 */
public class StringDecoder extends ParseElement<String> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(StringDecoder.class);

	@Override
	public final String parse(final String str, final String label) {
		if (str == null) {
			log.error("Text missing for " + label);
			return null;
		}
		return str;
	}
}
