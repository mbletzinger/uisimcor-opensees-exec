package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
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
	 * add 0.0 for each displacement that is not an effective DOF.
	 * @param data
	 *            Effective DOF values.
	 * @return Padded row of data.
	 */
	public final DoubleMatrix pad(final List<Double> data) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		int nodeCount = 0;
		final DofIndexMagic openseesMagic = new DofIndexMagic(
				substructCfg.getDimension(), false, false);
		final DofIndexMagic uisimcorMagic = new DofIndexMagic(
				substructCfg.getDimension(), false, true);
		for (Integer n : substructCfg.getNodeSequence()) {
			int lastEffectiveDof = 0;
			List<Double> row = new ArrayList<Double>();
			for (DispDof d : substructCfg.getEffectiveDofs(n)) {
				int uisimcorIdx;
				try {
					uisimcorIdx = uisimcorMagic.index(d);
				} catch (IllegalParameterException e) {
					log.error("Substructure " + substructCfg.getAddress()
							+ " is misconfigured");
					return null;
				}
				for (int j = lastEffectiveDof + 1; j < uisimcorIdx; j++) {
					row.add(0.0);
				}
				try {
					row.add(data.get(nodeCount * openseesMagic.numberOfDofs()
							+ openseesMagic.index(d)));
				} catch (IllegalParameterException e) {
					log.error("Misconfigured substructure "
							+ substructCfg.getAddress());
					return null;
				}
				lastEffectiveDof = uisimcorIdx;
			}
			while (lastEffectiveDof < uisimcorMagic.numberOfDofs() - 1) {
				row.add(0.0);
				lastEffectiveDof++;
			}
			result.add(row);
			nodeCount++;
		}
		return new DoubleMatrix(result);
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
					result.add(data.get(nodeCount
							* openseesMagic.numberOfDofs()
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
						+ strData + "\"",e);
				val = 0.0;
			}
			row.add(val);
		}
		return row;
	}

}
