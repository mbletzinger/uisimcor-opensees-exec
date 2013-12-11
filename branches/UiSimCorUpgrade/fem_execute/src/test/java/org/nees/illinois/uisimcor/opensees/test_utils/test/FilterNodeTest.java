package org.nees.illinois.uisimcor.opensees.test_utils.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.test.utils.CompareLists;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.nees.illinois.uisimcor.opensees.test_utils.FemExecutorConfigCreate;
import org.nees.illinois.uisimcor.opensees.test_utils.FilterNodes;
import org.nees.illinois.uisimcor.opensees.test_utils.ParseOpenSeesPrint;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.DofLabel;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test node filtering and config file generation.
 * @author Michael Bletzinger
 */
public class FilterNodeTest {
	/**
	 * Path to the OpenSees print result file.
	 */
	private String frameFile;
	/**
	 * Map of nodes read from the frame file.
	 */
	private Map<Integer, Node> nodeMap;
	/**
	 * Sorted list of node indexes.
	 */
	private List<Integer> nodeIdxs;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(FilterNodeTest.class);
	/**
	 * Expected control nodes.
	 */
	private List<Integer> expectedNodes;

	/**
	 * Find all of the files to parse.
	 */
	@BeforeClass
	public final void beforeClass() {
		frameFile = getPath("frame.txt");
		ParseOpenSeesPrint parsePrint = new ParseOpenSeesPrint(frameFile);
		parsePrint.parse();
		nodeMap = parsePrint.getNodes();
		nodeIdxs = new ArrayList<Integer>(nodeMap.keySet());
		Collections.sort(nodeIdxs);
		final int[] en = { 10100, 10300, 10500, 11100, 11300, 11500, 12100,
				12300, 12500, 13100, 13300, 13500, 20100, 20300, 20500, 21100,
				21300, 21500, 22100, 22300, 22500, 23100, 23300, 23500, 30100,
				30300, 30500, 31100, 31300, 31500, 32100, 32300, 32500, 33100,
				33300, 33500, 40100, 40300, 40500, 41100, 41300, 41500, 42100,
				42300, 42500, 43100, 43300, 43500, 50100, 50300, 50500, 51100,
				51300, 51500, 52100, 52300, 52500, 53100, 53300, 53500, 60100,
				60300, 60500, 61100, 61300, 61500, 62100, 62300, 62500, 63100,
				63300, 63500, 70100, 70300, 70500, 71100, 71300, 71500, 72100,
				72300, 72500, 73100, 73300, 73500, 80100, 80300, 80500, 81100,
				81300, 81500, 82100, 82300, 82500, 83100, 83300, 83500, 90100,
				90300, 90500, 91100, 91300, 91500, 92100, 92300, 92500, 93100,
				93300, 93500, 100100, 100300, 100500, 101100, 101300, 101500,
				102100, 102300, 102500, 103100, 103300, 103500 };
		expectedNodes = new ArrayList<Integer>();
		for (int n : en) {
			expectedNodes.add(n);
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

	/**
	 * Test the node filtering.
	 */
	@Test
	public final void loadControlNodes() {
		FilterNodes fn = new FilterNodes(nodeMap.values());
		fn.filter();
		CompareLists<Integer> cmp = new CompareLists<Integer>();
// cmp.compare(fn.getMassedNodes(), expectedNodes);
		cmp.compare(fn.getControlNodes(), expectedNodes);
	}
	/**
	 * Test file generation.
	 */
	@Test
	public final void writeConfigFile() {
		FilterNodes fn = new FilterNodes(nodeMap.values());
		fn.filter();
		List<DofLabel> controlDofs = new ArrayList<DofLabel>();
		controlDofs.add(DofLabel.Dy);
		FemExecutorConfigCreate fecc = new FemExecutorConfigCreate(controlDofs);
		List<Integer> controlNodes = new ArrayList<Integer>();
		final int controlNode = 10308;
		controlNodes.add(controlNode);
		List<String> sourceFiles = new ArrayList<String>();
		sourceFiles.add("Frame.tcl");
		sourceFiles.add("StaticAnalysisEnv.tcl");
		fecc.addSubstructure("MDL-01", fn.getControlNodes(), controlNodes);
		fecc.addSuffixes("MDL-01", sourceFiles, new ArrayList<String>());
		List<Integer> columnNodes = new ArrayList<Integer>();
		List<String> columnFiles = new ArrayList<String>();
		columnFiles.add("Column.tcl");
		columnFiles.add("StaticAnalysisEnv.tcl");
		fecc.addSubstructure("MDL-02", columnNodes, controlNodes);
		fecc.addSuffixes("MDL-02", columnFiles, new ArrayList<String>());
		fecc.createFile("FemExecutor.properties");
	}
}
