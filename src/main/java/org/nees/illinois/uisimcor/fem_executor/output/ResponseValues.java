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
	 * Reformat the output for UI-SimCor.
	 */
	private final DataFormatter dformat;

	/**
	 * Results of a step command.
	 */
	private List<Double> rawDisp;
	/**
	 * Results of a step command.
	 */
	private List<Double> rawForce;

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

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(ResponseValues.class);

	/**
	 * @param scfg
	 *            Substructure configuration which identifies the sequence of
	 *            values.
	 */
	public ResponseValues(final SubstructureDao scfg) {
		this.dformat = new DataFormatter(scfg);
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

}
