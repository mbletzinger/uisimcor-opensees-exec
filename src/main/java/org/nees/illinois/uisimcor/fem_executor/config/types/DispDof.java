package org.nees.illinois.uisimcor.fem_executor.config.types;

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
	/**
	 * Return the units of the DOF.
	 * @param isForce
	 *            return force units.
	 * @return String with units.
	 */
	public String units(final boolean isForce) {
		String result = null;
		String fresult = null;
		switch (this) {
		case DX:
		case DY:
		case DZ:
			result = "in";
			fresult = "kips";
			break;
		case RX:
		case RY:
		case RZ:
			result = "radians";
			fresult = "kip*inches";
			break;
		default:
			break;
		}
		return (isForce ? fresult : result);
	}
}
