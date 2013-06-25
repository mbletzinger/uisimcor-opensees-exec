package org.nees.illinois.uisimcor.fem_executor.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.nees.illinois.uisimcor.fem_executor.response.OpenSeesErrorFilter;
import org.nees.illinois.uisimcor.fem_executor.response.ResponseFilterI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the error filter for OpenSeesErrors.
 * @author Michael Bletzinger
 */
public class TestErrorFilter {
	/**
	 * Test errors.
	 */
	private final List<String> errors = new ArrayList<String>();
	/**
	 * Expected filter flag.
	 */
	private final List<Boolean> expected = new ArrayList<Boolean>();
	/**
	 * Expected extraction.
	 */
	private final List<String> expectedGet = new ArrayList<String>();
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(TestErrorFilter.class);

	/**
	 * Test the error filter for OpenSees errors.
	 */
	@Test
	public final void testFilter() {
		ResponseFilterI filter = new OpenSeesErrorFilter();
		for (int e = 0; e < errors.size(); e++) {
			log.info("Checking string \"" + errors.get(e) + "\"");
			Assert.assertEquals(expected.get(e).booleanValue(),
					filter.filter(errors.get(e)));
			Assert.assertEquals(expectedGet.get(e), filter.get());
		}
	}

	/**
	 * Fill in all of the list.
	 */
	@BeforeTest
	public final void setup() {
		errors.add(null);
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add("");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add("\n");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" ");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add("\t");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add("  ");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" 	 OpenSees -- Open System For Earthquake Engineering Simulation");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" 	Pacific Earthquake Engineering Research Center -- 2.4.0");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" 	    (c) Copyright 1999,2000 The Regents of the University of California");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" 				 All Rights Reserved");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add("     (Copyright and Disclaimer @ http://www.berkeley.edu/OpenSees/copyright.html)");
		expected.add(new Boolean(false));
		expectedGet.add(null);
		errors.add(" WARNING - ForceBeamColumn2d::update - failed to get compatible element forces & deformations for element: 1(dW: << 1.1945e-10)");
		expected.add(new Boolean(true));
		expectedGet
				.add(" WARNING - ForceBeamColumn2d::update - failed to get compatible element forces & deformations for element: 1(dW: << 1.1945e-10)");
		errors.add(" Domain::update - domain failed in update");
		expected.add(new Boolean(true));
		expectedGet.add(" Domain::update - domain failed in update");

	}
}
