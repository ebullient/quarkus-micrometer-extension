package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

public interface AnnotatedGaugeAdapter extends org.eclipse.microprofile.metrics.Gauge<Number> {
    String name();

    String description();

    String[] tags();

    String baseUnit();

    /**
     * Generic base instance of an AnnotatedGaugeAdapter.
     * Generated beans extend this base.
     */
    public static abstract class GaugeAdapterImpl implements AnnotatedGaugeAdapter {
        String name;
        String description;
        String baseUnit;
        String[] tags;

        public GaugeAdapterImpl(String name, String description, String[] tags) {
            this(name, description, null, tags);
        }

        public GaugeAdapterImpl(String name, String description, String baseUnit, String[] tags) {
            this.name = name;
            this.description = description;
            this.baseUnit = baseUnit;
            this.tags = tags;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public String baseUnit() {
            return baseUnit;
        }

        public String[] tags() {
            return tags;
        }

        public String toString() {
            return this.getClass().getName()
                    + "[ name=" + name
                    + ", desc=" + description
                    + ", tags=" + tags
                    + "]";
        }
    }
}
