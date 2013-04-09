package org.nees.uiuc.simcor.listener;

import org.nees.uiuc.simcor.tcp.Connection;

public class ClientIdWithConnection extends ClientId {
	public final Connection connection;

	public ClientIdWithConnection(ClientIdWithConnection other) {
		super(other);
		this.connection = other.connection;
		
	}
	public ClientIdWithConnection(Connection connection, String system,
			String remoteHost) {
		super(system, remoteHost);
		this.connection = connection;
	}
}
