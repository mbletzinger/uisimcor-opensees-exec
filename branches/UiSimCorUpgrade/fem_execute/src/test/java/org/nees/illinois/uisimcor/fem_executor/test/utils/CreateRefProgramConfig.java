package org.nees.illinois.uisimcor.fem_executor.test.utils;

import java.io.File;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.TemplateDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Class to create a program configuration.
 * @author Michael Bletzinger
 */
public class CreateRefProgramConfig {
	/**
	 * Resulting Program Configuration.
	 */
	private final ProgramDao config;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(CreateRefProgramConfig.class);
	/**
	 * @param command
	 *            path to executable.
	 */
	public CreateRefProgramConfig(final String command) {
		TemplateDao tdao = new TemplateDao("step_template.tcl",
				"init_template.tcl", "run_template.tcl");
		config = new ProgramDao(command, FemProgramType.OPENSEES, tdao);
	}

	/**
	 * Check to see if executable actually exist and can be executed.
	 */
	public final void checkExecutable() {
		File cmdF = new File(config.getExecutablePath());
		if (cmdF.exists() == false) {
			log.error("\"" + cmdF.getAbsolutePath() + "\" does not exist!");
			Assert.fail();
		}
		cmdF.setExecutable(true);
	}

	/**
	 * @return the configuration
	 */
	public final ProgramDao getConfig() {
		return config;
	}

	/**
	 * Wrap a windows batch script around a perl executable so that it can be
	 * started like a windows *.exe file.
	 * @param workDir
	 *            Folder where the batch file should reside.
	 * @return The new configuration with the batch file.
	 */
	public final ProgramDao windowsWrap(final String workDir) {
		ProgramDao result = config;
		if (WindowsProgramDaoCreator.isWindows()) {
			WindowsProgramDaoCreator wpbc = new WindowsProgramDaoCreator(
					workDir, result);
			result = wpbc.getBatchConfig();
		}
		return result;
	}
}
