/*
 * Copyright (c) 2008, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * - Neither the name tuckey.org nor the names of its contributors
 * may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.filters.urlrewrite;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * This class is responsible for a proxy http request.
 * It takes the incoming request and then it creates a new request to the target address and copies the response of that proxy request
 * to the response of the original request.
 * <p/>
 * This class uses the commons-httpclient classes from Apache.
 * <p/>
 * User: Joachim Ansorg, <jansorg@ksi.gr>
 * Date: 19.06.2008
 * Time: 16:02:54
 */
public final class RequestProxy {
    private static final Log log = Log.getLog(RequestProxy.class);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");


    /**
     * This method performs the proxying of the request to the target address.
     * <p/>
     * Cookies will not be forwarded to client.
     *
     * @param target     The target address. Has to be a fully qualified address. The request is send as-is to this address.
     * @param hsRequest  The request data which should be send to the
     * @param hsResponse The response data which will contain the data returned by the proxied request to target.
     * @throws java.io.IOException Passed on from the connection logic.
     */
    public static void execute(final String target, final HttpServletRequest hsRequest, final HttpServletResponse hsResponse) throws IOException {
        execute(target, hsRequest, hsResponse, true, false, false);
    }

    /**
     * This method performs the proxying of the request to the target address.
     *
     * @param target      The target address. Has to be a fully qualified address. The request is send as-is to this address.
     * @param hsRequest   The request data which should be send to the
     * @param hsResponse  The response data which will contain the data returned by the proxied request to target.
     * @param dropCookies Determinate whether cookies should be dropped (when {@code true}) or forwarded to client.
     * @throws java.io.IOException Passed on from the connection logic.
     */
    public static void execute(final String target, final HttpServletRequest hsRequest, final HttpServletResponse hsResponse, boolean dropCookies, boolean followRedirects, boolean useSystemProperties) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("execute, target is " + target);
            log.info("response commit state: " + hsResponse.isCommitted());
        }

        if (StringUtils.isBlank(target)) {
            log.error("The target address is not given. Please provide a target address.");
            return;
        }

        log.info("checking url");
        final URL url;
        try {
            url = new URL(target);
        } catch (MalformedURLException e) {
            log.error("The provided target url is not valid.", e);
            return;
        }

        log.info("setting up the host configuration");

        RequestConfig.Builder configBuilder = RequestConfig.custom();

        HttpHost proxyHost = getUseProxyServer((String) hsRequest.getAttribute("use-proxy"));
        if (proxyHost != null) configBuilder.setProxy(proxyHost);
        RequestConfig config = configBuilder.setRedirectsEnabled(followRedirects).build();

        if (log.isInfoEnabled()) {
            log.info("config is " + config.toString());
        }

        final HttpRequestBase targetRequest = setupProxyRequest(hsRequest, url, dropCookies);
        if (targetRequest == null) {
            log.error("Unsupported request method found: " + hsRequest.getMethod());
            return;
        }

        //perform the request to the target server
        try (CloseableHttpClient client = getHttpClient(config, useSystemProperties)) {
            if (log.isInfoEnabled()) {
                log.info("executeMethod / fetching data ...");
            }

            // holder variable, mainly to allow use of try-with-resources below
            HttpUriRequest requestParam = targetRequest;

            if (targetRequest instanceof HttpEntityEnclosingRequestBase) {
                final InputStreamEntity entity = new InputStreamEntity(
                        hsRequest.getInputStream(), hsRequest.getContentLength(), ContentType.parse(hsRequest.getContentType()));
                final HttpEntityEnclosingRequestBase entityEnclosingMethod = (HttpEntityEnclosingRequestBase) targetRequest;
                entityEnclosingMethod.setEntity(entity);
                requestParam = entityEnclosingMethod;
            }

            try (CloseableHttpResponse response = client.execute(requestParam)) {

                //copy the target response headers to our response
                setupResponseHeaders(response, hsResponse, dropCookies);

                //the body might be null, i.e. for responses with cache-headers which leave out the body, like 304 response
                if (response.getEntity() != null) {
                    final InputStream originalResponseStream = response.getEntity().getContent();
                    if (originalResponseStream != null) {
                        final OutputStream responseStream = hsResponse.getOutputStream();
                        copyStream(originalResponseStream, responseStream);
                    }
                }
                EntityUtils.consume(response.getEntity());

                log.info("set up response, result code was " + response.getStatusLine().getStatusCode());
            }
        }
    }

    private static CloseableHttpClient getHttpClient(RequestConfig config, boolean useSystemProperties) {
        final HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(new BasicHttpClientConnectionManager());
        if (useSystemProperties) {
            builder.useSystemProperties();
        }
        return builder.build();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[65536];
        int count;
        while ((count = in.read(buf)) != -1) {
            out.write(buf, 0, count);
        }
    }


    public static HttpHost getUseProxyServer(String useProxyServer) {
        HttpHost proxyHost = null;
        if (useProxyServer != null) {
            String proxyHostStr = useProxyServer;
            int colonIdx = proxyHostStr.indexOf(':');
            if (colonIdx != -1) {
                proxyHostStr = proxyHostStr.substring(0, colonIdx);
                final String proxyPortStr = useProxyServer.substring(colonIdx + 1);
                if (proxyPortStr.length() > 0 && NUMBER_PATTERN.matcher(proxyPortStr).matches()) {
                    int proxyPort = Integer.parseInt(proxyPortStr);
                    proxyHost = new HttpHost(proxyHostStr, proxyPort);
                } else {
                    proxyHost = new HttpHost(proxyHostStr, 80/*default port*/);
                }
            } else {
                proxyHost = new HttpHost(proxyHostStr);
            }
        }
        return proxyHost;
    }

    private static HttpRequestBase setupProxyRequest(final HttpServletRequest hsRequest, final URL targetUrl, boolean dropCookies) throws IOException {
        final String methodName = hsRequest.getMethod();
        final HttpRequestBase method;
        if ("POST".equalsIgnoreCase(methodName)) {
            HttpPost postMethod = new HttpPost();
            HttpEntity inputStreamRequestEntity = new InputStreamEntity(hsRequest.getInputStream());
            postMethod.setEntity(inputStreamRequestEntity);
            method = postMethod;
        } else if ("GET".equalsIgnoreCase(methodName)) {
            method = new HttpGet();
        } else if ("PUT".equalsIgnoreCase(methodName)) {
            HttpPut putMethod = new HttpPut();
            HttpEntity inputStreamRequestEntity = new InputStreamEntity(hsRequest.getInputStream());
            putMethod.setEntity(inputStreamRequestEntity);
            method = putMethod;
        } else if ("DELETE".equalsIgnoreCase(methodName)) {
            method = new HttpDelete();
        } else {
            log.warn("Unsupported HTTP method requested: " + hsRequest.getMethod());
            return null;
        }

        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .build();

        method.setConfig(config);
        try {
            method.setURI(targetUrl.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        Enumeration<String> e = hsRequest.getHeaderNames();
        if (e != null) {
            while (e.hasMoreElements()) {
                String headerName = e.nextElement();
                if ("host".equalsIgnoreCase(headerName)) {
                    //the host value is set by the http client
                    continue;
                } else if ("content-length".equalsIgnoreCase(headerName)) {
                    //the content-length is managed by the http client
                    continue;
                } else if ("accept-encoding".equalsIgnoreCase(headerName)) {
                    //the accepted encoding should only be those accepted by the http client.
                    //The response stream should (afaik) be deflated. If our http client does not support
                    //gzip then the response can not be unzipped and is delivered wrong.
                    continue;
                } else if (dropCookies && headerName.toLowerCase().startsWith("cookie")) {
                    //fixme : don't set any cookies in the proxied request, this needs a cleaner solution
                    continue;
                }

                Enumeration<String> values = hsRequest.getHeaders(headerName);
                while (values.hasMoreElements()) {
                    String headerValue = values.nextElement();
                    log.info("setting proxy request parameter:" + headerName + ", value: " + headerValue);
                    method.addHeader(headerName, headerValue);
                }
            }
        }

        if (log.isInfoEnabled()) log.info("proxy query string " + method.getRequestLine());
        return method;
    }

    private static void setupResponseHeaders(CloseableHttpResponse httpResponse, HttpServletResponse hsResponse, boolean dropCookies) {
        if (log.isInfoEnabled()) {
            log.info("setupResponseHeaders");
            log.info("status text: " + httpResponse.getStatusLine().getReasonPhrase());
            log.info("status line: " + httpResponse.getStatusLine().getStatusCode());
        }

        //filter the headers, which are copied from the proxy response. The http lib handles those itself.
        //Filtered out: the content encoding, the content length and cookies
        for (Header responseHeader : httpResponse.getAllHeaders()) {
            if ("content-encoding".equalsIgnoreCase(responseHeader.getName())) {
                continue;
            } else if ("content-length".equalsIgnoreCase(responseHeader.getName())) {
                continue;
            } else if ("transfer-encoding".equalsIgnoreCase(responseHeader.getName())) {
                continue;
            } else if (dropCookies) {
                if (responseHeader.getName().toLowerCase().startsWith("cookie")) {
                    //retrieving a cookie which sets the session id will change the calling session: bad! So we skip this header.
                    continue;
                } else if (responseHeader.getName().toLowerCase().startsWith("set-cookie")) {
                    //retrieving a cookie which sets the session id will change the calling session: bad! So we skip this header.
                    continue;
                }
            }

            hsResponse.addHeader(responseHeader.getName(), responseHeader.getValue());
            if (log.isInfoEnabled()) {
                log.info("setting response parameter:" + responseHeader.getName() + ", value: " + responseHeader.getValue());
            }
        }
        //fixme what about the response footers? (httpMethod.getResponseFooters())

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            hsResponse.setStatus(httpResponse.getStatusLine().getStatusCode());
        }
    }
}
