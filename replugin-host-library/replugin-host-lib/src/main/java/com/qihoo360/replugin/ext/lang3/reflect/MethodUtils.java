/*
 * Copyright (c) 2016, Liu Dong
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qihoo360.replugin.ext.lang3.reflect;

import com.qihoo360.replugin.ext.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.qihoo360.replugin.ext.lang3.ClassUtils;
import com.qihoo360.replugin.ext.lang3.ClassUtils.Interfaces;
import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>Utility reflection methods focused on {@link Method}s, originally from Commons BeanUtils.
 * Differences from the BeanUtils version may be noted, especially where similar functionality
 * already existed within Lang.
 * </p>
 *
 * <h3>Known Limitations</h3>
 * <h4>Accessing Public Methods In A Default Access Superclass</h4>
 * <p>There is an issue when invoking {@code public} methods contained in a default access superclass on JREs prior to 1.4.
 * Reflection locates these methods fine and correctly assigns them as {@code public}.
 * However, an {@link IllegalAccessException} is thrown if the method is invoked.</p>
 *
 * <p>{@link MethodUtils} contains a workaround for this situation.
 * It will attempt to call {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} on this method.
 * If this call succeeds, then the method can be invoked as normal.
 * This call will only succeed when the application has sufficient security privileges.
 * If this call fails then the method may fail.</p>
 *
 * @since 2.5
 */
public class MethodUtils {

    /**
     * <p>{@link MethodUtils} instances should NOT be constructed in standard programming.
     * Instead, the class should be used as
     * {@code MethodUtils.getAccessibleMethod(method)}.</p>
     *
     * <p>This constructor is {@code public} to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public MethodUtils() {
        super();
    }

    /**
     * <p>Invokes a named method without parameters.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object,String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     *
     *  @since 3.4
     */
    public static Object invokeMethod(final Object object, final String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a named method without parameters.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object,boolean forceAccess,String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object invoke method on this object
     * @param forceAccess force access to invoke method even if it's not accessible
     * @param methodName get method with this name
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     *
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, forceAccess, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object,String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     */
    public static Object invokeMethod(final Object object, final String methodName,
            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object,boolean forceAccess,String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object invoke method on this object
     * @param forceAccess force access to invoke method even if it's not accessible
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     *
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName,
            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, forceAccess, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * @param object invoke method on this object
     * @param forceAccess force access to invoke method even if it's not accessible
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat null as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName,
            Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        args = ArrayUtils.nullToEmpty(args);

        final String messagePrefix;
        Method method = null;

        if (forceAccess) {
            messagePrefix = "No such method: ";
            method = getMatchingMethod(object.getClass(),
                    methodName, parameterTypes);
            if (method != null && !method.isAccessible()) {
                method.setAccessible(true);
            }
        } else {
            messagePrefix = "No such accessible method: ";
            method = getMatchingAccessibleMethod(object.getClass(),
                    methodName, parameterTypes);
        }

        if (method == null) {
            throw new NoSuchMethodException(messagePrefix
                    + methodName + "() on object: "
                    + object.getClass().getName());
        }
        args = toVarArgs(method, args);

        return method.invoke(object, args);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat null as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException if the requested method is not accessible via reflection
     */
    public static Object invokeMethod(final Object object, final String methodName,
            final Object[] args, final Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        return invokeMethod(object, false, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a method whose parameter types match exactly the object
     * types.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod}(Class,String,Class[])}.</p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     *
     * @since 3.4
     */
    public static Object invokeExactMethod(final Object object, final String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        return invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a method with no parameters.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod}(Class,String,Class[])}.</p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeExactMethod(final Object object, final String methodName,
            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactMethod(object, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a method whose parameter types match exactly the parameter
     * types given.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class,String,Class[])}.</p>
     *
     * @param object invoke method on this object
     * @param methodName get method with this name
     * @param args use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeExactMethod(final Object object, final String methodName,
            Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(object.getClass(), methodName,
                parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on object: "
                    + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    /**
     * <p>Invokes a {@code static} method whose parameter types match exactly the parameter
     * types given.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
     *
     * @param cls invoke static method on this class
     * @param methodName get method with this name
     * @param args use these arguments - treat {@code null} as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName,
            Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(cls, methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on class: " + cls.getName());
        }
        return method.invoke(null, args);
    }

    /**
     * <p>Invokes a named {@code static} method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} class
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeStaticMethod(Class, String, Object[], Class[])}.
     * </p>
     *
     * @param cls invoke static method on this class
     * @param methodName get method with this name
     * @param args use these arguments - treat {@code null} as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName,
            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeStaticMethod(cls, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named {@code static} method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} class
     * would match a {@code boolean} primitive.</p>
     *
     *
     * @param cls invoke static method on this class
     * @param methodName get method with this name
     * @param args use these arguments - treat {@code null} as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName,
            Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getMatchingAccessibleMethod(cls, methodName,
                parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on class: " + cls.getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(null, args);
    }

    private static Object[] toVarArgs(final Method method, Object[] args) {
        if (method.isVarArgs()) {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            args = getVarArgs(args, methodParameterTypes);
        }
        return args;
    }

    /**
     * <p>Given an arguments array passed to a varargs method, return an array of arguments in the canonical form,
     * i.e. an array with the declared number of parameters, and whose last parameter is an array of the varargs type.
     * </p>
     *
     * @param args the array of arguments passed to the varags method
     * @param methodParameterTypes the declared array of method parameter types
     * @return an array of the variadic arguments passed to the method
     * @since 3.5
     */
    static Object[] getVarArgs(final Object[] args, final Class<?>[] methodParameterTypes) {
        if (args.length == methodParameterTypes.length
                && args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1])) {
            // The args array is already in the canonical form for the method.
            return args;
        }

        // Construct a new array matching the method's declared parameter types.
        final Object[] newArgs = new Object[methodParameterTypes.length];

        // Copy the normal (non-varargs) parameters
        System.arraycopy(args, 0, newArgs, 0, methodParameterTypes.length - 1);

        // Construct a new array for the variadic parameters
        final Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
        final int varArgLength = args.length - methodParameterTypes.length + 1;

        Object varArgsArray = Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength);
        // Copy the variadic arguments into the varargs array.
        System.arraycopy(args, methodParameterTypes.length - 1, varArgsArray, 0, varArgLength);

        if(varArgComponentType.isPrimitive()) {
            // unbox from wrapper type to primitive type
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }

        // Store the varargs array in the last position of the array to return
        newArgs[methodParameterTypes.length - 1] = varArgsArray;

        // Return the canonical varargs array.
        return newArgs;
    }

    /**
     * <p>Invokes a {@code static} method whose parameter types match exactly the object
     * types.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
     *
     * @param cls invoke static method on this class
     * @param methodName get method with this name
     * @param args use these arguments - treat {@code null} as empty array
     * @return The value returned by the invoked method
     *
     * @throws NoSuchMethodException if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *  method invoked
     * @throws IllegalAccessException if the requested method is not accessible
     *  via reflection
     */
    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName,
            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactStaticMethod(cls, methodName, args, parameterTypes);
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) with given name and parameters. If no such method
     * can be found, return {@code null}.
     * This is just a convenience wrapper for
     * {@link #getAccessibleMethod(Method)}.</p>
     *
     * @param cls get method from this class
     * @param methodName get method with this name
     * @param parameterTypes with these parameters types
     * @return The accessible method
     */
    public static Method getAccessibleMethod(final Class<?> cls, final String methodName,
            final Class<?>... parameterTypes) {
        try {
            return getAccessibleMethod(cls.getMethod(methodName,
                    parameterTypes));
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param method The method that we wish to call
     * @return The accessible method
     */
    public static Method getAccessibleMethod(Method method) {
        if (!MemberUtils.isAccessible(method)) {
            return null;
        }
        // If the declaring class is public, we are done
        final Class<?> cls = method.getDeclaringClass();
        if (Modifier.isPublic(cls.getModifiers())) {
            return method;
        }
        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();

        // Check the implemented interfaces and subinterfaces
        method = getAccessibleMethodFromInterfaceNest(cls, methodName,
                parameterTypes);

        // Check the superclass chain
        if (method == null) {
            method = getAccessibleMethodFromSuperclass(cls, methodName,
                    parameterTypes);
        }
        return method;
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) by scanning through the superclasses. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param cls Class to be checked
     * @param methodName Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return the accessible method or {@code null} if not found
     */
    private static Method getAccessibleMethodFromSuperclass(final Class<?> cls,
            final String methodName, final Class<?>... parameterTypes) {
        Class<?> parentClass = cls.getSuperclass();
        while (parentClass != null) {
            if (Modifier.isPublic(parentClass.getModifiers())) {
                try {
                    return parentClass.getMethod(methodName, parameterTypes);
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces. If no such method
     * can be found, return {@code null}.</p>
     *
     * <p>There isn't any good reason why this method must be {@code private}.
     * It is because there doesn't seem any reason why other classes should
     * call this rather than the higher level methods.</p>
     *
     * @param cls Parent class for the interfaces to be checked
     * @param methodName Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return the accessible method or {@code null} if not found
     */
    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls,
            final String methodName, final Class<?>... parameterTypes) {
        // Search up the superclass chain
        for (; cls != null; cls = cls.getSuperclass()) {

            // Check the implemented interfaces of the parent class
            final Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                // Is this interface public?
                if (!Modifier.isPublic(anInterface.getModifiers())) {
                    continue;
                }
                // Does the method exist on this interface?
                try {
                    return anInterface.getDeclaredMethod(methodName,
                            parameterTypes);
                } catch (final NoSuchMethodException e) { // NOPMD
                    /*
                     * Swallow, if no method is found after the loop then this
                     * method returns null.
                     */
                }
                // Recursively check our parent interfaces
                final Method method = getAccessibleMethodFromInterfaceNest(anInterface,
                        methodName, parameterTypes);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * <p>Finds an accessible method that matches the given name and has compatible parameters.
     * Compatible parameters mean that every method parameter is assignable from
     * the given parameters.
     * In other words, it finds a method with the given name
     * that will take the parameters given.</p>
     *
     * <p>This method is used by
     * {@link
     * #invokeMethod(Object object, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * <p>This method can match primitive parameter by passing in wrapper classes.
     * For example, a {@code Boolean} will match a primitive {@code boolean}
     * parameter.
     * </p>
     *
     * @param cls find method in this class
     * @param methodName find method with this name
     * @param parameterTypes find method with most compatible parameters
     * @return The accessible method
     */
    public static Method getMatchingAccessibleMethod(final Class<?> cls,
            final String methodName, final Class<?>... parameterTypes) {
        try {
            final Method method = cls.getMethod(methodName, parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        } catch (final NoSuchMethodException e) { // NOPMD - Swallow the exception
        }
        // search through all methods
        Method bestMatch = null;
        final Method[] methods = cls.getMethods();
        for (final Method method : methods) {
            // compare name and parameters
            if (method.getName().equals(methodName) &&
                    MemberUtils.isMatchingMethod(method, parameterTypes)) {
                // get accessible version of method
                final Method accessibleMethod = getAccessibleMethod(method);
                if (accessibleMethod != null && (bestMatch == null || MemberUtils.compareMethodFit(
                            accessibleMethod,
                            bestMatch,
                            parameterTypes) < 0)) {
                    bestMatch = accessibleMethod;
                }
            }
        }
        if (bestMatch != null) {
            MemberUtils.setAccessibleWorkaround(bestMatch);
        }

        if (bestMatch != null && bestMatch.isVarArgs() && bestMatch.getParameterTypes().length > 0 && parameterTypes.length > 0) {
            final Class<?>[] methodParameterTypes = bestMatch.getParameterTypes();
            final Class<?> methodParameterComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
            final String methodParameterComponentTypeName = ClassUtils.primitiveToWrapper(methodParameterComponentType).getName();
            final String parameterTypeName = parameterTypes[parameterTypes.length - 1].getName();
            final String parameterTypeSuperClassName = parameterTypes[parameterTypes.length - 1].getSuperclass().getName();

            if (!methodParameterComponentTypeName.equals(parameterTypeName)
                    && !methodParameterComponentTypeName.equals(parameterTypeSuperClassName)) {
                return null;
            }
        }

        return bestMatch;
    }

    /**
     * <p>Retrieves a method whether or not it's accessible. If no such method
     * can be found, return {@code null}.</p>
     * @param cls The class that will be subjected to the method search
     * @param methodName The method that we wish to call
     * @param parameterTypes Argument class types
     * @return The method
     *
     * @since 3.5
     */
    public static Method getMatchingMethod(final Class<?> cls, final String methodName,
            final Class<?>... parameterTypes) {
        Validate.notNull(cls, "Null class not allowed.");
        Validate.notEmpty(methodName, "Null or blank methodName not allowed.");

        // Address methods in superclasses
        Method[] methodArray = cls.getDeclaredMethods();
        final List<Class<?>> superclassList = ClassUtils.getAllSuperclasses(cls);
        for (final Class<?> klass : superclassList) {
            methodArray = ArrayUtils.addAll(methodArray, klass.getDeclaredMethods());
        }

        Method inexactMatch = null;
        for (final Method method : methodArray) {
            if (methodName.equals(method.getName()) &&
                    Objects.deepEquals(parameterTypes, method.getParameterTypes())) {
                return method;
            } else if (methodName.equals(method.getName()) &&
                    ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                if (inexactMatch == null) {
                    inexactMatch = method;
                } else if (distance(parameterTypes, method.getParameterTypes())
                        < distance(parameterTypes, inexactMatch.getParameterTypes())) {
                    inexactMatch = method;
                }
            }

        }
        return inexactMatch;
    }

    /**
     * <p>Returns the aggregate number of inheritance hops between assignable argument class types.  Returns -1
     * if the arguments aren't assignable.  Fills a specific purpose for getMatchingMethod and is not generalized.</p>
     * @param classArray
     * @param toClassArray
     * @return the aggregate number of inheritance hops between assignable argument class types.
     */
    private static int distance(final Class<?>[] classArray, final Class<?>[] toClassArray) {
        int answer = 0;

        if (!ClassUtils.isAssignable(classArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < classArray.length; offset++) {
            // Note InheritanceUtils.distance() uses different scoring system.
            if (classArray[offset].equals(toClassArray[offset])) {
                continue;
            } else if (ClassUtils.isAssignable(classArray[offset], toClassArray[offset], true)
                    && !ClassUtils.isAssignable(classArray[offset], toClassArray[offset], false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }

        return answer;
    }

    /**
     * Get the hierarchy of overridden methods down to {@code result} respecting generics.
     * @param method lowest to consider
     * @param interfacesBehavior whether to search interfaces, {@code null} {@code implies} false
     * @return Set&lt;Method&gt; in ascending order from sub- to superclass
     * @throws NullPointerException if the specified method is {@code null}
     * @since 3.2
     */
    public static Set<Method> getOverrideHierarchy(final Method method, final Interfaces interfacesBehavior) {
        Validate.notNull(method);
        final Set<Method> result = new LinkedHashSet<>();
        result.add(method);

        final Class<?>[] parameterTypes = method.getParameterTypes();

        final Class<?> declaringClass = method.getDeclaringClass();

        final Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
        //skip the declaring class :P
        hierarchy.next();
        hierarchyTraversal: while (hierarchy.hasNext()) {
            final Class<?> c = hierarchy.next();
            final Method m = getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
            if (m == null) {
                continue;
            }
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                // matches without generics
                result.add(m);
                continue;
            }
            // necessary to get arguments every time in the case that we are including interfaces
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                final Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                final Type parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType)) {
                    continue hierarchyTraversal;
                }
            }
            result.add(m);
        }
        return result;
    }

    /**
     * Gets all class level public methods of the given class that are annotated with the given annotation.
     * @param cls
     *            the {@link Class} to query
     * @param annotationCls
     *            the {@link Annotation} that must be present on a method to be matched
     * @return an array of Methods (possibly empty).
     * @throws IllegalArgumentException
     *            if the class or annotation are {@code null}
     * @since 3.4
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsWithAnnotation(cls, annotationCls, false, false);
    }

    /**
     * Gets all class level public methods of the given class that are annotated with the given annotation.
     * @param cls
     *            the {@link Class} to query
     * @param annotationCls
     *            the {@link Annotation} that must be present on a method to be matched
     * @return a list of Methods (possibly empty).
     * @throws IllegalArgumentException
     *            if the class or annotation are {@code null}
     * @since 3.4
     */
    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsListWithAnnotation(cls, annotationCls, false, false);
    }

    /**
     * Gets all methods of the given class that are annotated with the given annotation.
     * @param cls
     *            the {@link Class} to query
     * @param annotationCls
     *            the {@link Annotation} that must be present on a method to be matched
     * @param searchSupers
     *            determines if a lookup in the entire inheritance hierarchy of the given class should be performed
     * @param ignoreAccess
     *            determines if non public methods should be considered
     * @return an array of Methods (possibly empty).
     * @throws IllegalArgumentException
     *            if the class or annotation are {@code null}
     * @since 3.6
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls,
                                                    boolean searchSupers, boolean ignoreAccess) {
        final List<Method> annotatedMethodsList = getMethodsListWithAnnotation(cls, annotationCls, searchSupers,
                ignoreAccess);
        return annotatedMethodsList.toArray(new Method[annotatedMethodsList.size()]);
    }

    /**
     * Gets all methods of the given class that are annotated with the given annotation.
     * @param cls
     *            the {@link Class} to query
     * @param annotationCls
     *            the {@link Annotation} that must be present on a method to be matched
     * @param searchSupers
     *            determines if a lookup in the entire inheritance hierarchy of the given class should be performed
     * @param ignoreAccess
     *            determines if non public methods should be considered
     * @return a list of Methods (possibly empty).
     * @throws IllegalArgumentException
     *            if the class or annotation are {@code null}
     * @since 3.6
     */
    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls,
                                                            final Class<? extends Annotation> annotationCls,
                                                            boolean searchSupers, boolean ignoreAccess) {

        Validate.isTrue(cls != null, "The class must not be null");
        Validate.isTrue(annotationCls != null, "The annotation class must not be null");
        List<Class<?>> classes = (searchSupers ? getAllSuperclassesAndInterfaces(cls)
                : new ArrayList<Class<?>>());
        classes.add(0, cls);
        final List<Method> annotatedMethods = new ArrayList<>();
        for (Class<?> acls : classes) {
            final Method[] methods = (ignoreAccess ? acls.getDeclaredMethods() : acls.getMethods());
            for (final Method method : methods) {
                if (method.getAnnotation(annotationCls) != null) {
                    annotatedMethods.add(method);
                }
            }
        }
        return annotatedMethods;
    }

    /**
     * <p>Gets the annotation object with the given annotation type that is present on the given method
     * or optionally on any equivalent method in super classes and interfaces. Returns null if the annotation
     * type was not present.</p>
     *
     * <p>Stops searching for an annotation once the first annotation of the specified type has been
     * found. Additional annotations of the specified type will be silently ignored.</p>
     * @param <A>
     *            the annotation type
     * @param method
     *            the {@link Method} to query
     * @param annotationCls
     *            the {@link Annotation} to check if is present on the method
     * @param searchSupers
     *            determines if a lookup in the entire inheritance hierarchy of the given class is performed
     *            if the annotation was not directly present
     * @param ignoreAccess
     *            determines if underlying method has to be accessible
     * @return the first matching annotation, or {@code null} if not found
     * @throws IllegalArgumentException
     *            if the method or annotation are {@code null}
     * @since 3.6
     */
    public static <A extends Annotation> A getAnnotation(final Method method, final Class<A> annotationCls,
                                                         boolean searchSupers, boolean ignoreAccess) {

        Validate.isTrue(method != null, "The method must not be null");
        Validate.isTrue(annotationCls != null, "The annotation class must not be null");
        if (!ignoreAccess && !MemberUtils.isAccessible(method)) {
            return null;
        }

        A annotation = method.getAnnotation(annotationCls);

        if (annotation == null && searchSupers) {
            Class<?> mcls = method.getDeclaringClass();
            List<Class<?>> classes = getAllSuperclassesAndInterfaces(mcls);
            for (Class<?> acls : classes) {
                Method equivalentMethod;
                try {
                    equivalentMethod = (ignoreAccess ? acls.getDeclaredMethod(method.getName(), method.getParameterTypes())
                            : acls.getMethod(method.getName(), method.getParameterTypes()));
                } catch (NoSuchMethodException e) {
                    // if not found, just keep searching
                    continue;
                }
                annotation = equivalentMethod.getAnnotation(annotationCls);
                if (annotation != null) {
                    break;
                }
            }
        }

        return annotation;
    }

    /**
     * <p>Gets a combination of {@link ClassUtils#getAllSuperclasses}(Class)} and
     * {@link ClassUtils#getAllInterfaces}(Class)}, one from superclasses, one
     * from interfaces, and so on in a breadth first way.</p>
     *
     * @param cls  the class to look up, may be {@code null}
     * @return the combined {@code List} of superclasses and interfaces in order
     * going up from this one
     *  {@code null} if null input
     */
    private static List<Class<?>> getAllSuperclassesAndInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final List<Class<?>> allSuperClassesAndInterfaces = new ArrayList<>();
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
        int superClassIndex = 0;
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(cls);
        int interfaceIndex = 0;
        while (interfaceIndex < allInterfaces.size() ||
                superClassIndex < allSuperclasses.size()) {
            Class<?> acls;
            if (interfaceIndex >= allInterfaces.size()) {
                acls = allSuperclasses.get(superClassIndex++);
            } else if (superClassIndex >= allSuperclasses.size()) {
                acls = allInterfaces.get(interfaceIndex++);
            } else if (interfaceIndex < superClassIndex) {
                acls = allInterfaces.get(interfaceIndex++);
            } else if (superClassIndex < interfaceIndex) {
                acls = allSuperclasses.get(superClassIndex++);
            } else {
                acls = allInterfaces.get(interfaceIndex++);
            }
            allSuperClassesAndInterfaces.add(acls);
        }
        return allSuperClassesAndInterfaces;
    }
}
