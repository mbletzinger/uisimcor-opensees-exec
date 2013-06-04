/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.ProgramConfig;

/**
 * Configuration for the executor.
 * @author Michael Bletzinger
 */
public class FemExecutorConfig {
	/**
	 * Map of FEM program parameters.
	 */
	private final Map<FemProgramType, ProgramConfig> femProgramParameters = new HashMap<FemProgramType, ProgramConfig>();
	/**
	 * Substructure configurations map.
	 */
	private final Map<String, SubstructureConfig> substructCfgs = new HashMap<String, SubstructureConfig>();
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
	public final Map<FemProgramType, ProgramConfig> getFemProgramParameters() {
		return femProgramParameters;
	}

	/**
	 * @return the substructCfgs
	 */
	public final Map<String, SubstructureConfig> getSubstructCfgs() {
		return substructCfgs;
	}

	/**
	 * @return the workDir
	 */
	public final String getConfigRoot() {
		return configRoot;
	}

}
