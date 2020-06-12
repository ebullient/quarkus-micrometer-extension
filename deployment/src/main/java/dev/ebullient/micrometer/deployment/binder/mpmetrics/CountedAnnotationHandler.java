package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import io.quarkus.gizmo.ClassOutput;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

/**
 * Create beans to handle <code>@Counted</code> annotations.
 * This is a static utility class, it stores no state. It is ok to import and use
 * classes that reference MP Metrics classes.
 */
public class CountedAnnotationHandler {
    private static final Logger log = Logger.getLogger(CountedAnnotationHandler.class);

    static final DotName COUNTED_ANNOTATION = DotName.createSimple(Counted.class.getName());

    static void processCountedAnnotations(IndexView index, ClassOutput classOutput) {
        // @Counted applies to classes and methods
        for (AnnotationInstance annotation : index.getAnnotations(COUNTED_ANNOTATION)) {

        }

    }
}
