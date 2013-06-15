package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.nees.illinois.uisimcor.fem_executor.output.BinaryFileReader;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileMonitor;
import org.nees.illinois.uisimcor.fem_executor.output.WorkDirWatcher;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.utils.FileWithContentDelete;
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
@Test(groups = { "response" })
public class TestOutputFileMonitor {
	/**
	 * Command to execute for testing the response processing.
	 */
	private String command;
	/**
	 * Working directory for temp files.
	 */
	private String workDir;
	/**
	 * Flag to indicate whether the generated files should be removed or not
	 * after the tests are over.
	 */
	private boolean keepFiles = false;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory
			.getLogger(TestOutputFileMonitor.class);
	/**
	 * Process response for the output generating script. For debugging.
	 */
	private ProcessResponse errPr;
	/**
	 * Process response for the output generating script. For debugging.
	 */
	private ProcessResponse stoutPr;
	/**
	 * Thread wrapper for process response.
	 */
	private Thread errThrd;
	/**
	 * Thread wrapper for process response.
	 */
	private Thread stoutThrd;

	/**
	 * Response test.
	 */
	@Test
	public final void testMonitoring() {
		WorkDirWatcher watcher = new WorkDirWatcher();
		Thread watcherThrd = new Thread(watcher);
		watcherThrd.start();
		final int totalDofs = 6;
		// Mess with the heuristic delay 5 item history buffer. Watch the debug
		// messages to see if it works.
		final int[] values = { 14, 10, 4000, 20, 13, 12, 144, 14, 14, 13,
				50000, 3000, 14, 103, 13, 200, 20, 35, 14, 20000 };
		String dispFile = PathUtils.append(workDir, "tmp_disp.out");
		String forceFile = PathUtils.append(workDir, "tmp_forc.out");
		BinaryFileReader dbfr = new BinaryFileReader(dispFile, totalDofs);
		OutputFileMonitor ofm = new OutputFileMonitor(dbfr,
				new BinaryFileReader(forceFile, totalDofs), watcher);
		Thread ofmThrd = new Thread(ofm);
		ofmThrd.start();
		for (int v : values) {
			Process p = runCmd(v);
			QMessageT<List<Double>> disps = null;
			QMessageT<List<Double>> forces = null;
			final int patience = 10;
			int count = 0;
			while (disps == null && forces == null && count < patience) {
				try {
					disps = ofm.getResponseQ().poll(1, TimeUnit.SECONDS);
					if (disps != null) {
						forces = ofm.getResponseQ().poll(1, TimeUnit.SECONDS);
					}
				} catch (InterruptedException e) {
					log.debug("Ignoring interruption");
				}
				count++;
			}
			stopCmdMonitoring(p);
			Assert.assertNotNull(disps);
			Assert.assertNotNull(forces);
			Assert.assertNotNull(disps.getContent());
			Assert.assertNotNull(forces.getContent());
			Assert.assertEquals(totalDofs, disps.getContent().size());
			Assert.assertEquals(totalDofs, forces.getContent().size());
		}
		ofm.setQuit(true);
		ofmThrd.interrupt();
		watcher.setQuit(true);
		watcherThrd.interrupt();
	}

	/**
	 * Find the Perl script to execute.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("outputTest.pl");
		command = PathUtils.cleanPath(u.getPath());
		workDir = PathUtils
				.append(System.getProperty("user.dir"), "fem_output");
		File workDirF = new File(workDir);
		if (workDirF.exists() == false) {
			workDirF.mkdirs();
		}
	}

	/**
	 * Remove test files.
	 */

	@AfterClass
	public final void cleanup() {
		if (keepFiles) {
			return;
		}
		FileWithContentDelete dir = new FileWithContentDelete(workDir);
		boolean done = dir.delete();
		if (done == false) {
			log.error("Could not remove dir \"" + workDir + "\"");
			return;
		}
		log.debug("\"" + workDir + "\" was removed");
	}

	private Process runCmd(int numValues) {
		log.debug("Running " + " with " + numValues);
		String[] cmd = { "perl", command, Integer.toString(numValues) };
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
		String testName = "Output Test";
		final int milliWait = 100;
		errPr = new ProcessResponse(Level.ERROR, p.getErrorStream(), milliWait,
				testName);
		stoutPr = new ProcessResponse(Level.DEBUG, p.getInputStream(),
				milliWait, testName);
		errThrd = new Thread(errPr);
		stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		return p;
	}

	private void stopCmdMonitoring(Process p) {
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
	}
}
