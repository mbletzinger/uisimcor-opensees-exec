package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class parses a text file with straight floating point numbers and returns a
 * {@link DoubleMatrix set} of numbers.
 * @author Michael Bletzinger
 */
public class ResponseParser {
	/**
	 * The parsed data set.
	 */
	private List<Double> archive;

	/**
	 * Flag that indicates whether the data set is available or not.
	 */
	private boolean empty = true;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(ResponseParser.class);

	/**
	 * @return the archive
	 */
	public final List<Double> getArchive() {
		return archive;
	}

	/**
	 * @return the empty
	 */
	public final boolean isEmpty() {
		return empty;
	}

	/**
	 * Opens the text file and feeds all of the rows to the token reader.
	 * @param strData
	 *            String representation of displacements or forces.
	 * @param strFile
	 *            Name of the text file
	 */
	public final void parseDataString(final String strData, final String strFile) {
		log.debug("Parsing file \"" + strFile + "\"");
		log.debug("Parsing line \"" + strData + "\"");
		String[] tokens = strData.split("\\s+");
		List<Double> row = tokens2Double(tokens, strFile);
		if (row.isEmpty()) {
			log.error("No values for file " + strFile + " line \"" + strData
					+ "\"");
			return;
		}
		empty = false;
		archive = row;
		log.debug("Archive Row Text: " + archive);
	}

	/**
	 * @param empty
	 *            the empty to set
	 */
	public final void setEmpty(final boolean empty) {
		this.empty = empty;
	}

	/**
	 * Converts a set of tokens to {@link Double} numbers.
	 * @param tokens
	 *            The set of tokens.
	 * @param filename
	 *            Name of the original text file (used for error messages).
	 * @return A row of doubles.
	 */
	private List<Double> tokens2Double(final String[] tokens,
			final String filename) {
		List<Double> row = new ArrayList<Double>();
		for (int t = 0; t < tokens.length; t++) {
			Double val;
			try {
				val = new Double(tokens[t]);
			} catch (NumberFormatException e) {
				log.error("Token \"" + tokens[t]
						+ "\" is not a number.  Column " + t + " file \""
						+ filename + "\"");
				val = 0.0;
			}
			row.add(val);
		}
		return row;
	}
}
