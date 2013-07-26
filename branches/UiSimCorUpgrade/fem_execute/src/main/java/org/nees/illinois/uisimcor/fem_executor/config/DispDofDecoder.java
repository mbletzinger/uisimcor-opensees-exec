package org.nees.illinois.uisimcor.fem_executor.config;

import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to turn strings into displacement DOF enumerators.
 * @author Michael Bletzinger
 */
public class DispDofDecoder extends ParseElement<DispDof> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DispDofDecoder.class);

	@Override
	public final DispDof parse(final String raw, final String label) {
		if (raw == null) {
			log.error("Displacement DOF for " + label + " is missing.");
			return null;
		}
		String str = raw.toUpperCase();
		DispDof result = null;
		try {
			result = DispDof.valueOf(str);
		} catch (Exception e) {
			log.error("\"" + raw + "\" in " + label
					+ " is not a Displacement DOF");
			return null;
		}
		return result;
	}

}
