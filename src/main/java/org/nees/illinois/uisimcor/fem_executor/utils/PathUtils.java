package org.nees.illinois.uisimcor.fem_executor.utils;

import java.io.File;
import java.util.regex.Matcher;

/**
 * Utilities to clean file pathnames.
 * 
 * @author Michael Bletzinger
 */
public class PathUtils {
	/**
	 * Transforms Internet style path into a path understood by the local file
	 * system.
	 * 
	 * @param raw
	 *            Internet version.
	 * @return local version.
	 */
	public static String cleanPath(final String raw) {
		String sep = System.getProperty("file.separator");
		String result = raw.replaceAll("%20", " ");
		result = result.replaceAll("/", Matcher.quoteReplacement(sep));
		result = result.replaceAll(
				Matcher.quoteReplacement(sep) + Matcher.quoteReplacement(sep),
				Matcher.quoteReplacement(sep));
		result = result.replaceAll("\\\\C:", "C:");
		return result;
	}

	public static String append(String path, String file) {
		String sep = System.getProperty("file.separator");
		String result = path + sep + file;
		return cleanPath(result);
	}
	public static String parent(String path) {
		File pathF = new File(path);
		return pathF.getParent();
	}
}
