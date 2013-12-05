package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class acts a a container to collect responses from a substructure.
 * @author Michael Bletzinger
 */
public class ResponseValues {
	/**
	 * The complete set of records for a step including intermediate iterations.
	 */
	private DoubleMatrixI completeDisp;

	/**
	 * The complete set of records for a step including intermediate iterations.
	 */
	private DoubleMatrixI completeForce;
	/**
	 * Reformat the output for UI-SimCor.
	 */
	private final DataFormatter dformat;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(ResponseValues.class);
	/**
	 * Results of a step command.
	 */
	private List<Double> rawDisp;

	/**
	 * Results of a step command.
	 */
	private List<Double> rawForce;

	/**
	 * @param scfg
	 *            Substructure configuration which identifies the sequence of
	 *            values.
	 */
	public ResponseValues(final SubstructureDao scfg) {
		this.dformat = new DataFormatter(scfg);
	}

	/**
	 * @return the completeDisp
	 */
	public final DoubleMatrixI getCompleteDisp() {
		return completeDisp;
	}

	/**
	 * @return the completeForce
	 */
	public final DoubleMatrixI getCompleteForce() {
		return completeForce;
	}

	/**
	 * @return double array in node order of displacements at effective DOFs
	 */
	public final double[] getDisplacements() {
		List<Double> result = dformat.filter(rawDisp);
		log.debug("Filtered Displacements " + MtxUtils.list2String(result));
		return MtxUtils.list2Array(result);
	}

	/**
	 * @return double array in node order of reaction forces at effective DOFs
	 */
	public final double[] getForces() {
		List<Double> result = dformat.filter(rawForce);
		log.debug("Filtered Forces " + MtxUtils.list2String(result));
		return MtxUtils.list2Array(result);
	}

	/**
	 * @return the rawDisp
	 */
	public final List<Double> getRawDisp() {
		return rawDisp;
	}

	/**
	 * @return the rawForce
	 */
	public final List<Double> getRawForce() {
		return rawForce;
	}

	/**
	 * @param completeDisp the completeDisp to set
	 */
	public final void setCompleteDisp(DoubleMatrixI completeDisp) {
		this.completeDisp = completeDisp;
	}

	/**
	 * @param completeForce the completeForce to set
	 */
	public final void setCompleteForce(DoubleMatrixI completeForce) {
		this.completeForce = completeForce;
	}

	/**
	 * @param rawDisp
	 *            the rawDisp to set
	 */
	public final void setRawDisp(final List<Double> rawDisp) {
		this.rawDisp = rawDisp;
	}

	/**
	 * @param rawForce
	 *            the rawForce to set
	 */
	public final void setRawForce(final List<Double> rawForce) {
		this.rawForce = rawForce;
	}

}
