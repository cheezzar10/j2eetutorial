package odin.j2ee.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class SynchronizedActionExecutionLogger {
    private static final Logger log = LoggerFactory.getLogger(SynchronizedActionExecutorBean.class);

    @AroundInvoke
    public Object logSynchronizedExecution(InvocationContext context) throws Exception {
        try {
            log.debug("acquiring synchronized executor lock");

            return context.proceed();
        } finally {
            log.debug("synchronized executor lock released");
        }
    }
}
