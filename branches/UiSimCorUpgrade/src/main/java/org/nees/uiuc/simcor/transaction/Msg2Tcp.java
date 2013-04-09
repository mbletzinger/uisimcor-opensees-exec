package org.nees.uiuc.simcor.transaction;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;

public class Msg2Tcp {
	private TransactionIdentity id;

	private final Logger log = Logger.getLogger(Msg2Tcp.class);

	private final String mdlPattern = "MDL";

	private SimCorMsg msg = new SimCorMsg();

	public String assemble() {
		// propose trans200912317925.320 MDL-00-01:LBCB 2 x displacement 0.5 y
		// displacement 0.0
		String result = "";
		if (msg == null) {
//			log.error("Message not set");
			result = "null";
			log.debug("assemble msg \"" + result + "\"");
			return result;
		}
		if (msg.getType() == MsgType.COMMAND) {
			result += msg.getCommand();
			log.debug("assemble msg \"" + result + "\"");
		}
		if (msg.getType() == MsgType.OK_RESPONSE) {
			result += "OK	0";
			log.debug("assemble msg \"" + result + "\"");
		}
		if (msg.getType() == MsgType.NOT_OK_RESPONSE) {
			result += "NOTOK	0";
			log.debug("assemble msg \"" + result + "\"");
		}
		if (id != null && id.getTransId() != null) {
			if (id.getStep() >= 0) {
				result += "\t" + id;
			} else {
				result += "\t" + id.getTransId();
			}
			log.debug("assemble msg \"" + result + "\"");
		}
		if (msg instanceof SimCorCompoundMsg) {
			result += assembleCompoundContent();
		} else {
			result += assembleSimpleContent();
		}
		log.debug("assemble msg \"" + result + "\"");
		// result += "\n";
		return result;
	}
	private String assembleCompoundContent() {
		String result ="";
		SimCorCompoundMsg cmsg = (SimCorCompoundMsg) msg;
		for(Iterator<Address> i = cmsg.getAddresses().iterator(); i.hasNext();) {
			Address a = i.next();
			result += "\t" + a + "\t" + cmsg.getContent(a);
		}
		return result;
	}
	private String assembleSimpleContent() {
		String result ="";
		if(msg.getAddress() != null) {
			result += "\t" + msg.getAddress();
		}
		if(msg.getContent() != null) {
			result += "\t" + msg.getContent(); 
		}
		return result;
	}
	public void clear() {
		msg = null;
		id = null;
	}
	
	public TransactionIdentity getId() {
		return id;
	}

	public SimCorMsg getMsg() {
		return msg;
	}

	public void parse(String msgString) {

		log.debug("Parsing [" + msgString + "]");
		if (msgString.indexOf(mdlPattern) > 0
				&& msgString.indexOf(mdlPattern) != msgString
						.lastIndexOf(mdlPattern)) {
			msg = new SimCorCompoundMsg();
		} else {
			msg = new SimCorMsg();
		}
		id = null;
		String[] lineT = msgString.split("\\\t");
		List<String> tokens = StringUtilities.array2List(lineT);

		if (lineT[0].equals("OK")) {
			msg.setType(MsgType.OK_RESPONSE);
			parseResponse(tokens);
			log.debug("Result id: " + " msg: " + msg);
			return;
		} else if (lineT[0].equals("NOT OK")) {
			msg.setType(MsgType.NOT_OK_RESPONSE);
			parseResponse(tokens);
			log.debug("Result id: " + " msg: " + msg);
			return;
		} else {
			msg.setType(MsgType.COMMAND);
			parseCommand(tokens);
			log.debug("Result id: " + " msg: " + msg);
			return;

		}
			
	}
	private void parseCommand(List<String> tokens) {
		String command = tokens.get(0);
		msg.setCommand(command);
		if(command.equals("propose")) {
			parseTransId(tokens.get(1));
			parseContent(tokens.subList(2, tokens.size()));
			return;
		}
		if(command.equals("execute") || command.equals("trigger")) {
			parseTransId(tokens.get(1));
			return;
		}
		if(command.equals("get-control-point")) {
			parseTransId(tokens.get(1));
			parseContent(tokens.subList(2, tokens.size()));
			return;
		}
		List<String> sub = tokens.subList(1, tokens.size());
		parseContent(sub);
	}

	private void parseContent(List<String> tokens) {
		String content = null;
		String address = null;
		SimCorCompoundMsg cmsg = null;
		if (msg instanceof SimCorCompoundMsg) {
			cmsg = (SimCorCompoundMsg) msg;
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).contains("MDL")) {
				if (address != null) {
					log
							.debug("Setting addr:" + address + " content:"
									+ content);
					cmsg.setContent(new Address(address), content);
				}
				address = tokens.get(i);
				content = null;
				continue;
			}
			if (content == null) {
				content = tokens.get(i);
			} else {
				content += "\t" + tokens.get(i);
			}
		}
		log.debug("Setting addr:" + address + " content:" + content);
		if (msg instanceof SimCorCompoundMsg) {
			cmsg.setContent(new Address(address), content);
		} else {
			if (address != null) {
				msg.setAddress(new Address(address));
			}
			msg.setContent(content);
		}

	}

	private void parseResponse(List<String> tokens) {
		if (tokens.get(2).equals("dummy")) {// Assume this is a get-control-point
										// response
			parseTransId(tokens.get(2));
			parseContent(tokens.subList(3, tokens.size()));
			return;
		}
		if (tokens.get(2).contains("trans")) { // propose response
			parseTransId(tokens.get(2));
			parseContent(tokens.subList(3, tokens.size()));
			return;
		}
		parseContent(tokens.subList(2, tokens.size()));
	}

	public void parseTransId(String token) {
		int i = token.indexOf("[");
		log.debug("Parsing transid [" + token + "]");
		if (i >= 0) {
			int c1 = token.indexOf(" ");
			int c2 = token.lastIndexOf(" ");
			int k = token.indexOf("]");
			id = new TransactionIdentity();
			id.setTransId(token.substring(0, i));
			log.debug("Parsing \"" + token + "\" /c1=" + c1 + "/c2=" + c2 + "/k=" + k);
			if(c1 >= token.length() || k >= token.length()) {
				log.error("Parsing transid [" + token + "] failed i c k" + i + " " + c1 + "," + k);
			}
			if(c1 < 0 || k < 0 || i < 0) {
				log.error("Parsing transid [" + token + "] failed i c k" + i + " " + c1 + "," + k);
			}
			id.setStep(Integer.parseInt(token.substring(i + 1, c1)));
			id.setSubStep(Integer.parseInt(token.substring(c1 + 1, c2)));
			id.setCorrectionStep(Integer.parseInt(token.substring(c2 + 1, k)));
		} else {
			id = new TransactionIdentity();
			id.setTransId(token);
		}
	
	}
	
	public void setId(TransactionIdentity id) {
		this.id = id;
	}

	public void setMsg(SimCorMsg msg) {
		this.msg = msg;
	}
}
