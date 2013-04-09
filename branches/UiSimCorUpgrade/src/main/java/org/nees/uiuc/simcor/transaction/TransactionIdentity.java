package org.nees.uiuc.simcor.transaction;

import java.util.Date;

import org.nees.uiuc.timeformats.TransSimCorDateFormat;

public class TransactionIdentity {

	public enum StepTypes { CORRECTIONSTEP, STEP, SUBSTEP }
	private int correctionStep = 0;;
	private int step = -1;
	private int subStep = 0;
	private TransSimCorDateFormat transformat = new TransSimCorDateFormat();
	private String transId;; 
	public TransactionIdentity() {
	}
	public TransactionIdentity(TransactionIdentity i) {
		step = i.step;
		subStep = i.subStep;
		if(i.transId != null) {
			transId = new String(i.transId);
		}
	}
	public void createTransId() {
		Date now =	new Date();
		transId = transformat.format(now);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransactionIdentity == false) {
			return false;
		}
		TransactionIdentity other = (TransactionIdentity) obj;
		if (transId == null && other.transId != null) {
			return false;
		}
		if (transId == null && other.transId == null) {
			return step == other.step && subStep == other.subStep;
		}
		return transId.equals(other.transId) && step == other.step
				&& subStep == other.subStep
				&& correctionStep == other.correctionStep;

	}
	public int getCorrectionStep() {
		return correctionStep;
	}
	public int getStep() {
		return step;
	}
	public int getSubStep() {
		return subStep;
	}
	public String getTransId() {
		return transId;
	}
	public void setCorrectionStep(int correctionStep) {
		this.correctionStep = correctionStep;
	}
	public void setStep(int number) {
		step = number;
		
	}
	public void setSubStep(int number) {
		subStep = number;
		
	}
	public void setTransId(String Id) {
		transId = Id;
	}
	@Override
	public String toString() {
		if (step >= 0) {
			return transId + "[" + step + " " + subStep + " " + correctionStep + "]";
		}
		if (transId != null) {
			return transId;			
		}
		return "NO TRANSACTION ID";
	}

}
