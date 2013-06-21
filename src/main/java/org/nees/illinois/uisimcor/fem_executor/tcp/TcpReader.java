package org.nees.illinois.uisimcor.fem_executor.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to read data from an OpenSees TCP socket.
 * @author Michael Bletzinger
 */
public class TcpReader extends Thread implements AbortableI {
	/**
	 * @return the doublesQ
	 */
	public final BlockingQueue<List<Double>> getDoublesQ() {
		return doublesQ;
	}

	/**
	 * @return the link
	 */
	public final TcpLinkDto getLink() {
		return link;
	}

	/**
	 * Quit flag for {@link AbortableI}.
	 */
	private volatile boolean quit = false;
	/**
	 * Queue to be filled with double lists.
	 */
	private final BlockingQueue<List<Double>> doublesQ = new LinkedBlockingQueue<List<Double>>();
	/**
	 * Input stream for the client connection.
	 */
	private final InputStream in;

	/**
	 * @param link
	 *            Client connection to read.
	 * @throws IOException
	 *             If the input stream does not exist.
	 */
	public TcpReader(final TcpLinkDto link) throws IOException {
		this.link = link;
		in = link.getSocket().getInputStream();
	}

	/**
	 * Client connection to read.
	 */
	private final TcpLinkDto link;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TcpReader.class);

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public final void interrupt() {
		try {
			link.getSocket().close();
		} catch (IOException e) {
			log.error("Socket close failed because", e);
		}
		super.interrupt();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public final void run() {
		log.info("Listening to " + link.getRemoteHost());
		double junk = readNumber();
		log.debug("Clearing initial junk " + junk);
		while (isQuit() == false) {
			read();
		}
		log.info("Stopped reading from " + link.getRemoteHost());
		try {
			link.getSocket().close();
		} catch (IOException e) {
			log.debug("Close did not work but who cares", e);
		}
	}

	/**
	 * Read one number and trap any exceptions.
	 * @return a double value
	 */
	private Double readNumber() {
		double number;
		try {
			number = readBytesFor1Number();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				log.debug("socket read timed out");
				return null;
			}
			if (e instanceof SocketException) {
				log.info("socket was closed");
				setQuit(true);
				return null;
			}
			log.error(
					"Socket error with " + link.getRemoteHost() + " because ",
					e);
			return null;
		}
		if (number < 0) {
			log.info("Stream closed remotely");
			setQuit(true);
			this.interrupt();
		}
		return number;
	}

	/**
	 * Read a data record and put into the doubles queue.
	 */
	private void read() {
		double size;
		try {
			size = readNumber();
		} catch (NullPointerException e) {
			if (isQuit()) {
				log.debug("Ignore this because we are shutting down");
				return;
			}
			throw e;
		}
		if (size < 1.0) {
			log.error("Size is zero");
			return;
		}
		long sizei = Math.round(size);
		log.debug("Reading " + sizei + " numbers");
		List<Double> list = new ArrayList<Double>();
		for (int n = 0; n < sizei; n++) {
			try {
				double number = readBytesFor1Number();
				list.add(new Double(number));
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
				log.error("Socket error with " + link.getRemoteHost()
						+ " because ", e);
				return;
			}
		}
		log.debug("Read " + list + " record");
		try {
			doublesQ.put(list);
		} catch (InterruptedException e) {
			log.debug("Queue put was interrupted");
		}
	}

	/**
	 * Read a double from the client connection stream.
	 * @return the double.
	 * @throws IOException
	 *             if the input stream is broken.
	 */
	private double readBytesFor1Number() throws IOException {
		final int numberOfBytesInDouble = 8;
		byte[] number = new byte[numberOfBytesInDouble];
		int total = 0;
		// log.debug("Attempting to read");
		while (total < numberOfBytesInDouble) {
			byte[] buf = new byte[numberOfBytesInDouble - total];
			int count = 0;
			count = in.read(buf);
			if (count < 0) {
				// End of stream reached.
				throw new SocketException("End of stream reached");
			}
			for (int b = 0; b < count; b++) {
				number[total + b] = buf[b];
			}
			total += count;
		}
		return little2BigEndian(number);
	}

	/**
	 * Convert endian of a double to network order.
	 * @param number
	 *            bytes of the number to be converted.
	 * @return The resulting double.
	 */
	private double little2BigEndian(final byte[] number) {
		final int numberOfBytesInDouble = 8;
		ByteBuffer bnum = ByteBuffer.allocate(numberOfBytesInDouble);
		bnum.order(ByteOrder.LITTLE_ENDIAN);
		bnum.put(number);
		bnum.flip();
		return bnum.getDouble();
	}

	@Override
	public final synchronized boolean isQuit() {
		return quit;
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
		this.quit = quit;
	}
}
