package org.nees.illinois.uisimcor.fem_executor.test.opensees;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.nees.illinois.uisimcor.fem_executor.config.LoadSaveConfig;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.TemplateDao;
import org.nees.illinois.uisimcor.fem_executor.execute.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.execute.WorkingDir;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.output.DataFormatter;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessManagementWithStdin;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageType;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
import org.nees.illinois.uisimcor.fem_executor.test.utils.CompareLists;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Figures out how OpenSees does its stuff.
 * @author Michael Bletzinger
 */
public class TestOpenSeesExecution {
	/**
	 * OpenSees executable.
	 */
	private String command;
	/**
	 * Directory containing the configuration files for the test.
	 */
	private String configDir;
	/**
	 * Flag to indicate whether the generated files should be removed or not
	 * after the tests are over.
	 */
	private boolean keepFiles = false;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestOpenSeesExecution.class);
	/**
	 * Number of steps to execute.
	 */
	private final int numberOfSteps = 11;
	/**
	 * Substructure configuration data.
	 */
	private SubstructureDao sdao;
	/**
	 * Templates used to change the OpenSees recording.
	 */
	private Map<String, TemplateDao> templates = new HashMap<String, TemplateDao>();
	/**
	 * Displacement TCP reader.
	 */
	private TcpReader dispTcpReader;
	/**
	 * Force TCP reader.
	 */
	private TcpReader forceTcpReader;
	/**
	 * Displacement TCP listener.
	 */
	private TcpListener dispTcpListener;
	/**
	 * Displacement TCP port.
	 */
	private final int dispPort = 4114;
	/**
	 * Force TCP port.
	 */
	private final int forcePort = 4115;
	/**
	 * Force TCP listener.
	 */
	private TcpListener forceTcpListener;
	/**
	 * Displacement accumulator.
	 */
	private List<List<Double>> tcpDisplacements = new ArrayList<List<Double>>();
	/**
	 * Force accumulator.
	 */
	private List<List<Double>> tcpForces = new ArrayList<List<Double>>();
	/**
	 * TCP template name.
	 */
	private final String tcpName = "TCP";
	/**
	 * Directory to store temporary files during FEM execution.
	 */
	private String workDir;

	/**
	 * Clean up work directories.
	 */
	@AfterClass
	public final void afterClass() {
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
		dispTcpListener.setQuit(true);
		dispTcpListener.interrupt();
		dispTcpReader.setQuit(true);
		dispTcpReader.interrupt();
		forceTcpListener.setQuit(true);
		forceTcpListener.interrupt();
		forceTcpReader.setQuit(true);
		forceTcpReader.interrupt();
	}

	/**
	 * Set up template names and find folders and paths.
	 */
	@BeforeClass
	public final void beforeClass() {
		String stepT = "step_template.tcl";
		String runT = "run_template.tcl";
		URL u = ClassLoader.getSystemResource("openseescfg/" + stepT);
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"opensees_execute");
		String os = System.getProperty("os.name").toLowerCase();
		String exe = null;
		if (os.contains("mac")) {
			exe = "bin/OpenSeesMacOSX";
		} else if (os.contains("win")) {
			exe = "bin/OpenSeesWin.exe";
		} else {
			Assert.fail("Don't have an OpenSees executable for your os");
		}
		u = ClassLoader.getSystemResource(exe);
		command = PathUtils.cleanPath(u.getPath());
		File cmdF = new File(command);
		cmdF.setExecutable(true);
		templates.put("Text", new TemplateDao(stepT, "txt_init_template.tcl",
				runT));
		// templates
		// .put("Binary", new TemplateDao(stepT, "bin_init_template.tcl",runT));
		templates.put(tcpName, new TemplateDao(stepT, "tcp_init_template.tcl",
				runT));
		LoadSaveConfig lscfg = new LoadSaveConfig();
		lscfg.setConfigFilePath("OneSubstructureTestConfig.properties");
		lscfg.load(configDir);
		sdao = lscfg.getFemConfig().getSubstructCfgs().get("MDL-01");
		final int tcpWait = 50000;
		try {
			dispTcpListener = new TcpListener(new TcpParameters(null, 0,
					dispPort, tcpWait));
		} catch (IOException e) {
			log.error("Listener failed on port " + dispPort + " because ", e);
			Assert.fail();
		}
		dispTcpListener.start();
		try {
			forceTcpListener = new TcpListener(new TcpParameters(null, 0,
					forcePort, tcpWait));
		} catch (IOException e) {
			log.error("Listener failed on port " + forcePort + " because ", e);
			Assert.fail();
		}
		forceTcpListener.start();
	}

	/**
	 * Convert a byte array into a string representation.
	 * @param buf
	 *            Byte array.
	 * @return String.
	 */
	private String byte2HexString(final byte[] buf) {
		String str = "";
		for (int i = 0; i < buf.length; i++) {
			final int numberOfDigitsInHex = 16;
			String prefix = ((buf[i] < (byte) numberOfDigitsInHex) ? "0" : "");
			str += prefix + Integer.toHexString(buf[i]);
		}
		return str;
	}

	/**
	 * Compares the binary and text outputs of an OpenSees run.
	 * @param tfilename
	 *            Text output file.
	 * @param actual
	 *            Values for the TCP template.
	 */
	private void compareTcp2Text(final String tfilename,
			final List<List<Double>> actual) {
		WorkingDir wd = new WorkingDir(workDir, sdao, configDir);
		List<List<Double>> expected = null;
		try {
			expected = readTxtOutput(PathUtils.append(wd.getWorkDir(),
					tfilename));
		} catch (OutputFileException e) {
			Assert.fail(PathUtils.append(wd.getWorkDir(), tfilename)
					+ " cannot be read");
		}
		for (int s = 0; s < numberOfSteps; s++) {
			final double cmpTolerance = 0.001;
			CompareLists<Double> cmp = new CompareLists<Double>(cmpTolerance);
			log.debug("Comparing \n\t" + MtxUtils.list2String(actual.get(s))
					+ " to \n\t" + expected.get(s));
			cmp.compare(actual.get(s), expected.get(s));
		}
	}

	/**
	 * Suspend for 2 seconds.
	 */
	private void delaysDelays() {
		final int twoSec = 2000;
		try {
			Thread.sleep(twoSec);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason.");
		}

	}

	/**
	 * Print the double and byte string representation of a list of doubles.
	 * @param doubles
	 *            List of numbers.
	 * @param step
	 *            Current step.
	 */
//	private void printDoubleBytes(final List<Double> doubles, final int step) {
//		String msg = "Step " + step + "[";
//		boolean first = true;
//		for (Double d : doubles) {
//
//			msg += (first ? "" : ",") + d + "=>"
//					+ Long.toHexString(Double.doubleToRawLongBits(d));
//			first = false;
//		}
//		msg += "]";
//		log.debug(msg);
//	}

	/**
	 * Print a byte string representation and the double value of am array of
	 * bytes pretending to be from an OpenSees output file.
	 * @param record
	 *            Array of bytes.
	 * @param step
	 *            Current step.
	 */
	private void printDoubles(final byte[] record, final int step) {
		final int numDofs = 3;
		final int numberOfBytesInDouble = 8;
		String msg = "Step " + step + "[";
		boolean first = true;
		for (int i = 0; i < numDofs; i++) {
			int from = i * numberOfBytesInDouble;
			int to = (i + 1) * numberOfBytesInDouble;
			byte[] number = Arrays.copyOfRange(record, from, to);
			ByteBuffer bnum = ByteBuffer.allocate(numberOfBytesInDouble);
			bnum.order(ByteOrder.LITTLE_ENDIAN);
			bnum.put(number);
			bnum.flip();
			double dnum = bnum.getDouble();
			msg += (first ? "" : ",") + byte2HexString(number) + "=>" + dnum;
			first = false;
		}
		msg += "]";
		log.debug(msg);
	}

	/**
	 * Read a text output file and return all of the values in a 2D array.
	 * @param file
	 *            Name of the text file.
	 * @return Matrix of read values.
	 * @throws OutputFileException
	 *             If there are problems with the file.
	 */
	private List<List<Double>> readTxtOutput(final String file)
			throws OutputFileException {
		File fileF = new File(file);
		List<List<Double>> result = new ArrayList<List<Double>>();
		// log.debug("Checking \"" + file + "\"");
		if (fileF.exists() == false) {
			throw new OutputFileException("File \"" + fileF.getAbsolutePath()
					+ "\" is missing.");
		}
		List<String> contents;
		try {
			contents = FileUtils.readLines(fileF);
		} catch (IOException e) {
			throw new OutputFileException("Could not read file \"" + file
					+ "\" because", e);
		}
		DataFormatter df = new DataFormatter(sdao);
		for (String l : contents) {
			List<Double> record = df.tokenString2Double(l);
			result.add(record);
		}
		return result;
	}

	/**
	 * Execute a number of steps for the specified OpenSees script templates.
	 * @param name
	 *            Script templates to use.
	 */
	private void runTemplate(final String name) {
		final int waitTime = 200;
		TemplateDao template = templates.get(name);
		WorkingDir wd = new WorkingDir(workDir, sdao, configDir);
		wd.createWorkDir();
		OpenSeesSG input = new OpenSeesSG(configDir, sdao, template);
		ProcessManagementWithStdin pm = new ProcessManagementWithStdin(command,
				"PM Test", waitTime);
		pm.setWorkDir(wd.getWorkDir());
		try {
			pm.startExecute();
		} catch (IOException e) {
			log.error("Failed to start because ", e);
			Assert.fail(command + " \" failed to start");
		}
		BlockingQueue<QMessageT<String>> commands = pm.getStdinQ();
		try {
			commands.put(new QMessageT<String>(QMessageType.Setup, input
					.generateInit()));
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason.");
		}
		delaysDelays();
		if (name.equals(tcpName)) {
			listenTcp(true);
			listenTcp(false);
		}
		final int responseSize = 6;
		final int rzIdx = 3;
		double[] disp = new double[responseSize];
		final double interval = 0.0002;
		for (int i = 0; i < numberOfSteps; i++) {
			disp[0] = disp[0] + interval;
			disp[rzIdx] = disp[rzIdx] + Math.pow(interval, 2);
			String stepS = input.generateStep(i + 1, disp);
			try {
				commands.put(new QMessageT<String>(QMessageType.Command, stepS));
			} catch (InterruptedException e) {
				log.debug("Interrupted for some reason.");
			}
			delaysDelays();
			if (name.equals(tcpName)) {
				readTcp(true);
				readTcp(false);
			}
		}
		try {
			commands.put(new QMessageT<String>(QMessageType.Command, "exit"));
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason.");
		}
		delaysDelays();
		pm.abort();
	}

	/**
	 * Start the TCP listeners for OpenSees.
	 * @param isDisp
	 *            True if working with the displacement link.
	 */
	private void listenTcp(final boolean isDisp) {
		TcpListener listener = isDisp ? dispTcpListener : forceTcpListener;
		TcpLinkDto link = listener.getConnections().poll();
		TcpReader reader = null;
		Assert.assertNotNull(link);
		try {
			reader = new TcpReader(link);
			if (isDisp) {
				dispTcpReader = reader;
			} else {
				forceTcpReader = reader;
			}
		} catch (IOException e) {
			log.error((isDisp ? "Disp" : "Force")
					+ " connection failed because ", e);
		}
		reader.start();

	}

	/**
	 * Start the TCP readers for OpenSees.
	 * @param isDisp
	 *            True if working with the displacement link.
	 */
	private void readTcp(final boolean isDisp) {
		TcpReader reader = isDisp ? dispTcpReader : forceTcpReader;
		List<Double> list = reader.getDoublesQ().poll();
		if (list == null) {
			log.debug("Nothing to read from tcp");
			return;
		}
		List<List<Double>> accum = isDisp ? tcpDisplacements : tcpForces;
		log.debug("Read " + list);
		accum.add(list);
	}

	/**
	 * Split the binary OpenSees output file created by the previous test into a
	 * file for each step to emulate what these files look like during UI-SimCor
	 * execution.
	 * @param filename
	 *            Name of the file to split.
	 */
	@SuppressWarnings("unused")
	private void splitBinaryFile(final String filename) {

		final int recordLength = 25; // In this case 3 8-byte doubles plus the
										// end-of-line character;
		File file = new File(filename);
		Assert.assertTrue(file.exists(), "File \"" + filename
				+ "\" is not there.");
		Assert.assertTrue(file.canRead(), "File \"" + filename
				+ "\" cannot be read.");
		int expectedLength = recordLength * numberOfSteps;
		Assert.assertEquals(file.length(), expectedLength,
				"Length of binary file \"" + filename + "\" is incorrect");
		InputStream input = null;
		int totalBytesRead = 0;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			log.error("I don't need to tell you that the file does not exist");
			Assert.fail();
		}

		byte[] byteData = new byte[expectedLength];
		while (totalBytesRead < expectedLength) {
			int bytesRemaining = expectedLength - totalBytesRead;
			int bytesRead = 0;
			try {
				bytesRead = input
						.read(byteData, totalBytesRead, bytesRemaining);
			} catch (IOException e) {
				log.error("Reading failed for file \"" + filename
						+ "\" because ", e);
				try {
					input.close();
				} catch (IOException e1) {
					log.error("Close failed but who cares", e1);
				}
				Assert.fail();
			}
			if (bytesRead > 0) {
				totalBytesRead = totalBytesRead + bytesRead;
			}
		}
		try {
			input.close();
		} catch (IOException e) {
			log.error("Close failed but who cares");
		}

		for (int s = 0; s < numberOfSteps; s++) {
			String stepfilename = stepFileName(s, filename);
			OutputStream output = null;
			int from = s * recordLength;
			int to = (s + 1) * recordLength;
			byte[] record = Arrays.copyOfRange(byteData, from, to);
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						stepfilename));
				output.write(record);
			} catch (FileNotFoundException e) {
				log.error("Writing failed for file \"" + stepfilename
						+ "\" because ", e);
				Assert.fail();
			} catch (IOException e) {
				log.error("Writing failed for file \"" + stepfilename
						+ "\" because ", e);
				Assert.fail();
			} finally {
				try {
					output.close();
				} catch (IOException e) {
					log.error("Close failed but who cares");
				}
			}
			printDoubles(record, s);
		}
	}

	/**
	 * Create a step filename for splitting purposes.
	 * @param s
	 *            Step number.
	 * @param filename
	 *            Name of the consolidated file.
	 * @return The name of the stepfile.
	 */
	private String stepFileName(final int s, final String filename) {
		return filename.replaceAll(".bin", s + ".bin");
	}

	/**
	 * See what the various recorder formats look like.
	 */
	@Test
	public final void testOpensSeesRecorderFormats() {
		for (String t : templates.keySet()) {
			log.debug("Executing simulation style " + t);
			runTemplate(t);
		}
	}

	/**
	 * Compare the TCP and binary outputs of an OpenSees run.
	 */
	@Test
	public final void verifyTcpOutput() {
		compareTcp2Text("tmp_disp.txt", tcpDisplacements);
		compareTcp2Text("tmp_forc.txt", tcpForces);
	}
}
