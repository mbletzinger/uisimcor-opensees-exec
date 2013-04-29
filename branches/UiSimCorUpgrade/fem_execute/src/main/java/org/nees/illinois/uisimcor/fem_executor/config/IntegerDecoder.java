package org.nees.illinois.uisimcor.fem_executor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to turn strings into integers.
 * @author Michael Bletzinger
 */
public class IntegerDecoder extends ParseElement<Integer> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(IntegerDecoder.class);

	@Override
	public final Integer parse(final String str) {
		Integer result = null;
		try {
			result = Integer.decode(str);
		} catch (Exception e) {
			log.error("\"" + str + "\" is not an Integer");
			return null;
		}
		return result;
	}

}
