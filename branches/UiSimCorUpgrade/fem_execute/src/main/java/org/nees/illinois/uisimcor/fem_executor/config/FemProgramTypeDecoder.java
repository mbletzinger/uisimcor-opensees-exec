package org.nees.illinois.uisimcor.fem_executor.config;

import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to turn strings into FEM program type type enumerators.
 * @author Michael Bletzinger
 */
public class FemProgramTypeDecoder extends ParseElement<FemProgramType> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(FemProgramTypeDecoder.class);

	@Override
	public final FemProgramType parse(final String raw, final String label) {
		if (raw == null) {
			log.debug("FemProgramType for " + label + " is missing.");
			return null;
		}
		FemProgramType result = null;
		try {
			result = FemProgramType.valueOf(raw);
		} catch (Exception e) {
			log.error("\"" + raw + "\" in " + label
					+ " is not a FEM program type (needs to be one of " + FemProgramType.values());
			return null;
		}
		return result;
	}

}
