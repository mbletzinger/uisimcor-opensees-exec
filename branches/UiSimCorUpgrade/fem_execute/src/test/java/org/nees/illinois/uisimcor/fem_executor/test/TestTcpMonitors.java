package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.nees.illinois.uisimcor.fem_executor.response.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
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
@Test(groups = { "tcp" })
public class TestTcpMonitors {
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
			.getLogger(TestTcpMonitors.class);
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
	 * Port number to monitor.
	 */
	private final int port = 4114;
	/**
	 * Wait time for listeners.
	 */
	private final int tcpWait = 10000;
	/**
	 * Listener for displacements socket.
	 */
	private TcpListener dispListener;
	/**
	 * Listener for the reactions socket.
	 */
	private TcpListener forceListener;

	/**
	 * Response test.
	 */
	@Test
	public final void testMonitoring() {
		final int[] values = { 14, 10, 4000, 20, 13, 12, 144, 14, 14, 13,
				50000, 3000, 14, 103, 13, 200, 20, 35, 14, 20000 };
		for (int v : values) {
			Process p = runCmd(v);
			List<Double> disps = null;
			List<Double> forces = null;
			TcpReader dispReader = startMonitoring(
					dispListener.getConnections(), port);
			TcpReader forceReader = startMonitoring(
					forceListener.getConnections(), port + 1);
			final int patience = 10;
			int count = 0;
			while (disps == null && forces == null && count < patience) {
				try {
					disps = dispReader.getDoublesQ().poll(1, TimeUnit.SECONDS);
					if (disps != null) {
						forces = forceReader.getDoublesQ().poll(1,
								TimeUnit.SECONDS);
					}
				} catch (InterruptedException e) {
					log.debug("Ignoring interruption");
				}
				count++;
			}
			stopCmdMonitoring(p);
			dispReader.setQuit(true);
			dispReader.interrupt();
			forceReader.setQuit(true);
			forceReader.interrupt();
			Assert.assertNotNull(disps);
			Assert.assertNotNull(forces);
			Assert.assertEquals(v, disps.size());
			Assert.assertEquals(v, forces.size());

		}
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
		TcpParameters params = new TcpParameters(null, 0, port, tcpWait);
		try {
			dispListener = new TcpListener(params);
		} catch (IOException e) {
			log.error("Listiening on port " + port + " failed because ", e);
			Assert.fail();
		}
		int fport = port + 1;
		params = new TcpParameters(null, 0, fport, tcpWait);
		try {
			forceListener = new TcpListener(params);
		} catch (IOException e) {
			log.error("Listiening on port " + fport + " failed because ", e);
			Assert.fail();
		}
		dispListener.start();
		forceListener.start();

	}

	/**
	 * Remove test files.
	 */

	@AfterClass
	public final void cleanup() {
		dispListener.setQuit(true);
		dispListener.interrupt();
		forceListener.setQuit(true);
		forceListener.interrupt();
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

	/**
	 * Run a perl command.
	 * @param numValues
	 *            Input parameter to the perl script.
	 * @return the process.
	 */
	private Process runCmd(final int numValues) {
		log.debug("Running " + " with " + numValues);
		String[] cmd = { "perl", command, Integer.toString(numValues),
				Integer.toString(port) };
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
				testName, null);
		stoutPr = new ProcessResponse(Level.DEBUG, p.getInputStream(),
				milliWait, testName, null);
		errThrd = new Thread(errPr);
		stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		return p;

	}

	/**
	 * Stop the process monitors for the perl command.
	 * @param p
	 *            Process.
	 */
	private void stopCmdMonitoring(final Process p) {
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
	}

	/**
	 * Start a TCP reader.
	 * @param linkQ
	 *            Queue from the associated listener.
	 * @param port
	 *            Port number used by the listener.
	 * @return The reader.
	 */
	private TcpReader startMonitoring(final BlockingQueue<TcpLinkDto> linkQ,
			final int port) {
		final int pollWait = 2;
		TcpLinkDto link = null;
		try {
			link = linkQ.poll(pollWait, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Tired of waiting for link on port" + port);
			Assert.fail();
		}
		TcpReader result = null;
		if (link == null) {
			log.error("Tired of waiting for link on port" + port);
			Assert.fail();
		}
		try {
			result = new TcpReader(link);
		} catch (IOException e) {
			log.error("Tired of waiting for link on port" + port);
			Assert.fail();
		}
		result.start();
		return result;
	}
}
