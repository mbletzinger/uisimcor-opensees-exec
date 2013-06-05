package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Possible execution states.
 */
public enum ExecutionState {
	/**
	 * Start up the FEM program.
	 */
	StartSimulation,
	/**
	 * The step command is starting execution.
	 */
	StartStep,
	/**
	 * The step command is executing.
	 */
	StepExecuting,
	/**
	 * Wait for the next step command.
	 */
	WaitingForNextStep,
	/**
	 * We have not started executing yet.
	 */
	NotStarted,
	/**
	 * The step output are being parsed.
	 */
	ProcessingStepResponse,
	/**
	 * Aborting Simulation.
	 */
	Aborting,
	/**
	 * Shutting down the simulation.
	 */
	ShuttingDown;
	/**
	 * Return the sequence order of the enumerator type. The elements are listed
	 * in alphabetical order because of editor formatting so the ordinal()
	 * function does not return the proper sequence order.
	 * @return Sequence order.
	 */
	public int getOrder() {
		if (this.equals(NotStarted)) {
			return 0;
		}
		if (this.equals(StartSimulation)) {
			return 1;
		}
		if (this.equals(StartStep)) {
			return 2;
		}
		if (this.equals(StepExecuting)) {
			final int result = 3;
			return result;
		}
		if (this.equals(ProcessingStepResponse)) {
			final int result = 4;
			return result;
		}
		if (this.equals(WaitingForNextStep)) {
			final int result = 5;
			return result;
		}
		if (this.equals(Aborting)) {
			final int result = 6;
			return result;
		}
		final int result = 7;
		return result;
	}
}
