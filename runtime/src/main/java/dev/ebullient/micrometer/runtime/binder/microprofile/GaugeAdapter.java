package dev.ebullient.micrometer.runtime.binder.microprofile;

public interface GaugeAdapter extends org.eclipse.microprofile.metrics.Gauge<Number> {
    String name();

    String description();

    String[] tags();

    /**
     * Generic base instance of a GaugeAdapter.
     * Generated beans extend this base.
     */
    public static abstract class GaugeAdapterImpl implements GaugeAdapter {
        String name;
        String description;
        String[] tags;

        public GaugeAdapterImpl(String name, String description, String[] tags) {
            this.name = name;
            this.description = description;
            this.tags = tags;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
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
