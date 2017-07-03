/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qihoo360.replugin.ext.lang3.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>
 * A {@code DiffResult} contains a collection of the differences between two
 * {@link Diffable} objects. Typically these differences are displayed using
 * {@link #toString()} method, which returns a string describing the fields that
 * differ between the objects.
 * </p>
 * <p>
 * Use a {@link DiffBuilder} to build a {@code DiffResult} comparing two objects.
 * </p>
 *
 * @since 3.3
 */
public class DiffResult implements Iterable<Diff<?>> {

    /**
     * <p>
     * The {@code String} returned when the objects have no differences:
     * {@value}
     * </p>
     */
    public static final String OBJECTS_SAME_STRING = "";

    private static final String DIFFERS_STRING = "differs from";

    private final List<Diff<?>> diffs;
    private final Object lhs;
    private final Object rhs;
    private final ToStringStyle style;

    /**
     * <p>
     * Creates a {@link DiffResult} containing the differences between two
     * objects.
     * </p>
     *
     * @param lhs
     *            the left hand object
     * @param rhs
     *            the right hand object
     * @param diffs
     *            the list of differences, may be empty
     * @param style
     *            the style to use for the {@link #toString()} method. May be
     *            {@code null}, in which case
     *            {@link ToStringStyle#DEFAULT_STYLE} is used
     * @throws IllegalArgumentException
     *             if {@code lhs}, {@code rhs} or {@code diffs} is {@code null}
     */
    DiffResult(final Object lhs, final Object rhs, final List<Diff<?>> diffs,
            final ToStringStyle style) {
        Validate.isTrue(lhs != null, "Left hand object cannot be null");
        Validate.isTrue(rhs != null, "Right hand object cannot be null");
        Validate.isTrue(diffs != null, "List of differences cannot be null");

        this.diffs = diffs;
        this.lhs = lhs;
        this.rhs = rhs;

        if (style == null) {
            this.style = ToStringStyle.DEFAULT_STYLE;
        } else {
            this.style = style;
        }
    }

    /**
     * <p>
     * Returns an unmodifiable list of {@code Diff}s. The list may be empty if
     * there were no differences between the objects.
     * </p>
     *
     * @return an unmodifiable list of {@code Diff}s
     */
    public List<Diff<?>> getDiffs() {
        return Collections.unmodifiableList(diffs);
    }

    /**
     * <p>
     * Returns the number of differences between the two objects.
     * </p>
     *
     * @return the number of differences
     */
    public int getNumberOfDiffs() {
        return diffs.size();
    }

    /**
     * <p>
     * Returns the style used by the {@link #toString()} method.
     * </p>
     *
     * @return the style
     */
    public ToStringStyle getToStringStyle() {
        return style;
    }

    /**
     * <p>
     * Builds a {@code String} description of the differences contained within
     * this {@code DiffResult}. A {@link ToStringBuilder} is used for each object
     * and the style of the output is governed by the {@code ToStringStyle}
     * passed to the constructor.
     * </p>
     *
     * <p>
     * If there are no differences stored in this list, the method will return
     * {@link #OBJECTS_SAME_STRING}. Otherwise, using the example given in
     * {@link Diffable} and {@link ToStringStyle#SHORT_PREFIX_STYLE}, an output
     * might be:
     * </p>
     *
     * <pre>
     * Person[name=John Doe,age=32] differs from Person[name=Joe Bloggs,age=26]
     * </pre>
     *
     * <p>
     * This indicates that the objects differ in name and age, but not in
     * smoking status.
     * </p>
     *
     * <p>
     * To use a different {@code ToStringStyle} for an instance of this class,
     * use {@link #toString(ToStringStyle)}.
     * </p>
     *
     * @return a {@code String} description of the differences.
     */
    @Override
    public String toString() {
        return toString(style);
    }

    /**
     * <p>
     * Builds a {@code String} description of the differences contained within
     * this {@code DiffResult}, using the supplied {@code ToStringStyle}.
     * </p>
     *
     * @param style
     *            the {@code ToStringStyle} to use when outputting the objects
     *
     * @return a {@code String} description of the differences.
     */
    public String toString(final ToStringStyle style) {
        if (diffs.size() == 0) {
            return OBJECTS_SAME_STRING;
        }

        final ToStringBuilder lhsBuilder = new ToStringBuilder(lhs, style);
        final ToStringBuilder rhsBuilder = new ToStringBuilder(rhs, style);

        for (final Diff<?> diff : diffs) {
            lhsBuilder.append(diff.getFieldName(), diff.getLeft());
            rhsBuilder.append(diff.getFieldName(), diff.getRight());
        }

        return String.format("%s %s %s", lhsBuilder.build(), DIFFERS_STRING,
                rhsBuilder.build());
    }

    /**
     * <p>
     * Returns an iterator over the {@code Diff} objects contained in this list.
     * </p>
     *
     * @return the iterator
     */
    @Override
    public Iterator<Diff<?>> iterator() {
        return diffs.iterator();
    }
}
