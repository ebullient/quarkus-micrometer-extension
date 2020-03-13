package dev.ebullient.it.micrometer.prometheus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.micrometer.core.instrument.MeterRegistry;

@Path("/message")
public class MessageResource {

    private final MeterRegistry registry;

    public MessageResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    public String message() {
        return registry.getClass().getName();
    }

}
