package org.nees.illinois.uisimcor.opensees.test_utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.nees.illinois.uisimcor.opensees.test_utils.dao.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter control nodes from incoming node map.
 * @author Michael Bletzinger
 */
public class FilterNodes {
	/**
	 * Control node set.
	 */
	private List<Integer> controlNodes;
	/**
	 * Input node set.
	 */
	private final Collection<Node> inNodes;
	/**
	 * Set of nodes that have mass.
	 */
	private List<Integer> massedNodes;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(FilterNodes.class);

	/**
	 * @param collection
	 *            Input node set.
	 */
	public FilterNodes(final Collection<Node> collection) {
		this.inNodes = collection;
	}

	/**
	 * Sort out which nodes are control nodes.
	 */
	public final void filter() {
		controlNodes = new ArrayList<Integer>();
		massedNodes = new ArrayList<Integer>();
		for (Node n : inNodes) {
//			log.debug("Checking Node " + n.getNodeIndex());
			if (n.getMasses() != null && n.getMasses().hasMass()) {
				massedNodes.add(n.getNodeIndex());
				log.debug(n.getNodeIndex() + " has mass");
			} else {
				continue;
			}
			if (n.getLoads() != null && n.getLoads().hasLoad()) {
				controlNodes.add(n.getNodeIndex());
				log.debug(n.getNodeIndex() + " has load");
			}
		}
		Collections.sort(controlNodes);
		Collections.sort(massedNodes);
	}

	/**
	 * @return the controlNodes
	 */
	public final List<Integer> getControlNodes() {
		return controlNodes;
	}

	/**
	 * @return the inNodes
	 */
	public final Collection<Node> getInNodes() {
		return inNodes;
	}

	/**
	 * @return the massedNodes
	 */
	public final List<Integer> getMassedNodes() {
		return massedNodes;
	}

}
