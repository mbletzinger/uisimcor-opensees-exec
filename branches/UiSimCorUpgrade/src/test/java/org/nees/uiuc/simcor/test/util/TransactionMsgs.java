package org.nees.uiuc.simcor.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.SimCorCompoundMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;

public class TransactionMsgs {
	public List<SimpleTransaction> cmdList;
	public HashMap<String, SimpleTransaction> transactions;
	public Transaction triggerTransaction;
	private final Logger log = Logger.getLogger(TransactionMsgs.class);

	public TransactionMsgs() {
		this.cmdList = new ArrayList<SimpleTransaction>();
		this.transactions = new HashMap<String,SimpleTransaction>();

	}

	public void setUp() throws Exception {

		SimpleTransaction transaction = new TransactionWithTestFlags();
		SimCorMsg msg = new SimCorMsgWithTestFlags();
		SimCorMsg resp = new SimCorMsgWithTestFlags();
	
		transaction = new TransactionWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		msg.setCommand("set-parameter");
		msg.setContent("dummySetParam	nstep	0");
	
		resp = new SimCorMsgWithTestFlags();
		resp.setContent("Command ignored. Carry on.");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new TransactionWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		TransactionIdentity id = new TransactionIdentityWithTestFlags();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:LBCB2"));
	
		resp = new SimCorMsgWithTestFlags();
		resp.setAddress(new Address("MDL-00-01:LBCB2"));
		resp.setContent("x	displacement	5.036049E-1" +
				"	y	displacement	1.557691E-4" +
				"	z	displacement	-7.850649E-4" + 
				"	x	rotation	3.829964E-5" +
				"	y	rotation	-2.747683E-5" +
				"	z	rotation	1.195688E-3" +
				"	x	force	6.296413E+0" +
				"	y	force	1.451685E-1" +
				"	z	force	-1.252275E+0" +
				"	x	moment	7.296673E-1" +
				"	y	moment	-2.214440E+0" +
				"	z	moment	3.522242E-2");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new TransactionWithTestFlags();
		id = new TransactionIdentityWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:ExternalSensors"));
	
		resp = new SimCorMsgWithTestFlags();
		resp.setAddress(new Address("MDL-00-01:ExternalSensors"));
		resp.setContent("Ext.Long.LBCB2	external	2.422813E-1	" +
				"Ext.Tranv.TopLBCB2	external	2.422813E-1	" + 
				"Ext.Tranv.Bot.LBCB2	external	2.422813E-1	" + 
				"Ext.Long.LBCB1	external	2.422813E-1	" + 
				"Ext.Tranv.LeftLBCB1	external	2.422813E-1	" + 
				"Ext.Tranv.RightLBCB1	external	2.422813E-1");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new TransactionWithTestFlags();
		id = new TransactionIdentityWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		msg.setCommand("get-control-point");
		id.setTransId("dummy");
		msg.setAddress(new Address("MDL-00-01:ExternalSensors"));
	
		resp = new SimCorMsgWithTestFlags();
		resp.setAddress(new Address("MDL-00-01:ExternalSensors"));
		resp.setContent("	1_LBCB1_x	external	-7.565553E-1"
				+ "	2_LBCB1_z_right	external	0.000000E+0"
				+ "	3_LBCB1_z_left	external	-8.081407E-1"
				+ "	4_LBCB2_z_right	external	0.000000E+0"
				+ "	5_LBCB2_z_left	external	7.518438E-2"
				+ "	6_LBCB2_x	external	0.00]000E+0");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);

		transaction = new TransactionWithTestFlags();
		id = new TransactionIdentityWithTestFlags();
		SimCorCompoundMsg cmsg = new SimCorCompoundMsgWithTestFlags();
		cmsg.setCommand("propose");
		id.createTransId();
		cmsg.setContent(new Address("MDL-00-01:LBCB1"),"x	displacement	0.5	y	displacement	0.0");
		cmsg.setContent(new Address("MDL-00-01:LBCB2"),"z	displacement	0.5	y	rotation	0.002");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsgWithTestFlags();
		resp.setContent("propose accepted");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(cmsg);
		transaction.setResponse(resp);
		transactions.put(cmsg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new TransactionWithTestFlags();
		id = new TransactionIdentityWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		msg.setCommand("execute");
		id.createTransId();
		id.setTransId("trans200912317925.320");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsgWithTestFlags();
		resp.setContent("execute done");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
	
		transaction = new TransactionWithTestFlags();
		id = new TransactionIdentityWithTestFlags();
		msg = new SimCorMsgWithTestFlags();
		msg.setCommand("trigger");
		id.createTransId();
		id.setTransId("trans200912317925.320");
		id.setStep(100);
		id.setSubStep(23);
	
		resp = new SimCorMsgWithTestFlags();
		resp.setContent("trigger received");
		resp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(msg);
		transaction.setResponse(resp);
		transactions.put(msg.toString(),transaction);
		cmdList.add(transaction);
		triggerTransaction = transaction;
	
		transaction = new TransactionWithTestFlags();
		cmsg = new SimCorCompoundMsgWithTestFlags();
		cmsg.setCommand("propose");
		id = new TransactionIdentityWithTestFlags();
		id.setTransId("trans20080206155057.44");
		cmsg.setContent(new Address("MDL-00-01"),"x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003");
		cmsg.setContent(new Address("MDL-00-02"),"x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003");
		cmsg.setContent(new Address("MDL-00-03"),"x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003");
	
		SimCorCompoundMsg cresp = new SimCorCompoundMsgWithTestFlags();
		cresp.setContent(new Address("MDL-00-01"),"x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003");
		cresp.setContent(new Address("MDL-00-02"),"x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003");
		cresp.setContent(new Address("MDL-00-03"),"x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003");
		cresp.setType(MsgType.OK_RESPONSE);
		transaction.setId(id);
		transaction.setCommand(cmsg);
		transaction.setResponse(cresp);
		transactions.put(cmsg.toString(),transaction);
		cmdList.add(transaction);

//		transaction = new TransactionWithTestFlags();
//		id = new TransactionIdentityWithTestFlags();
//		msg = new SimCorMsgWithTestFlags();
//		msg.setCommand("close-session");
//		msg.setContent("dummy");
//		transaction.setCommand(msg);
//		transactions.put(msg.toString(),transaction);
//		cmdList.add(transaction);
	}

	public void checkCompoundMsg(SimCorCompoundMsgWithTestFlags expected, SimCorCompoundMsg tested) {
		if(expected.isAddressExists()) {
			Assert.assertEquals(expected.getAddress(), tested.getAddress());
		}
		if(expected.isCommandExists()) {
			Assert.assertEquals(expected.getCommand(), tested.getCommand());
		}
		if(expected.isContentExists()) {
			Assert.assertEquals(expected.getContent(), tested.getContent());
		}
		if(expected.isCommandExists()) {
			Assert.assertEquals(expected.getCommand(), tested.getCommand());
		}
		if(expected.isTypeExists()) {
			Assert.assertEquals(expected.getType(), tested.getType());
		}
		
	}

	public void checkMsg(SimCorMsgWithTestFlags expected, SimCorMsg tested) {
		if(expected.isAddressExists()) {
			Assert.assertEquals(expected.getAddress(), tested.getAddress());
		}
		if(expected.isCommandExists()) {
			Assert.assertEquals(expected.getCommand(), tested.getCommand());
		}
		if(expected.isContentExists()) {
			Assert.assertEquals(expected.getContent(), tested.getContent());
		}
		if(expected.isCommandExists()) {
			Assert.assertEquals(expected.getCommand(), tested.getCommand());
		}
		if(expected.isTypeExists()) {
			Assert.assertEquals(expected.getType(), tested.getType());
		}
		
	}

	public void checkTransaction(TransactionWithTestFlags expected, Transaction tested, TcpErrorTypes expectedError) {		
		log.debug("Checking: " + tested);
		if(expected.isCommandExiists()) {
			if(expected.getCommand() instanceof SimCorCompoundMsgWithTestFlags) {
				checkCompoundMsg((SimCorCompoundMsgWithTestFlags)expected.getCommand(),(SimCorCompoundMsg) tested.getCommand());
			} else {
				checkMsg((SimCorMsgWithTestFlags) expected.getCommand(), tested.getCommand());
			}
		}
		if(expected.isDirectionExists()) {
			Assert.assertEquals(expected.getDirection(), tested.getDirection());
		}
		if(expected.isErrorExists()) {
			Assert.assertEquals(expectedError, tested.getError().getType());
		}
		if(expected.isIdExists()) {
			checkTransId((TransactionIdentityWithTestFlags) expected.getId(), tested.getId());
		}
		if(expected.isTimeoutExists()) {
			Assert.assertEquals(expected.getTimeout(), tested.getTimeout());
		}
	}

	public void checkTransId(TransactionIdentityWithTestFlags expected, TransactionIdentity tested) {
		if(expected.isCorrectionStepExists()) {
			Assert.assertEquals(expected.getCorrectionStep(), tested.getCorrectionStep());
		}
		if(expected.isStepExists()) {
			Assert.assertEquals(expected.getStep(), tested.getStep());
		}
		if(expected.isSubStepExists()) {
			Assert.assertEquals(expected.getSubStep(), tested.getSubStep());
		}
		if(expected.isTransIdExists()) {
			Assert.assertEquals(expected.getTransId(), tested.getTransId());
		}
	
	}
	public String dumpExpected() {
		String result = "";
		for(SimpleTransaction t : cmdList) {
			result += t + "\n";
		}
		return result;
	}
}