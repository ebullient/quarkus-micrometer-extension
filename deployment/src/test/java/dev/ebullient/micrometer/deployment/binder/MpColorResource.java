package dev.ebullient.micrometer.deployment.binder;

import org.eclipse.microprofile.metrics.annotation.Counted;

public class MpColorResource {
    @Counted
    public void red() {
        // ...
    }

    @Counted(name = "blueCount")
    public void blue() {
        // ...
    }

    @Counted(name = "greenCount", absolute = true)
    public void green() {
        // ...
    }

    @Counted(absolute = true)
    public void yellow() {
        // ...
    }
}
