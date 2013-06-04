package org.nees.illinois.uisimcor.fem_executor.input;

/**
 * Interface for generating FEM scripts during a simulation.
 * @author Michael Bletzinger
 */
public interface ScriptGeneratorI {
	/**
	 * Create the initialization commands for the FEM program.
	 * @return Script fragment.
	 */
	String generateInit();

	/**
	 * Create script commands to execute the step.
	 * @param step
	 *            Step number
	 * @param displacements
	 *            Displacements associated with the step.
	 * @return Script fragment.
	 */
	String generateStep(final int step, final double[] displacements);

}
