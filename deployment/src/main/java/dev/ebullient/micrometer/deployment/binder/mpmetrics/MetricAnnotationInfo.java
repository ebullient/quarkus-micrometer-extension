package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.quarkus.arc.processor.DotNames;

public class MetricAnnotationInfo {
    private static final Logger log = Logger.getLogger(MetricAnnotationInfo.class);

    List<AnnotationValue> output = new ArrayList<>();

    String name;
    String description;
    String[] tags;

    MetricAnnotationInfo(AnnotationInstance input, IndexView index, ClassInfo classInfo, MethodInfo method) {
        output.add(input.valueWithDefault(index, "displayName"));

        // Remember the unit
        AnnotationValue value = input.valueWithDefault(index, "unit");
        output.add(value);
        String unit = value.asString();
        if ("none".equalsIgnoreCase(unit)) {
            unit = "";
        }

        // Remember absolute
        value = input.valueWithDefault(index, "absolute");
        output.add(value);
        boolean absolute = value.asBoolean();

        // Assign a name. Start with the name in the annotation...
        name = input.valueWithDefault(index, "name").asString();
        if (input.target().kind() == AnnotationTarget.Kind.METHOD) {
            if (absolute) {
                name = name.isEmpty() ? method.name() : name;
            } else {
                name = append(classInfo.name().toString(), name.isEmpty() ? method.name() : name);
            }
        }
        if (input.target().kind() == AnnotationTarget.Kind.CLASS) {
            String methodName = method == null ? "<method>" : method.name();
            log.debugf("### %s %s %s", name, classInfo, methodName);
            if (absolute) {
                name = append(name.isEmpty() ? classInfo.simpleName() : name, methodName);
            } else {
                DotName className = classInfo.name();
                name = append(name.isEmpty() ? DotNames.packageName(className) : className.toString(), methodName);
            }
        }

        output.add(AnnotationValue.createStringValue("name", name));

        description = input.valueWithDefault(index, "description").asString();
        if (description.isEmpty()) {
            description = (name + " " + unit).trim();
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

    static String append(String... values) {
        StringBuilder b = new StringBuilder();
        for (String s : values) {
            if (b.length() > 0 && !s.isEmpty()) {
                b.append('.');
            }
            b.append(s);
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