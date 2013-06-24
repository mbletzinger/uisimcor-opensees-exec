package org.nees.illinois.uisimcor.fem_executor.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.nees.illinois.uisimcor.fem_executor.FemExecutorConfig;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.TemplateDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to load and save FEM Executor configuration files stored as Java
 * Properties.
 * @author Michael Bletzinger
 */
public class LoadSaveConfig {
	/**
	 * Path to configuration file.
	 */
	private String configFilePath;

	/**
	 * Encoder/Decoder for displacement DOF lists.
	 */
	private final EncodeDecodeList<DispDof, DispDofDecoder> eoDispDofList = new EncodeDecodeList<DispDof, DispDofDecoder>(
			new DispDofDecoder());

	/**
	 * Encoder/Decoder for displacement integer lists.
	 */
	private final EncodeDecodeList<Integer, IntegerDecoder> eoIntegerList = new EncodeDecodeList<Integer, IntegerDecoder>(
			new IntegerDecoder());
	/**
	 * Encoder/Decoder for String lists.
	 */
	private final EncodeDecodeList<String, StringDecoder> eoStringList = new EncodeDecodeList<String, StringDecoder>(
			new StringDecoder());

	/**
	 * FEM Executor Configuration.
	 */
	private FemExecutorConfig femConfig;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(LoadSaveConfig.class);
	/**
	 * Properties for storing configuration data for a substructure.
	 */
	private Properties props;
	/**
	 * @return the configFilePath
	 */
	public final String getConfigFilePath() {
		return configFilePath;
	}
	/**
	 * @return the femConfig
	 */
	public final FemExecutorConfig getFemConfig() {
		return femConfig;
	}

	/**
	 * Load a configuration.
	 * @param workDir
	 *            Directory used for the temporary files when calculating
	 *            substructure models.
	 */
	public final void load(final String workDir) {
		File configFile = new File(PathUtils.append(workDir, configFilePath));
		if (configFile.canRead() == false) {
			log.error("Unable to read from \"" + configFile.getAbsolutePath()
					+ "\"");
			return;
		}
		props = new Properties();

		InputStream configI;
		try {
			configI = new FileInputStream(configFile);
			props.load(configI);
		} catch (Exception e) {
			log.error("Unable to read from \"" + configFile.getAbsolutePath()
					+ "\" because ", e);
			return;
		}
		femConfig = new FemExecutorConfig(workDir);
		for (FemProgramType p : FemProgramType.values()) {
			ProgramDao fProgCfg = loadFemProgram(p);
			if (fProgCfg == null) {
				continue;
			}
			femConfig.getFemProgramParameters().put(p, fProgCfg);
		}
		String str = props.getProperty("substructures");
		String[] names = str.split(", ");
		for (String n : names) {
			SubstructureDao cfg = loadSubStructure(n);
			femConfig.getSubstructCfgs().put(n, cfg);
		}
	}

	/**
	 * Extracting FEM program parameters from the properties file.
	 * @param ptype
	 *            FEM program type.
	 * @return FEM program parameters set
	 */
	private ProgramDao loadFemProgram(final FemProgramType ptype) {
		String stepT = props.getProperty(ptype + ".file.template.step");
		if (stepT == null) {
			return null;
		}
		String initT = props.getProperty(ptype + ".file.template.init");
		if (initT == null) {
			return null;
		}
		TemplateDao tdao = new TemplateDao(stepT, initT);
		String executable = props.getProperty(ptype + ".path.executable");
		if (executable == null) {
			return null;
		}
		ProgramDao result = new ProgramDao(executable, ptype, tdao);
		return result;
	}

	/**
	 * Load a configuration for a substructure.
	 * @param name
	 *            Name of substructure.
	 * @return Configuration data.
	 */
	private SubstructureDao loadSubStructure(final String name) {
		String address = name;
		String str = props.getProperty(name + ".dimension");
		DimensionType dim = null;
		if (str == null) {
			log.error("Dimension not found for " + name);
		} else {
			try {
				dim = DimensionType.valueOf(str);
			} catch (Exception e) {
				log.error("Dimension \"" + str + "\" not recognized for "
						+ name);
			}
		}
		str = props.getProperty(name + ".control.nodes");
		List<Integer> nodes = null;
		if (str == null) {
			log.error("Control nodes not found for " + name);
		} else {
			try {
				nodes = eoIntegerList.parse(str);
			} catch (Exception e) {
				log.error("Control node list \"" + str
						+ "\" not recognized for " + name, e);
			}
		}
		str = props.getProperty(name + ".fem.program");
		FemProgramType fem = null;
		if (str == null) {
			log.error("FEM program name not found for " + name);
		} else {
			try {
				fem = FemProgramType.valueOf(str);
			} catch (Exception e) {
				log.error("FEM program \"" + str + "\" not recognized for "
						+ name);
			}
		}
		str = props.getProperty(name + ".work.files");
		List<String> wfiles = null;
		if (str == null) {
			log.error("Work files not found for " + name);
		} else {
			try {
				wfiles = eoStringList.parse(str);
			} catch (Exception e) {
				log.error("Work files list \"" + str
						+ "\" not recognized for " + name, e);
			}
		}
		str = props.getProperty(name + ".source.files");
		List<String> sfiles = null;
		if (str == null) {
			log.error("Work files not found for " + name);
		} else {
			try {
				sfiles = eoStringList.parse(str);
			} catch (Exception e) {
				log.error("Source files list \"" + str
						+ "\" not recognized for " + name, e);
			}
		}
		final IntegerDecoder id = new IntegerDecoder();
		str = props.getProperty(name + ".tcp.port.disp");
		int dport = id.parse(str);
		str = props.getProperty(name + ".tcp.port.forc");
		int fport = id.parse(str);

		SubstructureDao result = new SubstructureDao(address, dim, fem,
				sfiles, nodes, wfiles,dport,fport);
		for (Integer node : nodes) {
			str = props.getProperty(name + ".effective.dofs." + node);
			List<DispDof> edofs = null;
			if (str == null) {
				log.error("Missing Effective DOFs for node " + node
						+ " substructure " + address);
				continue;
			}
			try {
				edofs = eoDispDofList.parse(str);
				result.addEffectiveDofs(node, edofs);
			} catch (Exception e) {
				log.error("Effective DOF list \"" + str
						+ "\" not recognized for node " + node
						+ " substructure " + name, e);
			}
		}
		return result;
	}

	/**
	 * Save a configuration.
	 */
	public final void save() {
		File configFile = new File(configFilePath);
		props = new Properties();
		List<String> sorted = new ArrayList<String>(femConfig
				.getSubstructCfgs().keySet());
		Collections.sort(sorted);
		String str = "";
		boolean first = true;
		for (String name : sorted) {
			saveSubStructure(name);
			str += (first ? "" : ", ") + name;
			first = false;
		}
		props.setProperty("substructures", str);
		for (ProgramDao fpCfg : femConfig.getFemProgramParameters()
				.values()) {
			saveFemProgram(fpCfg);
		}

		Writer configW;
		try {
			configW = new FileWriter(configFile);
			props.store(configW, "");
		} catch (IOException e) {
			log.error("Unable to write to \"" + configFilePath + "\" because ",
					e);
		}
	}

	/**
	 * Save a set of FEM program parameters.
	 * @param progCfg
	 *            FEM program parameters set
	 */
	private void saveFemProgram(final ProgramDao progCfg) {
		FemProgramType ptype = progCfg.getProgram();
		props.put(ptype + ".path.executable", progCfg.getExecutablePath());
		props.put(ptype + ".file.template.step", progCfg.getTemplateDao().getStepTemplateFile());
		props.put(ptype + ".file.template.init", progCfg.getTemplateDao().getInitTemplateFile());
	}

	/**
	 * Save a configuration for a substructure.
	 * @param name
	 *            Name of substructure.
	 */
	private void saveSubStructure(final String name) {
		SubstructureDao config = femConfig.getSubstructCfgs().get(name);
		props.setProperty(name + ".dimension", config.getDimension().name());
		props.setProperty(name + ".control.nodes",
				eoIntegerList.encode(config.getNodeSequence()));
		for (Integer node : config.getNodeSequence()) {
			if (config.getEffectiveDofs(node) == null) {
				log.error("Node " + node + " from " + name
						+ " has no effective DOFs");
				continue;
			}
			props.setProperty(name + ".effective.dofs." + node,
					eoDispDofList.encode(config.getEffectiveDofs(node)));
		}
		props.setProperty(name + ".fem.program", config.getFemProgram().name());
		props.setProperty(name + ".source.files", eoStringList.encode(config.getSourcedFilenames()));
		props.setProperty(name + ".work.files",
				eoStringList.encode(config.getWorkFiles()));
		props.setProperty(name + ".tcp.port.disp",Integer.toString(config.getDispPort()));
		props.setProperty(name + ".tcp.port.forc",Integer.toString(config.getForcePort()));
	}

	/**
	 * @param configFilePath
	 *            the configFilePath to set
	 */
	public final void setConfigFilePath(final String configFilePath) {
		this.configFilePath = configFilePath;
	}

	/**
	 * @param femConfig
	 *            the femConfig to set
	 */
	public final void setFemConfig(final FemExecutorConfig femConfig) {
		this.femConfig = femConfig;
	}
}
