package org.nees.illinois.uisimcor.fem_executor.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to wrap a test perl script into a windows batch file.
 * @author Michael Bletzinger
 */
public class WindowsPerlBatchCreator {
	/**
	 * Check if we are running windows.
	 * @return True if we are running on Windows.
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win");
	}

	/**
	 * Name of the batch file.
	 */
	private final String batchFilename;

	/**
	 * Folder for the batch file.
	 */
	private final String batchFolder;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(WindowsPerlBatchCreator.class);

	/**
	 * @param cmd
	 *            Path to perl script.
	 * @param folder
	 *            Folder containing script.
	 */
	public WindowsPerlBatchCreator(final String cmd, final String folder) {
		super();
		String batchContent = "perl \"" + cmd + "\" %1";
		this.batchFilename = cmd.replace(".pl", ".bat");
		this.batchFolder = folder;
		writeBatchFile(batchContent);
	}

	/**
	 * @return the batchFilename
	 */
	public final String getBatchFilename() {
		return batchFilename;
	}

	/**
	 * @return the batchFolder
	 */
	public final String getBatchFolder() {
		return batchFolder;
	}

	/**
	 * Write the input file content to a file.
	 * @param content
	 *            The content.
	 */
	private void writeBatchFile(final String content) {
		// String batchFilePath = PathUtils.append(batchFolder, batchFilename);
		File batchF = new File(batchFilename);
		if (batchF.exists()) {
			batchF.delete();
		}
		PrintWriter os = null;
		try {
			os = new PrintWriter(new FileWriter(batchFilename));
		} catch (IOException e) {
			log.error("Batch file \"" + batchFilename
					+ "\" cannot be created because ", e);
			return;
		}
		os.print(content);
		os.close();
		batchF.setExecutable(true);
	}

}
