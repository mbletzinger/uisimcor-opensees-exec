package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;

import org.nees.illinois.uisimcor.fem_executor.process.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Level;

public class TestProcessResponse {
	private String command;
	private final Logger log = LoggerFactory
			.getLogger(TestProcessResponse.class);

	@Test
	public void TestOutput() {
		String[] cmd = { "perl", command, "dummy1", "2", "9" };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process p = null;
		log.debug("Starting process");
		try {
			p = pb.start();
		} catch (IOException e) {
			log.error(command + " failed to start because", e);
			Assert.fail();
		}
		log.debug("Creating threads");
		ProcessResponse errPr = new ProcessResponse(Level.ERROR,
				p.getErrorStream(), 100, "printerTest");
		ProcessResponse stoutPr = new ProcessResponse(Level.DEBUG,
				p.getInputStream(), 100, "printerTest");
		Thread errThrd = new Thread(errPr);
		Thread stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			log.debug("I was Interrupted");
		}
		log.debug("Waiting for threads");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		log.debug("Ending threads");
		errPr.setDone(true);
		stoutPr.setDone(true);
		log.debug("Output \"" + stoutPr.getOutput() + "\"");
		log.debug("Error \"" + errPr.getOutput() + "\"");
		Assert.assertTrue(
				stoutPr.getOutput().contains("Printing out 0\nPrinting out 1\nPrinting out 2\nPrinting out 3\nPrinting out 4\nPrinting out 5\nPrinting out 6\nPrinting out 7\nPrinting out 8\nPrinting out 9\n"));
		Assert.assertEquals(errPr.getOutput(),
				"Erroring out 0\nErroring out 3\nErroring out 6\nErroring out 9\n");
	}

	@BeforeClass
	public void beforeClass() {
		URL u = ClassLoader.getSystemResource("printerTest.pl");
		command = PathUtils.cleanPath(u.getPath());
	}

}
