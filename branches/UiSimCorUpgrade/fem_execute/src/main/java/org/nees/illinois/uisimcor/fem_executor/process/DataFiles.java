package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Class parses a text file with straight floating point numbers and returns a {@link DoubleMatrix set} of numbers.
 * @author Michael Bletzinger
 *
 */
public class DataFiles {
	/**
	 * The parsed data set.
	 */
	private DoubleMatrix archive;

	/**
	 * Flag that indicates whether the data set is available or not.
	 */
	private boolean empty = true;

	/**
	 * Logger
	 **/
	private final Logger log = LoggerFactory.getLogger(DataFiles.class);
	/**
	 * @return the archive
	 */
	public final DoubleMatrix getArchive() {
		return archive;
	}
	/**
	 * @return the empty
	 */
	public final boolean isEmpty() {
		return empty;
	}
	/**
	 * Opens the text file and feeds all of the rows to the token reader.
	 *@param strFile
	 *Name of the text file
	 */
	public final void parseDataFile(final String strFile) {
		BufferedReader br = null;
		List<List<Double>> matrix = new ArrayList<List<Double>>();
		try {
			br = new BufferedReader(new FileReader(strFile));
		} catch (FileNotFoundException e) {
			log.error("File \"" + strFile + "\" not found");
			return;
		}
		String strLine = "";
		try {
			int rowN = 1;
			while ((strLine = br.readLine()) != null) {
				String[] tokens = strLine.split("\\s+");
				List<Double> row = tokens2Double(tokens, strFile, rowN);
				matrix.add(row);
				rowN++;
			}
			empty = false;
			archive = new DoubleMatrix(matrix);
			log.debug("Archive Row Text: " + archive);
		} catch (IOException e) {
			log.error("File \"" + strFile + "\" cannot be parsed because ", e);
		}
	}

	/**
	 * @param empty
	 *            the empty to set
	 */
	public final void setEmpty(final boolean empty) {
		this.empty = empty;
	}
/**
 * Converts a set of tokens to {@link Double} numbers.
 *@param tokens
 *The set of tokens.
 *@param filename
 *Name of the original text file (used for error messages).
 *@param rowNumber
 *Name of the original row the tokens came from (used for error messages).
 *@return
 *A row of doubles.
 */
	private List<Double> tokens2Double(final String[] tokens, final String filename,
			final int rowNumber) {
		List<Double> row = new ArrayList<Double>();
		for (int t = 0; t < tokens.length; t++) {
			Double val;
			try {
				val = new Double(tokens[t]);
			} catch (NumberFormatException e) {
				log.error("Token \"" + tokens[t] + "\" is not a number. Row "
						+ rowNumber + " column " + t + " file \"" + filename
						+ "\"");
				val = 0.0;
			}
			row.add(val);
		}
		return row;
	}
}
