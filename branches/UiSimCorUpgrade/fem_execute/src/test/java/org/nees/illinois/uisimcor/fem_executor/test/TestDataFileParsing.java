package org.nees.illinois.uisimcor.fem_executor.test;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;

import org.nees.illinois.uisimcor.fem_executor.output.DataPad;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParser;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParsingTask;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.Mtx2Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the parsing of text files.
 * @author Michael Bletzinger
 */
@Test(groups = { "data" })
public class TestDataFileParsing {
	/**
	 * Full path of a test text file.
	 */
	private String dispPath;
	/**
	 * Expected displacement values. Should match the contents of tmp_disp.out.
	 */

	private final double[] expectedDisp = { 1.0E-4, 2.37522E-26, 3.11327E-20,
			4.32399E-22, -2.37522E-26, 3.11327E-20, 6.83281E-21, 2.29679E-36,
			-2.19581E-34, 2.10932E-38, 5.36481E-37, -5.04524E-35, 1.1498E-37,
			-6.19862E-51, 5.95044E-49, -3.75328E-55, 1.21785E-51, -1.16916E-49 };
	/**
	 * Expected force values. Should match the contents of tmp_forc.out.
	 */
	private final double[] expectedForce = { 0.726521, -2.37522E-6, -3.11327,
			-0.0432399, 2.37522E-6, -3.11327, -0.683281, -1.39598E-16,
			1.34154E-14, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	/**
	 * Full path of a test text file.
	 */
	private String forcePath;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestDataFileParsing.class);

	/**
	 * Convert Internet space codes and replace file separators for Windows
	 * systems.
	 * @param dirty
	 *            Original path.
	 * @return Cleaned up path.
	 */
	private String cleanPath(final String dirty) {
		String sep = System.getProperty("file.separator");
		String result = dirty.replaceAll("%20", " ");
		result = result.replaceAll("(?<!^)(\\\\|/){2,}",
				Matcher.quoteReplacement(sep));
		return result;
	}

	/**
	 * Compares two data matrices.
	 * @param actual
	 *            The actual data.
	 * @param expected
	 *            The expected data.
	 */
	private void compareData(List<Double>actualL, final double[] expected) {
		double [] actual = new double[actualL.size()];
		int idx = 0;
		for(double d : actualL) {
			actual[idx++] = d;
		}
		log.debug("Comparing expected " + Mtx2Str.array2String(expected)
				+ "\nwith actual\n" + Mtx2Str.array2String(actual));
		Assert.assertEquals(actual.length, expected.length);
		for (int i = 0; i < expected.length; i++) {

				if (Double.isNaN(expected[i])) {
					Assert.assertTrue(Double.isNaN(actual[i]));
					continue;
				}
				final double increment = 0.001;
				Assert.assertEquals(actual[i], expected[i], increment);
		}
	}

	/**
	 * Finds the two test output files.
	 */
	@BeforeClass
	public final void setup() {
		URL u = ClassLoader.getSystemResource("tmp_disp.out");
		dispPath = cleanPath(u.getPath());
		u = ClassLoader.getSystemResource("tmp_forc.out");
		forcePath = cleanPath(u.getPath());
	}

	/**
	 * Test the parsing of the two output files.
	 */
	@Test
	public final void testParsing() {
		OutputFileParser df = new OutputFileParser();
		df.parseDataFile(dispPath);
		List<Double> result = df.getArchive();
		log.info("Parsed DISP:\n" + result);
		compareData(result, expectedDisp);

		df.parseDataFile(forcePath);
		result = df.getArchive();
		log.info("Parsed FORCE:\n" + result);
		compareData(result, expectedForce);
	}

	/**
	 * Test the parsing of the two output files.
	 */
	@Test
	public final void testParsingTasks() {
		OutputFileParsingTask ofpt1 = new OutputFileParsingTask(dispPath);
		OutputFileParsingTask ofpt2 = new OutputFileParsingTask(forcePath);
		Thread thrd1 = new Thread(ofpt1);
		Thread thrd2 = new Thread(ofpt2);
		log.debug("Starting threads");
		thrd1.start();
		thrd2.start();
		boolean done = false;
		while (done == false) {
			try {
				final int interval = 2000;
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				log.info("Checking tasks");
			}
			done = ofpt1.isDone() && ofpt2.isDone();
		}
		compareData(ofpt1.getData(), expectedDisp);
		compareData(ofpt2.getData(), expectedForce);
	}
	/**
	 * Test data padding the output.
	 */
	@Test
	public final void testDataPadding() {
		CreateRefSubstructureConfig cfgR = new CreateRefSubstructureConfig("MDL-02");
		DataPad dp = new DataPad(cfgR.getConfig());
		OutputFileParser df = new OutputFileParser();
		df.parseDataFile(dispPath);
		List<Double> result = df.getArchive();
		DoubleMatrix dm = dp.pad(result);
		log.info("Padded DISP:\n" + dm);

		df.parseDataFile(forcePath);
		result = df.getArchive();
		 dm = dp.pad(result);
		log.info("Padded FORCE:\n" + dm);
	}
}
