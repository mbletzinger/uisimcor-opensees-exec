package org.nees.illinois.uisimcor.fem_executor.execute;


/**
 * Interface to get a step executed from an FEM program.
 * @author Michael Bletzinger
 */
public interface SubstructureExecutorI {

	/**
	 * Abort the execution.
	 */
	void abort();

	/**
	 * @return double array in node order of displacements at effective DOFs
	 */
	double[] getDisplacements();

	/**
	 * @return double array in node order of reaction forces at effective DOFs
	 */
	double[] getForces();

	/**
	 * @return the statuses
	 */
	FemStatus getStatuses();

	/**
	 * Determines if the execution is still proceeding correctly.
	 * @return True if the execution is broken in some way.
	 */
	boolean iveGotProblems();

	/**
	 * Setup links for the FEM program.
	 * @return True if successful.
	 */
	boolean setup();

	/**
	 * Start the FEM program and listen for socket connection requests.
	 * @return True if simulation has started.
	 */
	boolean startSimulation();

	/**
	 * Send the next step command to the FEM program.
	 * @param step
	 *            Current step.
	 * @param displacements
	 *            Current displacement target.
	 */
	void startStep(int step, double[] displacements);

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 */
	boolean stepIsDone();

}
