package org.nees.illinois.uisimcor.fem_executor.test;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;

import org.nees.illinois.uisimcor.fem_executor.FemExecutor;
import org.nees.illinois.uisimcor.fem_executor.FemExecutor.ExecutionState;
import org.nees.illinois.uisimcor.fem_executor.process.FileWithContentDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestFemExecutor {
	private String command;
	private String[] workDirs;
	/**
	 * Logger
	 **/
	private final Logger log = LoggerFactory.getLogger(TestFemExecutor.class);

	@Test
	public void testRunExecutors() {
		FemExecutor [] exes = { new FemExecutor(command, "dummy1", workDirs[0]),new FemExecutor(command, "dummy2", workDirs[1]) };
		for (FemExecutor e : exes) {
			e.setProcessOutputFiles(true);
			e.startCmd();
			if(e.getCurrent().equals(ExecutionState.NotStarted)) {
				Assert.fail();
			}
		}
		boolean done = false;
		while(done == false) {
			log.debug("Checking execution");
			done = exes[0].isDone() && exes[1].isDone();
			try {
				Thread.sleep(400);
			} catch (InterruptedException e1) {
				@SuppressWarnings("unused")
				int nuttin = 0;
			}
		}
	}

	@BeforeClass
	public void beforeClass() {
		String sep = System.getProperty("file.separator");
		URL u = ClassLoader.getSystemResource("printerTest.pl");
		command = u.getPath().replaceAll("%20", " ");
		command = command.replaceAll("/", Matcher.quoteReplacement(sep));
		command = command.replaceAll("\\\\C:", "C:");
		File cmdF = new File(command);
		cmdF.setExecutable(true);
		String currentDir = System.getProperty("user.dir");
		String[] dirs = { currentDir + sep + "workD1",
				currentDir + sep + "workD2" };
		workDirs = dirs;
		for (String d : dirs) {
			File dir = new File(d);
			boolean done = dir.mkdirs();
			if (done == false) {
				log.error("Could not create dir \"" + d + "\"");
				return;
			}
			log.debug("\"" + d + "\" was created");
		}
	}

	@AfterTest
	public void afterTest() {
		for (String d : workDirs) {
			FileWithContentDelete dir = new FileWithContentDelete(d);
			boolean done = dir.delete();
			if (done == false) {
				log.error("Could not remove dir \"" + d + "\"");
				return;
			}
			log.debug("\"" + d + "\" was removed");
		}
	}

}
