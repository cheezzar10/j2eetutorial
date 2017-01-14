package odin.j2ee.api;

public class ReceiversLimitExceededException extends Exception {
	private static final long serialVersionUID = -3453334387562366027L;

	public ReceiversLimitExceededException(int limit) {
		super(limit + " polling receivers limit has been exceeded");
	}
}
