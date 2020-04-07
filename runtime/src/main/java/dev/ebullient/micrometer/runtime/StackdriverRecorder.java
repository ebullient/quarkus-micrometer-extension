package dev.ebullient.micrometer.runtime;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StackdriverRecorder {

    public BeanContainerListener setStackdriverConfig(StackdriverConfig config) {
        return beanContainer -> {
            StackdriverMeterRegistryProvider provider = beanContainer.instance(StackdriverMeterRegistryProvider.class);
            provider.setStackdriverConfig(config);
        };
    }

}
