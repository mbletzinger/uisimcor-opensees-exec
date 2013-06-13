package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Message types which indicate what to do with the contents.
 * @author Michael Bletzinger
 */
public enum QMessageType {
	/**
	 * Command to the FEM program; expecting a response.  Content is a String
	 */
	Command,
	/**
	 * We are shutting down. No content.
	 */
	Exit,
	/**
	 * Response to a command. Content is a list of doubles.
	 */
	Response,
	/**
	 * Initialization content; no response expected. No content.
	 */
	Setup,
	/**
	 * Indicates that a step has started.
	 */
	StepStarted
}