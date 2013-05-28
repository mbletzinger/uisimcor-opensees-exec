/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which customizes the FEM input file for a particular step.
 * @author Michael Bletzinger
 */
public class FemInputFile {
	/**
	 * Path to the directory containing the configuration files.
	 */
	private final String configDir;

	/**
	 * Format for displacement commands.
	 */
	private DecimalFormat format = new DecimalFormat(
			"###.00000000000000000000E000");

	/**
	 * Name of the input file.
	 */
	private final String inputFileName = "run.tcl";

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(FemInputFile.class);

	/**
	 * token name.
	 */
	private final String modelFileToken = "ModelFile";

	/**
	 * token name.
	 */
	private final String staticAnalysisFileToken = "StaticAnalysisFile";
	/**
	 * token name.
	 */
	private final String nodeListToken = "NodeList";

	/**
	 * Substructure configuration parameters.
	 */
	private final SubstructureConfig substructureCfg;
	/**
	 * run.tcl template string.
	 */
	private final String template;
	/**
	 * Map of tokens to substitute in run.tcl template.
	 */
	private final Map<String, String> tokenMap = new HashMap<String, String>();
	/**
	 * Path to the directory containing the input file.
	 */
	private final String workDir;

	/**
	 * Constructor.
	 * @param progCfg
	 *            FEM program parameters.
	 * @param substructureCfg
	 *            Substructure parameters.
	 * @param workDir
	 *            Directory containing generated files.
	 * @param configDir
	 *            Root of directory containing the model configuration files.
	 */
	public FemInputFile(final FemProgramConfig progCfg,
			final SubstructureConfig substructureCfg, final String workDir,
			final String configDir) {
		this.substructureCfg = substructureCfg;
		tokenMap.put(modelFileToken, substructureCfg.getModelFileName());
		tokenMap.put(staticAnalysisFileToken,
				progCfg.getStaticAnalysisScriptPath());
		String rDofs = (substructureCfg.getDimension() == DimensionType.TwoD ? "1 2 3"
				: "1 2 3 4 5 6");
		tokenMap.put("ResponseDofs", rDofs);
		String nodes = "";
		boolean first = true;
		for (Integer n : substructureCfg.getNodeSequence()) {
			nodes += (first ? "" : " ") + n;
			first = false;
		}
		tokenMap.put(nodeListToken, nodes);
		String rawTemplate = setTemplate(PathUtils.append(configDir,
				"run_template.tcl"));
		for (String k : tokenMap.keySet()) {
			rawTemplate = rawTemplate.replaceAll("\\$\\{" + k + "\\}",
					tokenMap.get(k));
		}
		template = rawTemplate;
		this.workDir = PathUtils.append(workDir, substructureCfg.getAddress());
		this.configDir = configDir;
		createWorkDir();
	}

	/**
	 * Creates the working directory for the substructure.
	 */
	private void createWorkDir() {
		File workDirF = new File(workDir);
		if (workDirF.exists() && (workDirF.isDirectory() == false)) {
			log.error("Cannot create working directory \"" + workDir + "\"");
			return;
		}
		try {
			workDirF.mkdirs();
			log.debug("\"" + workDir + "\" was created");
		} catch (Exception e) {
			log.error("Cannot create working directory \"" + workDir
					+ "\" because ", e);
			return;
		}

		String[] tokens = { modelFileToken, staticAnalysisFileToken };
		for (String t : tokens) {
			String f = tokenMap.get(t);
			try {
				PathUtils.cp(f, configDir, workDir);
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + workDir + "\" because ", e);
				return;
			}
		}
		for (String f : substructureCfg.getWorkFiles()) {
			try {
				PathUtils.cp(f, configDir, workDir);
			} catch (IOException e) {
				log.error(
						"Cannot copy file \"" + PathUtils.append(configDir, f)
								+ " \" to \"" + workDir + "\" because ", e);
				return;
			}
		}
	}

	/**
	 * Converts double array to a string for logger messages.
	 * @param array
	 *            array to convert.
	 * @return resulting string.
	 */
	private String doubleArray2String(final double[] array) {
		String result = "[";
		boolean first = true;
		for (double n : array) {
			result += (first ? "" : ",") + n;
			first = false;
		}
		result += "]";
		return result;
	}

	/**
	 * Generate a run.tcl file for the step.
	 * @param step
	 *            Step number.
	 * @param displacements
	 *            Matrix of displacements at the effective DOFs.
	 */
	public final void generate(final int step, final double[] displacements) {
		final String stepK = "StepNumber";
		final String loadK = "LoadPattern";
		final int openSeesUpperBound = 99000;
		String content = template.replaceAll("\\$\\{" + stepK + "\\}",
				Integer.toString(openSeesUpperBound + step));
		String load;
		try {
			load = generateLoadPattern(displacements);
		} catch (IllegalParameterException e) {
			log.error("Could not create displacement command for "
					+ substructureCfg.getAddress() + " because ", e);
			return;
		}
		content = content.replaceAll("\\$\\{" + loadK + "\\}", load);
		writeInputFile(content);
	}

	/**
	 * Generates the load pattern for a step.
	 * @param displacements
	 *            Displacements for the step.
	 * @return Load pattern string.
	 * @throws IllegalParameterException
	 *             For improper effective DOFs.
	 */
	private String generateLoadPattern(final double[] displacements)
			throws IllegalParameterException {
		String result = "";
		int cnt = 0;
		DofIndexMagic magic = new DofIndexMagic(substructureCfg.getDimension(), true, false);
		log.debug("Encoding Substructure " + substructureCfg + " with "
				+ doubleArray2String(displacements));
		for (Integer n : substructureCfg.getNodeSequence()) {
			for (DispDof d : substructureCfg.getEffectiveDofs(n)) {
				result += "sp " + n + " "
						+ magic.index(d) + " "
						+ format.format(displacements[cnt]) + "\n";
				cnt++;
			}
		}
		return result;
	}

	/**
	 * @return the format
	 */
	public final DecimalFormat getFormat() {
		return format;
	}

	/**
	 * @return the inputFileName
	 */
	public final String getInputFileName() {
		return inputFileName;
	}

	/**
	 * @return the substructureCfg
	 */
	public final SubstructureConfig getSubstructureCfg() {
		return substructureCfg;
	}

	/**
	 * Return a token value.
	 * @param name
	 *            Name of token.
	 * @return Token value.
	 */
	public final String getToken(final String name) {
		return tokenMap.get(name);
	}

	/**
	 * @return the working directory
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public final void setFormat(final DecimalFormat format) {
		this.format = format;
	}

	/**
	 * Get a string representation of the run.tcl template file.
	 * @param file
	 *            Path to template file.
	 * @return string representation of the template file.
	 */
	private String setTemplate(final String file) {
		File tfile = new File(file);
		if (tfile.canRead() == false) {
			log.error("Template file \"" + file + "\" cannot be read");
			System.exit(1);
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
			log.error("Template file \"" + file + "\" cannot be read because ",
					e);
			System.exit(1);
		}
		return result;
	}

	/**
	 * Write the input file content to a file.
	 * @param content
	 *            The content.
	 */
	private void writeInputFile(final String content) {
		String inputFilePath = PathUtils.append(workDir, inputFileName);
		File inputF = new File(inputFilePath);
		if (inputF.exists()) {
			inputF.delete();
		}
		PrintWriter os = null;
		try {
			os = new PrintWriter(new FileWriter(inputFilePath));
		} catch (IOException e) {
			log.error("Run file \"" + inputFilePath
					+ "\" cannot be created because ", e);
			return;
		}
		os.print(content);
		os.close();
	}
}

// source ${ModelFile}
// source ${StaticAnalysisFile}
// recorder Node -file tmp_disp.out -node -dof ${ResponseDofs} disp
// recorder Node -file tmp_forc.out -node -dof ${ResponseDofs} reaction
// pattern Plain ${StepNumber} Constant {
// ${LoadPattern}
// }
// analyze 1
// remove loadPattern ${StepNumber}

// Load pattern:
// sp ${nodeTag} ${effectiveDofTag} ${displacementAtStep}
