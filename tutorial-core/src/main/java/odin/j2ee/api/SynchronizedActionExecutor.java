package odin.j2ee.api;

import java.util.function.Supplier;

public interface SynchronizedActionExecutor {
    <R> R executeSynchronized(Supplier<R> action);
}
