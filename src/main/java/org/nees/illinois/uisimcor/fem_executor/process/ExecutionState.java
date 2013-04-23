package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Possible execution states.
 */
public enum ExecutionState {
	/**
	 * Input file for FEM program is being created.
	 */
	CreatingInputFile,
	/**
	 * The command is executing.
	 */
	Executing,
	/**
	 * The command has finished executing.
	 */
	ExecutionFinished,
	/**
	 * We are done.
	 */
	Finished,
	/**
	 * We have not started executing yet.
	 */
	NotStarted,
	/**
	 * The output files are being parsed.
	 */
	ProcessingOutputFiles;
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
		if (this.equals(CreatingInputFile)) {
			return 1;
		}
		if (this.equals(Executing)) {
			return 2;
		}
		if (this.equals(ExecutionFinished)) {
			final int result = 3;
			return result;
		}
		if (this.equals(ProcessingOutputFiles)) {
			final int result = 4;
			return result;
		}
		final int result = 5;
		return result;
	}
}
