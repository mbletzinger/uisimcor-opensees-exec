package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Wrapper class for messages sent in the command and response queues.
 * @author Michael Bletzinger
 * @param <C>
 * Type of the message content.
 */
public class QMessageT<C> {
	/**
	 * Message content.
	 */
	private final C content;

	/**
	 * Message type.
	 */
	private final QMessageType type;
	/**
	 * @param type
	 *            Message type.
	 * @param content
	 *            Message content.
	 */
	public QMessageT(final QMessageType type, final C content) {
		this.type = type;
		this.content = content;
	};

	/**
	 * @return the content
	 */
	public final C getContent() {
		return content;
	}

	/**
	 * @return the type
	 */
	public final QMessageType getType() {
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
