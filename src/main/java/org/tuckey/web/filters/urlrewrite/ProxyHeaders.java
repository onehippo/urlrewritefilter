package org.tuckey.web.filters.urlrewrite;

/**
 * The {@link ProxyHeaders} enum declares available headers which can be propagated when proxying requests.
 *
 * @author Mike Hill
 */
public enum ProxyHeaders {

    /**
     * When used, the system will continue to propagate the existing "Host" value from the original request. Otherwise, this header will be changed to the proxy target's host name.
     */
    HOST("Host"),
    /**
     * When used, the system will populate the list of proxy hops. E.g., {@code X-Forwarded-By: <Proxy 1 Host>, <Proxy 2 Host>, ...}.
     */
    X_FORWARDED_BY("X-Forwarded-By"),
    /**
     * When used, the system will track the origin IP and all proxy IPs for the request. E.g., {@code X-Forwarded-For: <Client IP>, <Proxy 1 IP>, <Proxy 2 IP>, ...}.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">MDN: X-Forwarded-For</a>
     */
    X_FORWARDED_FOR("X-Forwarded-For"),
    /**
     * When used, the system will track the original client-directed "Host" value for the request.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Host">MDN: X-Forwarded-Host</a>
     */
    X_FORWARDED_HOST("X-Forwarded-Host"),
    /**
     * When used, the system will track the original client-directed scheme for the request.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto">MDN: X-Forwarded-Proto</a>
     */
    X_FORWARDED_PROTO("X-Forwarded-Proto");

    public static final String INCLUDE_ALL = "All";

    private final String headerName;

    ProxyHeaders(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return this.headerName;
    }
}
