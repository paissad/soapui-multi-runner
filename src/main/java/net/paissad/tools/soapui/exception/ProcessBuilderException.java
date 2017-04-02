package net.paissad.tools.soapui.exception;

public class ProcessBuilderException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProcessBuilderException(String errMsg, Exception e) {
		super(errMsg, e);
	}

}
