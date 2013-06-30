package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
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
public class StaticExecution implements SubstructureExecutorI {

	/**
	 * Response values.
	 */
	private final ResponseValues responseVals;

	/**
	 * FEM execution management.
	 */
	private final ProcessExecution exec;
	/**
	 * FEM execution script generator.
	 */
	private final ScriptGeneratorI scriptGen;
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
	 * Directory the process is executing in.
	 */
	private final String workDir;

	/**
	 * Configuration of the substructure.
	 */
	private final SubstructureDao scfg;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(StaticExecution.class);

	/**
	 * Logger with counter to down-scale the message frequency.
	 */
	private final LogMessageWithCounter logC = new LogMessageWithCounter(10,
			log, Level.DEBUG);

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
		final int quarterSecond = 250;
		this.exec = new ProcessExecution(progCfg, workDir, quarterSecond, false);
		this.responseVals = new ResponseValues(scfg);
		this.scriptGen = new OpenSeesSG(configDir, scfg, progCfg.getTemplateDao());
		this.dispOutFile = PathUtils.append(workDir, "tmp_disp.out");
		this.forceOutFile = PathUtils.append(workDir, "tmp_forc.out");
		this.filename = "run.tcl";
		this.workDir = workDir;
		this.scfg = scfg;

	}

	@Override
	public final void abort() {
		exec.abort();
	}

	/**
	 * Check the displacement output file and set statuses.
	 */
	private void checkDisplacementResponse() {
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
			abort();
			OutputFileParser ofp = new OutputFileParser();
			ofp.parseDataFile(dispOutFile);
			responseVals.setRawDisp(ofp.getArchive());
			statuses.setDisplacementsAreHere(true);
			f.delete();
		}
		logC.log("\"" + dispOutFile + "\" has nothing in it yet");
	}

	/**
	 * Check the force output file and set statuses.
	 */
	private void checkForceResponse() {
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
			responseVals.setRawForce(ofp.getArchive());
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
		String run = scriptGen.generateRun(step, displacements);
		writeInputFile(run);
		exec.getProcess().addArg(filename);
		exec.start();
	}

	/**
	 * Write the input file content to a file.
	 * @param content
	 *            The content.
	 */
	private void writeInputFile(final String content) {
		String inputFilePath = PathUtils.append(workDir, filename);
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
	/*
	 * (non-Javadoc)
	 * @see
	 * org.nees.illinois.uisimcor.fem_executor.execute.SubstructurerExecutorI
	 * #stepIsDone()
	 */
	@Override
	public final boolean stepIsDone() {
		FemStatus statuses = exec.getStatuses();
		checkDisplacementResponse();
		checkForceResponse();
		exec.checkIfProcessIsAlive(statuses);
		exec.checkStepCompletion(statuses);
		exec.checkForErrors(statuses);
		if (statuses.isChanged()) {
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
			logC.reset();
		}
		logC.log(scfg.getAddress() + " Is " + statuses.getStatus());
		return statuses.responsesHaveArrived();
	}

	@Override
	public final double[] getDisplacements() {
		return responseVals.getDisplacements();
	}

	@Override
	public final double[] getForces() {
		return responseVals.getForces();
	}

	@Override
	public final FemStatus getStatuses() {
		return exec.getStatuses();
	}
}
