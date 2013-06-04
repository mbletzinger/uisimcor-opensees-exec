/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.config;

/**
 * Configuration parameters for an FEM program.
 *
 * @author Michael Bletzinger
 */
public class ProgramConfig {
	/**
	 * Path to executable.
	 */
	private final String executablePath;
	/**
	 * FEM program type.
	 */
	private final FemProgramType program;
	/**
	 * @param program
	 *            FEM program type.
	 * @param executablePath
	 *            Path to executable.
	 */
	public ProgramConfig(final FemProgramType program, final String executablePath) {
		this.program = program;
		this.executablePath = executablePath;
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

}
