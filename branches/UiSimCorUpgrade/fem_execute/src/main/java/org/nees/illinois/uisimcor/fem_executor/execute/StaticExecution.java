package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParser;
import org.nees.illinois.uisimcor.fem_executor.utils.LogMessageWithCounter;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

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
	 * Logger with counter to down-scale the message frequency.
	 */
	private final LogMessageWithCounter logC = new LogMessageWithCounter(10, log, Level.DEBUG);

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
			logC.log("\"" + dispOutFile + "\" does not exist yet");
			return;
		}
		if (f.length() > 0) {
			OutputFileParser ofp = new OutputFileParser();
			ofp.parseDataFile(dispOutFile);
			setRawDisp(ofp.getArchive());
			statuses.setDisplacementsAreHere(true);
			f.delete();
		}
		logC.log("\"" + dispOutFile + "\" has nothing in it yet");
	}

	@Override
	protected final void checkForceResponse() {
		FemStatus statuses = getStatuses();
		if (statuses.isFemProcessHasDied() == false) {
			return;
		}
		File f = new File(forceOutFile);
		if (f.canRead() == false) {
			logC.log("\"" + forceOutFile + "\" does not exist yet");
			return;
		}
		if (f.length() > 0) {
			OutputFileParser ofp = new OutputFileParser();
			ofp.parseDataFile(forceOutFile);
			setRawForce(ofp.getArchive());
			statuses.setForcesAreHere(true);
			f.delete();
		}
		logC.log("\"" + forceOutFile + "\" has nothing in it yet");
	}

	/**
	 * @return the filename
	 */
	public final String getFilename() {
		return filename;
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
