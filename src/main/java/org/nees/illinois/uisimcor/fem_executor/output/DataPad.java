package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
/**
 * Add double 0.0 elements to DOFs that are not effective.
 * @author Michael Bletzinger
 *
 */
public class DataPad {
	/**
	 * Substructure configuration containing the nodes and effective DOFs for
	 * the module.
	 */
	private final SubstructureConfig substructCfg;

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
		int count = 0;
		final int numSimCorDofs = 6;
		for (Integer n : substructCfg.getNodeSequence()) {
			int lastEDof = 0;
			List<Double> row = new ArrayList<Double>();
			for (DispDof d : substructCfg.getEffectiveDofs(n)) {
				int idx = d.ordinal();
				for (int j = lastEDof + 1; j < idx; j++) {
					row.add(0.0);
					count++;
				}
				row.add(data.get(count));
				count++;
				lastEDof = idx;
			}
			while(lastEDof < numSimCorDofs - 1) {
				row.add(0.0);
				lastEDof++;
			}
			result.add(row);
		}
		return new DoubleMatrix(result);
	}
}
