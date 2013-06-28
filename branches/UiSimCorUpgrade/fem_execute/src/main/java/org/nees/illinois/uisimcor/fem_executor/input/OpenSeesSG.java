package org.nees.illinois.uisimcor.fem_executor.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.TemplateDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates OpenSees script fragments used to run a segmented simulation.
 * @author Michael Bletzinger
 */
public class OpenSeesSG implements ScriptGeneratorI {
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
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(OpenSeesSG.class);

	/**
	 * token name.
	 */
	private final String nodeListToken = "NodeList";
	/**
	 * token name.
	 */
	private final String sourcedFilesToken = "SourcedFiles";

	/**
	 * Template for the step execution script.
	 */
	private final String stepTemplate;

	/**
	 * Template for the run once script.
	 */
	private final String runTemplate;

	/**
	 * Substructure configuration parameters.
	 */
	private final SubstructureDao substructureCfg;

	/**
	 * Filenames for all of the templates used.
	 */
	private final TemplateDao templateFiles;
	/**
	 * Map of tokens to substitute in templates.
	 */
	private final Map<String, String> tokenMap = new HashMap<String, String>();

	/**
	 * Constructor.
	 * @param configDir
	 *            Path to the directory containing the configuration files.
	 * @param substructureCfg
	 *            Substructure configuration parameters.
	 * @param templateFiles
	 *            Filenames for all of the templates used.
	 */
	public OpenSeesSG(final String configDir,
			final SubstructureDao substructureCfg,
			final TemplateDao templateFiles) {
		this.configDir = configDir;
		this.substructureCfg = substructureCfg;
		this.templateFiles = templateFiles;
		this.stepTemplate = setTemplate(PathUtils.append(configDir,
				this.templateFiles.getStepTemplateFile()));
		this.runTemplate = setTemplate(PathUtils.append(configDir,
				this.templateFiles.getRunTemplateFile()));
		String sourced = "";
		for (String f : this.substructureCfg.getSourcedFilenames()) {
			sourced += "source " + f + "\n";
		}
		tokenMap.put(sourcedFilesToken, sourced);
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
		tokenMap.put("DispPort",
				Integer.toString(substructureCfg.getDispPort()));
		tokenMap.put("ForcePort",
				Integer.toString(substructureCfg.getForcePort()));
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

	@Override
	public final String generateInit() {
		String result = setTemplate(PathUtils.append(configDir,
				templateFiles.getInitTemplateFile()));
		for (String k : tokenMap.keySet()) {
			result = result.replaceAll("\\$\\{" + k + "\\}", tokenMap.get(k));
		}
		log.debug("Generated Init for  " + substructureCfg.getAddress() + " ["
				+ result + "]");
		return result;
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
		DimensionType dim = substructureCfg.getDimension();
		DofIndexMagic magic = new DofIndexMagic(dim, true, false);
		log.debug("Encoding Substructure " + substructureCfg + " with "
				+ doubleArray2String(displacements));
		for (Integer n : substructureCfg.getNodeSequence()) {
			List<DispDof> edofs = substructureCfg.getEffectiveDofs(n);
			for (DispDof d : edofs) {
				double val = displacements[cnt];
				cnt++;
				result += "sp " + n + " " + magic.index(d) + " "
						+ format.format(val) + "\n";
			}
		}
		return result;
	}

	@Override
	public final String generateRun(final int step, final double[] displacements) {
		final String stepK = "StepNumber";
		final String loadK = "LoadPattern";
		final int openSeesUpperBound = 99000;
		tokenMap.put("Step", Integer.toString(step));
		String result = runTemplate.replaceAll("\\$\\{" + stepK + "\\}",
				Integer.toString(openSeesUpperBound + step));
		for (String k : tokenMap.keySet()) {
			result = result.replaceAll("\\$\\{" + k + "\\}", tokenMap.get(k));
		}
		String load;
		try {
			load = generateLoadPattern(displacements);
		} catch (IllegalParameterException e) {
			log.error("Could not create displacement command for "
					+ substructureCfg.getAddress() + " because ", e);
			return null;
		}
		result = result.replaceAll("\\$\\{" + loadK + "\\}", load);
		log.debug("Generated run step for  " + substructureCfg.getAddress()
				+ " [" + result + "]");
		return result;
	}

	@Override
	public final String generateStep(final int step,
			final double[] displacements) {
		final String stepK = "StepNumber";
		final String loadK = "LoadPattern";
		final int openSeesUpperBound = 99000;
		tokenMap.put("Step", Integer.toString(step));
		String result = stepTemplate.replaceAll("\\$\\{" + stepK + "\\}",
				Integer.toString(openSeesUpperBound + step));
		String load;
		try {
			load = generateLoadPattern(displacements);
		} catch (IllegalParameterException e) {
			log.error("Could not create displacement command for "
					+ substructureCfg.getAddress() + " because ", e);
			return null;
		}
		result = result.replaceAll("\\$\\{" + loadK + "\\}", load);
		log.debug("Generated step for  " + substructureCfg.getAddress() + " ["
				+ result + "]");
		return result;
	}

	/**
	 * Get a string representation of a template file.
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
}
