package org.nees.illinois.uisimcor.fem_executor.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

/**
 * Utilities to manage file pathnames.
 * @author Michael Bletzinger
 */
public final class PathUtils {
	/**
	 * Prevent the creation of PathUtil objects.
	 */
	private PathUtils() {
	}

	/**
	 * Transforms Internet style path into a path understood by the local file
	 * system.
	 * @param raw
	 *            Internet version.
	 * @return local version.
	 */
	public static String cleanPath(final String raw) {
		String sep = System.getProperty("file.separator");
		String result = raw.replaceAll("%20", " ");
		// Replace with appropriate separators
		result = result.replaceAll("/", Matcher.quoteReplacement(sep));
		// Remove redundant slashes
		result = result.replaceAll(
				Matcher.quoteReplacement(sep) + Matcher.quoteReplacement(sep),
				Matcher.quoteReplacement(sep));
		// Clean up drive letter need to replace with regex since we cannot assume C:
		result = result.replaceAll("\\\\C:", "C:");
		return result;
	}

	/**
	 * Appends the file to the end of the path using the appropriate file
	 * separator. Also calls the cleanPath() function to remove extra file
	 * separators.
	 * @param path
	 *            Name of path.
	 * @param file
	 *            Name of file.
	 * @return Full appended path.
	 */
	public static String append(final String path, final String file) {
		String sep = System.getProperty("file.separator");
		String result = path + sep + file;
		return cleanPath(result);
	}

	/**
	 * Returns the path minus the last file or folder name.
	 * @param path
	 *            Path name.
	 * @return Path name minus last folder or file name.
	 */
	public static String parent(final String path) {
		File pathF = new File(path);
		return pathF.getParent();
	}

	/**
	 * Copies a file to a different directory.
	 * @param file
	 *            Filename.
	 * @param srcDir
	 *            Current location.
	 * @param destDir
	 *            Target location.
	 * @throws IOException
	 *             In case the copy fails for some reason.
	 */
	public static void cp(final String file, final String srcDir, final String destDir)
			throws IOException {
		File dest = new File(append(destDir, file));
		File src = new File(append(srcDir, file));
		FileUtils.copyFile(src, dest);
	}

	public static void rm(final String dir, final String file) {
		File fileF = new File(append(dir, file));
		fileF.delete();
	}
}
