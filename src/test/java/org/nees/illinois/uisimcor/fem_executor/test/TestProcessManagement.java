package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.nees.illinois.uisimcor.fem_executor.process.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessManagement;
import org.nees.illinois.uisimcor.fem_executor.process.QMessage;
import org.nees.illinois.uisimcor.fem_executor.process.QMessage.MessageType;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the output logging of a an FEM execution run.
 * @author Michael Bletzinger
 */
@Test(groups = { "process" })
public class TestProcessManagement {
	/**
	 * Command to execute for testing process management.
	 */
	private String pmCommand;
	/**
	 * Working directory for process execution.
	 */
	private String workDir;

	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory
			.getLogger(TestProcessManagement.class);

	/**
	 * Process management test.
	 */
	@Test
	public final void testProcessManagement() {
		String dispFile = PathUtils.append(workDir, "tmp_disp.out");
		String forceFile = PathUtils.append(workDir, "tmp_forc.out");
		final int waitTime = 200;
		final int pollCount = 6;
		final int oneSec = 1000;
		ProcessManagement pm = new ProcessManagement(pmCommand, "PM Test",
				waitTime, dispFile, forceFile);
		pm.setWorkDir(workDir);
		try {
			pm.startExecute();
		} catch (IOException e) {
			log.error("Failed to start because ", e);
			Assert.fail(pmCommand + " \" failed to start");
		}
		BlockingQueue<QMessage> commands = pm.getCommandQ();
		BlockingQueue<QMessage> responses = pm.getResponseQ();
		final int lastStep = 11;
		for (int s = 1; s < lastStep; s++) {
			commands.add(new QMessage(MessageType.Command,"Execute Step " + s));
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
			Assert.assertTrue("Response contains step number",
					rsp.getContent().contains(Integer.toString(s)));
		}
		commands.add(new QMessage(MessageType.Exit,"EXIT"));
		pm.finish();
	}

	/**
	 * Find the Perl script to execute.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("processManagmentTest.pl");
		pmCommand = PathUtils.cleanPath(u.getPath());
		File cmdF = new File(pmCommand);
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
