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

package com.qihoo360.replugin.utils;

import android.content.Context;

import com.qihoo360.replugin.helper.LogRelease;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.qihoo360.replugin.helper.LogDebug.MISC_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
public final class ReflectUtils {

    public static final void setFieldNonE(Class<?> c, Object object, String fName, Object value) {
        try {
            setField(c, object, fName, value);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static final void setField(Class<?> c, Object object, String fName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = c.getDeclaredField(fName);
        boolean acc = f.isAccessible();
        if (!acc) {
            f.setAccessible(true);
        }
        f.set(object, value);
        if (!acc) {
            f.setAccessible(acc);
        }
    }


    public static final void dumpObject(Object object, FileDescriptor fd, PrintWriter writer, String[] args) {
        try {
            Class<?> c = object.getClass();
            do {
                writer.println("c=" + c.getName());
                Field fields[] = c.getDeclaredFields();
                for (Field f : fields) {
                    boolean acc = f.isAccessible();
                    if (!acc) {
                        f.setAccessible(true);
                    }
                    Object o = f.get(object);
                    writer.print(f.getName());
                    writer.print("=");
                    if (o != null) {
                        writer.println(o.toString());
                    } else {
                        writer.println("null");
                    }
                    if (!acc) {
                        f.setAccessible(acc);
                    }
                }
                c = c.getSuperclass();
            } while (c != null && !c.equals(Object.class) && !c.equals(Context.class));
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MISC_TAG, e.getMessage(), e);
            }
        }
    }

    public static Object invokeMethod(ClassLoader loader, String clzName,
                                      String methodName, Object methodReceiver,
                                      Class<?>[] methodParamTypes, Object... methodParamValues) throws
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (methodReceiver == null) {
            return null;
        }

        return invokeMethod(getMethod(loader, clzName, methodName, methodParamTypes), methodReceiver, methodParamValues);
    }

    public static Method getMethod(ClassLoader loader, String clzName,
                                   String methodName, Class<?>[] methodParamTypes) throws
            ClassNotFoundException, NoSuchMethodException {

        Class clz = Class.forName(clzName, false, loader);

        if (clz != null) {
            return clz.getDeclaredMethod(methodName, methodParamTypes);
        }

        return null;
    }

    public static Object invokeMethod(Method method, Object methodReceiver, Object... methodParamValues) throws
            InvocationTargetException, IllegalAccessException {

        if (method != null) {
            boolean acc = method.isAccessible();

            if (!acc) {
                method.setAccessible(true);
            }

            Object ret = method.invoke(methodReceiver, methodParamValues);

            if (!acc) {
                method.setAccessible(false);
            }

            return ret;
        }

        return null;
    }
}
