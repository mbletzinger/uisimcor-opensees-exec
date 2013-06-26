package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.nees.illinois.uisimcor.fem_executor.utils.IllegalParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add double 0.0 elements to DOFs that are not effective.
 * @author Michael Bletzinger
 */
public class DataFormatter {
	/**
	 * Substructure configuration containing the nodes and effective DOFs for
	 * the module.
	 */
	private final SubstructureDao substructCfg;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DataFormatter.class);

	/**
	 * @param substructCfg
	 *            Substructure configuration containing the nodes and effective
	 *            DOFs for the module.
	 */
	public DataFormatter(final SubstructureDao substructCfg) {
		this.substructCfg = substructCfg;
	}

	/**
	 * add filter only effective DOF for all nodes.
	 * @param data
	 *            Data from FEM.
	 * @return Effective DOF values as one array for all nodes.
	 */
	public final List<Double> filter(final List<Double> data) {
		List<Double> result = new ArrayList<Double>();
		int nodeCount = 0;
		final DofIndexMagic openseesMagic = new DofIndexMagic(
				substructCfg.getDimension(), false, false);
		for (Integer n : substructCfg.getNodeSequence()) {
			for (DispDof d : substructCfg.getEffectiveDofs(n)) {
				try {
					log.debug("Adding data at local index " + d + " nodeCount "
							+ nodeCount + " numDofs "
							+ openseesMagic.numberOfDofsPerNode()
							+ " root index " + openseesMagic.index(d)
							+ "\n\tcalculated index " + nodeCount
							* openseesMagic.numberOfDofsPerNode()
							+ openseesMagic.index(d));
					result.add(data.get(nodeCount
							* openseesMagic.numberOfDofsPerNode()
							+ openseesMagic.index(d)));
				} catch (IllegalParameterException e) {
					log.error("Misconfigured substructure "
							+ substructCfg.getAddress());
					return null;
				}
			}
			nodeCount++;
		}
		return result;
	}

	/**
	 * Converts a set of tokens to {@link Double} numbers.
	 * @param strData
	 *            The set of tokens.
	 * @return A row of doubles.
	 */
	public final List<Double> tokenString2Double(final String strData) {
		String[] tokens = strData.split("\\s+");
		List<Double> row = new ArrayList<Double>();
		for (int t = 0; t < tokens.length; t++) {
			Double val;
			try {
				val = new Double(tokens[t]);
			} catch (NumberFormatException e) {
				log.error("Token \"" + tokens[t]
						+ "\" is not a number.  Column " + t + " for \""
						+ strData + "\"", e);
				val = 0.0;
			}
			row.add(val);
		}
		return row;
	}
}
