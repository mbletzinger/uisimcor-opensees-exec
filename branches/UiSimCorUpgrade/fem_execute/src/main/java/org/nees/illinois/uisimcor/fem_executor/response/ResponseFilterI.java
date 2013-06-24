package org.nees.illinois.uisimcor.fem_executor.response;

/**
 * Interface used to search the response stream for events.
 * @author Michael Bletzinger
 */
public interface ResponseFilterI {
	/**
	 * Filter function.
	 * @param response
	 *            Response string to filter.
	 * @return True if the item is "found".
	 */
	boolean filter(String response);

	/**
	 * @return What was extracted from the response.
	 */
	String get();
}
