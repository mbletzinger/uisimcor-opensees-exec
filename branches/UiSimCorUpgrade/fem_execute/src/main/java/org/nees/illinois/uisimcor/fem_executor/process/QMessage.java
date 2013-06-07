package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Wrapper class for messages sent in the command and response queues.
 * @author Michael Bletzinger
 */
public class QMessage {
	/**
	 * Message types which indicate what to do with the contents.
	 * @author Michael Bletzinger
	 */
	public enum MessageType {
		/**
		 * Command to the FEM program; expecting a response.
		 */
		Command,
		/**
		 * We are shutting down.
		 */
		Exit,
		/**
		 * Response to a command.
		 */
		Response,
		/**
		 * Initialization content; no response expected.
		 */
		Setup
	}

	/**
	 * Message content.
	 */
	private final String content;;

	/**
	 * Message type.
	 */
	private final MessageType type;
	/**
	 * @param type
	 *            Message type.
	 * @param content
	 *            Message content.
	 */
	public QMessage(final MessageType type, final String content) {
		this.type = type;
		this.content = content;
	};

	/**
	 * @return the content
	 */
	public final String getContent() {
		return content;
	}

	/**
	 * @return the type
	 */
	public final MessageType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "QMessage [content=\"" + content + "\", type=" + type + "]";
	}

}
