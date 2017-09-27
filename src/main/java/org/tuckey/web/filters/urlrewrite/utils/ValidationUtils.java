/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tuckey.web.filters.urlrewrite.utils;

import java.util.regex.Pattern;

/**
 * Input validation utilities
 */
public final class ValidationUtils {

    private static final Pattern CR_OR_LF = Pattern.compile("\\r|\\n");

    private ValidationUtils() {
    }

    /**
     * Check name and value for CWE-113 (HTTP Response Splitting)
     *
     * @param key   key
     * @param value value
     *              see https://cwe.mitre.org/data/definitions/113.html
     */
    public static void validateNewLines(String key, String value) {
        if (containsNewLine(key) || containsNewLine(value)) {
            throw new IllegalArgumentException("Invalid characters found (CR/LF) in header or cookie key: "
                    + key + " value: "
                    + value);
        }
    }

    /**
     * Check name and value for CWE-113 (HTTP Response Splitting)
     *
     * @param value value
     *              see https://cwe.mitre.org/data/definitions/113.html
     */
    public static void validateNewLines(String value) {
        if (containsNewLine(value)) {
            throw new IllegalArgumentException("Invalid characters found (CR/LF) in header or cookie " + value);
        }
    }

    private static boolean containsNewLine(String value) {
        return value != null && CR_OR_LF.matcher(value).find();
    }
}
