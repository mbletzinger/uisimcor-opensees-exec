package org.nees.illinois.uisimcor.fem_executor.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to wrap a Perl test script into a batch file so that it can be used as
 * an executable. I love windows.
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
	 * Configuration of the batch file.
	 */
	private final ProgramDao batchConfig;
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
	 * Constructor.
	 * @param folder
	 *            Folder for the batch script.
	 * @param prog
	 *            Configuration of program to be wrapped.
	 */
	public WindowsPerlBatchCreator(final String folder,
			final ProgramDao prog) {
		String batchContent = "perl \"" + prog.getExecutablePath() + "\" %1";
		this.batchFilename = prog.getExecutablePath().replace(".pl", ".bat");
		this.batchFolder = folder;
		batchConfig = new ProgramDao(prog.getProgram(), batchFilename);
		writeBatchFile(batchContent);
	}

	/**
	 * @return the batchConfig
	 */
	public final ProgramDao getBatchConfig() {
		return batchConfig;
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
