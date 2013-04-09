package org.nees.uiuc.simcor.test;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.test.util.TransactionMsgs;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorCompoundMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class SimCorMsgTest  {
	public class TestMsg {
		public boolean isCompoundMsg = false;
		public String text;
		public MsgType type;

		public TestMsg(MsgType type, boolean isCompoundMsg, String text) {
			this.type = type;
			this.isCompoundMsg = isCompoundMsg;
			this.text = text;
		}

	}

	private final Logger log = Logger.getLogger(SimCorMsgTest.class);

	private ArrayList<TestMsg> testMsgs = new ArrayList<TestMsg>();

	@Before
	public void setUp() throws Exception {
		testMsgs.clear();
		testMsgs.add(new TestMsg(MsgType.COMMAND, false,
				"open-session	dummySession"));
		testMsgs.add(new TestMsg(MsgType.OK_RESPONSE, false,
				"OK	0	Open Session Succeeded."));
		testMsgs.add(new TestMsg(MsgType.COMMAND, false,
				"set-parameter	dummySetParam	nstep	0"));
		testMsgs.add(new TestMsg(MsgType.OK_RESPONSE, false,
				"OK	0	Command ignored. Carry on."));
		testMsgs
				.add(new TestMsg(
						MsgType.COMMAND,
						true,
						"propose	trans200912317925.320[100 23 0]"
								+ "	MDL-00-01:LBCB1	x	displacement	0.5	y	displacement	0.0"
								+ "	MDL-00-01:LBCB2	x	displacement	0.5	y	displacement	0.0"));
		testMsgs.add(new TestMsg(MsgType.OK_RESPONSE, false,
				"OK	0	trans200912317925.320[100 23 0]	propose Accepted"));
		testMsgs.add(new TestMsg(MsgType.COMMAND, false,
				"execute	trans200912317925.320[100 23 0]"));
		testMsgs.add(new TestMsg(MsgType.OK_RESPONSE, false,
				"OK	0	trans200912317925.320[100 23 0]	execute Done"));
		testMsgs.add(new TestMsg(MsgType.COMMAND, false,
				"get-control-point	dummy	MDL-00-01:LBCB2"));
		testMsgs
				.add(new TestMsg(
						MsgType.OK_RESPONSE,
						false,
						"OK	0	dummy"
								+ "	MDL-00-01:LBCB2	x	displacement	-5.009669E-1	y	displacement	-7.044421E-3	z	displacement	-9.913608E-5"
								+ "	x	rotation	-2.146664E-4	y	rotation	-2.384169E-6	z	rotation	-2.215282E-3"
								+ "	x	force	-6.449378E+0	y	force	3.214762E-2	z	force	3.022496E+0"
								+ "	x	moment	-4.772873E-1	y	moment	6.947462E+0	z	moment	-9.329250E-1"));
		testMsgs.add(new TestMsg(MsgType.COMMAND, false,
				"get-control-point	dummy	MDL-00-01:ExternalSensors"));
		testMsgs.add(new TestMsg(MsgType.OK_RESPONSE, false,
				"OK	0	dummy	MDL-00-01:ExternalSensors"
						+ "	Ext.Long.LBCB2	external	2.422813E-1"
						+ "	Ext.Tranv.TopLBCB2	external	2.422813E-1"
						+ "	Ext.Tranv.Bot.LBCB2	external	2.422813E-1"
						+ "	Ext.Long.LBCB1	external	2.422813E-1"
						+ "	Ext.Tranv.LeftLBCB1	external	2.422813E-1"
						+ "	Ext.Tranv.RightLBCB1	external	2.422813E-1"));
		testMsgs
				.add(new TestMsg(
						MsgType.OK_RESPONSE,
						false,
						"OK	0	MDL-00-01:ExternalSensors"
								+ "	1_LBCB1_x	external	-7.565553E-1"
								+ "	2_LBCB1_z_right	external	0.000000E+0"
								+ "	3_LBCB1_z_left	external	-8.081407E-1"
								+ "	4_LBCB2_z_right	external	0.000000E+0"
								+ "	5_LBCB2_z_left	external	7.518438E-2"
								+ "	6_LBCB2_x	external	0.00]000E+0"));

		testMsgs
				.add(new TestMsg(
						MsgType.COMMAND,
						true,
						"propose   trans20080206155057.444"
								+ "	MDL-00-01	x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003"
								+ "	MDL-00-02	x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003"
								+ "	MDL-00-03	x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003"));
		testMsgs
				.add(new TestMsg(
						MsgType.OK_RESPONSE,
						true,
						"OK	0	dummy"
								+ "	MDL-00-01	x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003"
								+ "	MDL-00-02	x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003"
								+ "	MDL-00-03	x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003"));
	}

	@Test
	public void testAllMsgs() {
		Msg2Tcp scmd = new Msg2Tcp();
		for (Iterator<TestMsg> i = testMsgs.iterator(); i.hasNext();) {
			TestMsg tm = i.next();
			log.debug("Parsing " + tm.text);

			scmd.parse(tm.text);
			Assert.assertEquals(tm.type, scmd.getMsg().getType());
			Assert.assertEquals(tm.isCompoundMsg,
					scmd.getMsg() instanceof SimCorCompoundMsg);
			String rm = scmd.assemble();
			Assert.assertEquals(tm.text, rm);
		}
	}

	@Test
	public void testEquate() {
		Iterator<TestMsg> i = testMsgs.iterator();
		Msg2Tcp m2t = new Msg2Tcp();
		SimCorMsg firstmsg;
		TransactionIdentity firstId;
		TestMsg s = i.next();
		m2t.parse(s.text);
		firstmsg = m2t.getMsg();
		firstId = m2t.getId();
		Assert.assertEquals(true, firstmsg.equals(firstmsg));
		while (i.hasNext()) {
			SimCorMsg msg;
			TransactionIdentity id;
			s = i.next();
			m2t.parse(s.text);
			msg = m2t.getMsg();
			id = m2t.getId();
			log.debug("Checking " + msg);
			Assert.assertEquals(true, msg.equals(msg));
			Assert.assertEquals(false, msg.equals(firstmsg));
			if (id != null) {
				Assert.assertEquals(true, id.equals(id));
				Assert.assertEquals(false, id.equals(firstId));
			}
		}
	}

	@Test
	public void testTransactionMsgs() throws Exception {
		TransactionMsgs data = new TransactionMsgs();
		data.setUp();
		Msg2Tcp m2t = new Msg2Tcp();
		for (Iterator<SimpleTransaction> t = data.cmdList.iterator(); t.hasNext();) {
			SimpleTransaction trns = t.next();
			log.debug("Checking " + trns);
			SimCorMsg cmd = trns.getCommand();
			SimCorMsg rsp = trns.getResponse();
			TransactionIdentity id = trns.getId();
			m2t.setId(id);
			m2t.setMsg(cmd);
			String msg = m2t.assemble();
			m2t.clear();
			m2t.parse(msg);
			Assert.assertEquals(cmd, m2t.getMsg());
			if (id == null) {
				Assert.assertNull(m2t.getId());
			} else {
				Assert.assertEquals(id, m2t.getId());
			}
			m2t.setMsg(rsp);
			msg = m2t.assemble();
			m2t.clear();
			m2t.parse(msg);
			Assert.assertEquals(rsp, m2t.getMsg());
		}

	}
}
