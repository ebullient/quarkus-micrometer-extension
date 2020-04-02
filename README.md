# quarkus-micrometer-extension

This is a quarkus extension that performs build time initialization, configuration, and injection of registries for micrometer. This is not yet a comprehensive implementation. Support for registry implementations will be incremental, starting with Prometheus and JMX (JVM-mode only).

## Using a SNAPSHOT

SNAPSHOT releases use JitPack:

```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <properties>
    <quarkus-micrometer-extension.version>1.0.0-SNAPSHOT</quarkus-micrometer-extension.version>
    <micrometer.version>1.3.5</micrometer.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>dev.ebullient.quarkus-micrometer-extension</groupId>
      <artifactId>quarkus-micrometer</artifactId>
      <version>${quarkus-micrometer-extension.version}</version>
    </dependency>

    <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-core</artifactId>
      <version>${micrometer.version}</version>
    </dependency>

    <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
      <version>${micrometer.version}</version>
    </dependency>
  </dependencies>
```

A fetch may take a bit if the snapshot has been updated, and you're the first to try grabbing it.
