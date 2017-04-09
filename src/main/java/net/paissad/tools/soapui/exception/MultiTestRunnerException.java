package net.paissad.tools.soapui.exception;

public class MultiTestRunnerException extends Exception {

	private static final long serialVersionUID = 1L;

	public MultiTestRunnerException(String errMsg, Exception e) {
		super(errMsg, e);
	}

	public MultiTestRunnerException(String errMsg) {
		this(errMsg, null);
	}

}
