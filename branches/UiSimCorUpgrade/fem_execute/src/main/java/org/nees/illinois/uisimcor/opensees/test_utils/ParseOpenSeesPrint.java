package org.nees.illinois.uisimcor.opensees.test_utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nees.illinois.uisimcor.fem_executor.output.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.Dof;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.Node;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.NodeLoads;
import org.nees.illinois.uisimcor.opensees.test_utils.dao.NodeMasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse an OpenSees print report into the internal representation.
 * @author Michael Bletzinger
 */
public class ParseOpenSeesPrint {
	/**
	 * The latest node that has been declared.
	 */
	private Node current;
	/**
	 * Scanner for the input OpenSees print results file.
	 */
	private final Scanner fileScan;
	/**
	 * Pattern for detecting load declarations.
	 */
	private final Pattern loadPattern = Pattern
			.compile("Load:\\s+(\\d+)\\s+load\\s+:\\s+");

	/**
	 * Pattern for detecting constraints.
	 */
	private final Pattern constraintPattern = Pattern
			.compile("SP_Constraint:\\s+\\d+\\s+Node:\\s+(\\d+)\\s+DOF:\\s+(\\d+)");
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(ParseOpenSeesPrint.class);
	/**
	 * Pattern for detecting the start of a mass matrix.
	 */
	private final Pattern massPattern = Pattern.compile("^\\s*Mass\\s+:\\s+$");
	/**
	 * Pattern for scanning node declarations.
	 */
	private final Pattern nodePattern = Pattern.compile("^\\s*Node:\\s+(\\d+)");
	/**
	 * Node map representing the parsed file.
	 */
	private final Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	/**
	 * Pattern for parsing numbers.
	 */
	private final Pattern numPattern = Pattern.compile("(-?[\\d\\.]+)");

	/**
	 * @param filePath
	 *            path to the OpenSees print results file.
	 */
	public ParseOpenSeesPrint(final String filePath) {
		this.fileScan = createScanner(filePath);
	}

	/**
	 * Scan for a constraint declaration. Example:
	 *
	 * <pre> SP_Constraint: 0	 Node: 100 DOF: 0 ref value: 0 current value: 0</pre>
	 * @param line
	 *            line to scan.
	 * @return True if detected
	 */
	private boolean checkForConstraint(final String line) {
		Matcher constraintMatch = constraintPattern.matcher(line);
		if (constraintMatch.find() == false) {
			return false;
		}
		int nidx = parseInt(constraintMatch.group(1));
		final int two = 2;
		int dof = parseInt(constraintMatch.group(two));
		Node node = nodes.get(nidx);
		if(node == null) {
			log.error("Node " + nidx + " does not exist");
		}
		Dof cdof = new Dof(dof, nidx, true);
		node.addDof(cdof);
		return true;
	}

	/**
	 * Scan for a load declaration. Example:
	 *
	 * <pre>Nodal Load: 103120 load : 0 0 -16.5846 0 0 0 </pre>
	 * @param line
	 *            line to scan.
	 */
	private void checkForLoad(final String line) {
		Matcher loadMatch = loadPattern.matcher(line);
		if (loadMatch.find() == false) {
			return;
		}
		List<Double> r = parseNumberLine(line);
		int nodeidx = (int) Math.round(r.get(0));
		log.debug("Found loads at node " + nodeidx);
		NodeLoads nl = new NodeLoads(new Integer(nodeidx),
				MtxUtils.list2DoubleArray(r.subList(1, r.size())));// first number is the Node number.
		Node node = nodes.get(nodeidx);
		if(node == null) {
			log.error("Node " + nodeidx + " does not exist");
			return;
		}
		node.setLoads(nl);
	}

	/**
	 * Scan for a mass matrix. Note that the mass matrix is declared over 7
	 * lines. The input argument is scanned for the start of a mass matrix
	 * declaration. Example:
	 * <p>
	 * <blockquote>
	 *
	 * <pre>
	 * Mass :
	 * 0 0 0 0 0 0
	 * 0 17.9297 0 0 0 0
	 * 0 0 0 0 0 0
	 * 0 0 0 0 0 0
	 * 0 0 0 0 0 0
	 * 0 0 0 0 0 0
	 * </pre>
	 * </blockquote>
	 * @param line
	 *            to scan.
	 * @return true if a mass matrix was found.
	 */
	private boolean checkForMass(final String line) {
		Matcher massMatch = massPattern.matcher(line);
		if (massMatch.find() == false) {
			return false;
		}
		final int numRows = 6;
		int row = 0;
		List<List<Double>> result = new ArrayList<List<Double>>();
		while (row < numRows) {
			String l = fileScan.nextLine();
			List<Double> r = parseNumberLine(l);
			row++;
			result.add(r);
		}
		NodeMasses nm = new NodeMasses(new DoubleMatrix(result));
		current.setMasses(nm);
		return true;
	}

	/**
	 * Scan line for a Node declaration. Example:
	 *
	 * <pre>
	 * Node:
	 * 1500
	 * </pre>
	 * @param line
	 *            to scan.
	 * @return true if node is found.
	 */
	private boolean checkForNode(final String line) {
		Matcher nodeMatch = nodePattern.matcher(line);
		if (nodeMatch.find() == false) {
			return false;
		}
		String nodeS = nodeMatch.group(1);
		int n = Integer.parseInt(nodeS);
		current = new Node(n);
		nodes.put(current.getNodeIndex(), current);
		return true;
	}

	/**
	 * Open the file for scanning.
	 * @param filePath
	 *            path to the file.
	 * @return the scanner.
	 */
	private Scanner createScanner(final String filePath) {
		File file = new File(filePath);
		Scanner fs = null;
		if (file.exists() == false) {
			log.error(filePath + " does not exist");
			return null;
		}
		if (file.canRead() == false) {
			log.error(filePath + " cannot be read");
			return null;
		}
		try {
			fs = new Scanner(file);
		} catch (FileNotFoundException e) {
			return null; // we'll never get here.
		}
		return fs;
	}

	/**
	 * @return the nodes
	 */
	public final Map<Integer, Node> getNodes() {
		return nodes;
	}

	/**
	 * Parse the OpenSees print results file.
	 */
	public final void parse() {
		while (fileScan.hasNextLine()) {
			String line = fileScan.nextLine();
			if (checkForNode(line)) {
				continue;
			}
			if (checkForMass(line)) {
				continue;
			}
			if (checkForConstraint(line)) {
				continue;
			}
			checkForLoad(line);
		}
	}

	/**
	 * Converts a string to an integer.
	 * @param token
	 *            the string.
	 * @return the integer.
	 */
	private int parseInt(final String token) {
		Integer result;
		try {
			result = new Integer(token);
		} catch (NumberFormatException e) {
			log.error("\"" + token + "\" is not an integer", e);
			return -1;
		}
		return result.intValue();
	}

	/**
	 * Parse a line of six double values.
	 * @param line
	 *            to parse.
	 * @return list of doubles.
	 */
	private List<Double> parseNumberLine(final String line) {
		Matcher numMatch = numPattern.matcher(line);
		List<Double> result = new ArrayList<Double>();
		while (numMatch.find()) {
			String numS = numMatch.group(1);
			Double n = new Double(numS);
			result.add(n);
		}
		return result;
	}
}
