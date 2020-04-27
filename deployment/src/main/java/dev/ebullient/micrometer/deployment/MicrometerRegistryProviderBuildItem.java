package dev.ebullient.micrometer.deployment;

import org.jboss.jandex.ClassInfo;

import io.quarkus.builder.item.MultiBuildItem;

public final class MicrometerRegistryProviderBuildItem extends MultiBuildItem {

    final String className;

    public MicrometerRegistryProviderBuildItem(ClassInfo provider) {
        this.className = provider.name().toString();
    }

    public MicrometerRegistryProviderBuildItem(Class<?> providedRegistryClass) {
        this.className = providedRegistryClass.getName();
    }

    public MicrometerRegistryProviderBuildItem(String returnType) {
        this.className = returnType;
    }

    public String getRegistryClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "MicrometerRegistryProviderBuildItem{"
                + className
                + '}';
    }
}
