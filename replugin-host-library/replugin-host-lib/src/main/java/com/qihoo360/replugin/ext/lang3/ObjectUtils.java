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
package com.qihoo360.replugin.ext.lang3;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.qihoo360.replugin.ext.lang3.exception.CloneFailedException;
import com.qihoo360.replugin.ext.lang3.mutable.MutableInt;

/**
 * <p>Operations on {@code Object}.</p>
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will generally not be thrown for a {@code null} input.
 * Each method documents its behaviour in more detail.</p>
 *
 * <p>#ThreadSafe#</p>
 * @since 1.0
 */
//@Immutable
@SuppressWarnings("deprecation") // deprecated class StrBuilder is imported
// because it is part of the signature of deprecated methods
public class ObjectUtils {

    /**
     * <p>Singleton used as a {@code null} placeholder where
     * {@code null} has another meaning.</p>
     *
     * <p>For example, in a {@code HashMap} the
     * {@link HashMap#get(Object)} method returns
     * {@code null} if the {@code Map} contains {@code null} or if there
     * is no matching key. The {@code Null} placeholder can be used to
     * distinguish between these two cases.</p>
     *
     * <p>Another example is {@code Hashtable}, where {@code null}
     * cannot be stored.</p>
     *
     * <p>This instance is Serializable.</p>
     */
    public static final Null NULL = new Null();

    /**
     * <p>{@code ObjectUtils} instances should NOT be constructed in
     * standard programming. Instead, the static methods on the class should
     * be used, such as {@code ObjectUtils.defaultIfNull("a","b");}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public ObjectUtils() {
        super();
    }

    // Defaulting
    //-----------------------------------------------------------------------
    /**
     * <p>Returns a default value if the object passed is {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.defaultIfNull(null, null)      = null
     * ObjectUtils.defaultIfNull(null, "")        = ""
     * ObjectUtils.defaultIfNull(null, "zz")      = "zz"
     * ObjectUtils.defaultIfNull("abc", *)        = "abc"
     * ObjectUtils.defaultIfNull(Boolean.TRUE, *) = Boolean.TRUE
     * </pre>
     *
     * @param <T> the type of the object
     * @param object  the {@code Object} to test, may be {@code null}
     * @param defaultValue  the default value to return, may be {@code null}
     * @return {@code object} if it is not {@code null}, defaultValue otherwise
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * <p>Returns the first value in the array which is not {@code null}.
     * If all the values are {@code null} or the array is {@code null}
     * or empty then {@code null} is returned.</p>
     *
     * <pre>
     * ObjectUtils.firstNonNull(null, null)      = null
     * ObjectUtils.firstNonNull(null, "")        = ""
     * ObjectUtils.firstNonNull(null, null, "")  = ""
     * ObjectUtils.firstNonNull(null, "zz")      = "zz"
     * ObjectUtils.firstNonNull("abc", *)        = "abc"
     * ObjectUtils.firstNonNull(null, "xyz", *)  = "xyz"
     * ObjectUtils.firstNonNull(Boolean.TRUE, *) = Boolean.TRUE
     * ObjectUtils.firstNonNull()                = null
     * </pre>
     *
     * @param <T> the component type of the array
     * @param values  the values to test, may be {@code null} or empty
     * @return the first value from {@code values} which is not {@code null},
     *  or {@code null} if there are no non-null values
     * @since 3.0
     */
    @SafeVarargs
    public static <T> T firstNonNull(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (val != null) {
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * Checks if any value in the given array is not {@code null}.
     *
     * <p>
     * If all the values are {@code null} or the array is {@code null}
     * or empty then {@code false} is returned. Otherwise {@code true} is returned.
     * </p>
     *
     * <pre>
     * ObjectUtils.anyNotNull(*)                = true
     * ObjectUtils.anyNotNull(*, null)          = true
     * ObjectUtils.anyNotNull(null, *)          = true
     * ObjectUtils.anyNotNull(null, null, *, *) = true
     * ObjectUtils.anyNotNull(null)             = false
     * ObjectUtils.anyNotNull(null, null)       = false
     * </pre>
     *
     * @param values  the values to test, may be {@code null} or empty
     * @return {@code true} if there is at least one non-null value in the array,
     * {@code false} if all values in the array are {@code null}s.
     * If the array is {@code null} or empty {@code false} is also returned.
     * @since 3.5
     */
    public static boolean anyNotNull(final Object... values) {
        return firstNonNull(values) != null;
    }

    /**
     * Checks if all values in the array are not {@code nulls}.
     *
     * <p>
     * If any value is {@code null} or the array is {@code null} then
     * {@code false} is returned. If all elements in array are not
     * {@code null} or the array is empty (contains no elements) {@code true}
     * is returned.
     * </p>
     *
     * <pre>
     * ObjectUtils.allNotNull(*)             = true
     * ObjectUtils.allNotNull(*, *)          = true
     * ObjectUtils.allNotNull(null)          = false
     * ObjectUtils.allNotNull(null, null)    = false
     * ObjectUtils.allNotNull(null, *)       = false
     * ObjectUtils.allNotNull(*, null)       = false
     * ObjectUtils.allNotNull(*, *, null, *) = false
     * </pre>
     *
     * @param values  the values to test, may be {@code null} or empty
     * @return {@code false} if there is at least one {@code null} value in the array or the array is {@code null},
     * {@code true} if all values in the array are not {@code null}s or array contains no elements.
     * @since 3.5
     */
    public static boolean allNotNull(final Object... values) {
        if (values == null) {
            return false;
        }

        for (final Object val : values) {
            if (val == null) {
                return false;
            }
        }

        return true;
    }

    // Null-safe equals/hashCode
    //-----------------------------------------------------------------------
    /**
     * <p>Compares two objects for equality, where either one or both
     * objects may be {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.equals(null, null)                  = true
     * ObjectUtils.equals(null, "")                    = false
     * ObjectUtils.equals("", null)                    = false
     * ObjectUtils.equals("", "")                      = true
     * ObjectUtils.equals(Boolean.TRUE, null)          = false
     * ObjectUtils.equals(Boolean.TRUE, "true")        = false
     * ObjectUtils.equals(Boolean.TRUE, Boolean.TRUE)  = true
     * ObjectUtils.equals(Boolean.TRUE, Boolean.FALSE) = false
     * </pre>
     *
     * @param object1  the first object, may be {@code null}
     * @param object2  the second object, may be {@code null}
     * @return {@code true} if the values of both objects are the same
     * @deprecated this method has been replaced by {@code java.util.Objects.equals(Object, Object)} in Java 7 and will
     * be removed from future releases.
     */
    @Deprecated
    public static boolean equals(final Object object1, final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        return object1.equals(object2);
    }

    /**
     * <p>Compares two objects for inequality, where either one or both
     * objects may be {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.notEqual(null, null)                  = false
     * ObjectUtils.notEqual(null, "")                    = true
     * ObjectUtils.notEqual("", null)                    = true
     * ObjectUtils.notEqual("", "")                      = false
     * ObjectUtils.notEqual(Boolean.TRUE, null)          = true
     * ObjectUtils.notEqual(Boolean.TRUE, "true")        = true
     * ObjectUtils.notEqual(Boolean.TRUE, Boolean.TRUE)  = false
     * ObjectUtils.notEqual(Boolean.TRUE, Boolean.FALSE) = true
     * </pre>
     *
     * @param object1  the first object, may be {@code null}
     * @param object2  the second object, may be {@code null}
     * @return {@code false} if the values of both objects are the same
     */
    public static boolean notEqual(final Object object1, final Object object2) {
        return ObjectUtils.equals(object1, object2) == false;
    }

    /**
     * <p>Gets the hash code of an object returning zero when the
     * object is {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.hashCode(null)   = 0
     * ObjectUtils.hashCode(obj)    = obj.hashCode()
     * </pre>
     *
     * @param obj  the object to obtain the hash code of, may be {@code null}
     * @return the hash code of the object, or zero if null
     * @since 2.1
     * @deprecated this method has been replaced by {@code java.util.Objects.hashCode(Object)} in Java 7 and will be
     * removed in future releases
     */
    @Deprecated
    public static int hashCode(final Object obj) {
        // hashCode(Object) retained for performance, as hash code is often critical
        return obj == null ? 0 : obj.hashCode();
    }

    // Identity ToString
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will return {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.identityToString(null)         = null
     * ObjectUtils.identityToString("")           = "java.lang.String@1e23"
     * ObjectUtils.identityToString(Boolean.TRUE) = "java.lang.Boolean@7fa"
     * </pre>
     *
     * @param object  the object to create a toString for, may be
     *  {@code null}
     * @return the default toString text, or {@code null} if
     *  {@code null} passed in
     */
    public static String identityToString(final Object object) {
        if (object == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        identityToString(builder, object);
        return builder.toString();
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(buf, "")            = buf.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param buffer  the buffer to append to
     * @param object  the object to create a toString for
     * @since 2.4
     */
    public static void identityToString(final StringBuffer buffer, final Object object) {
        Validate.notNull(object, "Cannot get the toString of a null identity");
        buffer.append(object.getClass().getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder  the builder to append to
     * @param object  the object to create a toString for
     * @since 3.2
     */
    public static void identityToString(final StringBuilder builder, final Object object) {
        Validate.notNull(object, "Cannot get the toString of a null identity");
        builder.append(object.getClass().getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }

    // ToString
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * an empty string ("") if {@code null} input.</p>
     *
     * <pre>
     * ObjectUtils.toString(null)         = ""
     * ObjectUtils.toString("")           = ""
     * ObjectUtils.toString("bat")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE) = "true"
     * </pre>
     *
     * @see StringUtils#defaultString(String)
     * @see String#valueOf(Object)
     * @param obj  the Object to {@code toString}, may be null
     * @return the passed in Object's toString, or {@code ""} if {@code null} input
     * @since 2.0
     * @deprecated this method has been replaced by {@code java.util.Objects.toString(Object)} in Java 7 and will be
     * removed in future releases. Note however that said method will return "null" for null references, while this
     * method returns an empty String. To preserve behavior use {@code java.util.Objects.toString(myObject, "")}
     */
    @Deprecated
    public static String toString(final Object obj) {
        return obj == null ? StringUtils.EMPTY : obj.toString();
    }

    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * a specified text if {@code null} input.</p>
     *
     * <pre>
     * ObjectUtils.toString(null, null)           = null
     * ObjectUtils.toString(null, "null")         = "null"
     * ObjectUtils.toString("", "null")           = ""
     * ObjectUtils.toString("bat", "null")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE, "null") = "true"
     * </pre>
     *
     * @see StringUtils#defaultString(String,String)
     * @see String#valueOf(Object)
     * @param obj  the Object to {@code toString}, may be null
     * @param nullStr  the String to return if {@code null} input, may be null
     * @return the passed in Object's toString, or {@code nullStr} if {@code null} input
     * @since 2.0
     * @deprecated this method has been replaced by {@code java.util.Objects.toString(Object, String)} in Java 7 and
     * will be removed in future releases.
     */
    @Deprecated
    public static String toString(final Object obj, final String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    // Comparable
    //-----------------------------------------------------------------------
    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param values the set of comparable values, may be null
     * @return
     *  <ul>
     *   <li>If any objects are non-null and unequal, the lesser object.
     *   <li>If all objects are non-null and equal, the first.
     *   <li>If any of the comparables are null, the lesser of the non-null objects.
     *   <li>If all the comparables are null, null is returned.
     *  </ul>
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> T min(final T... values) {
        T result = null;
        if (values != null) {
            for (final T value : values) {
                if (compare(value, result, true) < 0) {
                    result = value;
                }
            }
        }
        return result;
    }

    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param values the set of comparable values, may be null
     * @return
     *  <ul>
     *   <li>If any objects are non-null and unequal, the greater object.
     *   <li>If all objects are non-null and equal, the first.
     *   <li>If any of the comparables are null, the greater of the non-null objects.
     *   <li>If all the comparables are null, null is returned.
     *  </ul>
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> T max(final T... values) {
        T result = null;
        if (values != null) {
            for (final T value : values) {
                if (compare(value, result, false) > 0) {
                    result = value;
                }
            }
        }
        return result;
    }

    /**
     * <p>Null safe comparison of Comparables.
     * {@code null} is assumed to be less than a non-{@code null} value.</p>
     *
     * @param <T> type of the values processed by this method
     * @param c1  the first comparable, may be null
     * @param c2  the second comparable, may be null
     * @return a negative value if c1 &lt; c2, zero if c1 = c2
     *  and a positive value if c1 &gt; c2
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2) {
        return compare(c1, c2, false);
    }

    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param c1  the first comparable, may be null
     * @param c2  the second comparable, may be null
     * @param nullGreater if true {@code null} is considered greater
     *  than a non-{@code null} value or if false {@code null} is
     *  considered less than a Non-{@code null} value
     * @return a negative value if c1 &lt; c2, zero if c1 = c2
     *  and a positive value if c1 &gt; c2
     * @see Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2, final boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return nullGreater ? 1 : -1;
        } else if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * Find the "best guess" middle value among comparables. If there is an even
     * number of total values, the lower of the two middle values will be returned.
     * @param <T> type of values processed by this method
     * @param items to compare
     * @return T at middle position
     * @throws NullPointerException if items is {@code null}
     * @throws IllegalArgumentException if items is empty or contains {@code null} values
     * @since 3.0.1
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> T median(final T... items) {
        Validate.notEmpty(items);
        Validate.noNullElements(items);
        final TreeSet<T> sort = new TreeSet<>();
        Collections.addAll(sort, items);
        @SuppressWarnings("unchecked") //we know all items added were T instances
        final T result = (T) sort.toArray()[(sort.size() - 1) / 2];
        return result;
    }

    /**
     * Find the "best guess" middle value among comparables. If there is an even
     * number of total values, the lower of the two middle values will be returned.
     * @param <T> type of values processed by this method
     * @param comparator to use for comparisons
     * @param items to compare
     * @return T at middle position
     * @throws NullPointerException if items or comparator is {@code null}
     * @throws IllegalArgumentException if items is empty or contains {@code null} values
     * @since 3.0.1
     */
    @SafeVarargs
    public static <T> T median(final Comparator<T> comparator, final T... items) {
        Validate.notEmpty(items, "null/empty items");
        Validate.noNullElements(items);
        Validate.notNull(comparator, "null comparator");
        final TreeSet<T> sort = new TreeSet<>(comparator);
        Collections.addAll(sort, items);
        @SuppressWarnings("unchecked") //we know all items added were T instances
        final
        T result = (T) sort.toArray()[(sort.size() - 1) / 2];
        return result;
    }

    // Mode
    //-----------------------------------------------------------------------
    /**
     * Find the most frequently occurring item.
     *
     * @param <T> type of values processed by this method
     * @param items to check
     * @return most populous T, {@code null} if non-unique or no items supplied
     * @since 3.0.1
     */
    @SafeVarargs
    public static <T> T mode(final T... items) {
        if (ArrayUtils.isNotEmpty(items)) {
            final HashMap<T, MutableInt> occurrences = new HashMap<>(items.length);
            for (final T t : items) {
                final MutableInt count = occurrences.get(t);
                if (count == null) {
                    occurrences.put(t, new MutableInt(1));
                } else {
                    count.increment();
                }
            }
            T result = null;
            int max = 0;
            for (final Map.Entry<T, MutableInt> e : occurrences.entrySet()) {
                final int cmp = e.getValue().intValue();
                if (cmp == max) {
                    result = null;
                } else if (cmp > max) {
                    max = cmp;
                    result = e.getKey();
                }
            }
            return result;
        }
        return null;
    }

    // cloning
    //-----------------------------------------------------------------------
    /**
     * <p>Clone an object.</p>
     *
     * @param <T> the type of the object
     * @param obj  the object to clone, null returns null
     * @return the clone if the object implements {@link Cloneable} otherwise {@code null}
     * @throws CloneFailedException if the object is cloneable and the clone operation fails
     * @since 3.0
     */
    public static <T> T clone(final T obj) {
        if (obj instanceof Cloneable) {
            final Object result;
            if (obj.getClass().isArray()) {
                final Class<?> componentType = obj.getClass().getComponentType();
                if (!componentType.isPrimitive()) {
                    result = ((Object[]) obj).clone();
                } else {
                    int length = Array.getLength(obj);
                    result = Array.newInstance(componentType, length);
                    while (length-- > 0) {
                        Array.set(result, length, Array.get(obj, length));
                    }
                }
            } else {
                try {
                    final Method clone = obj.getClass().getMethod("clone");
                    result = clone.invoke(obj);
                } catch (final NoSuchMethodException e) {
                    throw new CloneFailedException("Cloneable type "
                        + obj.getClass().getName()
                        + " has no clone method", e);
                } catch (final IllegalAccessException e) {
                    throw new CloneFailedException("Cannot clone Cloneable type "
                        + obj.getClass().getName(), e);
                } catch (final InvocationTargetException e) {
                    throw new CloneFailedException("Exception cloning Cloneable type "
                        + obj.getClass().getName(), e.getCause());
                }
            }
            @SuppressWarnings("unchecked") // OK because input is of type T
            final T checked = (T) result;
            return checked;
        }

        return null;
    }

    /**
     * <p>Clone an object if possible.</p>
     *
     * <p>This method is similar to {@link #clone(Object)}, but will return the provided
     * instance as the return value instead of {@code null} if the instance
     * is not cloneable. This is more convenient if the caller uses different
     * implementations (e.g. of a service) and some of the implementations do not allow concurrent
     * processing or have state. In such cases the implementation can simply provide a proper
     * clone implementation and the caller's code does not have to change.</p>
     *
     * @param <T> the type of the object
     * @param obj  the object to clone, null returns null
     * @return the clone if the object implements {@link Cloneable} otherwise the object itself
     * @throws CloneFailedException if the object is cloneable and the clone operation fails
     * @since 3.0
     */
    public static <T> T cloneIfPossible(final T obj) {
        final T clone = clone(obj);
        return clone == null ? obj : clone;
    }

    // Null
    //-----------------------------------------------------------------------
    /**
     * <p>Class used as a null placeholder where {@code null}
     * has another meaning.</p>
     *
     * <p>For example, in a {@code HashMap} the
     * {@link HashMap#get(Object)} method returns
     * {@code null} if the {@code Map} contains {@code null} or if there is
     * no matching key. The {@code Null} placeholder can be used to distinguish
     * between these two cases.</p>
     *
     * <p>Another example is {@code Hashtable}, where {@code null}
     * cannot be stored.</p>
     */
    public static class Null implements Serializable {
        /**
         * Required for serialization support. Declare serialization compatibility with Commons Lang 1.0
         *
         * @see Serializable
         */
        private static final long serialVersionUID = 7092611880189329093L;

        /**
         * Restricted constructor - singleton.
         */
        Null() {
            super();
        }

        /**
         * <p>Ensure singleton.</p>
         *
         * @return the singleton value
         */
        private Object readResolve() {
            return ObjectUtils.NULL;
        }
    }


    // Constants (LANG-816):
    /*
        These methods ensure constants are not inlined by javac.
        For example, typically a developer might declare a constant like so:

            public final static int MAGIC_NUMBER = 5;

        Should a different jar file refer to this, and the MAGIC_NUMBER
        is changed a later date (e.g., MAGIC_NUMBER = 6), the different jar
        file will need to recompile itself.  This is because javac
        typically inlines the primitive or String constant directly into
        the bytecode, and removes the reference to the MAGIC_NUMBER field.

        To help the other jar (so that it does not need to recompile
        when constants are changed) the original developer can declare
        their constant using one of the CONST() utility methods, instead:

            public final static int MAGIC_NUMBER = CONST(5);
     */


    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static boolean MAGIC_FLAG = ObjectUtils.CONST(true);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the boolean value to return
     * @return the boolean v, unchanged
     * @since 3.2
     */
    public static boolean CONST(final boolean v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static byte MAGIC_BYTE = ObjectUtils.CONST((byte) 127);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the byte value to return
     * @return the byte v, unchanged
     * @since 3.2
     */
    public static byte CONST(final byte v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static byte MAGIC_BYTE = ObjectUtils.CONST_BYTE(127);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the byte literal (as an int) value to return
     * @throws IllegalArgumentException if the value passed to v
     *         is larger than a byte, that is, smaller than -128 or
     *         larger than 127.
     * @return the byte v, unchanged
     * @since 3.2
     */
    public static byte CONST_BYTE(final int v) throws IllegalArgumentException {
        if (v < Byte.MIN_VALUE || v > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -128 and 127: [" + v + "]");
        }
        return (byte) v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static char MAGIC_CHAR = ObjectUtils.CONST('a');
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the char value to return
     * @return the char v, unchanged
     * @since 3.2
     */
    public static char CONST(final char v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static short MAGIC_SHORT = ObjectUtils.CONST((short) 123);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the short value to return
     * @return the short v, unchanged
     * @since 3.2
     */
    public static short CONST(final short v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static short MAGIC_SHORT = ObjectUtils.CONST_SHORT(127);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the short literal (as an int) value to return
     * @throws IllegalArgumentException if the value passed to v
     *         is larger than a short, that is, smaller than -32768 or
     *         larger than 32767.
     * @return the byte v, unchanged
     * @since 3.2
     */
    public static short CONST_SHORT(final int v) throws IllegalArgumentException {
        if (v < Short.MIN_VALUE || v > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -32768 and 32767: [" + v + "]");
        }
        return (short) v;
    }


    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static int MAGIC_INT = ObjectUtils.CONST(123);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the int value to return
     * @return the int v, unchanged
     * @since 3.2
     */
    public static int CONST(final int v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static long MAGIC_LONG = ObjectUtils.CONST(123L);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the long value to return
     * @return the long v, unchanged
     * @since 3.2
     */
    public static long CONST(final long v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static float MAGIC_FLOAT = ObjectUtils.CONST(1.0f);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the float value to return
     * @return the float v, unchanged
     * @since 3.2
     */
    public static float CONST(final float v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static double MAGIC_DOUBLE = ObjectUtils.CONST(1.0);
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param v the double value to return
     * @return the double v, unchanged
     * @since 3.2
     */
    public static double CONST(final double v) {
        return v;
    }

    /**
     * This method returns the provided value unchanged.
     * This can prevent javac from inlining a constant
     * field, e.g.,
     *
     * <pre>
     *     public final static String MAGIC_STRING = ObjectUtils.CONST("abc");
     * </pre>
     *
     * This way any jars that refer to this field do not
     * have to recompile themselves if the field's value
     * changes at some future date.
     *
     * @param <T> the Object type
     * @param v the genericized Object value to return (typically a String).
     * @return the genericized Object v, unchanged (typically a String).
     * @since 3.2
     */
    public static <T> T CONST(final T v) {
        return v;
    }

}
