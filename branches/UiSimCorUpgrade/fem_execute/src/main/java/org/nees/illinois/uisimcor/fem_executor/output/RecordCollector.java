package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.execute.FemStatus;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects response records for an iteration step.
 * @author Michael Bletzinger
 */
public class RecordCollector {
	/**
	 * @return the responseVals
	 */
	public final ResponseValues getResponseVals() {
		return responseVals;
	}

	/**
	 * Record index to use for the response.
	 */
	private final int stepRecordIndex;
	/**
	 * Response values.
	 */
	private final ResponseValues responseVals;

	/**
	 * Listener for the displacements socket.
	 */
	private TcpListener dispListener;

	/**
	 * @param scfg
	 *            Substructure configuration.
	 * @param pcfg
	 *            FEM program configuration.
	 */
	public RecordCollector(final SubstructureDao scfg, final ProgramDao pcfg) {
		this.responseVals = new ResponseValues(scfg);
		this.stepRecordIndex = pcfg.getStepRecordIndex();

	}

	/**
	 * Reader for the disp socket.
	 */
	private TcpReader dispReader;

	/**
	 * Listener for the reaction socket.
	 */
	private TcpListener forceListener;

	/**
	 * Read forces from reaction link.
	 */
	private TcpReader forceReader;
	/**
	 * Displacement response records.
	 */
	private DoubleMatrix dispRecords;
	/**
	 * Force response records.
	 */
	private DoubleMatrix forceRecords;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(RecordCollector.class);

	/**
	 * Kill all of the TCP management threads.
	 */
	public final void abort() {
		dispListener.setQuit(true);
		forceListener.setQuit(true);
		dispListener.interrupt();
		forceListener.interrupt();
		if (dispReader == null) {
			return; // We were not running dynamic.
		}
		dispReader.setQuit(true);
		dispReader.interrupt();
		forceReader.setQuit(true);
		forceReader.interrupt();
	}

	/**
	 * Set up listeners for connect requests from the FEM analysis program.
	 * @param scfg
	 *            Substructure configuration associated with this FEM.
	 * @return true if nothing went wrong.
	 */
	public final boolean setup(final SubstructureDao scfg) {
		final int fiveSeconds = 5000;
		try {
			dispListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getDispPort(), fiveSeconds));
		} catch (IOException e) {
			log.error("Bind to displacements port " + scfg.getDispPort()
					+ " failed because ", e);
			return false;
		}
		dispListener.start();
		try {
			forceListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getForcePort(), fiveSeconds));
		} catch (IOException e) {
			log.error("Bind to displacements port " + scfg.getDispPort()
					+ " failed because ", e);
			return false;
		}
		forceListener.start();
		return true;
	}

	/**
	 * Connect to the sockets of the FEM analysis program.
	 * @return true if nothing went wrong.
	 */
	public final boolean connect() {
		final int tenSeconds = 10;
		TcpLinkDto link = null;
		try {
			link = dispListener.getConnections().poll(tenSeconds,
					TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link available on "
					+ dispListener.getParams().getLocalPort());
			return false;
		}
		try {
			dispReader = new TcpReader(link);
		} catch (IOException e1) {
			log.error("No link available on "
					+ dispListener.getParams().getLocalPort() + " because ", e1);
			return false;
		}
		dispReader.start();
		// dispListener.setQuit(true);
		// dispListener.interrupt();
		try {
			link = forceListener.getConnections().poll(tenSeconds,
					TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link available on "
					+ forceListener.getParams().getLocalPort());
			return false;
		}
		try {
			forceReader = new TcpReader(link);
		} catch (IOException e) {
			log.error("No link available on "
					+ forceListener.getParams().getLocalPort() + " because ", e);
			return false;
		}
		forceReader.start();
		return true;
	}

	/**
	 * Collect any displacement or force responses.
	 * @param statuses
	 *            Statuses reference.
	 */
	public final void checkResponses(final FemStatus statuses) {
		BlockingQueue<List<Double>> responses = dispReader.getDoublesQ();
		List<Double> rawDisp = responses.poll();
		while (rawDisp != null) {
			log.debug("Raw Displacements " + MtxUtils.list2String(rawDisp));
			if (dispRecords == null) {
				List<List<Double>> dm = new ArrayList<List<Double>>();
				dm.add(rawDisp);
				dispRecords = new DoubleMatrix(dm);
				statuses.setDisplacementsAreHere(true);
			} else {
				dispRecords.append(rawDisp);
			}
			rawDisp = responses.poll();
		}
		responses = forceReader.getDoublesQ();
		List<Double> rawForce = responses.poll();
		while (rawForce != null) {
			log.debug("Raw Forces " + MtxUtils.list2String(rawForce));
			if (forceRecords == null) {
				List<List<Double>> dm = new ArrayList<List<Double>>();
				dm.add(rawForce);
				forceRecords = new DoubleMatrix(dm);
				statuses.setForcesAreHere(true);
			} else {
				forceRecords.append(rawForce);
			}
			rawForce = responses.poll();
		}
	}

	/**
	 * Start the collection of response records.
	 */
	public final void start() {
		dispRecords = null;
		forceRecords = null;
	}

	/**
	 * Finish up the collection and put the correct record in the
	 * {@link ResponseValues response}.
	 */
	public final void finish() {
		List<List<Double>> dm = dispRecords.toList();
		int rows = dm.size();
		log.debug("Displacement response " + rows + " index " + (rows - (stepRecordIndex + 1)));
		responseVals.setRawDisp(dm.get(rows - (stepRecordIndex + 1)));
		dm = forceRecords.toList();
		rows = dm.size();
		log.debug("Force response " + rows + " index " + (rows - (stepRecordIndex + 1)));
		responseVals.setRawForce(dm.get(rows - (stepRecordIndex + 1)));
	}
}
