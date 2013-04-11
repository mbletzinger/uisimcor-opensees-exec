package org.nees.illinois.uisimcor.fem_executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class ProcessResponse implements Runnable {
	private boolean done;

	private final Level level;

	private final Logger log = LoggerFactory.getLogger(ProcessResponse.class);

	private final int millSecWait;

	private String output = "";
	private final String processName;
	private final InputStream strm;
	public ProcessResponse(Level level, InputStream strm, int millSecWait,
			String processName) {
		super();
		this.level = level;
		this.strm = strm;
		this.millSecWait = millSecWait;
		this.processName = processName;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}
	/**
	 * @return the done
	 */
	public synchronized boolean isDone() {
		return done;
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
		while (isDone() == false) {
			try {
				String cbuf;
				if (reader.ready()) {
					cbuf = reader.readLine();
					// log.debug("read \"" + cbuf + "\"");
					writeLog(cbuf);
					output += cbuf + "\n";
				}
			} catch (IOException e) {
				log.debug("Stream for \"" + processName + "\" has closed");
				setDone(true);
			}
			try {
				Thread.sleep(millSecWait);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * @param done
	 *            the done to set
	 */
	public synchronized void setDone(boolean done) {
		this.done = done;
	}

	private void writeLog(String line) {
		if (level.equals(Level.ERROR)) {
			log.error("[" + processName + "] " + line);
		}
		if (level.equals(Level.INFO)) {
			log.info("[" + processName + "] " + line);
		}
		if (level.equals(Level.DEBUG)) {
			log.debug("[" + processName + "] " + line);
		}
	}

}
