package org.nees.illinois.uisimcor.fem_executor.execute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of status flags for an FEM process.
 * @author Michael Bletzinger
 */
public class FemStatus {
	/**
	 * True if any statuses have changed since the last query.
	 */
	private boolean changed;
	/**
	 * Flag indicating that the process has finished executing a step. The
	 * process is most likely still sending responses via TCP at this point.
	 */
	private boolean currentStepHasExecuted = false;
	/**
	 * Flag indicating that displacements have been received from the process.
	 */
	private boolean displacementsAreHere = false;
	/**
	 * Flag indicating that the FEM process is no longer running.
	 */
	private boolean femProcessHasDied = false;
	/**
	 * Flag indicating the the FEM process has output errors in STDERR.
	 */
	private boolean femProcessHasErrors = false;
	/**
	 * Flag indicating that forces have been received from the process.
	 */
	private boolean forcesAreHere = false;
	/**
	 * The last step that has been executed by the FEM process so far.
	 */
	private String lastExecutedStep;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(FemStatus.class);

	/**
	 * @return the lastExecutedStep
	 */
	public final String getLastExecutedStep() {
		return lastExecutedStep;
	}

	/**
	 * @return a String describing the current status.
	 */
	public final String getStatus() {
		if (femProcessHasDied) {
			return "No Longer Running.";
		}
		if (femProcessHasErrors) {
			return "Has Errors";
		}
		if (currentStepHasExecuted == false) {
			return "Still Executing";
		}
		if (displacementsAreHere == false && forcesAreHere == false) {
			return "Waiting for Displacements and Forces";
		}
		if (displacementsAreHere == false) {
			return "Waiting for Displacements";
		}
		if (forcesAreHere == false) {
			return "Waiting for Forces";
		}
		log.debug("Status: " + dump());
		return "I'm Confused";
	}

	/**
	 * @return the changed. Also resets the flag to false;
	 */
	public final boolean isChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}

	/**
	 * @return the stepHasExecuted
	 */
	public final boolean isCurrentStepHasExecuted() {
		return currentStepHasExecuted;
	}

	/**
	 * @return the displacementsAreHere
	 */
	public final boolean isDisplacementsAreHere() {
		return displacementsAreHere;
	}

	/**
	 * @return the femProcessHasDied
	 */
	public final boolean isFemProcessHasDied() {
		return femProcessHasDied;
	}

	/**
	 * @return the femProcessHasErrors
	 */
	public final boolean isFemProcessHasErrors() {
		return femProcessHasErrors;
	}

	/**
	 * @return the forcesAreHere
	 */
	public final boolean isForcesAreHere() {
		return forcesAreHere;
	}

	/**
	 * Reset flags for a new step.
	 */
	public final void newStep() {
		displacementsAreHere = false;
		forcesAreHere = false;
		currentStepHasExecuted = false;
		changed = true;
	}

	/**
	 * @return Cumulative status indicating all of the responses have been
	 *         received for the current step.
	 */
	public final boolean responsesHaveArrived() {
		return displacementsAreHere && forcesAreHere;
	}

	/**
	 * @param stepHasExecuted
	 *            the stepHasExecuted to set
	 */
	public final void setCurrentStepHasExecuted(final boolean stepHasExecuted) {
		this.currentStepHasExecuted = stepHasExecuted;
		changed = true;
	}

	/**
	 * @param displacementsAreHere
	 *            the displacementsAreHere to set
	 */
	public final void setDisplacementsAreHere(final boolean displacementsAreHere) {
		this.displacementsAreHere = displacementsAreHere;
		changed = true;
	}

	/**
	 * @param femProcessHasDied
	 *            the femProcessHasDied to set
	 */
	public final void setFemProcessHasDied(final boolean femProcessHasDied) {
		this.femProcessHasDied = femProcessHasDied;
		changed = true;
	}

	/**
	 * @param femProcessHasErrors
	 *            the femProcessHasErrors to set
	 */
	public final void setFemProcessHasErrors(final boolean femProcessHasErrors) {
		this.femProcessHasErrors = femProcessHasErrors;
		changed = true;
	}

	/**
	 * @param forcesAreHere
	 *            the forcesAreHere to set
	 */
	public final void setForcesAreHere(final boolean forcesAreHere) {
		this.forcesAreHere = forcesAreHere;
		changed = true;
	}

	/**
	 * @param lastExecutedStep
	 *            the lastExecutedStep to set
	 */
	public final void setLastExecutedStep(final String lastExecutedStep) {
		this.lastExecutedStep = lastExecutedStep;
		changed = true;
	}

	/**
	 * @return A string representation of the statuses.
	 */
	public final String dump() {
		String result = (currentStepHasExecuted ? "[StepDone]"
				: "[StepStillExecuting]");
		result += (displacementsAreHere ? "[DisplacementsHere]"
				: "[DisplacementsMissing]");
		result += (forcesAreHere ? "[ForcesHere]" : "[ForcesMissing]");
		result += (femProcessHasDied ? "[Dead]" : "[Running]");
		result += (femProcessHasErrors ? "[Errors]" : "[Clean]");
		return result;
	}
}
