package org.nees.illinois.uisimcor.fem_executor.tcp;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Class containing TCP socket info.
 * @author Michael Bletzinger
 */
public class TcpLinkDto {
	/**
	 * Host on the other end of the link.
	 */
	private final String remoteHost;
	/**
	 * Socket connection.
	 */
	private final Socket socket;

	/**
	 * @param socket
	 *            Socket connection.
	 */
	public TcpLinkDto(final Socket socket) {
		this.socket = socket;
		InetAddress address = socket.getInetAddress();
		remoteHost = address.getCanonicalHostName();
	}

	/**
	 * @return the remote host.
	 */
	public final String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @return the socket.
	 */
	public final Socket getSocket() {
		return socket;
	}
}
