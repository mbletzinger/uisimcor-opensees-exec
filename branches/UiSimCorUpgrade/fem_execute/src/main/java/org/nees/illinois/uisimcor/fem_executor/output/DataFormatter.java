package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DofIndexMagic;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
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
						+ strData + "\"", e);
				val = 0.0;
			}
			row.add(val);
		}
		return row;
	}

	/**
	 * Read a binary file with one line of response values.
	 * @param file
	 *            Name of file.
	 * @param length
	 *            Number of values expected.
	 * @return List of doubles. Returns null if there were problems.
	 */
	public final List<Double> readFile(final String file, final int length) {
		// create FileInputStream object
		DataInputStream din;
		try {
			FileInputStream fin = new FileInputStream(file);
			din = new DataInputStream(fin);
		} catch (FileNotFoundException e) {
			log.error("File \"" + file + "\" could not be read because ", e);
			return null;
		}

		/*
		 * To read a Java double primitive from file, use byte readDouble()
		 * method of Java DataInputStream class. This method reads 8 bytes and
		 * returns it as a double value.
		 */
		List<Double> result = new ArrayList<Double>();
		for (int n = 0; n < length; n++) {
			double d = 0.0;
			try {
				d = din.readDouble();
			} catch (IOException e) {
				log.error("File \"" + file + "\" could not be read because ", e);
				try {
					din.close();
				} catch (IOException e1) {
					log.error("File \"" + file
							+ "\" could not be closed but who cares ", e1);
				}
				return null;
			}
			result.add(d);
		}
		try {
			din.close();
		} catch (IOException e1) {
			log.error("File \"" + file
					+ "\" could not be closed but who cares ", e1);
		}

		return result;
	}
}
