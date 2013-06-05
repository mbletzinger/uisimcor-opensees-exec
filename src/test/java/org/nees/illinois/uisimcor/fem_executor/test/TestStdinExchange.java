package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.nees.illinois.uisimcor.fem_executor.process.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.process.QMessage;
import org.nees.illinois.uisimcor.fem_executor.process.StdInExchange;
import org.nees.illinois.uisimcor.fem_executor.process.QMessage.MessageType;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Level;

/**
 * Test the output logging of a an FEM execution run.
 * @author Michael Bletzinger
 */
@Test(groups = { "exchange" })
public class TestStdinExchange {
	/**
	 * Command to execute for testing the response processing.
	 */
	private String command;
	/**
	 * Working directory for process execution.
	 */
	private String workDir;

	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(TestStdinExchange.class);

	/**
	 * Exchange test.
	 */
	@Test
	public final void testOutput() {
		String[] cmd = { "perl", command, "dummy1", "2", "9" };
		String dispFile = PathUtils.append(workDir, "tmp_disp.out");
		String forceFile = PathUtils.append(workDir, "tmp_forc.out");
		BlockingQueue<QMessage> commands = new LinkedBlockingQueue<QMessage>();
		BlockingQueue<QMessage> responses = new LinkedBlockingQueue<QMessage>();
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(workDir));
		Process p = null;
		log.debug("Starting process");
		try {
			p = pb.start();
		} catch (IOException e) {
			log.error(command + " failed to start because", e);
			AssertJUnit.fail();
		}
		log.debug("Creating threads");
		final int milliWait = 300;
		final String testName = "stdinExchangeTest";
		ProcessResponse errPr = new ProcessResponse(Level.ERROR,
				p.getErrorStream(), milliWait, testName);
		ProcessResponse stoutPr = new ProcessResponse(Level.DEBUG,
				p.getInputStream(), milliWait, testName);
		StdInExchange exchange = new StdInExchange(commands, dispFile,
				forceFile, responses, p.getOutputStream(), milliWait);
		stoutPr.addObserver(exchange);
		Thread errThrd = new Thread(errPr);
		Thread stoutThrd = new Thread(stoutPr);
		Thread exThrd = new Thread(exchange);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		final int threeSec = 3000;
		try {
			Thread.sleep(threeSec);
		} catch (InterruptedException e1) {
			log.debug("I was interrupted.");
		}
		exThrd.start();
		final int oneSec = 1000;
		final int pollCount = 6;
		final int lastStep = 11;
		for (int s = 1; s < lastStep; s++) {
			commands.add(new QMessage(MessageType.Command, "Execute Step " + s));
			QMessage rsp = null;
			int count = 0;
			while (rsp == null && count < pollCount) {
				try {
					rsp = responses.poll(oneSec, TimeUnit.MILLISECONDS);
					log.debug("Received \"" + rsp + "\" from process");
					rsp = responses.poll(oneSec, TimeUnit.MILLISECONDS);
					log.debug("Received \"" + rsp + "\" from process");
				} catch (InterruptedException e) {
					log.debug("Response poll was interrupted");
				}
				count++;
			}
			Assert.assertNotNull("Response not received within 5 seconds", rsp);
			Assert.assertTrue("Response " + rsp + " contains step number " + s,
					rsp.getContent().contains(Integer.toString(s)));
		}
		commands.add(new QMessage(MessageType.Exit, "EXIT"));
		log.debug("Ending threads");
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
		exchange.setQuit(true);
		exThrd.interrupt();
	}

	/**
	 * Find the Perl script to execute.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("processManagmentTest.pl");
		command = PathUtils.cleanPath(u.getPath());
		File cmdF = new File(command);
		cmdF.setExecutable(true);
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"process_managment");
		File workDirF = new File(workDir);
		if (workDirF.exists() && (workDirF.isDirectory() == false)) {
			log.error("Cannot create working directory \"" + workDir + "\"");
			return;
		}
		try {
			workDirF.mkdirs();
			log.debug("\"" + workDir + "\" was created");
		} catch (Exception e) {
			log.error("Cannot create working directory \"" + workDir
					+ "\" because ", e);
			return;
		}
	}

	 /**
	 * Remove test files.
	 */
	 @AfterClass
	 public final void cleanup() {
	 FileWithContentDelete dirF = new FileWithContentDelete(workDir);
	 boolean done = dirF.delete();
	 if (done == false) {
	 log.error("Could not remove dir \"" + workDir + "\"");
	 return;
	 }
	 log.debug("\"" + workDir + "\" was removed");
	 }

}
