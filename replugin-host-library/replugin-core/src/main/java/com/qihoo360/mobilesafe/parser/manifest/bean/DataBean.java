/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo360.mobilesafe.parser.manifest.bean;

import android.os.PatternMatcher;
import android.text.TextUtils;

/**
 * @author RePlugin Team
 */
public class DataBean {

    public String scheme;
    public String host;
    public String port;
    public String mimeType;
    public String path;
    public String pathPattern;
    public String pathPrefix;

    @Override
    public String toString() {
        return String.format(
                "{scheme:%s, host:%s, mimeType:%s, path:%s, pathPattern:%s, pathPrefix:%s, port:%s}", scheme, host, mimeType, pathPattern, pathPrefix, path, port);
    }

    /**
     * 获得 path 匹配类型
     */
    public int getPatternMatcherType() {
        if (TextUtils.isEmpty(pathPattern) && TextUtils.isEmpty(pathPattern)) {
            return PatternMatcher.PATTERN_LITERAL;
        } else if (!TextUtils.isEmpty(pathPrefix)) {
            return PatternMatcher.PATTERN_PREFIX;
        } else {
            return PatternMatcher.PATTERN_SIMPLE_GLOB;
        }
    }
}
