package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.matlab.StringListUtils;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class ReadTcpAction {
	public enum TcpReadStatus {
		DONE, ERRORED, STILL_READING
	}

	byte[] buf;

	byte crByte;

	private byte[] eom = new byte[2];

	private TcpError error;

	InputStream in = null;

	private final TcpLinkDto link;

	private final Logger log = Logger.getLogger(ReadTcpAction.class);

	private Msg2Tcp message;
	private int msgTimeout;;

	private final StringListUtils slu = new StringListUtils();
	StringBuffer soFar;

	long start;

	boolean stillReading;

	public ReadTcpAction(boolean isLfcrSendEom, TcpLinkDto link) {
		super();
		this.link = link;
		if (isLfcrSendEom) {
			String endOfMsgS = "\r\n";
			eom = endOfMsgS.getBytes();
		} else {
			String endOfMsgS = "\n";
			eom = endOfMsgS.getBytes();
		}
		log.debug("eom is [" + eom + "]");
		if (log.isDebugEnabled()) {
			String str = slu.Byte2HexString(eom);
			log.debug("EOM[" + str + "] length " + eom.length);
		}
		reset();
	}
	private int availableBytes() {
		int result;
		try {
			result = in.available();
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return -1;
		}
		return result;
	}

	private TcpReadStatus checkTimeOut() {
		long time = (System.currentTimeMillis() - start);
		error = new TcpError();
		message = new Msg2Tcp();
		if (msgTimeout <= 0) { // Timeout has been disabled
			error.setType(TcpErrorTypes.NONE);
			return TcpReadStatus.STILL_READING;			
		}
		if (time > msgTimeout) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " timed out > "
					+ msgTimeout;
			log.error(msg);
			error.setText(msg);
			error.setType(TcpErrorTypes.TIMEOUT);
			return TcpReadStatus.ERRORED;
		} else {
			error.setType(TcpErrorTypes.NONE);
			return TcpReadStatus.STILL_READING;
		}
	}

	/**
	 * @return the error
	 */
	public TcpError getError() {
		return error;
	}

	/**
	 * @return the link
	 */
	public TcpLinkDto getLink() {
		return link;
	}
	/**
	 * @return the message
	 */
	public Msg2Tcp getMessage() {
		return message;
	}
	/**
	 * @return the msgTimeout
	 */
	public int getMsgTimeout() {
		return msgTimeout;
	}
	/**
	 * @return the stillReading
	 */
	public boolean isStillReading() {
		return stillReading;
	}
	private TcpReadStatus readBytes(int avail) {
		int amt = avail;
		if (amt > buf.length)
			amt = buf.length;

		try {
			amt = in.read(buf, 0, amt);
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			message = new Msg2Tcp();
			return TcpReadStatus.ERRORED;
		}
		log.debug("READ: [" + slu.Byte2HexString(buf) + "]");
		int marker = 0;
		for (int i = 0; i < amt; i++) {
			// scan for the CRLF characters which delineate messages
			// This assumes that the LF character is always last
			int cridx = eom.length - 1;
			if (buf[i] == eom[cridx]) {
				marker = i - 1;
				log.debug("found CR at " + i);
				if (i > 0) {
					crByte = buf[i - 1];
				}
				if (crByte != eom[0]) {
					marker = i;
				}
				String tmp = new String(buf, 0, marker);
				log.debug("found chars " + tmp);
				soFar.append(tmp);
				message = new Msg2Tcp();
				message.parse(soFar.substring(0, soFar.length())); // Make
																	// sure
																	// CR
																	// is
																	// gone
				log.debug("Received [" + soFar.toString() + "]");
				error = new TcpError();
				error.setType(TcpErrorTypes.NONE);
				return TcpReadStatus.DONE;
			}
			crByte = buf[amt - 1];
		}
		if (marker < amt) {
			// save all so far, still waiting for the final EOM
			soFar.append(new String(buf, marker, amt - marker));
		}
		return TcpReadStatus.STILL_READING;
	}
	public TcpReadStatus readMessage() {
		if (stillReading == false) {
			TcpReadStatus strt = startReading();
			if (strt.equals(TcpReadStatus.ERRORED)) {
				reset();
				return strt;
			}
		}

		// collect all the bytes waiting on the input stream
		int avail = availableBytes();
		if(avail < 0) {
			reset();
			return TcpReadStatus.ERRORED;
		}

		while (avail > 0) {
			TcpReadStatus result = readBytes(avail);
			if(result.equals(TcpReadStatus.STILL_READING) == false) {
				stillReading = false;
				return result;
			}
			avail = availableBytes();
			if(avail < 0) {
				reset();
				return TcpReadStatus.ERRORED;
			}
		}
		TcpReadStatus result = checkTimeOut();
		return result;
	}

	private void reset() {
		stillReading = false;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(TcpError error) {
		this.error = error;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Msg2Tcp message) {
		this.message = message;
	}

	/**
	 * @param msgTimeout the msgTimeout to set
	 */
	public void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	/**
	 * @param stillReading the stillReading to set
	 */
	public void setStillReading(boolean stillReading) {
		this.stillReading = stillReading;
	}
	private TcpReadStatus startReading() {
		try {
			in = link.getSocket().getInputStream();
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return TcpReadStatus.ERRORED;
		}
		soFar = new StringBuffer();
		buf = new byte[1024];
		crByte = 0;
		stillReading = true;
		// loop until message is completed
		start = System.currentTimeMillis();
		return TcpReadStatus.STILL_READING;
	}
}
