package org.nees.illinois.uisimcor.fem_executor.test;

import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.output.DataFormatter;
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
public class TestResponseParsing {
	/**
	 * Reference substructure.
	 */
	private final CreateRefSubstructureConfig cfgR = new CreateRefSubstructureConfig(
			"MDL-02");;
	/**
	 * Sensitivity of the comparison.
	 */
	private final double compareTolerance = 0.0001;
	/**
	 * Data strings.
	 */
	private String[] dataStrs = {
			"0.0010 2.0E-4 3.002E-22 0.0011 2.1E-4 3.0021E-19 0.0012 2.2E-4 3.0022E-19",
			"12.44 56.54 1289000.0 12.45 56.55 1290000.0 12.46 56.56 1291000.0" };
	/**
	 * Expected displacement values. Should match the contents of tmp_disp.out.
	 */
	private final double[] expectedDisp = { 0.0010, 2.0E-4, 3.002E-22, 0.0011,
			2.1E-4, 3.0021E-19, 0.0012, 2.2E-4, 3.0022E-19 };
	/**
	 * Expected filtered displacement values.
	 */
	private final double[] expectedFilteredDisp = { 0.0010, 3.002E-22, 0.0011,
			0.0012 };
	/**
	 * Expected filtered force values.
	 */
	private final double[] expectedFilteredForce = { 12.44, 1289000.0, 12.45,
			12.46 };
	/**
	 * Expected force values. Should match the contents of tmp_forc.out.
	 */
	private final double[] expectedForce = { 12.44, 56.54, 1289000.0, 12.45,
			56.55, 1290000.0, 12.46, 56.56, 1291000.0 };

	/**
	 * Expected padded displacements.
	 */
	private final double[][] expectedPaddedDisp = {
			{ 0.0010, 0.0, 0.0, 0.0, 0.0, 3.002E-22 },
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
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestResponseParsing.class);

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
				Assert.assertEquals(actual[i][j], expected[i][j],
						compareTolerance);
			}
		}
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
	 * Finds the two test output files.
	 */
	@BeforeClass
	public final void setup() {
	}

	/**
	 * Test data padding the output.
	 */
	@Test(dependsOnMethods = { "testParsing" })
	public final void testDataFiltering() {
		DataFormatter df = new DataFormatter(cfgR.getConfig());
		List<Double> result = df.tokenString2Double(dataStrs[0]);
		List<Double> fresult = df.filter(result);
		log.info("Filtered DISP:\n" + fresult);
		compareData(fresult, expectedFilteredDisp);

		result = df.tokenString2Double(dataStrs[1]);
		fresult = df.filter(result);
		log.info("Filtered FORCE:\n" + fresult);
		compareData(fresult, expectedFilteredForce);
	}

	/**
	 * Test data padding the output.
	 */
	@Test(dependsOnMethods = { "testParsing" })
	public final void testDataPadding() {
		DataFormatter df = new DataFormatter(cfgR.getConfig());
		List<Double> result = df.tokenString2Double(dataStrs[0]);
		DoubleMatrix dm = df.pad(result);
		log.info("Padded DISP:\n" + dm);
		compareData(dm.getData(), expectedPaddedDisp);

		result = df.tokenString2Double(dataStrs[1]);
		dm = df.pad(result);
		log.info("Padded FORCE:\n" + dm);
		compareData(dm.getData(), expectedPaddedForce);
	}

	/**
	 * Test the parsing of the two output files.
	 */
	@Test
	public final void testParsing() {
		DataFormatter df = new DataFormatter(cfgR.getConfig());
		List<Double> result = df.tokenString2Double(dataStrs[0]);
		log.info("Parsed DISP:\n" + result);
		compareData(result, expectedDisp);

		result = df.tokenString2Double(dataStrs[1]);
		log.info("Parsed FORCE:\n" + result);
		compareData(result, expectedForce);
	}
}
