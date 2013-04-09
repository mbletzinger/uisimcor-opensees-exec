package org.nees.uiuc.simcor.test.util;

import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class TransactionIdentityWithTestFlags extends TransactionIdentity {
	private boolean correctionStepExists = false;
	private boolean stepExists = false;
	private boolean subStepExists = false;
	private boolean transIdExists = false;

	@Override
	public void setCorrectionStep(int correctionStep) {
		correctionStepExists = true;
		super.setCorrectionStep(correctionStep);
	}

	@Override
	public void setStep(int number) {
		stepExists = true;
		super.setStep(number);
	}

	@Override
	public void setSubStep(int number) {
		subStepExists = true;
		super.setSubStep(number);
	}

	@Override
	public void setTransId(String Id) {
		transIdExists = true;
		super.setTransId(Id);
	}

	public boolean isCorrectionStepExists() {
		return correctionStepExists;
	}

	public boolean isStepExists() {
		return stepExists;
	}

	public boolean isSubStepExists() {
		return subStepExists;
	}

	public boolean isTransIdExists() {
		return transIdExists;
	}

}
