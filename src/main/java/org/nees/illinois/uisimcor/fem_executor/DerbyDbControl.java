package org.nees.illinois.uisimcor.fem_executor;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyDbControl {
	/**
	 * @return the started
	 */

	private String derbyHome = new String("/Users/mbletzin/Install/derby");
	private final Logger log = LoggerFactory.getLogger(DerbyDbControl.class);
	private boolean localStart = false;
	private FileWithContentDelete derbyData;

	public void startDerby() {
		if (isRunning()) {
			log.info("Derby server already started");
			localStart = false;
			return;
		}
		createDirectory();
		ProcessManagement pm = new ProcessManagement(derbyHome
				+ "/bin/startNetworkServer", 4000);
		pm.addEnv("DERBY_OPTS", "-Dderby.system.home=" + derbyData.getAbsolutePath());
		pm.execute();
		localStart = true;
	}

	public void stopDerby() {
		if (localStart) {
			ProcessManagement pm = new ProcessManagement(derbyHome
					+ "/bin/stopNetworkServer", 4000);
			pm.execute();
			log.info("Derby server has stopped");
			cleanup();
		}
	}

	public boolean isRunning() {
		ProcessManagement pm = new ProcessManagement(derbyHome
				+ "/bin/NetworkServerControl", 4000);
		pm.addArg("ping");
		pm.execute();
		if (pm.getOutput().contains("Connection refused")) {
			return false;
		}
		return true;
	}
	private void createDirectory() {
		Properties p = System.getProperties();
		String userDir = p.getProperty("user.dir");
		derbyData = new FileWithContentDelete(userDir, "derbyDb");
		if(derbyData.exists() == false) {
			log.debug("creating folder " + derbyData);
			derbyData.mkdir();
		}
		p.setProperty("derby.system.home", derbyData.getAbsolutePath());
	}
	private void cleanup() {
		if(derbyData.exists()) {
			log.debug("removing folder " + derbyData);
			boolean done = derbyData.delete();
			if(done) {
				return;
			}
			log.error("Could not delete " + derbyData.getAbsolutePath());
		}
	}
}
