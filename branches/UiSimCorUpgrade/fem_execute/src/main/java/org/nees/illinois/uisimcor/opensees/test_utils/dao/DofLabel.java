package org.nees.illinois.uisimcor.opensees.test_utils.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Degree of freedom vector labels.
 * @author Michael Bletzinger
 */
public enum DofLabel {
	/**
	 * X translation.
	 */
	Dx,
	/**
	 * Y translation.
	 */
	Dy,
	/**
	 * Z translation.
	 */
	Dz,
	/**
	 * X rotation.
	 */
	Rx,
	/**
	 * Y rotation.
	 */
	Ry,
	/**
	 * Z rotation.
	 */
	Rz;

	/**
	 * Return the enumerator from the index.
	 * @param idx
	 *            index to enumerator.
	 * @return The corresponding enumerator.
	 */
	public static DofLabel index2Enum(final int idx) {
		/**
		 * Logger.
		 **/
		final Logger log = LoggerFactory.getLogger(DofLabel.class);
		final int three = 3;
		final int four = 4;
		final int five = 5;
		switch (idx) {
		case 0:
			return Dx;
		case 1:
			return Dy;
		case 2:
			return Dz;
		case three:
			return Rx;
		case four:
			return Ry;
		case five:
			return Rz;
		default:
			log.error(idx + " is not a valid DOF index");
			return Dx;
		}
	}
	public final int enum2Index() {
		final int two = 2;
		final int three = 3;
		final int four = 4;
		final int five = 5;
		switch (this) {
		case Dx:
			return 0;
		case Dy:
			return 1;
		case Dz:
			return two;
		case Rx:
			return three;
		case Ry:
			return four;
		case Rz:
			return five;
		}
		return -1;
	}
}
