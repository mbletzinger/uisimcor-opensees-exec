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
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
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
		List<String> names;
		try {
			names = eoStringList.parse(props.getProperty("substructures"),
					"substructures");
		} catch (IllegalParameterException e) {
			log.error(configFilePath
					+ " has a substructures line that cannot be parsed");
			return;
		}
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
		StringDecoder decodeS = new StringDecoder();
		IntegerDecoder decodeI = new IntegerDecoder();
		String label = ptype + ".file.template.step";
		String stepT = decodeS.parse(props.getProperty(label), label);
		if (stepT == null) {
			return null;
		}
		label = ptype + ".file.template.init";
		String initT = decodeS.parse(props.getProperty(label), label);
		if (initT == null) {
			return null;
		}
		label = ptype + ".file.template.run";
		String runT = decodeS.parse(props.getProperty(label), label);
		if (runT == null) {
			return null;
		}
		TemplateDao tdao = new TemplateDao(stepT, initT, runT);
		label = ptype + ".path.executable";
		String executable = decodeS.parse(props.getProperty(label), label);
		if (executable == null) {
			return null;
		}
		label = ptype + ".step.record.index";
		int index = decodeI.parse(props.getProperty(label), label);
		ProgramDao result = new ProgramDao(executable, ptype, tdao, index);
		return result;
	}

	/**
	 * Load a configuration for a substructure.
	 * @param name
	 *            Name of substructure.
	 * @return Configuration data.
	 */
	private SubstructureDao loadSubStructure(final String name) {
		String label = name + ".dimension";
		DimensionTypeDecoder decodeDim = new DimensionTypeDecoder();
		DimensionType dim = decodeDim.parse(props.getProperty(label), label);
		label = name + ".control.nodes";
		List<Integer> nodes = null;
		try {
			nodes = eoIntegerList.parse(props.getProperty(label), label);
		} catch (Exception e) {
			log.error("Control node list not recognized for " + name, e);
		}
		label = name + ".fem.program";
		FemProgramTypeDecoder decodeF = new FemProgramTypeDecoder();
		FemProgramType fem = decodeF.parse(props.getProperty(label), label);
		label = name + ".work.files";
		List<String> wfiles = null;
		try {
			wfiles = eoStringList.parse(props.getProperty(label), label);
		} catch (Exception e) {
			log.error("Work files list not recognized for " + name, e);
		}
		label = name + ".source.files";
		List<String> sfiles = null;
		try {
			sfiles = eoStringList.parse(props.getProperty(label), label);
		} catch (Exception e) {
			log.error("Source files list not recognized for " + name, e);
		}
		final IntegerDecoder id = new IntegerDecoder();
		label = name + ".tcp.port.disp";
		int dport = id.parse(props.getProperty(label), label);
		label = name + ".tcp.port.forc";
		int fport = id.parse(props.getProperty(label), label);

		SubstructureDao result = new SubstructureDao(name, dim, fem, sfiles,
				nodes, wfiles, dport, fport);
		for (Integer node : nodes) {
			label = name + ".effective.dofs." + node;
			List<DispDof> edofs = null;
			try {
				edofs = eoDispDofList.parse(props.getProperty(label), label);
				result.addEffectiveDofs(node, edofs);
			} catch (Exception e) {
				log.error("Effective DOF list not recognized for node " + node
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
		for (ProgramDao fpCfg : femConfig.getFemProgramParameters().values()) {
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
		props.put(ptype + ".file.template.step", progCfg.getTemplateDao()
				.getStepTemplateFile());
		props.put(ptype + ".file.template.init", progCfg.getTemplateDao()
				.getInitTemplateFile());
		props.put(ptype + ".file.template.run", progCfg.getTemplateDao()
				.getRunTemplateFile());
		props.put(ptype + ".step.record.index", Integer.toString(progCfg.getStepRecordIndex()));
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
		props.setProperty(name + ".source.files",
				eoStringList.encode(config.getSourcedFilenames()));
		props.setProperty(name + ".work.files",
				eoStringList.encode(config.getWorkFiles()));
		props.setProperty(name + ".tcp.port.disp",
				Integer.toString(config.getDispPort()));
		props.setProperty(name + ".tcp.port.forc",
				Integer.toString(config.getForcePort()));
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
