package org.nees.illinois.uisimcor.opensees.test_utils.dao;

import java.util.Arrays;

import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(NodeLoads.class);

	/**
	 * @param nodeIndex
	 *            Index of the loaded node.
	 * @param loads
	 *            Loads.
	 */
	public NodeLoads(final int nodeIndex,final double[] loads) {
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

	/**
	 * @return if the node has any loads.
	 */
	public final boolean hasLoad() {
		log.debug("Checking loads " + MtxUtils.array2String(loads));
		for (int d = 0;d < loads.length;d++) {
			if (loads[d] > 0.0 || loads[d] < 0.0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "/loads=" + Arrays.toString(loads);
	}

}
