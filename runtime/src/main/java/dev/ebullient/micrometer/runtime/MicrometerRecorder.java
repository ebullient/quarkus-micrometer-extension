package dev.ebullient.micrometer.runtime;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MicrometerRecorder {
    private static final Logger LOGGER = Logger.getLogger(MicrometerRecorder.class.getName());

    public void dropRegistriesAtShutdown(ShutdownContext shutdownContext) {
        // shutdownContext.addShutdownTask(MetricRegistries::dropAll);
    }
}
