/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.input;

import java.io.File;
import java.io.IOException;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
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
	private SubstructureDao substructureCfg;

	/**
	 * Path to the working directory of the current substructure.
	 */
	private String workDir;

	/**
	 * Path to the root directory containing all of the processing files.
	 */
	private final String workDirRoot;

	/**
	 * Constructor.
	 * @param workDirRoot
	 *            Path to the root directory containing all of the processing
	 *            files.
	 * @param configDir
	 *            Root of directory containing the model configuration files.
	 */
	public WorkingDir(final String workDirRoot, final String configDir) {
		this.configDir = configDir;
		this.workDirRoot = workDirRoot;
	}

	/**
	 * Creates the working directory for the substructure. and copies all needed
	 * files.
	 */
	public final void createWorkDir() {
		if (workDir == null) {
			log.error("Please set my substructure configuration using setSubstructureCfg before telling me to create a working directory......idiot");
		}
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

	/**
	 * @return the configDir
	 */
	public final String getConfigDir() {
		return configDir;
	}

	/**
	 * @return the substructureCfg
	 */
	public final SubstructureDao getSubstructureCfg() {
		return substructureCfg;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * @return the workDirRoot
	 */
	public final String getWorkDirRoot() {
		return workDirRoot;
	}

	/**
	 * @param substructureCfg
	 *            the substructureCfg to set
	 */
	public final void setSubstructureCfg(final SubstructureDao substructureCfg) {
		this.substructureCfg = substructureCfg;
		this.workDir = PathUtils.append(workDirRoot,
				substructureCfg.getAddress());

		log.debug("For substructure " + substructureCfg.getAddress()
				+ " the workDir is \"" + this.workDir + "\"");
		log.debug("For substructure " + substructureCfg.getAddress()
				+ " the configDir is \"" + this.configDir + "\"");
	}

}
