package org.nees.illinois.uisimcor.fem_executor.config;

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
	 * Returns the MATLAB 1-based index.
	 * @return Index + 1
	 */
	public int mtlb() {
		if (equals(TwoD)) {
			return 2;
		}
		if (equals(ThreeD)) {
			final int result = 3;
			return result;
		}
		final int result = 0;
		return result;
	}

}
