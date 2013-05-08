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
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.input.FemInputFile;
import org.nees.illinois.uisimcor.fem_executor.process.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
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
	private double[] data;
	/**
	 * Directory containing the configuration files for the test.
	 */
	private String configDir;
	/**
	 * Reference model name.
	 */
	private final String mdl = "MDL-01";
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestInputFileGeneration.class);

	/**
	 * Test saving a configuration. Just looking for no exceptions.
	 */
	@Test
	public final void testGenerate() {
		FemInputFile fif = new FemInputFile(femCfg.getFemProgramParameters()
				.get(FemProgramType.OPENSEES), femCfg.getSubstructCfgs().get(
				mdl), System.getProperty("user.dir"), configDir);
		final int stepNumber = 3;
		fif.generate(stepNumber, data);
		compareConfigs(inputFilePath, referenceFile);
	}

	/**
	 * Set up the reference configuration.
	 */
	@BeforeTest
	public final void beforeTest() {
		final int node1 = 2;
		final int node2 = 3;
		final int node3 = 4;
		final String workfile1 = "Wsection.tcl";
		final String workfile2 = "acc475C.dat";
		URL u = ClassLoader.getSystemResource("reference_run.tcl");
		referenceFile = PathUtils.cleanPath(u.getPath());
		u = ClassLoader.getSystemResource("config/run_template.tcl");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		femCfg = new FemExecutorConfig(configDir);
		FemProgramConfig femProg = new FemProgramConfig(
				FemProgramType.OPENSEES, "/usr/bin/OpenSees",
				"StaticAnalysisEnv.tcl");
		femCfg.getFemProgramParameters().put(FemProgramType.OPENSEES, femProg);
		String address = mdl;
		inputFilePath = PathUtils.append(System.getProperty("user.dir"),
				address);
		inputFilePath = PathUtils.append(inputFilePath, "run.tcl");

		DimensionType dim = DimensionType.TwoD;
		List<Integer> nodes = new ArrayList<Integer>();
		String modelFilename;
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		List<String> workFiles = new ArrayList<String>();
		workFiles.add(workfile1);
		workFiles.add(workfile2);
		final double[] dat = { 13.0203e-08, 34.00012e-12, 12.00345e-08,
				15.011e-08 };
		data = dat;
		modelFilename = "Middle.tcl";
		FemProgramType program = FemProgramType.OPENSEES;
		SubstructureConfig cfg = new SubstructureConfig(address, dim, program,
				modelFilename, nodes, workFiles);
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

	/**
	 * Remove the files generated from the test.
	 */
	@AfterTest
	public final void afterTest() {
		String workDir = PathUtils.append(System.getProperty("user.dir"), mdl);
		FileWithContentDelete dir = new FileWithContentDelete(workDir);
		boolean done = dir.delete();
		if (done == false) {
			log.error("Could not remove dir \"" + workDir + "\"");
			return;
		}
		log.debug("\"" + workDir + "\" was removed");
	}
}