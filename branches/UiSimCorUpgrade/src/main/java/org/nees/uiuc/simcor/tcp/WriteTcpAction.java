package org.nees.uiuc.simcor.tcp;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.matlab.StringListUtils;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class WriteTcpAction {
	private byte[] eom = new byte[2];

	private TcpError error;

	private final boolean isLfcrSendEom;
	private final TcpLinkDto link;
	private final Logger log = Logger.getLogger(WriteTcpAction.class);
	private final StringListUtils slu = new StringListUtils();

	public WriteTcpAction(boolean isLfcrSendEom, TcpLinkDto link) {
		super();
		this.isLfcrSendEom = isLfcrSendEom;
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
	}

	/**
	 * @return the error
	 */
	public TcpError getError() {
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(TcpError error) {
		this.error = error;
	}

	public boolean write(Msg2Tcp tmsg) {
		OutputStream out = null;
		String remoteHost = link.getRemoteHost();
		String outMsg = tmsg.assemble();
		if (link.getSocket() == null) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed socket was not initialized";
			log.error(msg);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;

		}

		if (link.getSocket().isOutputShutdown() || link.getSocket().isClosed()
				|| link.getSocket().isInputShutdown()
				|| (link.getSocket().isConnected() == false)) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed socket is closed";
			log.error(msg);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;

		}
		try {
			out = link.getSocket().getOutputStream();
			int lng = outMsg.length();
			byte[] mbuf = outMsg.getBytes();
			byte[] buf = new byte[lng + eom.length];

			for (int b = 0; b < lng; b++) {
				buf[b] = mbuf[b];
			}

			buf[lng] = eom[0];

			if (isLfcrSendEom) {
				buf[lng + 1] = eom[1];
			}
			log.debug("WRITE: [" + slu.Byte2HexString(buf) + "]");
			out.write(buf);
			out.flush();
		} catch (Exception e) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;
		}
		log.debug("Sent [" + outMsg + "]");
		error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		return true;
	}
}
