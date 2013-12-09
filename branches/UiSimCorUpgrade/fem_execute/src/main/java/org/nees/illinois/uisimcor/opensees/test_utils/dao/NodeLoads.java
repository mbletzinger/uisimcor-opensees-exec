package org.nees.illinois.uisimcor.opensees.test_utils.dao;

import java.util.Arrays;

/**
 * Contains the load vector for a node.
 * @author Michael Bletzinger
 */
public class NodeLoads {
	/**
	 * Loads.
	 */
	private final double[] loads;
	/**
	 * Index of the loaded node.
	 */
	private final int nodeIndex;

	/**
	 * @param nodeIndex
	 *            Index of the loaded node.
	 * @param loads
	 *            Loads.
	 */
	public NodeLoads(final int nodeIndex, final double[] loads) {
		this.nodeIndex = nodeIndex;
		this.loads = loads;
	}

	/**
	 * @return the loads
	 */
	public final double[] getLoads() {
		return loads;
	}

	/**
	 * @return the nodeIndex
	 */
	public final int getNodeIndex() {
		return nodeIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "/loads=" + Arrays.toString(loads);
	}

}
