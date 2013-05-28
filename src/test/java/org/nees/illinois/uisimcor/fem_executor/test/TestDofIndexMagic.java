package org.nees.illinois.uisimcor.fem_executor.test;

import org.nees.illinois.uisimcor.fem_executor.config.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "dofindexmagic" })
public class TestDofIndexMagic {
	/**
	 * Expected UI-SimCor indexes. Also 3D indexes.
	 */
	private final int[] expectedUiSimCor = { 0, 1, 2, 3, 4, 5 };
	/**
	 * Expected UI-SimCor and 3D with MATLAB indexes.
	 */
	private final int[] expectedUiSimCorMtlb = { 1, 2, 3, 4, 5, 6 };
	/**
	 * Expected 2D indexes.
	 */
	private final int[] expected2D = { 0, 1, -1, -1, -1, 2 };
	/**
	 * Expected 2D indexes with MATLAB.
	 */
	private final int[] expected2DMtlb = { 1, 2, -1, -1, -1, 3 };
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TestDofIndexMagic.class);

	/**
	 * Test the indexes of UI-SimCor.
	 */
	@Test
	public final void testUiSimCorIndexes() {
		DofIndexMagic magic = new DofIndexMagic(DimensionType.ThreeD, false,
				true);
		int idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expectedUiSimCor[idx]);
			idx++;
		}
		magic = new DofIndexMagic(DimensionType.TwoD, true, true);
		idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expectedUiSimCorMtlb[idx]);
			idx++;
		}
	}

	/**
	 * Test the indexes of 3D.
	 */
	@Test
	public final void test3DIndexes() {
		DofIndexMagic magic = new DofIndexMagic(DimensionType.ThreeD, false,
				false);
		int idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expectedUiSimCor[idx]);
			idx++;
		}
		magic = new DofIndexMagic(DimensionType.ThreeD, true, false);
		idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expectedUiSimCorMtlb[idx]);
			idx++;
		}
	}

	/**
	 * Test the indexes of 2D.
	 */
	@Test
	public final void test2DIndexes() {
		DofIndexMagic magic = new DofIndexMagic(DimensionType.TwoD, false,
				false);
		int idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expected2D[idx]);
			idx++;
		}
		magic = new DofIndexMagic(DimensionType.TwoD, true, false);
		idx = 0;
		for (DispDof d : DispDof.values()) {
			int actual = checkIndex(magic, d);
			Assert.assertEquals(actual, expected2DMtlb[idx]);
			idx++;
		}
	}

	/**
	 * Execute the magic and return what happened.
	 * @param magic
	 *            Magic instance.
	 * @param dof
	 *            DOF to check.
	 * @return Either the valid index or -1 if invalid.
	 */
	private int checkIndex(final DofIndexMagic magic, final DispDof dof) {
		int result;
		log.debug("Checking DOF " + dof + " with " + magic);
		try {
			result = magic.index(dof);
		} catch (IllegalParameterException e) {
			return -1;
		}
		return result;
	}
}
