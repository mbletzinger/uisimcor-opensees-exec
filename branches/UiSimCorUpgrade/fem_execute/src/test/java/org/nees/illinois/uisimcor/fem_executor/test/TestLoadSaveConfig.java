package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.FemExecutorConfig;
import org.nees.illinois.uisimcor.fem_executor.config.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.LoadSaveConfig;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the LoadSaveConfig class.
 * @author Michael Bletzinger
 */
@Test(groups = { "loadSave" })
public class TestLoadSaveConfig {
	/**
	 * Reference set of configurations.
	 */
	private FemExecutorConfig femCfg;
	/**
	 * Test saving the reference configuration.
	 */
	private String configFilename;
	/**
	 * Existing file that contains the reference configuration.
	 */
	private String configRefFile;
	/**
	 * Existing folder that contains the reference configuration file.
	 */
	private String configRefFolder;
	/**
	 * Directory for storing temporary files.
	 */
	private String workDir;

	/**
	 * Test saving a configuration. Just looking for no exceptions.
	 */

	@Test
	public final void testSave() {
		LoadSaveConfig lscfg = new LoadSaveConfig();
		lscfg.setConfigFilePath(configFilename);
		lscfg.setFemConfig(femCfg);
		lscfg.save();
	}

	/**
	 * Test loading the reference configuration.
	 */
	@Test(dependsOnMethods = { "testSave" })
	public final void testLoad() {
		LoadSaveConfig lscfg = new LoadSaveConfig();
		lscfg.setConfigFilePath(configRefFile);
		lscfg.load(configRefFolder);
		compareConfigs(lscfg.getFemConfig(), femCfg);
	}

	/**
	 * Test loading the test configuration.
	 */
	@Test(dependsOnMethods = { "testLoad" })
	public final void testLoadingSaved() {
		LoadSaveConfig lscfg = new LoadSaveConfig();
		lscfg.setConfigFilePath(configFilename);
		lscfg.load(workDir);
		compareConfigs(lscfg.getFemConfig(), femCfg);
	}

	/**
	 * Set up the reference configuration.
	 */
	@BeforeTest
	public final void beforeTest() {
		String sep = System.getProperty("file.separator");
		workDir = System.getProperty("user.dir");
		configFilename = "TestConfig.properties";
		URL u = ClassLoader.getSystemResource("ReferenceConfig.properties");
		String path = PathUtils.cleanPath(u.getPath());
		File pathF = new File(path);
		configRefFile = pathF.getName();
		configRefFolder = pathF.getParent();
		final int noSubstructures = 3;
		final int node1 = 2;
		final int node2 = 3;
		final int node3 = 4;
		femCfg = new FemExecutorConfig("/home/mbletzin/Tmp");
		FemProgramConfig femProg = new FemProgramConfig(
				FemProgramType.OPENSEES, "/usr/bin/OpenSees",
				"/Example/MOST/01_Left_OpenSees/StaticAnalysisEnv.tcl");
		femCfg.getFemProgramParameters().put(FemProgramType.OPENSEES, femProg);
		for (int i = 1; i < noSubstructures + 1; i++) {
			String address = "MDL-0" + i;
			DimensionType dim = DimensionType.TwoD;
			List<Integer> nodes = new ArrayList<Integer>();
			String modelFilename;
			if (i == 1) {
				nodes.add(node1);
				modelFilename = "Examples" + sep + "MOST" + sep
						+ "01_Left_OpenSees";
			} else if (i == 2) {
				nodes.add(node1);
				nodes.add(node2);
				nodes.add(node3);
				modelFilename = "Examples" + sep + "MOST" + sep
						+ "02_Middle_OpenSees";
			} else {
				nodes.add(node2);
				modelFilename = "Examples" + sep + "MOST" + sep
						+ "03_Right_OpenSees";
			}
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

	}

	/**
	 * Compares two configurations.
	 * @param actual
	 *            Loaded configuration.
	 * @param expected
	 *            Expected values.
	 */
	private void compareConfigs(final FemExecutorConfig actual,
			final FemExecutorConfig expected) {
//		Assert.assertEquals(actual.getConfigRoot(), workDir);
		List<FemProgramType> eprogs = new ArrayList<FemProgramType>(expected
				.getFemProgramParameters().keySet());
		for (FemProgramType p : eprogs) {
			FemProgramConfig eprogCfg = expected.getFemProgramParameters().get(
					p);
			FemProgramConfig aprogCfg = actual.getFemProgramParameters().get(p);
			Assert.assertNotNull(aprogCfg, "Checking program parameters for "
					+ p);
			Assert.assertEquals(aprogCfg.getExecutablePath(),
					eprogCfg.getExecutablePath(),
					"Checking program parameters for " + p);
			Assert.assertEquals(aprogCfg.getStaticAnalysisScriptPath(),
					eprogCfg.getStaticAnalysisScriptPath(),
					"Checking program parameters for " + p);
		}
		for (String n : expected.getSubstructCfgs().keySet()) {
			SubstructureConfig ecfg = expected.getSubstructCfgs().get(n);
			SubstructureConfig acfg = actual.getSubstructCfgs().get(n);
			Assert.assertNotNull(acfg, "Checking substructure \"" + n + "\"");
			Assert.assertEquals(acfg.getModelFileName(),
					ecfg.getModelFileName(), "Checking substructure \"" + n
							+ "\"");
			Assert.assertEquals(acfg.getDimension(), ecfg.getDimension(),
					"Checking substructure \"" + n + "\"");
			Assert.assertEquals(acfg.getFemProgram(), ecfg.getFemProgram(),
					"Checking substructure \"" + n + "\"");
			List<Integer> enodes = ecfg.getNodeSequence();
			List<Integer> anodes = acfg.getNodeSequence();
			for (int nd = 0; nd < enodes.size(); nd++) {
				Integer node = enodes.get(nd);
				Assert.assertEquals(anodes.get(nd), node,
						"Checking substructure \"" + n + "\" node " + node);
				List<DispDof> eedofs = ecfg.getEffectiveDofs(node);
				List<DispDof> aedofs = acfg.getEffectiveDofs(node);
				Assert.assertNotNull(aedofs, "Checking substructure \"" + n
						+ "\" node " + node);
				Assert.assertEquals(aedofs.size(), eedofs.size(),
						"Checking substructure \"" + n + "\" node " + node);
				for (int d = 0; d < eedofs.size(); d++) {
					Assert.assertEquals(aedofs.get(d), eedofs.get(d),
							"Checking substructure " + n + "\" node " + node
									+ " dof idx " + d);
				}
			}
		}
	}
}
