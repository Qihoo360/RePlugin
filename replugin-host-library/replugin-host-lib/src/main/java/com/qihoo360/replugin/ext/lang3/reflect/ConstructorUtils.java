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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.qihoo360.replugin.ext.lang3.ClassUtils;
import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p> Utility reflection methods focused on constructors, modeled after
 * {@link MethodUtils}. </p>
 *
 * <h3>Known Limitations</h3> <h4>Accessing Public Constructors In A Default
 * Access Superclass</h4> <p>There is an issue when invoking {@code public} constructors
 * contained in a default access superclass. Reflection correctly locates these
 * constructors and assigns them as {@code public}. However, an
 * {@link IllegalAccessException} is thrown if the constructor is
 * invoked.</p>
 *
 * <p>{@link ConstructorUtils} contains a workaround for this situation: it
 * will attempt to call {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} on this constructor. If this
 * call succeeds, then the method can be invoked as normal. This call will only
 * succeed when the application has sufficient security privileges. If this call
 * fails then a warning will be logged and the method may fail.</p>
 *
 * @since 2.5
 */
public class ConstructorUtils {

    /**
     * <p>ConstructorUtils instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * {@code ConstructorUtils.invokeConstructor(cls, args)}.</p>
     *
     * <p>This constructor is {@code public} to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public ConstructorUtils() {
        super();
    }

    /**
     * <p>Returns a new instance of the specified class inferring the right constructor
     * from the types of the arguments.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the argument types by assignment compatibility.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not {@code null}
     * @param args  the array of arguments, {@code null} treated as empty
     * @return new instance of {@code cls}, not {@code null}
     *
     * @throws NullPointerException if {@code cls} is {@code null}
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see #invokeConstructor(Class, Object[], Class[])
     */
    public static <T> T invokeConstructor(final Class<T> cls, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?> parameterTypes[] = ClassUtils.toClass(args);
        return invokeConstructor(cls, args, parameterTypes);
    }

    /**
     * <p>Returns a new instance of the specified class choosing the right constructor
     * from the list of parameter types.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the parameter types by assignment compatibility.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not {@code null}
     * @param args  the array of arguments, {@code null} treated as empty
     * @param parameterTypes  the array of parameter types, {@code null} treated as empty
     * @return new instance of {@code cls}, not {@code null}
     *
     * @throws NullPointerException if {@code cls} is {@code null}
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see Constructor#newInstance
     */
    public static <T> T invokeConstructor(final Class<T> cls, Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Constructor<T> ctor = getMatchingAccessibleConstructor(cls, parameterTypes);
        if (ctor == null) {
            throw new NoSuchMethodException(
                "No such accessible constructor on object: " + cls.getName());
        }
        if (ctor.isVarArgs()) {
            final Class<?>[] methodParameterTypes = ctor.getParameterTypes();
            args = MethodUtils.getVarArgs(args, methodParameterTypes);
        }
        return ctor.newInstance(args);
    }

    /**
     * <p>Returns a new instance of the specified class inferring the right constructor
     * from the types of the arguments.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the argument types exactly.</p>
     *
     * @param <T> the type to be constructed
     * @param cls the class to be constructed, not {@code null}
     * @param args the array of arguments, {@code null} treated as empty
     * @return new instance of {@code cls}, not {@code null}
     *
     * @throws NullPointerException if {@code cls} is {@code null}
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see #invokeExactConstructor(Class, Object[], Class[])
     */
    public static <T> T invokeExactConstructor(final Class<T> cls, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?> parameterTypes[] = ClassUtils.toClass(args);
        return invokeExactConstructor(cls, args, parameterTypes);
    }

    /**
     * <p>Returns a new instance of the specified class choosing the right constructor
     * from the list of parameter types.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the parameter types exactly.</p>
     *
     * @param <T> the type to be constructed
     * @param cls the class to be constructed, not {@code null}
     * @param args the array of arguments, {@code null} treated as empty
     * @param parameterTypes  the array of parameter types, {@code null} treated as empty
     * @return new instance of <code>cls</code>, not {@code null}
     *
     * @throws NullPointerException if {@code cls} is {@code null}
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see Constructor#newInstance
     */
    public static <T> T invokeExactConstructor(final Class<T> cls, Object[] args,
            Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Constructor<T> ctor = getAccessibleConstructor(cls, parameterTypes);
        if (ctor == null) {
            throw new NoSuchMethodException(
                "No such accessible constructor on object: "+ cls.getName());
        }
        return ctor.newInstance(args);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Finds a constructor given a class and signature, checking accessibility.</p>
     *
     * <p>This finds the constructor and ensures that it is accessible.
     * The constructor signature must match the parameter types exactly.</p>
     *
     * @param <T> the constructor type
     * @param cls the class to find a constructor for, not {@code null}
     * @param parameterTypes the array of parameter types, {@code null} treated as empty
     * @return the constructor, {@code null} if no matching accessible constructor found
     * @see Class#getConstructor
     * @see #getAccessibleConstructor(Constructor)
     * @throws NullPointerException if {@code cls} is {@code null}
     */
    public static <T> Constructor<T> getAccessibleConstructor(final Class<T> cls,
            final Class<?>... parameterTypes) {
        Validate.notNull(cls, "class cannot be null");
        try {
            return getAccessibleConstructor(cls.getConstructor(parameterTypes));
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * <p>Checks if the specified constructor is accessible.</p>
     *
     * <p>This simply ensures that the constructor is accessible.</p>
     *
     * @param <T> the constructor type
     * @param ctor  the prototype constructor object, not {@code null}
     * @return the constructor, {@code null} if no matching accessible constructor found
     * @see SecurityManager
     * @throws NullPointerException if {@code ctor} is {@code null}
     */
    public static <T> Constructor<T> getAccessibleConstructor(final Constructor<T> ctor) {
        Validate.notNull(ctor, "constructor cannot be null");
        return MemberUtils.isAccessible(ctor)
                && isAccessible(ctor.getDeclaringClass()) ? ctor : null;
    }

    /**
     * <p>Finds an accessible constructor with compatible parameters.</p>
     *
     * <p>This checks all the constructor and finds one with compatible parameters
     * This requires that every parameter is assignable from the given parameter types.
     * This is a more flexible search than the normal exact matching algorithm.</p>
     *
     * <p>First it checks if there is a constructor matching the exact signature.
     * If not then all the constructors of the class are checked to see if their
     * signatures are assignment-compatible with the parameter types.
     * The first assignment-compatible matching constructor is returned.</p>
     *
     * @param <T> the constructor type
     * @param cls  the class to find a constructor for, not {@code null}
     * @param parameterTypes find method with compatible parameters
     * @return the constructor, null if no matching accessible constructor found
     * @throws NullPointerException if {@code cls} is {@code null}
     */
    public static <T> Constructor<T> getMatchingAccessibleConstructor(final Class<T> cls,
            final Class<?>... parameterTypes) {
        Validate.notNull(cls, "class cannot be null");
        // see if we can find the constructor directly
        // most of the time this works and it's much faster
        try {
            final Constructor<T> ctor = cls.getConstructor(parameterTypes);
            MemberUtils.setAccessibleWorkaround(ctor);
            return ctor;
        } catch (final NoSuchMethodException e) { // NOPMD - Swallow
        }
        Constructor<T> result = null;
        /*
         * (1) Class.getConstructors() is documented to return Constructor<T> so as
         * long as the array is not subsequently modified, everything's fine.
         */
        final Constructor<?>[] ctors = cls.getConstructors();

        // return best match:
        for (Constructor<?> ctor : ctors) {
            // compare parameters
            if (MemberUtils.isMatchingConstructor(ctor, parameterTypes)) {
                // get accessible version of constructor
                ctor = getAccessibleConstructor(ctor);
                if (ctor != null) {
                    MemberUtils.setAccessibleWorkaround(ctor);
                    if (result == null || MemberUtils.compareConstructorFit(ctor, result, parameterTypes) < 0) {
                        // temporary variable for annotation, see comment above (1)
                        @SuppressWarnings("unchecked")
                        final
                        Constructor<T> constructor = (Constructor<T>)ctor;
                        result = constructor;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Learn whether the specified class is generally accessible, i.e. is
     * declared in an entirely {@code public} manner.
     * @param type to check
     * @return {@code true} if {@code type} and any enclosing classes are
     *         {@code public}.
     */
    private static boolean isAccessible(final Class<?> type) {
        Class<?> cls = type;
        while (cls != null) {
            if (!Modifier.isPublic(cls.getModifiers())) {
                return false;
            }
            cls = cls.getEnclosingClass();
        }
        return true;
    }

}
