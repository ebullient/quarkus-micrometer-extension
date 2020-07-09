package dev.ebullient.micrometer.runtime.binder;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.hibernate.SessionFactory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import io.micrometer.core.instrument.binder.jpa.HibernateQueryMetrics;
import io.quarkus.hibernate.orm.runtime.JPAConfig;
import io.quarkus.runtime.StartupEvent;

@Dependent
public class HibernateMetricsProvider {
    @Inject
    MeterRegistry meterRegistry;

    @Inject
    Instance<JPAConfig> jpaConfigInstance;

    void onApplicationStart(@Observes @Priority(javax.interceptor.Interceptor.Priority.LIBRARY_BEFORE) StartupEvent event) {
        if (!jpaConfigInstance.isUnsatisfied()) {
            JPAConfig jpaConfig = jpaConfigInstance.get();
            for (String puName : jpaConfig.getPersistenceUnits()) {
                SessionFactory sessionFactory = jpaConfig.getEntityManagerFactory(puName).unwrap(SessionFactory.class);
                if (sessionFactory != null) {
                    // Configure HibernateMetrics
                    HibernateMetrics.monitor(meterRegistry, sessionFactory, puName, Tags.empty());

                    // Configure HibernateQueryMetrics
                    HibernateQueryMetrics.monitor(meterRegistry, sessionFactory, puName, Tags.empty());
                }
            }
        }
    }
}
