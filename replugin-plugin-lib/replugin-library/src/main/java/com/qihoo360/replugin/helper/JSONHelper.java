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

package com.qihoo360.replugin.helper;

import com.qihoo360.replugin.RePluginInternal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 和JSON操作有关的帮助类
 *
 * @author RePlugin Team
 */

public class JSONHelper {

    private static final boolean LOG = RePluginInternal.FOR_DEV;

    /**
     * 不抛出异常，直接Put
     *
     * @param jo    JSONObject对象
     * @param key   键
     * @param value 值
     */
    public static <T> void putNoThrows(JSONObject jo, String key, T value) {
        try {
            jo.put(key, value);
        } catch (JSONException e) {
            if (LOG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 克隆一份JSON对象
     *
     * @param from JSON对象
     * @return 克隆后的JSON对象
     */
    public static JSONObject cloneNoThrows(JSONObject from) {
        try {
            // 不能用new JsonObject(JSONObject, String[])版本，因为不是深拷贝
            return new JSONObject(from.toString());
        } catch (JSONException e) {
            // 不太可能走到这里
            if (LOG) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
