package org.nees.illinois.uisimcor.fem_executor.test.utils;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;

/**
 * Class to wrap a Perl test script into a batch file so that it can be used as
 * an executable. I love windows.
 * @author Michael Bletzinger
 */
public class WindowsProgramDaoCreator extends WindowsPerlBatchCreator {
	/**
	 * Configuration of the batch file.
	 */
	private final ProgramDao batchConfig;
	/**
	 * Logger.
	 **/
//	private final Logger log = LoggerFactory
//			.getLogger(WindowsProgramDaoCreator.class);
//
	/**
	 * Constructor.
	 * @param folder
	 *            Folder for the batch script.
	 * @param prog
	 *            Configuration of program to be wrapped.
	 */
	public WindowsProgramDaoCreator(final String folder, final ProgramDao prog) {
		super(prog.getExecutablePath(), folder);
		batchConfig = new ProgramDao(getBatchFilename(), prog.getProgram(),
				prog.getTemplateDao(), 0);
	}

	/**
	 * @return the batchConfig
	 */
	public final ProgramDao getBatchConfig() {
		return batchConfig;
	}

}
