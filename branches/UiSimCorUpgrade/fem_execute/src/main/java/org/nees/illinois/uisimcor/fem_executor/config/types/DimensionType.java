package org.nees.illinois.uisimcor.fem_executor.config.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration specifying the dimensional space of the model.
 * @author Michael Bletzinger
 */
public enum DimensionType {
	/**
	 * 3D.
	 */
	ThreeD,
	/**
	 * 2D.
	 */
	TwoD;
	/**
	 * Returns a list of indexes corresponding to the number of DOFs in the
	 * dimension. The indexes are zero-based.
	 * @return List of indexes.
	 */
	public final int[] indexes() {
		final int[] dof6 = { 0, 1, 2, 3, 4, 5 };
		final int[] dof3 = { 0, 1, 2 };
		if (this.equals(TwoD)) {
			return dof3;
		}
		return dof6;
	}

	/**
	 * Return the list of DOFs corresponding to this dimension.
	 * @return List of DOFs.
	 */
	public final List<DispDof> dofs() {
		final DispDof[] dof6 = { DispDof.DX, DispDof.DY, DispDof.DZ,
				DispDof.RX, DispDof.RY, DispDof.RZ };
		final DispDof[] dof3 = { DispDof.DX, DispDof.DY, DispDof.RZ };
		if (this.equals(TwoD)) {
			return array2List(dof3);
		}
		return array2List(dof6);
	}

	/**
	 * Convert array of DOFs to a list.
	 * @param dofs
	 *            array.
	 * @return list.
	 */
	private List<DispDof> array2List(final DispDof[] dofs) {
		List<DispDof> result = new ArrayList<DispDof>();
		for (DispDof d : dofs) {
			result.add(d);
		}
		return result;
	}
}
