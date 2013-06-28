package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParser;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to execute an FEM program to statically analyze a substructure at one
 * step.
 * @author Michael Bletzinger
 */
public class StaticExecution extends SubstructureExecutor {

	/**
	 * Output file for displacements.
	 */
	private final String dispOutFile;
	/**
	 * Filename as first argument.
	 */
	private String filename;
	/**
	 * Output file for forces.
	 */
	private final String forceOutFile;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(StaticExecution.class);
	/**
	 * Flag indicating that output files need to be parsed after execution.
	 */
	private boolean processOutputFiles = true;

	/**
	 * @param progCfg
	 *            FEM program configuration parameters.
	 * @param scfg
	 *            Configuration for the substructure.
	 * @param configDir
	 *            Directory containing templates and configuration files..
	 * @param workDir
	 *            Directory to store temporary files.
	 */
	public StaticExecution(final ProgramDao progCfg,
			final SubstructureDao scfg, final String configDir,
			final String workDir) {
		super(progCfg, scfg, configDir, workDir);
		dispOutFile = PathUtils.append(workDir, "tmp_disp.out");
		forceOutFile = PathUtils.append(workDir, "tmp_forc.out");
		filename = "run.tcl";

	}

	@Override
	public final void abort() {
		getFem().abort();
	}

	@Override
	protected final void checkDisplacementResponse() {
		FemStatus statuses = getStatuses();
		if (statuses.isFemProcessHasDied() == false) {
			return;
		}
		File f = new File(dispOutFile);
		if (f.canRead() == false) {
			return;
		}
		if (f.length() > 0) {
			OutputFileParser ofp = new OutputFileParser();
			ofp.parseDataFile(dispOutFile);
			setRawDisp(ofp.getArchive());
			statuses.setDisplacementsAreHere(true);
		}
	}

	@Override
	protected final void checkForceResponse() {
		FemStatus statuses = getStatuses();
		if (statuses.isFemProcessHasDied() == false) {
			return;
		}
		File f = new File(forceOutFile);
		if (f.canRead() == false) {
			return;
		}
		if (f.length() > 0) {
			OutputFileParser ofp = new OutputFileParser();
			ofp.parseDataFile(forceOutFile);
			setRawForce(ofp.getArchive());
			statuses.setForcesAreHere(true);
		}
	}

	/**
	 * @return the filename
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the processOutputFiles
	 */
	public final boolean isProcessOutputFiles() {
		return processOutputFiles;
	}

	@Override
	public final boolean iveGotProblems() {
		FemStatus statuses = getStatuses();
		return statuses.isFemProcessHasErrors();
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public final void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * @param processOutputFiles
	 *            the processOutputFiles to set
	 */
	public final void setProcessOutputFiles(final boolean processOutputFiles) {
		this.processOutputFiles = processOutputFiles;
	}

	@Override
	public final boolean setup() {
		return true;
	}

	@Override
	public final boolean startSimulation() {
		return true;
	}

	@Override
	public final void startStep(final int step, final double[] displacements) {
		String run = getScriptGen().generateRun(step, displacements);
		writeInputFile(run);
		ProcessExecution fem = getFem();
		fem.getProcess().addArg(filename);
		fem.start();
	}

	/**
	 * Write the input file content to a file.
	 * @param content
	 *            The content.
	 */
	private void writeInputFile(final String content) {
		String inputFilePath = PathUtils.append(getWorkDir(), filename);
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
