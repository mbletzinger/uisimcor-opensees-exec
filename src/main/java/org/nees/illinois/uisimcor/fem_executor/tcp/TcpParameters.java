package org.nees.illinois.uisimcor.fem_executor.tcp;

/**
 * Class containing parameters to set up and manage a TCP link.
 * @author Michael Bletzinger
 */
public class TcpParameters {
	/**
	 * Local port for links that are server sockets.
	 */
	private final int localPort;
	/**
	 * Remote host for client connection links.
	 */
	private final String remoteHost;
	/**
	 * Remote port for client connection links.
	 */
	private final int remotePort;
	/**
	 * Amount of time to do socket commands such as read, write, and accept.
	 */
	private final int tcpConnectionTimeout;

	/**
	 * Constructor.
	 * @param remoteHost
	 *            Remote host for client connection links.
	 * @param remotePort
	 *            Remote port for client connection links.
	 * @param localPort
	 *            Local port for links that are server sockets.
	 * @param tcpReadTimeout
	 *            Amount of time to do socket commands such as read, write, and
	 *            accept.
	 */
	public TcpParameters(final String remoteHost, final int remotePort, final int localPort,
			final int tcpReadTimeout) {
		super();
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.tcpConnectionTimeout = tcpReadTimeout;
	}

	/**
	 * @return the local port.
	 */
	public final int getLocalPort() {
		return localPort;
	}

	/**
	 * @return the remote host.
	 */
	public final String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @return the remote port.
	 */
	public final int getRemotePort() {
		return remotePort;
	}

	/**
	 * @return the timeout value.
	 */

	public final int getTcpTimeout() {
		return tcpConnectionTimeout;
	}

	/**
	 * @return true if the link is a server connection.
	 */

	public final boolean isListener() {
		return localPort > 0;
	}
}
