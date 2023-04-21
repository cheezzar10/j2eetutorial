package odin.j2ee.api;

public class SynchronizedActionExecutionException extends Exception {
    private final String message;
    private final long timestamp;

    public SynchronizedActionExecutionException(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
