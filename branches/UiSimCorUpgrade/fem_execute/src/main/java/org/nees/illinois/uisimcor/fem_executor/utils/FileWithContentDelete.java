package org.nees.illinois.uisimcor.fem_executor.utils;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the File class so that a non-empty directory can be deleted.
 * @author Michael Bletzinger
 */
public class FileWithContentDelete extends File {

	/**
	 * Eclipse required UID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory
			.getLogger(FileWithContentDelete.class);

	/**
	 * Constructor for File super class.
	 * @param parent
	 *            Parent directory.
	 * @param child
	 *            sub-directory or file name.
	 */
	public FileWithContentDelete(final File parent, final String child) {
		super(parent, child);
	}

	/**
	 * Constructor for File super class.
	 * @param pathname
	 *            Path to file or directory.
	 */
	public FileWithContentDelete(final String pathname) {
		super(pathname);
	}

	/**
	 * Constructor for File super class.
	 * @param parent
	 *            Parent directory.
	 * @param child
	 *            sub-directory or file name.
	 */
	public FileWithContentDelete(final String parent, final String child) {
		super(parent, child);
	}

	/**
	 * Constructor for File super class.
	 * @param uri
	 *            URI to file or directory.
	 */
	public FileWithContentDelete(final URI uri) {
		super(uri);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.File#delete()
	 */
	@Override
	public final boolean delete() {
		log.debug("deleting " + getAbsolutePath());
		if (isFile()) {
			return super.delete();
		}
		if (list() == null) {
			return super.delete();
		}
		for (String f : list()) {
			FileWithContentDelete sub = new FileWithContentDelete(
					getAbsoluteFile(), f);
			sub.delete();
		}
		return super.delete();
	}

}
