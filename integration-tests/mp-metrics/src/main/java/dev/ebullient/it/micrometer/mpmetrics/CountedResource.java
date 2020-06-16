package dev.ebullient.it.micrometer.mpmetrics;

import org.eclipse.microprofile.metrics.annotation.Counted;

@Counted(description = "called for each discovered prime number")
public class CountedResource {

    CountedResource() {
    }

    public void called() {
    }
}
