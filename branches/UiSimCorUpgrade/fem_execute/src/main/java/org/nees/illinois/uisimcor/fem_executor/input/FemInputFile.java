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
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which customizes the FEM input file for a particular step.
 * @author Michael Bletzinger
 */
public class FemInputFile {
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
		tokenMap.put("ModelFile",
				PathUtils.append(configDir, substructureCfg.getModelFileName()));
		tokenMap.put(
				"StaticAnalysisFile",
				PathUtils.append(configDir,
						progCfg.getStaticAnalysisScriptPath()));
		String rDofs = (substructureCfg.getDimension() == DimensionType.TwoD ? "1 2 3"
				: "1 2 3 4 5 6");
		tokenMap.put("ResponseDofs", rDofs);
		String rawTemplate = setTemplate(PathUtils.append(configDir,
				"run_template.tcl"));
		for (String k : tokenMap.keySet()) {
			rawTemplate = rawTemplate.replaceAll("\\$\\{" + k + "\\}",
					tokenMap.get(k));
		}
		template = rawTemplate;
		this.workDir = PathUtils.append(workDir, substructureCfg.getAddress());
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
	}

	/**
	 * Generate a run.tcl file for the step.
	 * @param step
	 *            Step number.
	 * @param displacements
	 *            Matrix of displacements at the effective DOFs.
	 */
	public final void generate(final int step, final DoubleMatrix displacements) {
		final String stepK = "StepNumber";
		final String loadK = "LoadPattern";
		String content = template.replaceAll("\\$\\{" + stepK + "\\}", "Step"
				+ step);
		String load = generateLoadPattern(displacements);
		content = content.replaceAll("\\$\\{" + loadK + "\\}", load);
		writeInputFile(content);
	}

	/**
	 * Generates the load pattern for a step.
	 * @param displacements
	 *            Displacements for the step.
	 * @return Load pattern string.
	 */
	private String generateLoadPattern(final DoubleMatrix displacements) {
		String result = "";
		int row = 0;
		for (Integer n : substructureCfg.getNodeSequence()) {
			for (DispDof d : substructureCfg.getEffectiveDofs(n)) {
				result += "sp " + n + " " + d.mtlb() + " "
						+ format.format(displacements.value(row, d.ordinal()))
						+ "\n";
			}
			row++;
		}
		return result;
	}

	/**
	 * @return the inputFileName
	 */
	public final String getInputFileName() {
		return inputFileName;
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
	 * @return the working directory
	 */
	public final String getWorkDir() {
		return workDir;
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
