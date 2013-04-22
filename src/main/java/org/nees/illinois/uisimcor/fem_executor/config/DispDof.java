package org.nees.illinois.uisimcor.fem_executor.config;
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
	 *@return
	 *Index + 1
	 */
	public final int mtlb() {
		return this.ordinal() + 1;
	}

}
