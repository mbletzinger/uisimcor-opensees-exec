package org.nees.illinois.uisimcor.fem_executor.config;

import org.nees.illinois.uisimcor.fem_executor.config.types.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to turn strings into dimension type enumerators (TwoD or ThreeD).
 * @author Michael Bletzinger
 */
public class DimensionTypeDecoder extends ParseElement<DimensionType> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(DimensionTypeDecoder.class);

	@Override
	public final DimensionType parse(final String raw, final String label) {
		if (raw == null) {
			log.error("DimensionType for " + label + " is missing.");
			return null;
		}
		DimensionType result = null;
		try {
			result = DimensionType.valueOf(raw);
		} catch (Exception e) {
			log.error("\"" + raw + "\" in " + label
					+ " is not a Dimension Type (needs to be TwoD or ThreeD");
			return null;
		}
		return result;
	}

}
