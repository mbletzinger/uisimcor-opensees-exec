package org.nees.uiuc.simcor.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
  HexTest.class,
  SimCorMsgTest.class,
  ConnectionTest.class, 
  ListenerTest.class
})

public class BasicTests {
}
