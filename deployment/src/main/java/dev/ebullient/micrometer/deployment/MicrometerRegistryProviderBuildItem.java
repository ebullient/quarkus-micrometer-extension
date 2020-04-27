package dev.ebullient.micrometer.deployment;

import org.jboss.jandex.ClassInfo;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.quarkus.builder.item.MultiBuildItem;

public final class MicrometerRegistryProviderBuildItem extends MultiBuildItem {

    final Class<?> providedRegistryClass;

    public MicrometerRegistryProviderBuildItem(ClassInfo provider) {
        this.providedRegistryClass = MicrometerRecorder.getClassForName(provider.name().toString());
    }

    public MicrometerRegistryProviderBuildItem(Class<?> providedRegistryClass) {
        this.providedRegistryClass = providedRegistryClass;
    }

    public Class<?> getProvidedRegistryClass() {
        return providedRegistryClass;
    }

    @Override
    public String toString() {
        return "MicrometerRegistryProviderBuildItem{"
                + providedRegistryClass.getName()
                + '}';
    }
}
