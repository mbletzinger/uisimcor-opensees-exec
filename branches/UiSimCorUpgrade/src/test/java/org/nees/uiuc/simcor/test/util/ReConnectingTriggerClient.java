package org.nees.uiuc.simcor.test.util;

import org.nees.uiuc.simcor.UiSimCorTcp;
import org.nees.uiuc.simcor.UiSimCorTcp.ConnectType;
import org.nees.uiuc.simcor.tcp.TcpParameters;

public class ReConnectingTriggerClient extends TriggerClient {

	/* (non-Javadoc)
	 * @see org.nees.uiuc.simcor.test.util.TriggerClient#run()
	 */
	@Override
	public void run() {
		super.run();
		for(int i = 0; i < 3; i++)  {
			simcor = new UiSimCorTcp(ConnectType.TRIGGER_CLIENT, "MDL-00-01",
					getClientId());
			super.run();
		}
	}

	public ReConnectingTriggerClient(TcpParameters params,
			String systemDescription) {
		super(params, systemDescription);
	}

}
