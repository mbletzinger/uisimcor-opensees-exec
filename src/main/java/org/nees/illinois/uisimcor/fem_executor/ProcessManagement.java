package org.nees.illinois.uisimcor.fem_executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class ProcessManagement {
	private final String cmd;
	private final int waitInMillSecs;
	private final List<String> args = new ArrayList<String>();
	private final Map<String, String> env = new HashMap<String, String>();
	private ProcessResponse errPr;
	private ProcessResponse stoutPr;
	private final Logger log = LoggerFactory.getLogger(ProcessManagement.class);

	public ProcessManagement(String cmd, int waitInMilliSec) {
		super();
		this.cmd = cmd;
		this.waitInMillSecs = waitInMilliSec;
	}

	public void addEnv(String name, String value) {
		env.put(name, value);
	}

	public void addArg(String arg) {
		args.add(arg);
	}

	private String[] assemble() {
		String[] result = new String[args.size() + 1];
		result[0] = cmd;
		int i = 1;
		for (String a : args) {
			result[i] = a;
			i++;
		}
		return result;
	}

	public void execute() {
		String[] executeLine = assemble();
		ProcessBuilder pb = new ProcessBuilder(executeLine);
		pb.environment().putAll(env);

		Process p = null;
		log.debug("Starting process");
		try {
			p = pb.start();
		} catch (IOException e) {
			log.error(cmd + " failed to start because", e);
		}
		log.debug("Creating threads");
		errPr = new ProcessResponse(Level.ERROR, p.getErrorStream(), 100, cmd);
		stoutPr = new ProcessResponse(Level.DEBUG, p.getInputStream(), 100, cmd);
		Thread errThrd = new Thread(errPr);
		Thread stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			log.debug("I was Interrupted");
		}
		log.debug("Waiting for threads");
		try {
			Thread.sleep(waitInMillSecs);
		} catch (InterruptedException e) {
		}
		log.debug("Ending threads");
		errPr.setDone(true);
		stoutPr.setDone(true);
	}

	public String getOutput() {
		return stoutPr.getOutput();
	}

	public String getError() {
		return errPr.getOutput();
	}
}
