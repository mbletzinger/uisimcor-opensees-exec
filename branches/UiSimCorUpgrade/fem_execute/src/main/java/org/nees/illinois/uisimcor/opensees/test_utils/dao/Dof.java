package org.nees.illinois.uisimcor.opensees.test_utils.dao;

/**
 * Representation of a degree of freedom vector for a node.
 * @author Michael Bletzinger
 */
public class Dof {
	/**
	 * Index of the DOF.
	 */
	private final int dofIndex;
	/**
	 * Index of the node.
	 */
	private final int node;
	/**
	 * True if the DOF cannot move.
	 */
	private final boolean constrained;

	/**
	 * @param dofIndex
	 *            Index of the DOF.
	 * @param node
	 *            Index of the node.
	 * @param constrained
	 *            True if the DOF cannot move.
	 */
	public Dof(final int dofIndex,final int node,final boolean constrained) {
		this.dofIndex = dofIndex;
		this.node = node;
		this.constrained = constrained;
	}

	/**
	 * @return the dofIndex
	 */
	public final int getDofIndex() {
		return dofIndex;
	}

	/**
	 * @return the node
	 */
	public final int getNode() {
		return node;
	}

	/**
	 * @return the constrained
	 */
	public final boolean isConstrained() {
		return constrained;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return DofLabel.index2Enum(dofIndex)
				+ (constrained ? "[CONSTRAINED]" : "[FREE]");
	}

}
