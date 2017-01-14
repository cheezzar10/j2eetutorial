package odin.j2ee.api;

public class ReceivingFailedException extends Exception {
	private static final long serialVersionUID = -6336970352469609284L;

	public ReceivingFailedException(String subscriptionId, Throwable cause) {
		super("failed to receive notifications using subscription: " + subscriptionId, cause);
	}
}
