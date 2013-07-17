/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.input;

import java.io.IOException;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.execute.SubstructureDir;
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
	 * Subdirectory creator.
	 */
	private final SubstructureDir subDir;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(WorkingDir.class);

	/**
	 * Constructor.
	 * @param dirRoot
	 *            Path to the root directory containing all of the processing
	 *            files.
	 * @param configDir
	 *            Root of directory containing the model configuration files.
	 * @param substructureCfg
	 *            Substructure configuration parameters.
	 */
	public WorkingDir(final String dirRoot, final SubstructureDao substructureCfg, final String configDir) {
		this.configDir = configDir;
		this.subDir = new SubstructureDir(dirRoot, substructureCfg, "workDir");
		log.debug("For substructure " + substructureCfg.getAddress()
				+ " the workDir is \"" + subDir.getSubstructDir() + "\"");
		log.debug("For substructure " + substructureCfg.getAddress()
				+ " the configDir is \"" + this.configDir + "\"");
	}

	/**
	 * Creates the working directory for the substructure. and copies all needed
	 * files.
	 */
	public final void createWorkDir() {
		subDir.createSubstructDir();
		for (String f : subDir.getSubstructureCfg().getSourcedFilenames()) {
			try {
				PathUtils.cp(f, configDir, subDir.getSubstructDir());
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + subDir.getSubstructDir()
								+ "\" because ", e);
				return;
			}
		}
		for (String f : subDir.getSubstructureCfg().getWorkFiles()) {
			try {
				PathUtils.cp(f, configDir, subDir.getSubstructDir());
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + subDir.getSubstructDir()
								+ "\" because ", e);
				return;
			}
		}
	}

	/**
	 * @return the configDir
	 */
	public final String getConfigDir() {
		return configDir;
	}
	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return subDir.getSubstructDir();
	}

}
