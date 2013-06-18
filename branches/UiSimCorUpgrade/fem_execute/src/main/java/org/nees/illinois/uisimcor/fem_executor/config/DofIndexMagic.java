package org.nees.illinois.uisimcor.fem_executor.config;

import org.nees.illinois.uisimcor.fem_executor.config.types.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;

/**
 * Maps the myriad of possible indexes for DOFs into one class.
 * @author Michael Bletzinger
 */
public class DofIndexMagic {
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "DofIndexMagic=" + dim + (isMatlab ? "/isMatlab" : "")
				+ (isUiSimCor ? "/isUiSimCor" : "");
	}

	/**
	 * Dimension of DOF space.
	 */
	private final DimensionType dim;
	/**
	 * Flag for a MATLAB index.
	 */
	private final boolean isMatlab;
	/**
	 * Flag if for UI-SimCor.
	 */
	private final boolean isUiSimCor;
	/**
	 * Number of DOFs for a 2 dimensional simulation.
	 */
	private final int numDofs2D = 3;
	/**
	 * Number of DOFs for a 3 dimensional simulation.
	 */
	private final int numDofs3D = 6;

	/**
	 * @param dim
	 *            Dimension of DOF space.
	 * @param isMatlab
	 *            Flag for a MATLAB index.
	 * @param isUiSimCor
	 *            Flag if for UI-SimCor.
	 */
	public DofIndexMagic(final DimensionType dim, final boolean isMatlab,
			final boolean isUiSimCor) {
		this.dim = dim;
		this.isMatlab = isMatlab;
		this.isUiSimCor = isUiSimCor;
	}

	/**
	 * Returns the index of a DOF based on the settings of the class.
	 * @param dof
	 *            DOF.
	 * @return The index.
	 * @throws IllegalParameterException
	 *             DOF is not part of the dimensional space.
	 */
	public final int index(final DispDof dof) throws IllegalParameterException {
		int result = dof.ordinal();
		if (isUiSimCor) {
			result += (isMatlab ? 1 : 0);
			return result;
		}
		if (dim.equals(DimensionType.ThreeD)) {
			result += (isMatlab ? 1 : 0);
			return result;
		}
		// Mapping must be 2D for OpenSEES.
		// Check for Rz
		if (dof.equals(DispDof.RZ)) {
			result = 2;
			result += (isMatlab ? 1 : 0);
			return result;
		}
		// Check for out-of-bounds DOFs
		if (result > 1) {
			throw new IllegalParameterException(dof + " is not a 2D DOF");
		}
		result += (isMatlab ? 1 : 0);
		return result;
	}

	/**
	 * Return the number of DOFs based on the settings of the class.
	 * @return The number of DOFs.
	 */
	public final int numberOfDofsPerNode() {
		if (isUiSimCor) {
			return numDofs3D;
		}
		if (dim.equals(DimensionType.ThreeD)) {
			return numDofs3D;
		}
		return numDofs2D;
	}
}
