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
public class SubstructureConfig {
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
	 * Files which are sourced.
	 */
	private final List<String> sourcedFilenames;
	/**
	 * Control node numbers. Note: The sequence of nodes should be consistent
	 * with Nodes in SimCor.
	 */
	private final List<Integer> nodeSequence;
	/**
	 *            Files which need to be copied from the configuration directory to the work directory but are not sourced.
	 */
	private final List<String> workFiles;

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
	 * @param sourcedFilenames
	 *            Model file name (include extension).
	 * @param nodeSequence
	 *            Control node numbers. Note: The sequence of nodes should be
	 *            consistent with Nodes in SimCor.
	 * @param workFiles
	 *            Files which need to be copied from the config directory to the work directory but are not sourced.
	 */
	public SubstructureConfig(final String address,
			final DimensionType dimension, final FemProgramType femProgram,
			final List<String> sourcedFilenames, final List<Integer> nodeSequence,
			final List<String> workFiles) {
		this.address = address;
		this.dimension = dimension;
		this.femProgram = femProgram;
		this.sourcedFilenames = sourcedFilenames;
		this.nodeSequence = nodeSequence;
		this.workFiles = workFiles;
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
	 * Create an integer vector with six elements with nonzero element for every
	 * effective DOF.
	 * @param node
	 *            Node for the mask.
	 * @return Mask.
	 */
	public final double[] getEffectiveDofMask(final int node) {
		List<DispDof> dofL = effectiveDofs.get(node);
		double[] result = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		for (DispDof d : dofL) {
			result[d.ordinal()] = 1;
		}
		return result;
	}

	/**
	 * Create a matrix of effective DOF masks where the rows are the nodes and
	 * the columns are the six displacement DOFs.
	 * @return
	 * The matrix.
	 */
	public final double[][] getDofMaskMatrix() {
		final int dof3DSize = 6;
		double[][] result = new double[getNumberOfNodes()][dof3DSize];
		for (int i = 0; i < getNumberOfNodes(); i++) {
			result[i] = getEffectiveDofMask(getNodeSequence().get(i));
		}
		return result;
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
	public final List<String> getSourcedFilenames() {
		return sourcedFilenames;
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
	public final int getNumberOfNodes() {
		return nodeSequence.size();
	}

	/**
	 * @return Total number of effective DOFs
	 */
	public final int getTotalDofs() {
		int result = 0;
		for (Integer n : nodeSequence) {
			result += effectiveDofs.get(n).size();
		}
		return result;
	}

	/**
	 * @return the workFiles
	 */
	public final List<String> getWorkFiles() {
		return workFiles;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "SubstructureConfig \"" + address + "\", nodes=" + nodeSequence
				+ ", dofs=" + effectiveDofs + "]";
	}
}
