package org.nees.illinois.uisimcor.fem_executor.config;


/**
 * Enumeration of Cartesian Degrees of Freedom (DOF).
 * @author Michael Bletzinger
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
}
