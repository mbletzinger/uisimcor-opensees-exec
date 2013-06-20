package org.nees.illinois.uisimcor.fem_executor.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread for listening to socket connections.
 * @author Michael Bletzinger
 */
public class TcpListener extends Thread implements AbortableI {
	/**
	 * Queue for accepted client connections.
	 */
	private final BlockingQueue<TcpLinkDto> connections = new LinkedBlockingQueue<TcpLinkDto>();

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TcpListener.class);
	/**
	 * Parameters for the server socket.
	 */
	private final TcpParameters params;
	/**
	 * AbortableI flag.
	 */
	private volatile boolean quit = false;
	/**
	 * The socket for the server connection.
	 */
	private final ServerSocket server;
	/**
	 * @param params
	 *            Parameters for the server socket.
	 * @throws IOException
	 *             If the bind socket failed.
	 */
	public TcpListener(final TcpParameters params) throws IOException {
		this.params = params;
		server = new ServerSocket();
		server.setSoTimeout(params.getTcpTimeout());
		server.setReuseAddress(true);
		server.bind(new InetSocketAddress(params.getLocalPort()));
	}
	/**
	 * @return the connections
	 */
	public final BlockingQueue<TcpLinkDto> getConnections() {
		return connections;
	}

	/**
	 * @return the parameters for the TCP server socket.
	 */
	public final TcpParameters getParams() {
		return params;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public final void interrupt() {
		try {
			server.close();
		} catch (IOException e) {
			log.error("Server close failed because", e);
		}
		super.interrupt();
	}

	@Override
	public final synchronized boolean isQuit() {
		return quit;
	}

	/**
	 * Listen for connections.  Add any connections to the connections queue.
	 */
	private void listen() {
		Socket clientSocket = null;
		try {
			clientSocket = server.accept();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				log.debug("socket accept timed out");
				return;
			}
			if (e instanceof SocketException) {
				log.info("socket was closed");
				setQuit(true);
				return;
			}
			log.error("Server socket error on local port " + params.getLocalPort() + " because ",e);
			return;
		}
		TcpLinkDto connection = new TcpLinkDto(clientSocket);
		log.debug("Received new connection from " + connection.getRemoteHost());
		try {
			connections.put(connection);
		} catch (InterruptedException e) {
			log.debug("Queue put was interrupted");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public final void run() {
		log.info("Listening on port " + params.getLocalPort());
		while(isQuit() == false) {
			listen();
		}
		log.info("Stopped listening on port " + params.getLocalPort());
		try {
			server.close();
		} catch (IOException e) {
			log.debug("Close did not work but who cares", e);
		}
	}
	@Override
	public final synchronized void setQuit(final boolean quit) {
		this.quit = quit;
	}
}
