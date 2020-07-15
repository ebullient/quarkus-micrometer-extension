package dev.ebullient.micrometer.runtime.binder;

import org.hibernate.SessionFactory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import io.micrometer.core.instrument.binder.jpa.HibernateQueryMetrics;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.hibernate.orm.runtime.JPAConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class HibernateMetricsRecorder {

    /* RUNTIME_INIT */
    public void registerMetrics(BeanContainer beanContainer) {
        MeterRegistry meterRegistry = Metrics.globalRegistry;

        JPAConfig jpaConfig = beanContainer.instance(JPAConfig.class);
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
