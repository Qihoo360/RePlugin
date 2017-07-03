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
package com.qihoo360.replugin.ext.lang3.exception;

import com.qihoo360.replugin.ext.lang3.ArrayUtils;
import com.qihoo360.replugin.ext.lang3.StringUtils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.qihoo360.replugin.ext.lang3.ClassUtils;
import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>Provides utilities for manipulating and examining
 * <code>Throwable</code> objects.</p>
 *
 * @since 1.0
 */
public class ExceptionUtils {

    /**
     * <p>Used when printing stack frames to denote the start of a
     * wrapped exception.</p>
     *
     * <p>Package private for accessibility by test suite.</p>
     */
    static final String WRAPPED_MARKER = " [wrapped] ";

    /**
     * <p>The names of methods commonly used to access a wrapped exception.</p>
     */
    // TODO: Remove in Lang 4.0
    private static final String[] CAUSE_METHOD_NAMES = {
        "getCause",
        "getNextException",
        "getTargetException",
        "getException",
        "getSourceException",
        "getRootCause",
        "getCausedByException",
        "getNested",
        "getLinkedException",
        "getNestedException",
        "getLinkedCause",
        "getThrowable",
    };

    /**
     * <p>
     * Public constructor allows an instance of <code>ExceptionUtils</code> to be created, although that is not
     * normally necessary.
     * </p>
     */
    public ExceptionUtils() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the default names used when searching for the cause of an exception.</p>
     *
     * <p>This may be modified and used in the overloaded getCause(Throwable, String[]) method.</p>
     *
     * @return cloned array of the default method names
     * @since 3.0
     * @deprecated This feature will be removed in Lang 4.0
     */
    @Deprecated
    public static String[] getDefaultCauseMethodNames() {
        return ArrayUtils.clone(CAUSE_METHOD_NAMES);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
     *
     * <p>The method searches for methods with specific names that return a
     * <code>Throwable</code> object. This will pick up most wrapping exceptions,
     * including those from JDK 1.4.
     *
     * <p>The default list searched for are:</p>
     * <ul>
     *  <li><code>getCause()</code></li>
     *  <li><code>getNextException()</code></li>
     *  <li><code>getTargetException()</code></li>
     *  <li><code>getException()</code></li>
     *  <li><code>getSourceException()</code></li>
     *  <li><code>getRootCause()</code></li>
     *  <li><code>getCausedByException()</code></li>
     *  <li><code>getNested()</code></li>
     * </ul>
     *
     * <p>If none of the above is found, returns <code>null</code>.</p>
     *
     * @param throwable  the throwable to introspect for a cause, may be null
     * @return the cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     * @since 1.0
     * @deprecated This feature will be removed in Lang 4.0, use {@link Throwable#getCause} instead
     */
    @Deprecated
    public static Throwable getCause(final Throwable throwable) {
        return getCause(throwable, null);
    }

    /**
     * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
     *
     * <p>A <code>null</code> set of method names means use the default set.
     * A <code>null</code> in the set of method names will be ignored.</p>
     *
     * @param throwable  the throwable to introspect for a cause, may be null
     * @param methodNames  the method names, null treated as default set
     * @return the cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     * @since 1.0
     * @deprecated This feature will be removed in Lang 4.0, use {@link Throwable#getCause} instead
     */
    @Deprecated
    public static Throwable getCause(final Throwable throwable, String[] methodNames) {
        if (throwable == null) {
            return null;
        }

        if (methodNames == null) {
            final Throwable cause = throwable.getCause();
            if (cause != null) {
                return cause;
            }

            methodNames = CAUSE_METHOD_NAMES;
        }

        for (final String methodName : methodNames) {
            if (methodName != null) {
                final Throwable legacyCause = getCauseUsingMethodName(throwable, methodName);
                if (legacyCause != null) {
                    return legacyCause;
                }
            }
        }

        return null;
    }

    /**
     * <p>Introspects the <code>Throwable</code> to obtain the root cause.</p>
     *
     * <p>This method walks through the exception chain to the last element,
     * "root" of the tree, using {@link #getCause(Throwable)}, and
     * returns that exception.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. If the throwable parameter
     * has a cause of itself, then null will be returned. If the throwable
     * parameter cause chain loops, the last element in the chain before the
     * loop is returned.</p>
     *
     * @param throwable  the throwable to get the root cause for, may be null
     * @return the root cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     */
    public static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.size() < 2 ? null : list.get(list.size() - 1);
    }

    /**
     * <p>Finds a <code>Throwable</code> by method name.</p>
     *
     * @param throwable  the exception to examine
     * @param methodName  the name of the method to find and invoke
     * @return the wrapped exception, or <code>null</code> if not found
     */
    // TODO: Remove in Lang 4.0
    private static Throwable getCauseUsingMethodName(final Throwable throwable, final String methodName) {
        Method method = null;
        try {
            method = throwable.getClass().getMethod(methodName);
        } catch (final NoSuchMethodException | SecurityException ignored) { // NOPMD
            // exception ignored
        }

        if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Throwable) method.invoke(throwable);
            } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) { // NOPMD
                // exception ignored
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Counts the number of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return <code>1</code>.
     * A throwable with one cause will return <code>2</code> and so on.
     * A <code>null</code> throwable will return <code>0</code>.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. The cause chain is
     * processed until the end is reached, or until the next item in the
     * chain is already in the result set.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @return the count of throwables, zero if null input
     */
    public static int getThrowableCount(final Throwable throwable) {
        return getThrowableList(throwable).size();
    }

    /**
     * <p>Returns the list of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return an array containing
     * one element - the input throwable.
     * A throwable with one cause will return an array containing
     * two elements. - the input throwable and the cause throwable.
     * A <code>null</code> throwable will return an array of size zero.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. The cause chain is
     * processed until the end is reached, or until the next item in the
     * chain is already in the result set.</p>
     *
     * @see #getThrowableList(Throwable)
     * @param throwable  the throwable to inspect, may be null
     * @return the array of throwables, never null
     */
    public static Throwable[] getThrowables(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.toArray(new Throwable[list.size()]);
    }

    /**
     * <p>Returns the list of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return a list containing
     * one element - the input throwable.
     * A throwable with one cause will return a list containing
     * two elements. - the input throwable and the cause throwable.
     * A <code>null</code> throwable will return a list of size zero.</p>
     *
     * <p>This method handles recursive cause structures that might
     * otherwise cause infinite loops. The cause chain is processed until
     * the end is reached, or until the next item in the chain is already
     * in the result set.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @return the list of throwables, never null
     * @since Commons Lang 2.2
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = ExceptionUtils.getCause(throwable);
        }
        return list;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified class (exactly) in the exception chain.
     * Subclasses of the specified class do not match - see
     * {@link #indexOfType(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param clazz  the class to search for, subclasses do not match, null returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     */
    public static int indexOfThrowable(final Throwable throwable, final Class<?> clazz) {
        return indexOf(throwable, clazz, 0, false);
    }

    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified type in the exception chain from
     * a specified index.
     * Subclasses of the specified class do not match - see
     * {@link #indexOfType(Throwable, Class, int)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.
     * A negative start index is treated as zero.
     * A start index greater than the number of throwables returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param clazz  the class to search for, subclasses do not match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     */
    public static int indexOfThrowable(final Throwable throwable, final Class<?> clazz, final int fromIndex) {
        return indexOf(throwable, clazz, fromIndex, false);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified class or subclass in the exception chain.
     * Subclasses of the specified class do match - see
     * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     * @since 2.1
     */
    public static int indexOfType(final Throwable throwable, final Class<?> type) {
        return indexOf(throwable, type, 0, true);
    }

    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified type in the exception chain from
     * a specified index.
     * Subclasses of the specified class do match - see
     * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.
     * A negative start index is treated as zero.
     * A start index greater than the number of throwables returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     * @since 2.1
     */
    public static int indexOfType(final Throwable throwable, final Class<?> type, final int fromIndex) {
        return indexOf(throwable, type, fromIndex, true);
    }

    /**
     * <p>Worker method for the <code>indexOfType</code> methods.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @param subclass if <code>true</code>, compares with {@link Class#isAssignableFrom(Class)}, otherwise compares
     * using references
     * @return index of the <code>type</code> within throwables nested within the specified <code>throwable</code>
     */
    private static int indexOf(final Throwable throwable, final Class<?> type, int fromIndex, final boolean subclass) {
        if (throwable == null || type == null) {
            return -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        final Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        if (fromIndex >= throwables.length) {
            return -1;
        }
        if (subclass) {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.isAssignableFrom(throwables[i].getClass())) {
                    return i;
                }
            }
        } else {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.equals(throwables[i].getClass())) {
                    return i;
                }
            }
        }
        return -1;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Prints a compact stack trace for the root cause of a throwable
     * to <code>System.err</code>.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output
     * @since 2.0
     */
    public static void printRootCauseStackTrace(final Throwable throwable) {
        printRootCauseStackTrace(throwable, System.err);
    }

    /**
     * <p>Prints a compact stack trace for the root cause of a throwable.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output, may be null
     * @param stream  the stream to output to, may not be null
     * @throws IllegalArgumentException if the stream is <code>null</code>
     * @since 2.0
     */
    public static void printRootCauseStackTrace(final Throwable throwable, final PrintStream stream) {
        if (throwable == null) {
            return;
        }
        Validate.isTrue(stream != null, "The PrintStream must not be null");
        final String trace[] = getRootCauseStackTrace(throwable);
        for (final String element : trace) {
            stream.println(element);
        }
        stream.flush();
    }

    /**
     * <p>Prints a compact stack trace for the root cause of a throwable.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output, may be null
     * @param writer  the writer to output to, may not be null
     * @throws IllegalArgumentException if the writer is <code>null</code>
     * @since 2.0
     */
    public static void printRootCauseStackTrace(final Throwable throwable, final PrintWriter writer) {
        if (throwable == null) {
            return;
        }
        Validate.isTrue(writer != null, "The PrintWriter must not be null");
        final String trace[] = getRootCauseStackTrace(throwable);
        for (final String element : trace) {
            writer.println(element);
        }
        writer.flush();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Creates a compact stack trace for the root cause of the supplied
     * <code>Throwable</code>.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * It consists of the root exception followed by each of its wrapping
     * exceptions separated by '[wrapped]'. Note that this is the opposite
     * order to the JDK1.4 display.</p>
     *
     * @param throwable  the throwable to examine, may be null
     * @return an array of stack trace frames, never null
     * @since 2.0
     */
    public static String[] getRootCauseStackTrace(final Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final Throwable throwables[] = getThrowables(throwable);
        final int count = throwables.length;
        final List<String> frames = new ArrayList<>();
        List<String> nextTrace = getStackFrameList(throwables[count - 1]);
        for (int i = count; --i >= 0;) {
            final List<String> trace = nextTrace;
            if (i != 0) {
                nextTrace = getStackFrameList(throwables[i - 1]);
                removeCommonFrames(trace, nextTrace);
            }
            if (i == count - 1) {
                frames.add(throwables[i].toString());
            } else {
                frames.add(WRAPPED_MARKER + throwables[i].toString());
            }
            for (int j = 0; j < trace.size(); j++) {
                frames.add(trace.get(j));
            }
        }
        return frames.toArray(new String[frames.size()]);
    }

    /**
     * <p>Removes common frames from the cause trace given the two stack traces.</p>
     *
     * @param causeFrames  stack trace of a cause throwable
     * @param wrapperFrames  stack trace of a wrapper throwable
     * @throws IllegalArgumentException if either argument is null
     * @since 2.0
     */
    public static void removeCommonFrames(final List<String> causeFrames, final List<String> wrapperFrames) {
        if (causeFrames == null || wrapperFrames == null) {
            throw new IllegalArgumentException("The List must not be null");
        }
        int causeFrameIndex = causeFrames.size() - 1;
        int wrapperFrameIndex = wrapperFrames.size() - 1;
        while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
            // Remove the frame from the cause trace if it is the same
            // as in the wrapper trace
            final String causeFrame = causeFrames.get(causeFrameIndex);
            final String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
                causeFrames.remove(causeFrameIndex);
            }
            causeFrameIndex--;
            wrapperFrameIndex--;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Gets the stack trace from a Throwable as a String.</p>
     *
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     *
     * @param throwable  the <code>Throwable</code> to be examined
     * @return the stack trace as generated by the exception's
     *  <code>printStackTrace(PrintWriter)</code> method
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * <p>Captures the stack trace associated with the specified
     * <code>Throwable</code> object, decomposing it into a list of
     * stack frames.</p>
     *
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     *
     * @param throwable  the <code>Throwable</code> to examine, may be null
     * @return an array of strings describing each stack frame, never null
     */
    public static String[] getStackFrames(final Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return getStackFrames(getStackTrace(throwable));
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns an array where each element is a line from the argument.</p>
     *
     * <p>The end of line is determined by the value of {@link SystemUtils#LINE_SEPARATOR}.</p>
     *
     * @param stackTrace  a stack trace String
     * @return an array where each element is a line from the argument
     */
    static String[] getStackFrames(final String stackTrace) {
        final String linebreak = System.lineSeparator();
        final StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        final List<String> list = new ArrayList<>();
        while (frames.hasMoreTokens()) {
            list.add(frames.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * <p>Produces a <code>List</code> of stack frames - the message
     * is not included. Only the trace of the specified exception is
     * returned, any caused by trace is stripped.</p>
     *
     * <p>This works in most cases - it will only fail if the exception
     * message contains a line that starts with:
     * <code>&quot;&nbsp;&nbsp;&nbsp;at&quot;.</code></p>
     *
     * @param t is any throwable
     * @return List of stack frames
     */
    static List<String> getStackFrameList(final Throwable t) {
        final String stackTrace = getStackTrace(t);
        final String linebreak = System.lineSeparator();
        final StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        final List<String> list = new ArrayList<>();
        boolean traceStarted = false;
        while (frames.hasMoreTokens()) {
            final String token = frames.nextToken();
            // Determine if the line starts with <whitespace>at
            final int at = token.indexOf("at");
            if (at != -1 && token.substring(0, at).trim().isEmpty()) {
                traceStarted = true;
                list.add(token);
            } else if (traceStarted) {
                break;
            }
        }
        return list;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a short message summarising the exception.
     * <p>
     * The message returned is of the form
     * {ClassNameWithoutPackage}: {ThrowableMessage}
     *
     * @param th  the throwable to get a message for, null returns empty string
     * @return the message, non-null
     * @since Commons Lang 2.2
     */
    public static String getMessage(final Throwable th) {
        if (th == null) {
            return StringUtils.EMPTY;
        }
        final String clsName = ClassUtils.getShortClassName(th, null);
        final String msg = th.getMessage();
        return clsName + ": " + StringUtils.defaultString(msg);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a short message summarising the root cause exception.
     * <p>
     * The message returned is of the form
     * {ClassNameWithoutPackage}: {ThrowableMessage}
     *
     * @param th  the throwable to get a message for, null returns empty string
     * @return the message, non-null
     * @since Commons Lang 2.2
     */
    public static String getRootCauseMessage(final Throwable th) {
        Throwable root = ExceptionUtils.getRootCause(th);
        root = root == null ? th : root;
        return getMessage(root);
    }

    /**
     * Throw a checked exception without adding the exception to the throws
     * clause of the calling method. This method prevents throws clause
     * pollution and reduces the clutter of "Caused by" exceptions in the
     * stacktrace.
     * <p>
     * The use of this technique may be controversial, but exceedingly useful to
     * library developers.
     * <code>
     *  public int propagateExample { // note that there is no throws clause
     *      try {
     *          return invocation(); // throws IOException
     *      } catch (Exception e) {
     *          return ExceptionUtils.rethrow(e);  // propagates a checked exception
     *      }
     *  }
     * </code>
     * <p>
     * This is an alternative to the more conservative approach of wrapping the
     * checked exception in a RuntimeException:
     * <code>
     *  public int wrapExample { // note that there is no throws clause
     *      try {
     *          return invocation(); // throws IOException
     *      } catch (Error e) {
     *          throw e;
     *      } catch (RuntimeException e) {
     *          throw e;  // wraps a checked exception
     *      } catch (Exception e) {
     *          throw new UndeclaredThrowableException(e);  // wraps a checked exception
     *      }
     *  }
     * </code>
     * <p>
     * One downside to using this approach is that the java compiler will not
     * allow invoking code to specify a checked exception in a catch clause
     * unless there is some code path within the try block that has invoked a
     * method declared with that checked exception. If the invoking site wishes
     * to catch the shaded checked exception, it must either invoke the shaded
     * code through a method re-declaring the desired checked exception, or
     * catch Exception and use the instanceof operator. Either of these
     * techniques are required when interacting with non-java jvm code such as
     * Jython, Scala, or Groovy, since these languages do not consider any
     * exceptions as checked.
     *
     * @param throwable
     *            The throwable to rethrow.
     * @param <R> The type of the returned value.
     * @return Never actually returned, this generic type matches any type
     *         which the calling site requires. "Returning" the results of this
     *         method, as done in the propagateExample above, will satisfy the
     *         java compiler requirement that all code paths return a value.
     * @since 3.5
     * @see #wrapAndThrow(Throwable)
     */
    public static <R> R rethrow(final Throwable throwable) {
        // claim that the typeErasure invocation throws a RuntimeException
        return ExceptionUtils.<R, RuntimeException> typeErasure(throwable);
    }

    /**
     * Claim a Throwable is another Exception type using type erasure. This
     * hides a checked exception from the java compiler, allowing a checked
     * exception to be thrown without having the exception in the method's throw
     * clause.
     */
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(final Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Throw a checked exception without adding the exception to the throws
     * clause of the calling method. For checked exceptions, this method throws
     * an UndeclaredThrowableException wrapping the checked exception. For
     * Errors and RuntimeExceptions, the original exception is rethrown.
     * <p>
     * The downside to using this approach is that invoking code which needs to
     * handle specific checked exceptions must sniff up the exception chain to
     * determine if the caught exception was caused by the checked exception.
     *
     * @param throwable
     *            The throwable to rethrow.
     * @param <R> The type of the returned value.
     * @return Never actually returned, this generic type matches any type
     *         which the calling site requires. "Returning" the results of this
     *         method will satisfy the java compiler requirement that all code
     *         paths return a value.
     * @since 3.5
     * @see #rethrow(Throwable)
     * @see #hasCause(Throwable, Class)
     */
    public static <R> R wrapAndThrow(final Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new UndeclaredThrowableException(throwable);
    }

    /**
     * Does the throwable's causal chain have an immediate or wrapped exception
     * of the given type?
     *
     * @param chain
     *            The root of a Throwable causal chain.
     * @param type
     *            The exception type to test.
     * @return true, if chain is an instance of type or is an
     *         UndeclaredThrowableException wrapping a cause.
     * @since 3.5
     * @see #wrapAndThrow(Throwable)
     */
    public static boolean hasCause(Throwable chain,
            final Class<? extends Throwable> type) {
        if (chain instanceof UndeclaredThrowableException) {
            chain = chain.getCause();
        }
        return type.isInstance(chain);
    }
}
