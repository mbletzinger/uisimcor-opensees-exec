/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.FemSubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgram;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;

/**
 * Configuration for the executor.
 * 
 * @author Michael Bletzinger
 */
public class FemExecutorConfig {
	/**
	 * Map of FEM program parameters
	 */
	private final Map<FemProgram, FemProgramConfig> femProgramParameters = new HashMap<FemProgram, FemProgramConfig>();
	/**
	 * Substructure configurations map.
	 */
	private final Map<String, FemSubstructureConfig> substructCfgs = new HashMap<String, FemSubstructureConfig>();
	/**
	 * Directory to store temporary files.
	 */
	private final String workDir;

	/**
	 * @param workDir
	 *            Directory to store temporary files.
	 */
	public FemExecutorConfig(final String workDir) {
		this.workDir = workDir;
	}

	/**
	 * @return the femProgramPaths
	 */
	public final Map<FemProgram, FemProgramConfig> getFemProgramParameters() {
		return femProgramParameters;
	}

	/**
	 * @return the substructCfgs
	 */
	public final Map<String, FemSubstructureConfig> getSubstructCfgs() {
		return substructCfgs;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

}
