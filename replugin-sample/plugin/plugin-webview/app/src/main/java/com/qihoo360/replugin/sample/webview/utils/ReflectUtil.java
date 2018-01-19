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

package com.qihoo360.replugin.sample.webview.utils;

import android.util.Log;

import com.qihoo360.replugin.sample.webview.env.Env;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author RePlugin Team
 */
public class ReflectUtil {
    private static final String TAG = "ReflectUtil";

    public static Object invokeStaticMethod(String clzName, String methodName, Class<?>[] methodParamTypes, Object... methodParamValues) {
        try {
            Class clz = Class.forName(clzName);
            if (clz != null) {
                Method med = clz.getDeclaredMethod(methodName, methodParamTypes);
                if (med != null) {
                    med.setAccessible(true);
                    Object retObj = med.invoke(null, methodParamValues);
                    return retObj;
                }
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "invokeStaticMethod got Exception:", e);
            }
        }
        return null;
    }

    public static Object invokeMethod(String clzName, String methodName, Object methodReceiver, Class<?>[] methodParamTypes, Object... methodParamValues) {
        try {
            if (methodReceiver == null) {
                return null;
            }

            Class clz = Class.forName(clzName);
            if (clz != null) {
                Method med = clz.getDeclaredMethod(methodName, methodParamTypes);
                if (med != null) {
                    med.setAccessible(true);
                    Object retObj = med.invoke(methodReceiver, methodParamValues);
                    return retObj;
                }
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "invokeStaticMethod got Exception:", e);
            }
        }
        return null;
    }

    public static final Object getStaticField(String clzName, String filedName) {
        try {
            Field field = null;
            Class<?> clz = Class.forName(clzName);
            if (clz != null) {
                field = clz.getField(filedName);
                if (field != null) {
                    return field.get("");
                }
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "getStaticField got Exception:", e);
            }
        }

        return null;
    }

    public static final Object getField(String clzName, Object obj, String filedName) {
        try {
            if (obj == null) {
                return null;
            }

            Class<?> clz = Class.forName(clzName);
            if (clz != null) {
                Field field = clz.getField(filedName);
                if (field != null) {
                    return field.get(obj);
                }
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "getStaticField got Exception:", e);
            }
        }

        return null;
    }
}
