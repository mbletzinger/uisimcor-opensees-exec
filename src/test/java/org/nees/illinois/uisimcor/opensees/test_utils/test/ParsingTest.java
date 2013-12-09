package org.nees.illinois.uisimcor.opensees.test_utils.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.nees.illinois.uisimcor.opensees.test_utils.ParseOpenSeesPrint;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the parsing routines.
 * @author Michael Bletzinger
 */
public class ParsingTest {
	/**
	 * File containing basic elements of an OpenSees print.
	 */
	private String basicFile;
	/**
	 * Results from the RC Frames model.
	 */
	private String largeFile;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(ParsingTest.class);

	/**
	 * Cleanup.
	 */
	@AfterClass
	public void afterClass() {
	}

	/**
	 * Try a basic file with all of the elements.
	 */
	@Test
	public final void basicParsing() {
		ParseOpenSeesPrint parsePrint = new ParseOpenSeesPrint(basicFile);
		parsePrint.parse();
		Map<Integer, Node> nodeMap = parsePrint.getNodes();
		List<Integer> nodeIdxs = new ArrayList<Integer>(nodeMap.keySet());
		Collections.sort(nodeIdxs);
		for(Integer n : nodeIdxs) {
			log.info("Node " + nodeMap.get(n));
		}
	}
	/**
	 * Find all of the files to parse.
	 */
	@BeforeClass
	public final void beforeClass() {
		basicFile = getPath("ParsingTest.txt");
		largeFile = getPath("frame.txt");
	}

	/**
	 * Try a large file with all of the elements.
	 */
	@Test
	public final void extensiveParsing() {
		ParseOpenSeesPrint parsePrint = new ParseOpenSeesPrint(largeFile);
		parsePrint.parse();
		Map<Integer, Node> nodeMap = parsePrint.getNodes();
		List<Integer> nodeIdxs = new ArrayList<Integer>(nodeMap.keySet());
		Collections.sort(nodeIdxs);
		for(Integer n : nodeIdxs) {
			log.info("Node " + nodeMap.get(n));
		}
	}

	/**
	 * Find files as resources.
	 * @param file
	 *            to find.
	 * @return path to the file.
	 */
	private String getPath(final String file) {
		URL u = ClassLoader.getSystemResource(file);
		if (u == null) {
			log.error("Cannot find \"" + file + "\"");
		}
		String path = PathUtils.cleanPath(u.getPath());
		log.debug("Found \"" + path + "\"");
		return path;
	}
}
