package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.FemExecutorConfig;
import org.nees.illinois.uisimcor.fem_executor.config.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.input.FemInputFile;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the FemInputFile class.
 * @author Michael Bletzinger
 */
@Test(groups = { "input" })
public class TestInputFileGeneration {
	/**
	 * Reference set of configurations.
	 */
	private FemExecutorConfig femCfg;
	/**
	 * Test saving the reference input file.
	 */
	private String referenceFile;
	/**
	 * Generated input file.
	 */
	private String inputFilePath;
	/**
	 * Displacement data.
	 */
	private DoubleMatrix data;
	/**
	 * Directory containing the configuration files for the test.
	 */
	private String configDir;

	/**
	 * Test saving a configuration. Just looking for no exceptions.
	 */
	@Test
	public final void testGenerate() {
		FemInputFile fif = new FemInputFile(femCfg.getFemProgramParameters()
				.get(FemProgramType.OPENSEES), femCfg.getSubstructCfgs().get(
				"MDL-01"), femCfg.getConfigRoot(), configDir);
		final int stepNumber = 3;
		fif.generate(stepNumber, data);
		compareConfigs(inputFilePath, referenceFile);
	}

	/**
	 * Set up the reference configuration.
	 */
	@BeforeTest
	public final void beforeTest() {
		final String sep = System.getProperty("file.separator");
		final int node1 = 2;
		final int node2 = 3;
		final int node3 = 4;
		URL u = ClassLoader.getSystemResource("reference_run.tcl");
		referenceFile = PathUtils.cleanPath(u.getPath());
		u = ClassLoader.getSystemResource("config/run_template.tcl");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		femCfg = new FemExecutorConfig(configDir);
		FemProgramConfig femProg = new FemProgramConfig(
				FemProgramType.OPENSEES, "/usr/bin/OpenSees",
				"/Example/MOST/01_Left_OpenSees/StaticAnalysisEnv.tcl");
		femCfg.getFemProgramParameters().put(FemProgramType.OPENSEES, femProg);
		String address = "MDL-01";
		inputFilePath = PathUtils.append(System.getProperty("user.dir"),
				address);
		inputFilePath = PathUtils.append(inputFilePath, "run.tcl");

		DimensionType dim = DimensionType.TwoD;
		List<Integer> nodes = new ArrayList<Integer>();
		String modelFilename;
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		final double[][] dat = {
				{ 13.0203e-08, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
						34.00012e-12 },
				{ 12.00345e-08, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
						Double.NaN },
				{ 15.011e-08, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
						Double.NaN } };
		data = new DoubleMatrix(dat);
		modelFilename = "Examples" + sep + "MOST" + sep + "02_Middle_OpenSees"
				+ sep + "Middle.tcl";
		FemProgramType program = FemProgramType.OPENSEES;
		SubstructureConfig cfg = new SubstructureConfig(address, dim,
				program, modelFilename, nodes);
		for (Integer n : nodes) {
			List<DispDof> edof = new ArrayList<DispDof>();
			if (n == node1) {
				edof.add(DispDof.DX);
				edof.add(DispDof.RZ);
			} else {
				edof.add(DispDof.DX);
			}
			cfg.addEffectiveDofs(n, edof);
		}
		femCfg.getSubstructCfgs().put(address, cfg);
	}

	/**
	 * load a text file into a String.
	 * @param path
	 *            Text file path.
	 * @return String content.
	 */
	private String loadRunTclFile(final String path) {
		File tfile = new File(path);
		if (tfile.canRead() == false) {
			Assert.fail("Template file \"" + path + "\" cannot be read");
		}
		String result = "";
		try {
			BufferedReader is = new BufferedReader(new FileReader(tfile));
			String ln;
			while ((ln = is.readLine()) != null) {
				result += ln + "\n";
			}
			is.close();
		} catch (Exception e) {
			Assert.fail("Template file \"" + path
					+ "\" cannot be read because ", e);
		}
		return result;
	}

	/**
	 * Compares two configurations.
	 * @param actual
	 *            Loaded configuration.
	 * @param expected
	 *            Expected values.
	 */
	private void compareConfigs(final String actual, final String expected) {
		String expectedContent = loadRunTclFile(expected);
		String actualContent = loadRunTclFile(actual);
		Assert.assertEquals(actualContent, expectedContent);
	}
}
