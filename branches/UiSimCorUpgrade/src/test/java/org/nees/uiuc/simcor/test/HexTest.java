package org.nees.uiuc.simcor.test;


import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.nees.uiuc.simcor.matlab.StringListUtils;

public class HexTest {

	private byte [] buf = new byte[100];
	private Logger log = Logger.getLogger(HexTest.class);
	@Before
	public void setUp() throws Exception {
		for (int i = 0; i < 100; i++) {
			buf[i] = (byte)i;
		}
	}
	@Test
	public void testByte2HexString() {
		StringListUtils slu = new StringListUtils();
		log.info("BUFFER [" + slu.Byte2HexString(buf) + "]");
	}

}
