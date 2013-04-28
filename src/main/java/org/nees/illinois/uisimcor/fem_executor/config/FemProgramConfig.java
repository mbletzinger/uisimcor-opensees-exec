/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.config;

/**
 * Configuration parameters for an FEM program.
 *
 * @author Michael Bletzinger
 */
public class FemProgramConfig {
	/**
	 * Path to executable.
	 */
	private final String executablePath;
	/**
	 * FEM program type.
	 */
	private final FemProgramType program;
	/**
	 * Path to static analysis script.
	 */
	private final String staticAnalysisScriptPath;

	/**
	 * @param program
	 *            FEM program type.
	 * @param executablePath
	 *            Path to executable.
	 * @param staticAnalysisScriptPath
	 *            Path to static analysis script.
	 */
	public FemProgramConfig(final FemProgramType program, final String executablePath,
			final String staticAnalysisScriptPath) {
		this.program = program;
		this.executablePath = executablePath;
		this.staticAnalysisScriptPath = staticAnalysisScriptPath;
	}

	/**
	 * @return the executablePath
	 */
	public final String getExecutablePath() {
		return executablePath;
	}

	/**
	 * @return the program
	 */
	public final FemProgramType getProgram() {
		return program;
	}

	/**
	 * @return the staticAnalysisScriptPath
	 */
	public final String getStaticAnalysisScriptPath() {
		return staticAnalysisScriptPath;
	}

}
