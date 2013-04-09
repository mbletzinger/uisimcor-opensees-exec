package org.nees.uiuc.simcor.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  StateActionsTest.class,
  ListenerStateMachineTest.class,
  TransactionTest.class,
  TriggerTest.class,
  BroadcastTest.class
})

public class TransactionAndTriggerTests {

}
