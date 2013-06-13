package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.File;
import java.util.List;
import java.util.Observable;

import name.pachler.nio.file.Path;
import name.pachler.nio.file.WatchEvent;
/**
 * Class used to direct file changes to the appropriate file monitor.
 * @author Michael Bletzinger
 *
 */
public class FileObservable extends Observable {
	/**
	 * Last time file was modified. It is measured in milliseconds since the
	 * epoch (00:00:00 GMT, January 1, 1970).
	 */
	private volatile long lastModified;

	/**
	 * File path to be observed.
	 */
	private final String path;

	/**
	 *@param path
	 * File path to be observed.
	 */
	public FileObservable(final String path) {
		this.path = path;
	}

	/**
	 * Store the last modified number.
	 */
	public final void checkModified() {
		File pathF = new File(path);
		setLastModified(pathF.lastModified());
	}

	/**
	 * Set the changed flag and the last modified value.
	 * @param events TODO
	 */
	public final void fileHasChanged(List<WatchEvent<?>> events) {
		checkModified();
		setChanged();
		notifyObservers(events);
	}
	/**
	 * @return the lastModified
	 */
	public final synchronized long getLastModified() {
		return lastModified;
	}

	/**
	 * @return the path
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * @param lastModified
	 *            the lastModified to set
	 */
	public final synchronized void setLastModified(final long lastModified) {
		this.lastModified = lastModified;
	}
}
