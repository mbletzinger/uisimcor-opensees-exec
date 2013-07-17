package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to create processing directories that are organized.
 * @author Michael Bletzinger
 */
public class SubstructureDir {

	/**
	 * Path to the root directory containing all of the processing files.
	 */
	private final String dirRoot;
	/**
	 * Path to the working directory of the current substructure.
	 */
	private String substructDir;
	/**
	 * Substructure configuration parameters.
	 */
	private final SubstructureDao substructureCfg;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(SubstructureDir.class);

	/**
	 * @param dirRoot
	 *            Path to the root directory containing all of the processing
	 *            files.
	 * @param dirType
	 *            Type of directory structure. For example "workDir" or
	 *            "logDir";
	 * @param substructureCfg
	 *            Substructure configuration parameters.
	 */
	public SubstructureDir(final String dirRoot,
			final SubstructureDao substructureCfg, final String dirType) {
		this.dirRoot = dirRoot;
		this.substructureCfg = substructureCfg;
		String path = PathUtils.append(dirRoot, dirType);
		this.substructDir = PathUtils
				.append(path, substructureCfg.getAddress());
	}

	/**
	 * @return the workDirRoot
	 */
	public final String getDirRoot() {
		return dirRoot;
	}

	/**
	 * @return the workDir
	 */
	public final String getSubstructDir() {
		return substructDir;
	}

	/**
	 * @return the substructureCfg
	 */
	public final SubstructureDao getSubstructureCfg() {
		return substructureCfg;
	}

	/**
	 * Creates the directory for the substructure.
	 */
	public final void createSubstructDir() {
		if (substructureCfg == null) {
			log.error("Please set my substructure configuration using setSubstructureCfg before telling me to create a directory");
		}
		File workDirF = new File(substructDir);
		if (workDirF.exists()) {
			FileWithContentDelete killDir = new FileWithContentDelete(
					substructDir);
			killDir.delete();
		}
		try {
			workDirF.mkdirs();
			log.debug("\"" + substructDir + "\" was created");
		} catch (Exception e) {
			log.error("Cannot create directory \"" + substructDir
					+ "\" because ", e);
			return;
		}
		log.debug("Directory created at \"" + substructDir + "\"");

	}

}
