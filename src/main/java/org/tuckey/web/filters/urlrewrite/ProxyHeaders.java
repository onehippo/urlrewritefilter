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
    X_FORWARDED_PROTO("X-Forwarded-Proto"),
    /**
     * When used, the system will track the original client-directed context URI for the request. This is the context prefix path for which cookies, redirects, links, and similar content should be written.
     *
     * @see <a href="https://github.com/spring-projects/spring-framework/blob/7cf1ccc41540752a16e370b5dd353b94108ebf22/spring-web/src/main/java/org/springframework/web/filter/ForwardedHeaderFilter.java#L346">ForwardedHeaderFilter (Spring's handling of X-Forwarded-Prefix)</a>
     * @see <a href="https://docs.humio.com/integrations/proxies/nginx/">Humio NGINX reverse proxy integration (example X-Forwarded-Prefix usage)</a>
     * @see <a href="https://docs.traefik.io/middlewares/stripprefix/">Traefik "StripPrefix" middleware (example X-Forwarded-Prefix usage)</a>
     * @see <a href="https://github.com/envoyproxy/envoy/issues/5528">Requested X-Forwarded-Prefix for Envoy</a>
     */
    X_FORWARDED_PREFIX("X-Forwarded-Prefix");

    public static final String INCLUDE_ALL = "All";

    private final String headerName;

    ProxyHeaders(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return this.headerName;
    }
}
