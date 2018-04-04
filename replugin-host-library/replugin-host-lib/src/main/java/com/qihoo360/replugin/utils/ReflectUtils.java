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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.qihoo360.replugin.helper.LogDebug.MISC_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 和反射操作有关的Utils
 *
 * @author RePlugin Team
 */
public final class ReflectUtils {

    // ----------------
    // Class & Constructor
    // ----------------

    public static Class<?> getClass(final String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    public static <T> T invokeConstructor(Class<T> cls, Class[] parameterTypes, Object... args) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> c = cls.getConstructor(parameterTypes);
        if (c != null) {
            c.setAccessible(true);
            return c.newInstance(args);
        }
        return null;
    }

    // ----------------
    // Field
    // ----------------

    public static Field getField(Class<?> cls, String fieldName) {
        // From Apache: FieldUtils.getField()

        // check up the superclass hierarchy
        for (Class<?> acls = cls; acls != null; acls = acls.getSuperclass()) {
            try {
                final Field field = acls.getDeclaredField(fieldName);
                // getDeclaredField checks for non-public scopes as well
                // and it returns accurate results
                setAccessible(field, true);

                return field;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        // check the public interface case. This must be manually searched for
        // incase there is a public supersuperclass field hidden by a private/package
        // superclass field.
        Field match = null;
        for (final Class<?> class1 : cls.getInterfaces()) {
            try {
                final Field test = class1.getField(fieldName);
                Validate.isTrue(match == null, "Reference to field %s is ambiguous relative to %s"
                        + "; a matching field exists on two or more implemented interfaces.", fieldName, cls);
                match = test;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        return match;
    }

    public static Object readStaticField(Class<?> c, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return readField(c, null, fieldName);
    }

    public static Object readField(Object target, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        return readField(target.getClass(), target, fieldName);
    }

    public static Object readField(Class<?> c, Object target, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field f = getField(c, fieldName);

        return readField(f, target);
    }

    public static Object readField(final Field field, final Object target) throws IllegalAccessException {
        return field.get(target);
    }

    public static void writeField(Object target, String fName, Object value) throws NoSuchFieldException, IllegalAccessException {
        writeField(target.getClass(), target, fName, value);
    }

    public static void writeField(Class<?> c, Object object, String fName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = getField(c, fName);
        writeField(f, object, value);
    }

    public static void writeField(final Field field, final Object target, final Object value) throws IllegalAccessException {
        field.set(target, value);
    }

    public static List<Field> getAllFieldsList(final Class<?> cls) {
        // From Apache: FieldUtils.getAllFieldsList()

        Validate.isTrue(cls != null, "The class must not be null");
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (final Field field : declaredFields) {
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static void removeFieldFinalModifier(final Field field) {
        // From Apache: FieldUtils.removeFinalModifier()
        Validate.isTrue(field != null, "The field must not be null");

        try {
            if (Modifier.isFinal(field.getModifiers())) {
                // Do all JREs implement Field with a private ivar called "modifiers"?
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                final boolean doForceAccess = !modifiersField.isAccessible();
                if (doForceAccess) {
                    modifiersField.setAccessible(true);
                }
                try {
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                } finally {
                    if (doForceAccess) {
                        modifiersField.setAccessible(false);
                    }
                }
            }
        } catch (final NoSuchFieldException ignored) {
            // The field class contains always a modifiers field
        } catch (final IllegalAccessException ignored) {
            // The modifiers field is made accessible
        }
    }

    // ----------------
    // Method
    // ----------------

    public static Method getMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        // check up the superclass hierarchy
        for (Class<?> acls = cls; acls != null; acls = acls.getSuperclass()) {
            try {
                final Method method = acls.getDeclaredMethod(methodName, parameterTypes);
                // getDeclaredField checks for non-public scopes as well
                // and it returns accurate results
                setAccessible(method, true);

                return method;
            } catch (final NoSuchMethodException ex) { // NOPMD
                // ignore
            }
        }
        // check the public interface case. This must be manually searched for
        // incase there is a public supersuperclass field hidden by a private/package
        // superclass field.
        Method match = null;
        for (final Class<?> class1 : cls.getInterfaces()) {
            try {
                final Method test = class1.getMethod(methodName, parameterTypes);
                Validate.isTrue(match == null, "Reference to field %s is ambiguous relative to %s"
                        + "; a matching field exists on two or more implemented interfaces.", methodName, cls);
                match = test;
            } catch (final NoSuchMethodException ex) { // NOPMD
                // ignore
            }
        }
        return match;
    }


    public static Object invokeMethod(final Object object, final String methodName, Class<?>[] methodParamTypes, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clz = object.getClass();
        Method m = getMethod(clz, methodName, methodParamTypes);
        return m.invoke(args);
    }

    public static Object invokeMethod(ClassLoader loader, String clzName,
                                      String methodName, Object methodReceiver,
                                      Class<?>[] methodParamTypes, Object... methodParamValues) throws
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (methodReceiver == null) {
            return null;
        }

        Class clz = Class.forName(clzName, false, loader);
        if (clz != null) {
            Method med = clz.getMethod(methodName, methodParamTypes);
            if (med != null) {
                med.setAccessible(true);
                return med.invoke(methodReceiver, methodParamValues);
            }
        }
        return null;
    }

    public static void setAccessible(AccessibleObject ao, boolean value) {
        if (ao.isAccessible() != value) {
            ao.setAccessible(value);
        }
    }

    // ----------------
    // Other
    // ----------------

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


}
