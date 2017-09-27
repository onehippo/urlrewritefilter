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

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationUtilsTest {

    @Test
    public void ok() {
        ValidationUtils.validateNewLines("test", "me");
        assertTrue(true);
        ValidationUtils.validateNewLines("test");
        assertTrue(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesKeyValue1() {
        ValidationUtils.validateNewLines("test", "foo\nbar");
    }   @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesKeyValue11() {
        ValidationUtils.validateNewLines("te\nst", "foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesKeyValue2() {
        ValidationUtils.validateNewLines("test", "foo\rbar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesKeyValue3() {
        ValidationUtils.validateNewLines("test", "foo\n\rbar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesValue1() {
        ValidationUtils.validateNewLines("foo\nbar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesValue2() {
        ValidationUtils.validateNewLines("foo\rbar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNewLinesValue3() {
        ValidationUtils.validateNewLines("foo\r\n");
    }
}