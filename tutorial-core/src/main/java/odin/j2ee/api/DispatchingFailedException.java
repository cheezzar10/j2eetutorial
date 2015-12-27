package odin.j2ee.api;

public class DispatchingFailedException extends Exception {
	private static final long serialVersionUID = -7524516400162888873L;

	public DispatchingFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
