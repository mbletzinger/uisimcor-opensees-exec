package org.nees.illinois.uisimcor.fem_executor.config.types;
/**
 * Enumeration of FEM programs which UI-SimCor can communicate with.
 * @author Michael Bletzinger
 *
 */
public enum FemProgramType {
	/**
	 * Commercial FEM application. http://www.3ds.com/products/simulia/portfolio/abaqus/overview/
	 */
	ABAQUS,
	/**
	 *  Generic console-in console-out application.
	 */
	GENERIC,
	/**
	 * FEM program from Berkeley. http://opensees.berkeley.edu/
	 */
	OPENSEES,
	/**
	 * FEM program from University of Toronto. http://www.civ.utoronto.ca/vector/software.html
	 */
	VECTOR,
	/**
	 * FEM program from University of Illinois. http://code.google.com/p/zeus-nl/
	 */
	ZEUS_NL;
	/**
	 * Returns the MATLAB 1-based index.
	 *@return
	 *Index + 1
	 */
	public int mtlb() {
		if(equals(ZEUS_NL)) {
			return 1;
		}
		if(equals(OPENSEES)) {
			return 2;
		}
		if(equals(ABAQUS)) {
			final int result = 3;
			return result;
		}
		if(equals(VECTOR)) {
			final int result = 4;
			return result;
		}
		final int result = 9;
		return result;
	}
}
