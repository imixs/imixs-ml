package org.imixs.ml.core;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Response;

/**
 * Redirect Helper class.
 * 
 * See: https://stackoverflow.com/questions/11305520/jersey-client-doesnt-follow-redirects
 * <p>
 * Usage: 
 * {@code
 * client.register(RedirectFilterWorkAround.class);
 * }
 * @author rsoika
 *
 */
public class RedirectFilterWorkAround implements ClientResponseFilter {
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (responseContext.getStatusInfo().getFamily() != Response.Status.Family.REDIRECTION) {
            return;
        }

        Response resp = requestContext.getClient().target(responseContext.getLocation()).request().method(requestContext.getMethod());

        responseContext.setEntityStream((InputStream) resp.getEntity());
        responseContext.setStatusInfo(resp.getStatusInfo());
        responseContext.setStatus(resp.getStatus());
    }
}