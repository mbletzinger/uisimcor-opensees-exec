/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.ProgramDao;

/**
 * Configuration for the executor.
 * @author Michael Bletzinger
 */
public class FemExecutorConfig {
	/**
	 * Map of FEM program parameters.
	 */
	private final Map<FemProgramType, ProgramDao> femProgramParameters = new HashMap<FemProgramType, ProgramDao>();
	/**
	 * Substructure configurations map.
	 */
	private final Map<String, SubstructureDao> substructCfgs = new HashMap<String, SubstructureDao>();
	/**
	 * Directory to store temporary files.
	 */
	private final String configRoot;

	/**
	 * @param configRoot
	 *            Directory to store temporary files.
	 */
	public FemExecutorConfig(final String configRoot) {
		this.configRoot = configRoot;
	}

	/**
	 * @return the femProgramPaths
	 */
	public final Map<FemProgramType, ProgramDao> getFemProgramParameters() {
		return femProgramParameters;
	}

	/**
	 * @return the substructCfgs
	 */
	public final Map<String, SubstructureDao> getSubstructCfgs() {
		return substructCfgs;
	}

	/**
	 * @return the workDir
	 */
	public final String getConfigRoot() {
		return configRoot;
	}

}
