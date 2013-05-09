package org.nees.illinois.uisimcor.fem_executor.config;

import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;

/**
 * Enumeration of Cartesian Degrees of Freedom (DOF).
 * @author Michael Bletzinger
 *
 */
public enum DispDof {
	/**
	 * Shear displacement.
	 */
	DX,
	/**
	 * Out-of-plane displacement.
	 */
	DY,
	/**
	 * Axial displacement.
	 */
	DZ,
	/**
	 * Out-of-plane rotation.
	 */
	RX,
	/**
	 * Overturning rotation.
	 */
	RY,
	/**
	 * Torque rotation.
	 */
	RZ;
	/**
	 * Returns the MATLAB 1-based index for the DOF.
	 * @param dim Dimension of the simulation.
	 *@return
	 *Index + 1
	 * @throws IllegalParameterException 
	 */
	public final int mtlb(DimensionType dim) throws IllegalParameterException {
		int result = this.ordinal() + 1;
		if(dim.equals(DimensionType.TwoD) && (result > 3) ) {
			if(result == 6) {
				return 3; // RZ is the 3rd DOF in 2D.
			}
			throw new IllegalParameterException("Dimension" + this + " not allowed for 2D simulations.");
		}
		return result;
	}

}
