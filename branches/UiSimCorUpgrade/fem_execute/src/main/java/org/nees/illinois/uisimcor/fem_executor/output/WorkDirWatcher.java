package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which runs a watch {@link WatchService service} on the FEM process
 * working directory in its own thread.
 * @author Michael Bletzinger
 */
public class WorkDirWatcher implements AbortableI {
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(WorkDirWatcher.class);

	/**
	 * Flag to exit the thread.
	 */
	private volatile boolean quit = false;
	/**
	 * Map used to direct file changes to the correct monitors.
	 */
	private final ConcurrentMap<WatchKey, BlockingQueue<List<WatchEvent<?>>>> watches = new ConcurrentHashMap<WatchKey, BlockingQueue<List<WatchEvent<?>>>>();
	/**
	 * Watch service for files.
	 */
	private final WatchService watchService = FileSystems.getDefault()
			.newWatchService();

	/**
	 * Add full path of file to watch list.
	 * @param path
	 *            Full pathname.
	 * @param queue
	 *            Queue used to send events.
	 */
	public final synchronized void addFileWatch(final String path,
			final BlockingQueue<List<WatchEvent<?>>> queue) {
		Path wp = Paths.get(path);
		WatchKey key;
		try {
			key = wp.register(watchService,
					StandardWatchEventKind.ENTRY_CREATE,
					StandardWatchEventKind.ENTRY_MODIFY);
		} catch (IOException e) {
			log.error("Watch service can't register \"" + path + "\" because ",
					e);
			return;
		}
		watches.put(key, queue);
	}

	@Override
	public final synchronized boolean isQuit() {
		return quit;
	}

	@Override
	public final void run() {
		while (isQuit() == false) {
			WatchKey signaledK;
			try {
				signaledK = watchService.take();
			} catch (ClosedWatchServiceException e) {
				log.error("Watch service has died because ", e);
				return;
			} catch (InterruptedException e) {
				log.debug("Checking Quit");
				continue;
			}
			List<WatchEvent<?>> events = signaledK.pollEvents();
			log.debug("Got events for " + events.get(0).context());
			signaledK.reset();
			try {
				watches.get(signaledK).put(events);
			} catch (InterruptedException e) {
				log.debug("Queue put to OutputFileMonitor was interrupted");
				continue;
			}
		}
		log.info("Watch service is closing");
		try {
			watchService.close();
		} catch (IOException e) {
			log.error("Watchservice won't close because ", e);
		}
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
		this.quit = quit;
	}
}
