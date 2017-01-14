package odin.j2ee.api;

public class SubscriptionActivationFailedException extends Exception {
	private static final long serialVersionUID = -3704334772861615164L;

	public SubscriptionActivationFailedException(String sid, Throwable cause) {
		super("failed to activate notification subscription: " + sid, cause);
	}
}
