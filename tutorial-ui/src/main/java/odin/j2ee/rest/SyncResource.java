package odin.j2ee.rest;

import odin.j2ee.api.SynchronizedActionExecutionException;
import odin.j2ee.api.SynchronizedActionExecutor;
import odin.j2ee.rest.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("catalog")
public class SyncResource {
    private static final Logger log = LoggerFactory.getLogger(SyncResource.class);

    @EJB
    private SynchronizedActionExecutor synchronizedExecutor;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("import")
    public Response importProduct(Product product) {
        log.debug("importing product");

        try {
            synchronizedExecutor.executeSynchronized(() -> {
                log.debug("import started");

                sleepQuiet(5000);
                if (Math.abs(-1) == 1) {
                    throw new RuntimeException("import failed");
                }

                log.debug("import completed");

                return null;
            });

            return Response.ok(product).build();
        } catch (SynchronizedActionExecutionException ex) {
            log.error(
                    "synchronized action execution failed at: {} with message {}",
                    ex.getTimestamp(),
                    ex.getMessage());

            return Response.serverError().build();
        }
    }

    private void sleepQuiet(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException intrEx) {
            log.debug("sleep interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
