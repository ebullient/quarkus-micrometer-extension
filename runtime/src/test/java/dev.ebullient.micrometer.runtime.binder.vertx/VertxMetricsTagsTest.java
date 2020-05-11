package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.micrometer.core.instrument.Tag;
import io.vertx.core.http.HttpServerResponse;

/**
 * Test tag creation
 */
public class VertxMetricsTagsTest {

    @Mock
    HttpServerResponse response;

    final List<Pattern> NO_IGNORE_PATTERNS = Collections.emptyList();
    final List<Pattern> NO_MATCH_PATTERNS = Collections.emptyList();

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParsePathNoIgnorePatterns() {
        Assertions.assertEquals("/", VertxMetricsTags.parseUriPath(NO_IGNORE_PATTERNS, "//"));
        Assertions.assertEquals("/", VertxMetricsTags.parseUriPath(NO_IGNORE_PATTERNS, ""));
        Assertions.assertEquals("/path/with/no/leading/slash",
                VertxMetricsTags.parseUriPath(NO_IGNORE_PATTERNS, "path/with/no/leading/slash"));
        Assertions.assertEquals("/path/with/query/string",
                VertxMetricsTags.parseUriPath(NO_IGNORE_PATTERNS, "/path/with/query/string?stuff"));
    }

    @Test
    public void testParsePathWithIgnorePatterns() {
        List<Pattern> ignorePatterns = Arrays.asList(Pattern.compile("/ignore.*"));

        Assertions.assertNull(VertxMetricsTags.parseUriPath(ignorePatterns, "ignore/me/with/no/leading/slash"));
        Assertions.assertNull(VertxMetricsTags.parseUriPath(ignorePatterns, "/ignore/me/with/query/string?stuff"));
    }

    @Test
    public void testStatus() {
        Mockito.when(response.getStatusCode()).thenReturn(200);
        Assertions.assertEquals(Tag.of("status", "200"), VertxMetricsTags.status(response));

        Mockito.when(response.getStatusCode()).thenReturn(301);
        Assertions.assertEquals(Tag.of("status", "301"), VertxMetricsTags.status(response));

        Mockito.when(response.getStatusCode()).thenReturn(304);
        Assertions.assertEquals(Tag.of("status", "304"), VertxMetricsTags.status(response));
    }

    @Test
    public void testUriRedirect() {
        Mockito.when(response.getStatusCode()).thenReturn(301);
        Assertions.assertEquals(VertxMetricsTags.URI_REDIRECTION, VertxMetricsTags.uri(NO_MATCH_PATTERNS, "/moved", response));

        Mockito.when(response.getStatusCode()).thenReturn(302);
        Assertions.assertEquals(VertxMetricsTags.URI_REDIRECTION, VertxMetricsTags.uri(NO_MATCH_PATTERNS, "/moved", response));

        Mockito.when(response.getStatusCode()).thenReturn(304);
        Assertions.assertEquals(VertxMetricsTags.URI_REDIRECTION, VertxMetricsTags.uri(NO_MATCH_PATTERNS, "/moved", response));
    }

    @Test
    public void testUriNotFound() {
        Mockito.when(response.getStatusCode()).thenReturn(404);
        Assertions.assertEquals(VertxMetricsTags.URI_NOT_FOUND, VertxMetricsTags.uri(NO_MATCH_PATTERNS, "/invalid", response));
        Assertions.assertEquals(Tag.of("status", "404"), VertxMetricsTags.status(response));
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
