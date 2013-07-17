package org.nees.illinois.uisimcor.fem_executor.test.opensees;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.FemExecutor;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.execute.FileWithContentDelete;
import org.nees.illinois.uisimcor.fem_executor.test.utils.CreateRefProgramConfig;
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

public class TestFemExecutorWithOpenSees {
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
			.getLogger(TestFemExecutorWithOpenSees.class);
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
	private boolean keepFiles = true;
	/**
	 * Number of steps to execute.
	 */
	private final int numberOfSteps = 11;

	/**
	 * Run all of the test configuration through the FEM executor.
	 */
	@Test
	public final void testRunDynamic() {
		FemExecutor fexec = new FemExecutor(configDir, workDir);
		for (String c : configFiles) {
			runSubstructure(fexec, c);
		}
	}

	/**
	 * Run the substructure through some steps.
	 * @param fexec
	 *            The executor.
	 * @param c
	 *            The configuration name.
	 */
	private void runSubstructure(final FemExecutor fexec, final String c) {
		fexec.loadConfig(c);
		// Replace OpenSees with fake script
		fexec.getConfig().getFemProgramParameters()
				.put(FemProgramType.OPENSEES, femProg);
		Collection<SubstructureDao> mdlCfgs = fexec.getConfig()
				.getSubstructCfgs().values();
		Assert.assertTrue(fexec.setup());
		Assert.assertTrue(fexec.startSimulation());
		for (int s = 1; s < numberOfSteps; s++) {
			for (SubstructureDao mCfg : mdlCfgs) {
				loadExecutor(fexec, mCfg, s);
			}
			fexec.execute();
			int count = 0;
			final int tiredOfWaiting = 100;
			while (fexec.isDone() == false) {
				final int interval = 500;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					log.debug("Sleeping...");
				}
				if (count > tiredOfWaiting) {
					fexec.finish();
					Assert.fail("Execution has hung for some reason");
				}
				count++;
			}
			Collection<String> mdls = fexec.getConfig().getSubstructCfgs()
					.keySet();
			for (String m : mdls) {
				double[] vals = fexec.getDisplacements(m);
				final int numberOfDofs = fexec.getConfig().getSubstructCfgs()
						.get(m).getTotalDofs(); // UI-SimCor
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

	/**
	 * Set up a substructure with displacements for execution.
	 * @param fexec
	 *            Executor.
	 * @param subCfg
	 *            Configuration for the substructure.
	 * @param step
	 *            current step.
	 */
	private void loadExecutor(final FemExecutor fexec,
			final SubstructureDao subCfg, final int step) {
		double[] disp = calculateStep(subCfg.getNumberOfNodes(), step);
		log.debug("Set displacements for " + subCfg + " to "
				+ MtxUtils.array2String(disp));
		fexec.setStep(step);
		fexec.setDisplacements(subCfg.getAddress(), disp);
	}

	/**
	 * Set up test environment.
	 */
	@BeforeClass
	public final void beforeClass() {
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"fem_execute_opensees");
		String stepT = "step_template.tcl";
		URL u = ClassLoader.getSystemResource("openseescfg/" + stepT);
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
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
		String command = PathUtils.cleanPath(u.getPath());
		CreateRefProgramConfig crpcfg = new CreateRefProgramConfig(command);
		crpcfg.checkExecutable();
		femProg = crpcfg.getConfig();

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

	/**
	 * Calculate some displacements for the substructure at the current step.
	 * @param numNodes
	 *            Number of nodes in the substructure.
	 * @param step
	 *            Current step.
	 * @return displacements.
	 */
	private double[] calculateStep(final int numNodes, final int step) {
		final int rzIdx = 3;
		final int numDofs = 3;// assume 2D for now.
		double[] result = new double[numNodes * numDofs + 1];
		final double interval = 0.0002;
		for (int n = 0; n < numNodes; n++) {
			result[n] = interval * step;
			result[rzIdx + n] = Math.pow(interval, 2) * step;
		}
		return result;
	}
}
