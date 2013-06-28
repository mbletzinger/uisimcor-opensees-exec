package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.nees.illinois.uisimcor.fem_executor.FemExecutorConfig;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.test.utils.CreateRefProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.test.utils.CreateRefSubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the FemInputFile class.
 * @author Michael Bletzinger
 */
@Test(groups = { "input" })
public class TestInputCommandGeneration {
	/**
	 * Reference set of configurations.
	 */
	private FemExecutorConfig femCfg;
	/**
	 * Test saving the reference init.
	 */
	private String initReference = "source Middle.tcl\nsource StaticAnalysisEnv.tcl\n\n"
			+ "recorder Node -tcp 127.0.0.1 4116 -node 2 3 4 -dof 1 2 3 disp\n"
			+ "recorder Node -tcp 127.0.0.1 4117 -node 2 3 4 -dof 1 2 3 reaction\n";
	/**
	 * Reference step.
	 */
	private String stepReference = "pattern Plain 99003	Constant {\n"
			+ "sp 2 1 130.20300000000000000E-009\n"
			+ "sp 2 3 34.000120000000000000E-012\n"
			+ "sp 3 1 120.03450000000000000E-009\n"
			+ "sp 4 1 150.11000000000000000E-009\n" + "\n}\n" + "analyze 1\n"
			+ "remove loadPattern 99003\n"
			+ "puts \"Current step 99003 - done #:\"\n";
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
	private final String mdl = "MDL-02";
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestInputCommandGeneration.class);

	/**
	 * Test saving a configuration. Just looking for no exceptions.
	 */
	@Test
	public final void testGenerate() {
		ScriptGeneratorI fif = new OpenSeesSG(configDir, femCfg
				.getSubstructCfgs().get(mdl), femCfg.getFemProgramParameters()
				.get(FemProgramType.OPENSEES).getTemplateDao());
		final int stepNumber = 3;
		log.debug("Generating Init template");
		String initInput = fif.generateInit();
		Assert.assertEquals(initInput, initReference);
		log.debug("Generating Step template");
		String stepInput = fif.generateStep(stepNumber, data);
		Assert.assertEquals(stepInput, stepReference);
		String expectedRun = loadRunTclFile(PathUtils.append(configDir, "run_example.tcl"));
		log.debug("Generating Run template");
		String actualRun = fif.generateRun(stepNumber, data);
		Assert.assertEquals(actualRun, expectedRun);
	}

	/**
	 * Set up the reference configuration.
	 */
	@BeforeTest
	public final void beforeTest() {
		CreateRefSubstructureConfig cfgC = new CreateRefSubstructureConfig(mdl);
		URL u = ClassLoader.getSystemResource("config/init_template.tcl");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		femCfg = new FemExecutorConfig(configDir);
		CreateRefProgramConfig crpcfg = new CreateRefProgramConfig("/usr/bin/OpenSees");
		femCfg.getFemProgramParameters().put(FemProgramType.OPENSEES, crpcfg.getConfig());
		String address = mdl;
		final double[] dat = { 13.0203e-08, 34.00012e-12, 12.00345e-08,
				15.011e-08 };
		data = dat;
		femCfg.getSubstructCfgs().put(address, cfgC.getConfig());
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
}
