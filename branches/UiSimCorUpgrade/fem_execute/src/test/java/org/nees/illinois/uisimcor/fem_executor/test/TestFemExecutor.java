package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.FemExecutor;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.process.FileWithContentDelete;
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
public class TestFemExecutor {
	/**
	 * Configuration containing the fake OpenSees.
	 */
	private FemProgramConfig femProg;
	/**
	 * Directory containing the configuration files for the test.
	 */
	private String configDir;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TestFemExecutor.class);
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
		for (String c : configFiles) {
			fexec.loadConfig(c);
			fexec.getConfig().getFemProgramParameters()
					.put(FemProgramType.OPENSEES, femProg);
			Collection<SubstructureConfig> mdlCfgs = fexec.getConfig()
					.getSubstructCfgs().values();
			fexec.setup();
			for (SubstructureConfig mCfg : mdlCfgs) {
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
					fexec.abort();
					Assert.fail("Execution has hung for some reason");
				}
			}
			Collection<String> mdls = fexec.getConfig().getSubstructCfgs()
					.keySet();
			for (String m : mdls) {
				double[][] vals = fexec.getDisplacements(m);
				int nnodes = fexec.getConfig().getSubstructCfgs().get(m)
						.getNumberOfNodes();
				final int numberOfDofs = 3; // Configurations are 2D.
				nnodes = (nnodes * numberOfDofs) + 1;
				DoubleMatrix disp = new DoubleMatrix(vals);
				int[] sz = disp.sizes();
				log.debug("Displacements for " + m + " are " + disp);
				Assert.assertEquals(sz[0], 1);
				Assert.assertEquals(sz[1], nnodes);
				vals = fexec.getForces(m);
				DoubleMatrix forces = new DoubleMatrix(vals);
				sz = forces.sizes();
				log.debug("Forces for " + m + " are " + forces);
				Assert.assertEquals(sz[0], 1);
				Assert.assertEquals(sz[1], nnodes);
			}
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
			final SubstructureConfig subCfg) {
		final Double[] disp = { 0.00023e-4, 0.00004e-5, 0.00023e-4, 0.00004e-5,
				0.00023e-4, 0.00004e-5 };
		List<List<Double>> matrix = new ArrayList<List<Double>>();
		List<Integer> nodes = subCfg.getNodeSequence();
		for (int r = 0; r < nodes.size(); r++) {
			matrix.add(Arrays.asList(disp));
		}
		DoubleMatrix dm = new DoubleMatrix(matrix);
		fexec.setDisplacements(subCfg.getAddress(), dm.getData());
	}

	/**
	 * Set up test environment.
	 */
	@BeforeClass
	public final void beforeClass() {
		URL u = ClassLoader.getSystemResource("OpenSeesEmulator.pl");
		String command = PathUtils.cleanPath(u.getPath());
		File cmdF = new File(command);
		cmdF.setExecutable(true);
		femProg = new FemProgramConfig(FemProgramType.OPENSEES, command,
				"/Example/MOST/01_Left_OpenSees/StaticAnalysisEnv.tcl");
		String[] configFileNames = { "OneSubstructureTestConfig",
				"TwoSubstructureTestConfig", "ThreeSubstructureTestConfig" };
		for (String f : configFileNames) {
			configFiles.add(f + ".properties");
		}
		u = ClassLoader.getSystemResource("ReferenceConfig.properties");
		String cf = PathUtils.cleanPath(u.getPath());
		configDir = PathUtils.parent(cf);
		configDir = PathUtils.append(configDir, "config");
		workDir = PathUtils.append(System.getProperty("user.dir"),
				"fem_execute");
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
