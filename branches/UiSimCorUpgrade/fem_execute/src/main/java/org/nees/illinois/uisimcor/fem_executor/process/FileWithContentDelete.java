package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWithContentDelete extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private final Logger log = LoggerFactory.getLogger(FileWithContentDelete.class);
	public FileWithContentDelete(File parent, String child) {
		super(parent, child);
	}

	public FileWithContentDelete(String pathname) {
		super(pathname);
	}

	public FileWithContentDelete(String parent, String child) {
		super(parent, child);
	}

	public FileWithContentDelete(URI uri) {
		super(uri);
	}

	/* (non-Javadoc)
	 * @see java.io.File#delete()
	 */
	@Override
	public boolean delete() {
		log.debug("deleting " + getAbsolutePath());
		if(isFile()) {
			return super.delete();
		}
		if (list() == null) {
			return super.delete();
		}
		for ( String f : list()) {
			FileWithContentDelete sub = new FileWithContentDelete(getAbsoluteFile(), f);
			sub.delete();
		}
		return super.delete();
	}

	
}
