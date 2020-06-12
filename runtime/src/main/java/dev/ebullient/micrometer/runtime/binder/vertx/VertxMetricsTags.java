package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.List;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Tag;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class VertxMetricsTags {
    private static final Logger log = Logger.getLogger(VertxMetricsTags.class);

    static final Tag URI_NOT_FOUND = Tag.of("uri", "NOT_FOUND");
    static final Tag URI_REDIRECTION = Tag.of("uri", "REDIRECTION");
    static final Tag URI_ROOT = Tag.of("uri", "root");
    static final Tag URI_UNKNOWN = Tag.of("uri", "UNKNOWN");

    static final Tag STATUS_UNKNOWN = Tag.of("status", "UNKNOWN");
    static final Tag STATUS_RESET = Tag.of("status", "RESET");

    static final Tag OUTCOME_INFORMATIONAL = Tag.of("outcome", "INFORMATIONAL");
    static final Tag OUTCOME_SUCCESS = Tag.of("outcome", "SUCCESS");
    static final Tag OUTCOME_REDIRECTION = Tag.of("outcome", "REDIRECTION");
    static final Tag OUTCOME_CLIENT_ERROR = Tag.of("outcome", "CLIENT_ERROR");
    static final Tag OUTCOME_SERVER_ERROR = Tag.of("outcome", "SERVER_ERROR");
    static final Tag OUTCOME_UNKNOWN = Tag.of("outcome", "UNKNOWN");

    static final Tag METHOD_UNKNOWN = Tag.of("method", "UNKNOWN");

    private static final Pattern TRAILING_SLASH_PATTERN = Pattern.compile("/$");

    private static final Pattern MULTIPLE_SLASH_PATTERN = Pattern.compile("//+");

    /**
     * Creates a {@code method} tag based on the {@link HttpServerRequest#method()}
     * method} of the given {@code request}.
     *
     * @param method the request method
     * @return the method tag whose value is a capitalized method (e.g. GET).
     */
    public static Tag method(HttpMethod method) {
        return (method != null) ? Tag.of("method", method.toString()) : METHOD_UNKNOWN;
    }

    /**
     * Creates a {@code status} tag based on the status of the given {@code response}.
     *
     * @param response the HTTP response
     * @return the status tag derived from the status of the response
     */
    public static Tag status(HttpServerResponse response) {
        return (response != null) ? Tag.of("status", Integer.toString(response.getStatusCode())) : STATUS_UNKNOWN;
    }

    /**
     * Creates an {@code outcome} {@code Tag} derived from the given {@code response}.
     *
     * @param response the response
     * @return the outcome tag
     */
    public static Tag outcome(HttpServerResponse response) {
        if (response != null) {
            int codeFamily = response.getStatusCode() / 100;
            switch (codeFamily) {
                case 1:
                    return OUTCOME_INFORMATIONAL;
                case 2:
                    return OUTCOME_SUCCESS;
                case 3:
                    return OUTCOME_REDIRECTION;
                case 4:
                    return OUTCOME_CLIENT_ERROR;
                case 5:
                    return OUTCOME_SERVER_ERROR;
            }
        }
        return OUTCOME_UNKNOWN;
    }

    /**
     * Creates a {@code uri} tag based on the URI of the given {@code request}.
     * Falling back to {@code REDIRECTION} for 3xx responses, {@code NOT_FOUND}
     * for 404 responses, {@code root} for requests with no path info, and {@code UNKNOWN}
     * for all other requests.
     *
     *
     * @param pathInfo
     * @param response the response
     * @return the uri tag derived from the request
     */
    public static Tag uri(String pathInfo, HttpServerResponse response) {
        if (response != null) {
            int code = response.getStatusCode();
            if (code / 100 == 3) {
                return URI_REDIRECTION;
            } else if (code == 404) {
                return URI_NOT_FOUND;
            }
        }
        if (pathInfo == null) {
            return URI_UNKNOWN;
        }
        if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return URI_ROOT;
        }

        // Use first segment of request path
        return Tag.of("uri", pathInfo);
    }

    /**
     * Extract the path out of the uri. Return null if the path should be
     * ignored.
     */
    static String parseUriPath(List<Pattern> matchPattern, List<Pattern> ignorePatterns, String uri) {
        if (uri == null) {
            return null;
        }

        String path = "/" + extractPath(uri);
        path = MULTIPLE_SLASH_PATTERN.matcher(path).replaceAll("/");
        path = TRAILING_SLASH_PATTERN.matcher(path).replaceAll("");

        if (path.isEmpty()) {
            path = "/";
        }

        // Compare path against "ignore this path" patterns
        for (Pattern p : ignorePatterns) {
            if (p.matcher(path).matches()) {
                log.debugf("Path %s ignored; matches pattern %s", uri, p.pattern());
                return null;
            }
        }
        return path;
    }

    private static String extractPath(String uri) {
        if (uri.isEmpty()) {
            return uri;
        }
        int i;
        if (uri.charAt(0) == '/') {
            i = 0;
        } else {
            i = uri.indexOf("://");
            if (i == -1) {
                i = 0;
            } else {
                i = uri.indexOf('/', i + 3);
                if (i == -1) {
                    // contains no /
                    return "/";
                }
            }
        }

        int queryStart = uri.indexOf('?', i);
        if (queryStart == -1) {
            queryStart = uri.length();
        }
        return uri.substring(i, queryStart);
    }
}
