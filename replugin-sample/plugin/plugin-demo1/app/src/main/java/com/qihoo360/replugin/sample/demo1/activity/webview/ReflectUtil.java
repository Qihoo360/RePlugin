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

package com.qihoo360.replugin.sample.demo1.activity.webview;

import java.lang.reflect.Method;

/**
 * @author RePlugin Team
 */
public class ReflectUtil {

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })

    public static Object invokeStaticMethod(String clzName, String methodName, Class<?>[] methodParamTypes, Object... methodParamValues) {
        try {
            Class clz = Class.forName(clzName);
            if (clz != null) {
                Method method = clz.getDeclaredMethod(methodName, methodParamTypes);
                if (method != null) {
                    method.setAccessible(true);
                    return method.invoke(null, methodParamValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}