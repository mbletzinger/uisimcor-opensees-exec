package org.nees.illinois.uisimcor.fem_executor.config;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic to convert strings to lists of datatypes.
 * @author Michael Bletzinger
 * @param <T>
 *            Datatype to be encoded/decoded.
 * @param <P>
 *            Parser to convert a list element.
 */
public class EncodeDecodeList<T, P extends ParseElement<T>> {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(EncodeDecodeList.class);

	/**
	 * Parser to convert string to datatype T.
	 */
	private final P parser;

	/**
	 * @param parser
	 *            Parser to convert string to datatype T
	 */
	public EncodeDecodeList(final P parser) {
		this.parser = parser;
	}

	/**
	 * Convert list to a string.
	 * @param list
	 *            List of datatypes.
	 * @return String representation of list.
	 */
	public final String encode(final List<T> list) {
		String result = "";
		boolean first = true;
		for (T element : list) {
			result += (first ? "" : ", ") + element;
			first = false;
		}
		return result;
	}

	/**
	 * Converts string to a datatype list.
	 * @param listStr
	 *            Input string.
	 * @return List of datatypes
	 * @throws IllegalParameterException
	 *             for a list strings that are not able to be parsed.
	 */
	public final List<T> parse(final String listStr)
			throws IllegalParameterException {
		List<T> result = new ArrayList<T>();
		String[] tokens;
		log.debug("Parsing line \"" + listStr + "\"");
		try {
			tokens = listStr.split(", ");
		} catch (Exception e) {
			throw new IllegalParameterException("\"" + listStr
					+ "\" is not a valid list");
		}
		if (tokens.length == 0) {
			throw new IllegalParameterException("\"" + listStr
					+ "\" is not a valid list");
		}
		for (String t : tokens) {
			String token = t;
			T ddof;
			try {
				ddof = parser.parse(token);
			} catch (Exception e) {
				throw new IllegalParameterException("\"" + t
						+ "\" is not an element");
			}
			result.add(ddof);
		}
		return result;
	}

}
