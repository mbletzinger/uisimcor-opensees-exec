package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.IOException;
import java.net.URL;

import org.nees.illinois.uisimcor.fem_executor.process.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Level;

/**
 * Test the output logging of a an FEM execution run.
 * @author Michael Bletzinger
 */
@Test(groups = { "response" })
public class TestProcessResponse {
	/**
	 * Command to execute.
	 */
	private String command;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory
			.getLogger(TestProcessResponse.class);

	/**
	 * The test.
	 */
	@Test
	public final void testOutput() {
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
		final int milliWait = 100;
		final String testName = "printerTest";
		ProcessResponse errPr = new ProcessResponse(Level.ERROR,
				p.getErrorStream(), milliWait, testName);
		ProcessResponse stoutPr = new ProcessResponse(Level.DEBUG,
				p.getInputStream(), milliWait, testName);
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
			final int milliWait2 = 2000;
			Thread.sleep(milliWait2);
		} catch (InterruptedException e) {
			log.debug("Sleeping...");
		}
		log.debug("Ending threads");
		errPr.setDone(true);
		stoutPr.setDone(true);
		log.debug("Output \"" + stoutPr.getOutput() + "\"");
		log.debug("Error \"" + errPr.getOutput() + "\"");
		Assert.assertTrue(stoutPr
				.getOutput()
				.contains(
						"Printing out 0\nPrinting out 1\nPrinting out 2\nPrinting out 3\nPrinting out 4\nPrinting out 5\nPrinting out 6\nPrinting out 7\nPrinting out 8\nPrinting out 9\n"));
		Assert.assertEquals(errPr.getOutput(),
				"Erroring out 0\nErroring out 3\nErroring out 6\nErroring out 9\n");
	}

	/**
	 * Find the Perl script to execute.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("printerTest.pl");
		command = PathUtils.cleanPath(u.getPath());
	}
	@AfterClass
	public final void cleanup() {
		String dir = System.getProperty("user.dir");
		PathUtils.rm(dir, "tmp_disp.out");
		PathUtils.rm(dir, "tmp_forc.out");
	}

}
