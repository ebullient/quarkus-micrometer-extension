package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Tag;

public interface GaugeAdapter extends org.eclipse.microprofile.metrics.Gauge<Number> {
    String name();

    String description();

    Iterable<Tag> tags();

    public static abstract class GaugeAdapterImpl implements GaugeAdapter {
        String name;
        String description;
        Iterable<Tag> tags;

        public GaugeAdapterImpl(String name, String description, String[] tags) {
            this.name = name;
            this.description = description;
            this.tags = createTags(tags);
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public Iterable<Tag> tags() {
            return tags;
        }

        /**
         * @param tags array of key=value strings
         * @return iterable collection of Micrometer tags
         */
        List<Tag> createTags(String[] tags) {
            List<Tag> mTags = new ArrayList<>(tags.length + 1);
            for (String s : tags) {
                int pos = s.indexOf('=');
                if (pos > 0 && s.length() > 2) {
                    mTags.add(Tag.of(s.substring(0, pos), s.substring(pos + 1)));
                }
            }
            return mTags;
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
