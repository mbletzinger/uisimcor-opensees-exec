/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.input;

import java.io.File;
import java.io.IOException;

import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a working directory field with the specified files from the
 * configuration directory need to run the FEM model.
 * @author Michael Bletzinger
 */
public class WorkingDir {
	/**
	 * Path to the directory containing the configuration files.
	 */
	private final String configDir;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(WorkingDir.class);

	/**
	 * Substructure configuration parameters.
	 */
	private final SubstructureConfig substructureCfg;

	/**
	 * Path to the directory containing the input file.
	 */
	private final String workDir;

	/**
	 * Constructor.
	 * @param substructureCfg
	 *            Substructure parameters.
	 * @param workDir
	 *            Directory containing generated files.
	 * @param configDir
	 *            Root of directory containing the model configuration files.
	 */
	public WorkingDir(final SubstructureConfig substructureCfg,
			final String workDir, final String configDir) {
		this.substructureCfg = substructureCfg;
		this.workDir = PathUtils.append(workDir, substructureCfg.getAddress());
		this.configDir = configDir;
	}

	/**
	 * Creates the working directory for the substructure. and copies all needed
	 * files.
	 */
	public final void createWorkDir() {
		File workDirF = new File(workDir);
		if (workDirF.exists() && (workDirF.isDirectory() == false)) {
			log.error("Cannot create working directory \"" + workDir + "\"");
			return;
		}
		try {
			workDirF.mkdirs();
			log.debug("\"" + workDir + "\" was created");
		} catch (Exception e) {
			log.error("Cannot create working directory \"" + workDir
					+ "\" because ", e);
			return;
		}

		for (String f : substructureCfg.getSourcedFilenames()) {
			try {
				PathUtils.cp(f, configDir, workDir);
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + workDir + "\" because ", e);
				return;
			}
		}
		for (String f : substructureCfg.getWorkFiles()) {
			try {
				PathUtils.cp(f, configDir, workDir);
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + workDir + "\" because ", e);
				return;
			}
		}
	}

}
