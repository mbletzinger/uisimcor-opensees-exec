package org.nees.illinois.uisimcor.opensees.test_utils.dao;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for node information.
 * @author Michael Bletzinger
 */
public class Node {
	/**
	 * Map of DOFs for the node.
	 */
	private final Map<DofLabel, Dof> dofs = new HashMap<DofLabel, Dof>();
	/**
	 * Load vector for the node.
	 */
	private NodeLoads loads;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(Node.class);
	/**
	 * Mass matrix for the node.
	 */
	private NodeMasses masses;
	/**
	 * Index of the node.
	 */
	private final int nodeIndex;

	/**
	 * @param nodeIndex
	 *            Index of node.
	 */
	public Node(final int nodeIndex) {
		this.nodeIndex = nodeIndex;
		for(DofLabel dl : DofLabel.values()) {
			Dof dof = new Dof(dl.enum2Index(),nodeIndex, false);
			addDof(dof);
		}
	}

	/**
	 * Add a DOF to the node.
	 * @param dof
	 *            DOF instance to add.
	 */
	public final void addDof(final Dof dof) {
		DofLabel label = DofLabel.index2Enum(dof.getDofIndex());
		if (dofs.get(label) != null) {
			log.debug(label + " is already defined for node " + nodeIndex);
		}
		dofs.put(label, dof);
	}

	/**
	 * Return a DOF.
	 * @param label
	 *            Enumerator corresponding to the DOF.
	 * @return the DOF instance.
	 */
	public final Dof getDof(final DofLabel label) {
		return dofs.get(label);
	}

	/**
	 * @return the loads
	 */
	public final NodeLoads getLoads() {
		return loads;
	}

	/**
	 * @return the masses
	 */
	public final NodeMasses getMasses() {
		return masses;
	}

	/**
	 * @return the nodeIndex
	 */
	public final int getNodeIndex() {
		return nodeIndex;
	}

	/**
	 * @param loads
	 *            the loads to set
	 */
	public final void setLoads(final NodeLoads loads) {
		this.loads = loads;
	}

	/**
	 * @param masses
	 *            the masses to set
	 */
	public final void setMasses(final NodeMasses masses) {
		this.masses = masses;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "/nodeIndex=" + nodeIndex + "\n/dofs="
				+ (dofs != null ? dofs : "NO DOFs") + "\n"
				+ (loads != null ? loads : "NO loads") + "\n"
				+ (masses != null ? masses : "NO masses");
	}

}
