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
@Test(groups = { "execute_static" })
public class TestStaticExecution {
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
	private final Logger log = LoggerFactory.getLogger(TestStaticExecution.class);
	/**
	 * List of test configurations.
	 */
	private List<String> configFiles = new ArrayList<String>();
	/**
	 * Directory to store tmp files during FEM execution.
	 */
	private String workDir;

	/**
	 * Run all of the test configuration through the FEM executor.
	 */
	@Test
	public final void testRunFakeSubstructures() {
		FemExecutor fexec = new FemExecutor(configDir, workDir);
		fexec.setDynamic(false);
		for (String c : configFiles) {
			fexec.loadConfig(c);
			// Replace OpenSees with fake script
			fexec.getConfig().getFemProgramParameters()
					.put(FemProgramType.OPENSEES, femProg);
			Collection<SubstructureDao> mdlCfgs = fexec.getConfig()
					.getSubstructCfgs().values();
			fexec.setup();
			for (SubstructureDao mCfg : mdlCfgs) {
				loadExecutor(fexec, mCfg);
			}
			fexec.execute();
			int count = 0;
			final int tiredOfWaiting = 50;
			while (fexec.isDone() == false) {
				final int interval = 200;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					log.debug("Sleeping...");
				}
				if (count > tiredOfWaiting) {
					fexec.finish();
					Assert.fail("Execution has hung for some reason");
				}
			}
			Collection<String> mdls = fexec.getConfig().getSubstructCfgs()
					.keySet();
			for (String m : mdls) {
				double[] vals = fexec.getDisplacements(m);
				final int numberOfDofs = fexec.getConfig().getSubstructCfgs().get(m).getTotalDofs(); // UI-SimCor vector size.
				log.debug("Displacements for " + m + " are " + vals);
				Assert.assertEquals(vals.length, numberOfDofs);
				vals = fexec.getForces(m);
				Assert.assertEquals(vals.length, numberOfDofs);
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
	 */
	private void loadExecutor(final FemExecutor fexec,
			final SubstructureDao subCfg) {
		final double[] disp = { 0.00023e-4, 0.00004e-5, 0.00023e-4, 0.00004e-5,
				0.00023e-4, 0.00004e-5 };
		fexec.setDisplacements(subCfg.getAddress(), disp);
	}

	/**
	 * Set up test environment.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("config/ReferenceConfig.properties");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"fem_execute");
		u = ClassLoader.getSystemResource("OpenSeesStaticEmulator.pl");
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
		FileWithContentDelete dir = new FileWithContentDelete(workDir);
		boolean done = dir.delete();
		if (done == false) {
			log.error("Could not remove dir \"" + workDir + "\"");
			return;
		}
		log.debug("\"" + workDir + "\" was removed");
	}

}