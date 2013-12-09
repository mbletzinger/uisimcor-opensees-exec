package org.nees.illinois.uisimcor.opensees.test_utils.dao;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.output.DoubleMatrixI;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;

/**
 * Encapsulates the mass matrix for the node.
 * @author Michael Bletzinger
 */
public class NodeMasses {
	/**
	 * Matrix of masses for the DOFs of the node.
	 */
	private final DoubleMatrixI masses;

	/**
	 * @param masses
	 *            Matrix of masses for the DOFs of the node.
	 */
	public NodeMasses(final DoubleMatrixI masses) {
		this.masses = masses;
	}

	/**
	 * @return an array of indexes for DOFs that have mass.
	 */
	public final int[] getMassedDofIndexes() {
		int[] sz = masses.sizes();
		List<Integer> result = new ArrayList<Integer>();
		for (int r = 0; r < sz[0]; r++) {
			for (int c = 0; c < sz[1]; c++) {
				if (masses.value(r, c) != 0) {
					result.add(new Integer(r));
					break;
				}
			}
		}
		return MtxUtils.list2IntArray(result);
	}

	/**
	 * @return a list of DOFs that have mass.
	 */
	public final List<DofLabel> getMassedDofs() {
		int[] indexes = getMassedDofIndexes();
		List<DofLabel> result = new ArrayList<DofLabel>();
		for (int i : indexes) {
			result.add(DofLabel.index2Enum(i));
		}
		return result;
	}

	/**
	 * @return true if the node has at least one DOF with mass.
	 */
	public final boolean hasMass() {
		int[] indexes = getMassedDofIndexes();
		return indexes.length > 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "/masses=" + masses;
	}
}
