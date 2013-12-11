package org.nees.illinois.uisimcor.fem_executor.test.utils;

import java.util.List;

import org.testng.Assert;

/**
 * Class which test two lists of items for equality.
 * @author Michael Bletzinger
 * @param <Item>
 *            Item type.
 */
public class CompareLists<Item> {
	/**
	 */
	private final Double tolerance;

	/**
	 * Constructor.
	 */
	public CompareLists() {
		final double defaultTolerance = 0.0001;
		this.tolerance = defaultTolerance;
	}

	/**
	 * @param tolerance
	 *            Amount of error allowed between 2 doubles.
	 */
	public CompareLists(final Double tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * Compare two list.
	 * @param actual
	 *            Generated list.
	 * @param expected
	 *            Expected results.
	 */
	public final void compare(final List<Item> actual, final List<Item> expected) {
		Assert.assertEquals(actual.size(), expected.size(), " with actual " + actual);
		for (int e = 0; e < expected.size(); e++) {
			if (expected.get(e) instanceof Double) {
				Double a = (Double) actual.get(e);
				Double ex = (Double) expected.get(e);
				Assert.assertEquals(a, ex, tolerance, "At " + e + " out of "
						+ expected.size());
			} else {
				Assert.assertEquals(actual.get(e), expected.get(e), "At " + e
						+ " out of " + expected.size());
			}
		}
	}

}
