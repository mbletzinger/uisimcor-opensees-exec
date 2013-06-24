package org.nees.illinois.uisimcor.fem_executor.response;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Observer which sends the observed steps into a queue.
 * @author Michael Bletzinger
 *
 */
public class ResponseMonitor implements Observer {
/**
 * Logger.
 **/
private final Logger log = LoggerFactory.getLogger(ResponseMonitor.class);

/**
 * Queue which transmits step strings.
 */
	private final BlockingQueue<String> steps = new LinkedBlockingQueue<String>();
	/**
		 * @return the steps
		 */
		public final BlockingQueue<String> getSteps() {
			return steps;
		}

	@Override
	public final void update(final Observable o, final Object arg) {
		if(arg == null) {
		log.error("Observed step is null");
		return;
		}
		String step = (String) arg;
		try {
			steps.put(step);
		} catch (InterruptedException e) {
			log.debug("Interrupted");
		}
	}

}
