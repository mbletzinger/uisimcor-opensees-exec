package org.nees.illinois.uisimcor.fem_executor.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.FemExecutor;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.test.utils.CreateRefProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.utils.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the operation of the FEM executor.
 * @author Michael Bletzinger
 */
@Test(groups = { "execute_dynamic" })
public class TestFemExecutorExecution {
	/**
	 * Configuration containing the fake OpenSees.
	 */
	private ProgramDao femProg;
	/**
	 * Directory containing the configuration files for the test.
	 */
	private String configDir;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(TestFemExecutorExecution.class);
	/**
	 * List of test configurations.
	 */
	private List<String> configFiles = new ArrayList<String>();
	/**
	 * Directory to store temporary files during FEM execution.
	 */
	private String workDir;
	/**
	 * Flag to indicate whether the generated files should be removed or not
	 * after the tests are over.
	 */
	private boolean keepFiles = false;

	/**
	 * Run all of the test configuration through the FEM executor.
	 */
	@Test
	public final void testRunFakeSubstructures() {
		FemExecutor fexec = new FemExecutor(configDir, workDir);

		for (String c : configFiles) {
			log.debug("Loading configuration for " + c);
			fexec.loadConfig(c);
			// Replace OpenSees with fake script
			fexec.getConfig().getFemProgramParameters()
					.put(FemProgramType.OPENSEES, femProg);
			Collection<SubstructureDao> mdlCfgs = fexec.getConfig()
					.getSubstructCfgs().values();
			Assert.assertTrue(fexec.setup());
			Assert.assertTrue(fexec.startSimulation());
			final int numSteps = 12;
			for (int s = 1; s < numSteps; s++) {
				log.debug("Loading displacements for " + c);
				for (SubstructureDao mCfg : mdlCfgs) {
					loadExecutor(fexec, mCfg, s);
				}
				fexec.setStep(s);
				log.debug("Executing for " + c + " at step " + s);
				fexec.execute();
				int count = 0;
				final int tiredOfWaiting = 20;
				log.debug("Waiting for done for " + c);
				while (fexec.isDone() == false) {
					final int interval = 200;
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						log.debug("Sleeping...");
					}
					if (count > tiredOfWaiting) {
						log.error("Tired of waiting for " + c);
						fexec.finish();
						Assert.fail("Execution has hung for some reason");
					}
					count++;
				}
				Collection<String> mdls = fexec.getConfig().getSubstructCfgs()
						.keySet();
				log.debug("Getting displacements and finishing for " + c);
				for (String m : mdls) {
					double[] vals = fexec.getDisplacements(m);
					final int numberOfDofs = fexec.getConfig()
							.getSubstructCfgs().get(m).getTotalDofs(); // UI-SimCor
																		// vector
																		// size.
					log.debug("Displacements for " + m + " are "
							+ MtxUtils.array2String(vals));
					Assert.assertEquals(vals.length, numberOfDofs);
					vals = fexec.getForces(m);
					Assert.assertEquals(vals.length, numberOfDofs);
				}
			}
			fexec.finish();
		}
	}

	/**
	 * Set up a substructure with displacements for execution.
	 * @param fexec
	 *            Executor.
	 * @param subCfg
	 *            Configuration for the substructure.
	 * @param step
	 *            Current step number.
	 */
	private void loadExecutor(final FemExecutor fexec,
			final SubstructureDao subCfg, final int step) {
		final double[] disp = { 0.00023e-4 * step, 0.00004e-5 * step,
				0.00023e-4 * step, 0.00004e-5 * step, 0.00023e-4 * step,
				0.00004e-5 * step };
		fexec.setDisplacements(subCfg.getAddress(), disp);
	}

	/**
	 * Set up test environment.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader
				.getSystemResource("config/ReferenceConfig.properties");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"fem_execute");
		u = ClassLoader.getSystemResource("OpenSeesDynamicEmulator.pl");
		String command = PathUtils.cleanPath(u.getPath());
		CreateRefProgramConfig crpcfg = new CreateRefProgramConfig(command);
		crpcfg.checkExecutable();
		femProg = crpcfg.windowsWrap(workDir);

		String[] configFileNames = { "OneSubstructureTestConfig",
				"TwoSubstructureTestConfig", "ThreeSubstructureTestConfig" };
		for (String f : configFileNames) {
			configFiles.add(f + ".properties");
		}
	}

	/**
	 * Remove the files generated from the test.
	 */
	@AfterTest
	public final void afterTest() {
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

}
