package org.nees.illinois.uisimcor.fem_executor.test;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;

import org.nees.illinois.uisimcor.fem_executor.output.DataPad;
import org.nees.illinois.uisimcor.fem_executor.output.ResponseParser;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParsingTask;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
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
	private final double[] expectedDisp = { 0.0010, 2.0E-4, 3.002E-22, 0.0011,
			2.1E-4, 3.0021E-19, 0.0012, 2.2E-4, 3.0022E-19 };
	/**
	 * Expected force values. Should match the contents of tmp_forc.out.
	 */
	private final double[] expectedForce = { 12.44, 56.54, 1289000.0, 12.45, 56.55, 1290000.0, 12.46, 56.56, 1291000.0 };
	/**
	 * Expected padded displacements.
	 */
	private final double[][] expectedPaddedDisp = {
			{ 0.0010, 0.0, 0.0, 0.0, 0.0, 3.002E-22},
			{ 0.0011, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 0.0012, 0.0, 0.0, 0.0, 0.0, 0.0 } };
	/**
	 * Expected padded forces.
	 */
	private final double[][] expectedPaddedForce = {
			{ 12.44, 0.0, 0.0, 0.0, 0.0, 1289000.0 },
			{ 12.45, 0.0, 0.0, 0.0, 0.0, 0.0 },
			{ 12.46, 0.0, 0.0, 0.0, 0.0, 0.0 } };
	/**
	 * Expected filtered displacement values.
	 */
	private final double[] expectedFilteredDisp = { 0.0010, 3.002E-22, 0.0011, 0.0012 };
	/**
	 * Expected filtered force values.
	 */
	private final double[] expectedFilteredForce = { 12.44, 1289000.0, 12.45, 12.46 };
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
	 * Sensitivity of the comparison.
	 */
	private final double compareTolerance = 0.0001;

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
	 * Compares two data arrays.
	 * @param actualL
	 *            The actual data.
	 * @param expected
	 *            The expected data.
	 */
	private void compareData(final List<Double> actualL, final double[] expected) {
		double[] actual = new double[actualL.size()];
		int idx = 0;
		for (double d : actualL) {
			actual[idx++] = d;
		}
		log.debug("Comparing expected " + MtxUtils.array2String(expected)
				+ "\nwith actual\n" + MtxUtils.array2String(actual));
		Assert.assertEquals(actual.length, expected.length);
		for (int i = 0; i < expected.length; i++) {

			if (Double.isNaN(expected[i])) {
				Assert.assertTrue(Double.isNaN(actual[i]));
				continue;
			}
			Assert.assertEquals(actual[i], expected[i], compareTolerance);
		}
	}

	/**
	 * Compares two data matrices.
	 * @param actual
	 *            The actual data.
	 * @param expected
	 *            The expected data.
	 */
	private void compareData(final double[][] actual, final double[][] expected) {
		log.debug("Comparing expected " + MtxUtils.matrix2String(expected)
				+ "\nwith actual\n" + MtxUtils.matrix2String(actual));
		Assert.assertEquals(actual.length, expected.length);
		Assert.assertEquals(actual[0].length, expected[0].length);
		for (int i = 0; i < expected.length; i++) {
			for (int j = 0; j < expected[0].length; j++) {

				if (Double.isNaN(expected[i][j])) {
					Assert.assertTrue(Double.isNaN(actual[i][j]));
					continue;
				}
				Assert.assertEquals(actual[i][j], expected[i][j], compareTolerance);
			}
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
		ResponseParser df = new ResponseParser();
		df.parseDataString(null, dispPath);
		List<Double> result = df.getArchive();
		log.info("Parsed DISP:\n" + result);
		compareData(result, expectedDisp);

		df.parseDataString(null, forcePath);
		result = df.getArchive();
		log.info("Parsed FORCE:\n" + result);
		compareData(result, expectedForce);
	}

	/**
	 * Test the parsing of the two output files.
	 */
	@Test(dependsOnMethods = { "testParsing" })
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
	@Test(dependsOnMethods = { "testParsing" })
	public final void testDataPadding() {
		CreateRefSubstructureConfig cfgR = new CreateRefSubstructureConfig(
				"MDL-02");
		DataPad dp = new DataPad(cfgR.getConfig());
		ResponseParser df = new ResponseParser();
		df.parseDataString(null, dispPath);
		List<Double> result = df.getArchive();
		DoubleMatrix dm = dp.pad(result);
		log.info("Padded DISP:\n" + dm);
		compareData(dm.getData(), expectedPaddedDisp);

		df.parseDataString(null, forcePath);
		result = df.getArchive();
		dm = dp.pad(result);
		log.info("Padded FORCE:\n" + dm);
		compareData(dm.getData(), expectedPaddedForce);
	}
	/**
	 * Test data padding the output.
	 */
	@Test(dependsOnMethods = { "testParsing" })
	public final void testDataFiltering() {
		CreateRefSubstructureConfig cfgR = new CreateRefSubstructureConfig(
				"MDL-02");
		DataPad dp = new DataPad(cfgR.getConfig());
		ResponseParser df = new ResponseParser();
		df.parseDataString(null, dispPath);
		List<Double> result = df.getArchive();
		List<Double> fresult = dp.filter(result);
		log.info("Filtered DISP:\n" + fresult);
		compareData(fresult, expectedFilteredDisp);

		df.parseDataString(null, forcePath);
		result = df.getArchive();
		fresult = dp.filter(result);
		log.info("Filtered FORCE:\n" + fresult);
		compareData(fresult, expectedFilteredForce);
	}
}
