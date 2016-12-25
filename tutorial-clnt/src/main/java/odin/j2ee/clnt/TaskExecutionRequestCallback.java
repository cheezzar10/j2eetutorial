package odin.j2ee.clnt;

import java.util.concurrent.CountDownLatch;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

public class TaskExecutionRequestCallback implements InvocationCallback<Response> {
	private final CountDownLatch done;

	public TaskExecutionRequestCallback(CountDownLatch done) {
		this.done = done;
	}

	@Override
	public void completed(Response response) {
		System.out.printf("[%s] task execution request completed with status: %d%n", 
				Thread.currentThread().getName(), response.getStatus());
		done.countDown();
	}

	@Override
	public void failed(Throwable throwable) {
		System.out.printf("[%s] task execution request failed: %s%n", 
				Thread.currentThread().getName(), throwable.getMessage());
		done.countDown();
	}
}
