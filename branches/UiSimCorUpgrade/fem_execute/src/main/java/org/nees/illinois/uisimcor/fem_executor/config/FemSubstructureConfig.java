/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class defining the parameters used to execute FEM programs.
 * @author Michael Bletzinger
 */
public class FemSubstructureConfig {
	/**
	 * Address identifying the substructure to UI-SimCor.
	 */
	private final String address;

	/**
	 * This tag is used only for OpenSees and Abaqus. Ignore for other analysis
	 * types. In the current version, 2D 3DOF system and 3D 6DOF system are
	 * supported.
	 */
	private final DimensionType dimension;

	/**
	 * Effective DOFs in control point. Note: The sequence of DOFs should be
	 * consistent with sequence of DOFs in UI-SimCor.
	 */
	private final Map<Integer, List<DispDof>> effectiveDofs = new HashMap<Integer, List<DispDof>>();
	/**
	 * 1 for Zeus-NL, 2 for OpenSees, 3 for Abaqus, 4 for Vector, and 9 for
	 * generic console-in console-out application. Vector has not been
	 * implemented yet.
	 */
	private final FemProgramType femProgram;
	/**
	 * Model file name (include extension).
	 */
	private final String modelFileName;

	/**
	 * Control node numbers. Note: The sequence of nodes should be consistent
	 * with Nodes in SimCor.
	 */
	private final List<Integer> nodeSequence;

	/**
	 * Constructor.
	 * @param address
	 *            Address identifying the substructure to UI-SimCor
	 * @param dimension
	 *            This tag is used only for OpenSees and Abaqus. Ignore for
	 *            other analysis types. In the current version, 2D 3DOF system
	 *            and 3D 6DOF system are supported.
	 * @param femProgram
	 *            1 for Zeus-NL, 2 for OpenSees, 3 for Abaqus, 4 for Vector, and
	 *            9 for generic console-in console-out application. Vector has
	 *            not been implemented yet.
	 * @param nodeSequence
	 *            Control node numbers. Note: The sequence of nodes should be
	 *            consistent with Nodes in SimCor.
	 * @param modelFileName
	 *            Model file name (include extension).
	 */
	public FemSubstructureConfig(final String address,
			final DimensionType dimension, final FemProgramType femProgram,
			final String modelFileName, final List<Integer> nodeSequence) {
		this.address = address;
		this.dimension = dimension;
		this.femProgram = femProgram;
		this.modelFileName = modelFileName;
		this.nodeSequence = nodeSequence;
	}

	/**
	 * Set a list of effective DOFs for a node.
	 * @param node
	 *            Node.
	 * @param dofs
	 *            List of DOFs.
	 */
	public final void addEffectiveDofs(final int node, final List<DispDof> dofs) {
		effectiveDofs.put(node, dofs);
	}

	/**
	 * @return the address
	 */
	public final String getAddress() {
		return address;
	}

	/**
	 * @return the dimension
	 */
	public final DimensionType getDimension() {
		return dimension;
	}

	/**
	 * @return the effectiveDofs
	 */
	public final Map<Integer, List<DispDof>> getEffectiveDofs() {
		return effectiveDofs;
	}

	/**
	 * Get the list of effective DOFs for a node.
	 * @param node
	 *            Node
	 * @return List of DOFs.
	 */
	public final List<DispDof> getEffectiveDofs(final int node) {
		return effectiveDofs.get(node);
	}

	/**
	 * @return the femProgram
	 */
	public final FemProgramType getFemProgram() {
		return femProgram;
	}

	/**
	 * @return the modelFileName
	 */
	public final String getModelFileName() {
		return modelFileName;
	}

	/**
	 * @return the nodeSequence
	 */
	public final List<Integer> getNodeSequence() {
		return nodeSequence;
	}

	/**
	 * @return Total number of nodes.
	 */
	public int getNumberOfNodes() {
		return nodeSequence.size();
	}

	/**
	 * @return Total number of effective DOFs
	 */
	public int getTotalDofs() {
		int result = 0;
		for (Integer n : nodeSequence) {
			result += effectiveDofs.get(n).size();
		}
		return result;
	}
}
