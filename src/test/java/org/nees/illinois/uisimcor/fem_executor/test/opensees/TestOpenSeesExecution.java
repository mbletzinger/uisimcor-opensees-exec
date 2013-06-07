package org.nees.illinois.uisimcor.fem_executor.test.opensees;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.FemExecutorConfig;
import org.nees.illinois.uisimcor.fem_executor.config.dao.TemplateDAO;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
/**
 * Figures out how OpenSees does its stuff.
 * @author Michael Bletzinger
 *
 */
public class TestOpenSeesExecution {
	private String configDir;
	private Map<String, TemplateDAO> templates = new HashMap<String, TemplateDAO>();
	private FemExecutorConfig femCfg;
	
	/**
	 * See what the various recorder formats look like.
	 */
  @Test
  public void testOpensSeesRecorderFormats() {
  }
  /**
   * Set up template names.
   */
  @BeforeClass
  public void beforeClass() {
  }
/**
 * Clean up work directories.
 */
  @AfterClass
  public void afterClass() {
  }

}
