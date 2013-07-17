package org.nees.illinois.uisimcor.fem_executor.archiving;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which writes text records into the same file.
 * @author Michael Bletzinger
 */
public class TextArchive {
	/**
	 * Path to the text file.
	 */
	private final File path;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TextArchive.class);

	/**
	 * @param path
	 *            Path to the text file.
	 */
	public TextArchive(final File path) {
		this.path = path;
	}

	/**
	 * Write the record to the text file.
	 * @param record
	 *            text to write.
	 */
	public final void write(final String record) {
		Writer writing = null;
		try {
			writing = new OutputStreamWriter(new FileOutputStream(path,true));
		} catch (FileNotFoundException e) {
			log.error("Cannot write to \"" + path + "\" because ", e);
			return;
		}
		try {
			writing.write(record);
		} catch (IOException e) {
			log.error("Cannot write to \"" + path + "\" because ", e);
		} finally {
			try {
				writing.close();
			} catch (IOException e) {
				log.debug("Who cares", e);
			}
		}
	}
}
