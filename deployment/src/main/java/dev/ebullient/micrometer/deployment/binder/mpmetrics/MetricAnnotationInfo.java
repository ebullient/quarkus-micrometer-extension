package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.*;
import org.jboss.logging.Logger;

public class MetricAnnotationInfo {
    private static final Logger log = Logger.getLogger(MetricAnnotationInfo.class);

    List<AnnotationValue> output = new ArrayList<>();

    String name;
    String description;
    String[] tags;

    MetricAnnotationInfo(AnnotationInstance input, IndexView index, ClassInfo classInfo, MethodInfo method) {
        AnnotationValue value;

        output.add(input.valueWithDefault(index, "displayName"));

        // Remember the unit
        value = input.valueWithDefault(index, "unit");
        output.add(value);
        String unit = value.asString();
        if ("none".equalsIgnoreCase(unit)) {
            unit = "";
        }

        // Remember absolute
        value = input.valueWithDefault(index, "absolute");
        output.add(value);
        boolean absolute = value.asBoolean();

        // Assign a name
        name = input.valueWithDefault(index, "name").asString();
        if (!absolute) {
            // Generate a name: micrometer conventions for dotted strings
            if (name.isEmpty()) {
                String methodName = method == null ? "<method>" : method.name();
                name = createMetricName(classInfo.simpleName(), methodName, unit);
            } else {
                name = createMetricName(classInfo.simpleName(), name, unit);
            }
        }
        output.add(AnnotationValue.createStringValue("name", name));

        description = input.valueWithDefault(index, "description").asString();
        if (description.isEmpty()) {
            description = name;
        }
        output.add(AnnotationValue.createStringValue("description", description));

        tags = createTags(input, index);
        AnnotationValue[] tagValues = new AnnotationValue[tags.length];
        for (int i = 0; i < tags.length; i++) {
            tagValues[i] = AnnotationValue.createStringValue("tags", tags[i]);
        }
        output.add(AnnotationValue.createArrayValue("tags", tagValues));

        log.infof("%s --> name='%s', description='%s', tags='%s'", input, name, description, Arrays.asList(tags));
    }

    static String createMetricName(String... values) {
        StringBuilder b = new StringBuilder();
        for (String s : values) {
            if (b.length() > 0 && !s.isEmpty()) {
                b.append('.');
            }
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (Character.isUpperCase(ch)) {
                    if (i > 0) {
                        b.append('.');
                    }
                    b.append(Character.toLowerCase(ch));
                } else {
                    b.append(ch);
                }
            }
        }
        return b.toString();
    }

    static String[] createTags(AnnotationInstance annotation, IndexView index) {
        List<String> tags = new ArrayList<>();
        tags.add("scope");
        tags.add("application");

        for (String s : annotation.valueWithDefault(index, "tags").asStringArray()) {
            // separate key=value strings into two parts
            int pos = s.indexOf('=');
            if (pos > 0 && s.length() > 2) {
                tags.add(s.substring(0, pos));
                tags.add(s.substring(pos + 1));
            } else {
                tags.add(s);
            }
        }
        if (tags.size() % 2 != 0) {
            log.warnf("Problem parsing tag values from %s", annotation);
        }
        return tags.toArray(new String[tags.size()]);
    }

    public AnnotationValue[] getAnnotationValues() {
        return output.toArray(new AnnotationValue[0]);
    }
}
