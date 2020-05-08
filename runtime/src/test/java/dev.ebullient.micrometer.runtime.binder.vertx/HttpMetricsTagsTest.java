package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinder.VertxHttpMetricsConfig;
import io.micrometer.core.instrument.Tag;
import io.vertx.core.http.HttpServerResponse;

/**
 * Test tag creation
 */
public class HttpMetricsTagsTest {

    @Mock
    HttpServerResponse response;

    @Mock
    VertxHttpMetricsConfig config;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParsePathNoIgnorePatterns() {
        Mockito.when(config.getIgnorePatterns()).thenReturn(Collections.emptyList());
        Assertions.assertEquals("/", HttpMetricsTags.parseUriPath(config, "//"));
        Assertions.assertEquals("/", HttpMetricsTags.parseUriPath(config, ""));
        Assertions.assertEquals("/path/with/no/leading/slash",
                HttpMetricsTags.parseUriPath(config, "path/with/no/leading/slash"));
        Assertions.assertEquals("/path/with/query/string",
                HttpMetricsTags.parseUriPath(config, "/path/with/query/string?stuff"));
    }

    @Test
    public void testParsePathWithIgnorePatterns() {
        Mockito.when(config.getIgnorePatterns()).thenReturn(Arrays.asList(Pattern.compile("/ignore.*")));
        Assertions.assertNull(HttpMetricsTags.parseUriPath(config, "ignore/me/with/no/leading/slash"));
        Assertions.assertNull(HttpMetricsTags.parseUriPath(config, "/ignore/me/with/query/string?stuff"));
    }

    @Test
    public void testStatus() {
        Mockito.when(response.getStatusCode()).thenReturn(200);
        Assertions.assertEquals(Tag.of("status", "200"), HttpMetricsTags.status(response));

        Mockito.when(response.getStatusCode()).thenReturn(301);
        Assertions.assertEquals(Tag.of("status", "301"), HttpMetricsTags.status(response));

        Mockito.when(response.getStatusCode()).thenReturn(304);
        Assertions.assertEquals(Tag.of("status", "304"), HttpMetricsTags.status(response));
    }

    @Test
    public void testUriRedirect() {
        Mockito.when(response.getStatusCode()).thenReturn(301);
        Assertions.assertEquals(HttpMetricsTags.URI_REDIRECTION, HttpMetricsTags.uri(config, "/moved", response));

        Mockito.when(response.getStatusCode()).thenReturn(302);
        Assertions.assertEquals(HttpMetricsTags.URI_REDIRECTION, HttpMetricsTags.uri(config, "/moved", response));

        Mockito.when(response.getStatusCode()).thenReturn(304);
        Assertions.assertEquals(HttpMetricsTags.URI_REDIRECTION, HttpMetricsTags.uri(config, "/moved", response));
    }

    @Test
    public void testUriNotFound() {
        Mockito.when(response.getStatusCode()).thenReturn(404);
        Assertions.assertEquals(HttpMetricsTags.URI_NOT_FOUND, HttpMetricsTags.uri(config, "/invalid", response));
        Assertions.assertEquals(Tag.of("status", "404"), HttpMetricsTags.status(response));
    }

    //    @Test
    //    public void testUriRootOk() {
    //        Mockito.when(response.getStatusCode()).thenReturn(200);
    //        Assertions.assertEquals(HttpMetricsTags.URI_ROOT, HttpMetricsTags.uri(config, "/", response));
    //    }

    //    @Test
    //    public void testUriDefaults() {
    //        Mockito.when(response.getStatusCode()).thenReturn(200);
    //        Assertions.assertEquals(Tag.of("uri", "/known"), HttpMetricsTags.uri(config, "/known/ok", response));
    //
    //        Mockito.when(response.getStatusCode()).thenReturn(400);
    //        Assertions.assertEquals(Tag.of("uri", "/known"), HttpMetricsTags.uri(config, "/known/bad/request", response));
    //
    //        Mockito.when(response.getStatusCode()).thenReturn(500);
    //        Assertions.assertEquals(Tag.of("uri", "/known"), HttpMetricsTags.uri(config, "/known/server/error", response));
    //    }

    //    @Test
    //    public void testUriWhitelistMatch() {
    //        Mockito.when(response.getStatusCode()).thenReturn(200);
    //    }
    //
    //    @Test
    //    public void testUriIgnore() {
    //        Mockito.when(response.getStatusCode()).thenReturn(200);
    //
    //    }

}
