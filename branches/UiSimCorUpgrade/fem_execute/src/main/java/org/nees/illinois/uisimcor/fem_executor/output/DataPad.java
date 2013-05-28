package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add double 0.0 elements to DOFs that are not effective.
 * @author Michael Bletzinger
 */
public class DataPad {
	/**
	 * Substructure configuration containing the nodes and effective DOFs for
	 * the module.
	 */
	private final SubstructureConfig substructCfg;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DataPad.class);

	/**
	 * @param substructCfg
	 *            Substructure configuration containing the nodes and effective
	 *            DOFs for the module.
	 */
	public DataPad(final SubstructureConfig substructCfg) {
		this.substructCfg = substructCfg;
	}

	/**
	 * add 0.0 for each displacement that is not an effective DOF.
	 * @param data
	 *            Effective DOF values.
	 * @return Padded row of data.
	 */
	public final DoubleMatrix pad(final List<Double> data) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		int nodeCount = 0;
		final DofIndexMagic openseesMagic = new DofIndexMagic(
				substructCfg.getDimension(), false, false);
		final DofIndexMagic uisimcorMagic = new DofIndexMagic(
				substructCfg.getDimension(), false, true);
		for (Integer n : substructCfg.getNodeSequence()) {
			int lastEffectiveDof = 0;
			List<Double> row = new ArrayList<Double>();
			for (DispDof d : substructCfg.getEffectiveDofs(n)) {
				int uisimcorIdx;
				try {
					uisimcorIdx = uisimcorMagic.index(d);
				} catch (IllegalParameterException e) {
					log.error("Substructure " + substructCfg.getAddress()
							+ " is misconfigured");
					return null;
				}
				for (int j = lastEffectiveDof + 1; j < uisimcorIdx; j++) {
					row.add(0.0);
				}
				try {
					row.add(data.get(nodeCount * openseesMagic.numberOfDofs() + openseesMagic.index(d)));
				} catch (IllegalParameterException e) {
					log.error("Misconfigured substructure " + substructCfg.getAddress());
					return null;
				}
				lastEffectiveDof = uisimcorIdx;
			}
			while (lastEffectiveDof < uisimcorMagic.numberOfDofs() - 1) {
				row.add(0.0);
				lastEffectiveDof++;
			}
			result.add(row);
			nodeCount++;
		}
		return new DoubleMatrix(result);
	}
}
