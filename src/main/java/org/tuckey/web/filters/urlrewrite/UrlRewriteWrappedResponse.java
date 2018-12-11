/**
 * Copyright (c) 2005-2007, Paul Tuckey
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * Handles wrapping the response so we can encode the url's on the way "out" (ie, in JSP or servlet generation).
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class UrlRewriteWrappedResponse extends HttpServletResponseWrapper {

    private static final Log log = Log.getLog(UrlRewriteWrappedResponse.class);
    private UrlRewriter urlRerwiter;
    private HttpServletResponse httpServletResponse;
    private HttpServletRequest httpServletRequest;

    //is a <string, string[]> map
    Map<String, String[]> overridenRequestParameters;
    String overridenMethod;

    private Set<String> disallowedDuplicateHeaders;

    public UrlRewriteWrappedResponse(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest,
                                     UrlRewriter urlRerwiter) {
        super(httpServletResponse);
        this.httpServletResponse = httpServletResponse;
        this.httpServletRequest = httpServletRequest;
        this.urlRerwiter = urlRerwiter;
        this.disallowedDuplicateHeaders = Collections.emptySet();
    }

    public UrlRewriteWrappedResponse(final HttpServletResponse hsResponse, final HttpServletRequest hsRequest, final UrlRewriter rewriter, final Set<String> disallowedDuplicateHeaders) {
        this(hsResponse, hsRequest, rewriter);
        this.disallowedDuplicateHeaders = disallowedDuplicateHeaders;
    }

    public String encodeURL(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeURL(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeRedirectURL(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeRedirectURL(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeUrl(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeUrl(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeRedirectUrl(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeRedirectUrl(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    /**
     * Handle rewriting.
     *
     * @param s
     */
    private RewrittenOutboundUrl processPreEncodeURL(String s) {
        if (urlRerwiter == null) {
            return null;
        }
        return urlRerwiter.processEncodeURL(httpServletResponse, httpServletRequest, false, s);
    }

    /**
     * Handle rewriting after the containers encodeUrl has been called.
     *
     * @param s
     */
    private RewrittenOutboundUrl processPostEncodeURL(String s) {
        if (urlRerwiter == null) {
            return null;
        }
        return urlRerwiter.processEncodeURL(httpServletResponse, httpServletRequest, true, s);
    }

    public void addOverridenRequestParameter(String k, String v) {
        if (overridenRequestParameters == null) {
            overridenRequestParameters = new HashMap<>();
        }
        if (overridenRequestParameters.get(k) == null) {
            overridenRequestParameters.put(k, new String[]{v});
        } else {
            String[] currentValues = overridenRequestParameters.get(k);
            String[] finalValues = new String[currentValues.length + 1];
            System.arraycopy(currentValues, 0, finalValues, 0, currentValues.length);
            finalValues[finalValues.length - 1] = v;
            overridenRequestParameters.put(k, finalValues);
        }
    }

    public Map<String, String[]> getOverridenRequestParameters() {
        return overridenRequestParameters;
    }

    public String getOverridenMethod() {
        return overridenMethod;
    }

    public void setOverridenMethod(String overridenMethod) {
        this.overridenMethod = overridenMethod;
    }
    
    @Override
    public void addHeader(final String name, final String value) {
        if (disallowedDuplicateHeaders == null || disallowedDuplicateHeaders.isEmpty()) {
            // just print warning for duplicates
            if (checkDuplicate(name)) {
                log.warn("Duplicated header name for: " + name + ". Use debug level to print values");
            }
            super.addHeader(name, value);
            return;
        }
        for (String disallowedName : disallowedDuplicateHeaders) {
            if (disallowedName.equals(name)) {
                if (checkDuplicate(name)) {
                    log.warn("Duplicated header name for: " + name + ". Use debug level to print values");
                    return;
                }
            }
        }
        super.addHeader(name, value);
    }
    

    private boolean checkDuplicate(final String name) {
        final Collection<String> values = httpServletResponse.getHeaders(name);
        if (values != null && !values.isEmpty()) {
            if (log.isDebugEnabled()) {
                for (final String v : values) {
                    log.debug("(header, value):("+name +',' + v + ')');
                }
            }
            return true;
        }
        return false;
    }
}
