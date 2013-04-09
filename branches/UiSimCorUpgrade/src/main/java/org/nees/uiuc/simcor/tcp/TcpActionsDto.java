package org.nees.uiuc.simcor.tcp;

import java.util.Date;

import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class TcpActionsDto {
	public enum ActionsType {CLOSE, CONNECT, EXIT,NONE,READ,WRITE}
	private ActionsType action = ActionsType.NONE;

	TcpError error = new TcpError();

	Msg2Tcp msg = new Msg2Tcp();;
	private boolean stillReading = false;
	public TcpActionsDto() {
		super();
	}
	public TcpActionsDto(TcpActionsDto tdto) {
		action = tdto.action;
		msg = tdto.msg;
		error = tdto.error;
		stillReading = tdto.stillReading;
	}
	public ActionsType getAction() {
		return action;
	}
	public TcpError getError() {
		return error;
	}
	
	public Msg2Tcp getMsg() {
		return msg;
	}

	/**
	 * @return the stillReading
	 */
	public boolean isStillReading() {
		return stillReading;
	}
	
	public void setAction(ActionsType action) {
		this.action = action;
	}
	public void setAction(String action) {
		this.action = ActionsType.valueOf(action);
	}
	public void setError(TcpError error) {
		this.error = error;
	}
	public void setMsg(Msg2Tcp msg) {
		this.msg = msg;
	}
	/**
	 * @param stillReading the stillReading to set
	 */
	public void setStillReading(boolean stillReading) {
		this.stillReading = stillReading;
	}

	public void timestamp() {
		msg.getMsg().setTimestamp(new Date());
	}
	
	@Override
	public String toString() {
		String result = "/msg=" + msg.assemble();
		result += "/action=" + action;
		result += "/error=" + error;
		return result;
	}
	
}
