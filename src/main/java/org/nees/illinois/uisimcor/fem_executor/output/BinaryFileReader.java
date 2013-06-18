package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to read a binary file for one line of doubles repeatedly.
 * @author Michael Bletzinger
 */
public class BinaryFileReader {
	/**
	 * Name of file.
	 */
	private final String filename;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(BinaryFileReader.class);

	/**
	 * Number of doubles to be found in file.
	 */
	private final int totalDofs;
	/**
	 * @param filename
	 *            Name of file.
	 * @param totalDofs
	 *            Number of doubles to be found in file.
	 */
	public BinaryFileReader(final String filename, final int totalDofs) {
		this.filename = filename;
		this.totalDofs = totalDofs;
		log.debug("Set up to read " + this.totalDofs + " dofs from " + this.filename);
	}
	/**
	 * Attempts to delete the file.
	 * @throws OutputFileException
	 *             If the file cannot be deleted.
	 */
	public final void clean() throws OutputFileException {
		File fileF = new File(filename);
		if (fileF.exists()) {
			log.debug("Deleting \"" + filename + "\"");
			boolean done = fileF.delete();
			if (done == false) {
				throw new OutputFileException("\"" + filename
						+ "\" cannot be deleted");
			}
		}

	}
	/**
	 * @return the last modification date of the file.
	 */
	public final long getFileModDate() {
		File file = new File(filename);
		long time = file.lastModified();
		return time;
	}

	/**
	 * @return the filename that is being read.
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the directory containing the file that is being read.
	 */
	public final String getDirectory() {
		return PathUtils.parent(filename);
	}
	/**
	 * @return the number of double values that are expected in the file.
	 */
	public final int getTotalDofs() {
		return totalDofs;
	}

	/**
	 * Read the file again.
	 * @return The double numbers in the file.
	 * @throws OutputFileException
	 *             if there are any problems reading the file.
	 */
	public final List<Double> read() throws OutputFileException {
		byte[] dataBytes = readBytes();
		return readDoubles(dataBytes);
	}

	/**
	 * Read the contents of the file into a byte array.
	 * @return The byte array.
	 * @throws OutputFileException
	 *             if there are problems reading the file.
	 */
	private byte[] readBytes() throws OutputFileException {
		File file = new File(filename);
		if (file.exists() == false) {
			throw new OutputFileException("\"" + filename + "\" does not exist");
		}
		if (file.canRead() == false) {
			throw new OutputFileException("\"" + filename + "\" cannot be read");
		}
		int expectedLength = (int) file.length();
		InputStream input = null;
		int totalBytesRead = 0;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new OutputFileException("\"" + filename + "\" does not exist");
		}

		byte[] result = new byte[expectedLength];
		while (totalBytesRead < expectedLength) {
			int bytesRemaining = expectedLength - totalBytesRead;
			int bytesRead = 0;
			try {
				bytesRead = input.read(result, totalBytesRead, bytesRemaining);
			} catch (IOException e) {
				try {
					input.close();
				} catch (IOException e1) {
					log.error("Close failed but who cares", e1);
				}
				throw new OutputFileException("Reading failed for file \""
						+ filename + "\" because ", e);
			}
			if (bytesRead > 0) {
				totalBytesRead = totalBytesRead + bytesRead;
			}
		}
		try {
			input.close();
		} catch (IOException e) {
			log.error("Close failed but who cares");
		}
		return result;
	}
	/**
	 * Parse byte array into double numbers.
	 * @param record
	 *            byte array.
	 * @return List of parsed doubles.
	 * @throws OutputFileException
	 *             If there aren't enough bytes.
	 */
	private List<Double> readDoubles(final byte[] record)
			throws OutputFileException {
		final int numberOfBytesInDouble = 8;
		if (record.length < numberOfBytesInDouble * (totalDofs + 1)) {
			throw new OutputFileException("\"" + filename
					+ "\" does not have enough values");
		}
		List<Double> result = new ArrayList<Double>();
		for (int i = 1; i < totalDofs + 1; i++) { // Skip the first number which is time
			int from = i * numberOfBytesInDouble;
			int to = (i + 1) * numberOfBytesInDouble;
			byte[] number = Arrays.copyOfRange(record, from, to);
			ByteBuffer bnum = ByteBuffer.allocate(numberOfBytesInDouble);
			bnum.order(ByteOrder.LITTLE_ENDIAN);
			bnum.put(number);
			bnum.flip();
			double dnum = bnum.getDouble();
			result.add(dnum);
		}
		return result;
	}
}
