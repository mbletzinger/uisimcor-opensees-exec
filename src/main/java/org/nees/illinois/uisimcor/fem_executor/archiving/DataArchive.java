package org.nees.illinois.uisimcor.fem_executor.archiving;

import java.io.File;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which writes a set of doubles as text to a file.
 * @author Michael Bletzinger
 */
public class DataArchive {
	/**
	 * Text archiver.
	 */
	private final TextArchive archive;
	/**
	 * Format for displacement commands.
	 */
	private DecimalFormat format = new DecimalFormat(
			"###.00000000000000000000E000");
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DataArchive.class);

	/**
	 * Write a data record.
	 * @param step
	 *            Step number of record.
	 * @param data
	 *            to record.
	 */
	public final void write(final int step, final double[] data) {
		String record = Integer.toString(step);
		for (double d : data) {
			record += "\t" + format.format(d);
		}
		record += "\n";
		archive.write(record);
	}

	/**
	 * @param path
	 *            Path to data archive.
	 */
	public DataArchive(final String path) {
		File pathF = new File(path + ".txt");
		this.archive = new TextArchive(pathF);
	}
}
