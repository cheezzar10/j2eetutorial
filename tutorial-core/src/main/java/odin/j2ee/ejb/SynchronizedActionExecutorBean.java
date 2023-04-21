package odin.j2ee.ejb;

import odin.j2ee.api.SynchronizedActionExecutionException;
import odin.j2ee.api.SynchronizedActionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Singleton
@AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
public class SynchronizedActionExecutorBean implements SynchronizedActionExecutor {
    private static final Logger log = LoggerFactory.getLogger(SynchronizedActionExecutorBean.class);

    @Override
    @Interceptors({SynchronizedActionExecutionLogger.class})
    public <R> R executeSynchronized(Supplier<R> action) throws SynchronizedActionExecutionException {
        log.debug("synchronized executor lock acquired");

        try {
            return action.get();
        } catch (Exception ex) {
            throw new SynchronizedActionExecutionException(ex.getMessage(), System.currentTimeMillis());
        } finally {
            log.debug("releasing synchronized executor lock");
        }
    }
}
