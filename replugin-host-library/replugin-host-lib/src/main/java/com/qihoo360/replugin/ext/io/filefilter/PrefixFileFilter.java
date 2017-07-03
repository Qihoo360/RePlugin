/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qihoo360.replugin.ext.io.filefilter;

import com.qihoo360.replugin.ext.io.IOCase;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Filters filenames for a certain prefix.
 * <p>
 * For example, to print all files and directories in the
 * current directory whose name starts with <code>Test</code>:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( new PrefixFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.0
 * @version $Id: PrefixFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 * @see FileFilterUtils#prefixFileFilter(String)
 * @see FileFilterUtils#prefixFileFilter(String, IOCase)
 */
public class PrefixFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 8533897440809599867L;

    /** The filename prefixes to search for */
    private final String[] prefixes;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Constructs a new Prefix file filter for a single prefix.
     *
     * @param prefix  the prefix to allow, must not be null
     * @throws IllegalArgumentException if the prefix is null
     */
    public PrefixFileFilter(final String prefix) {
        this(prefix, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Prefix file filter for a single prefix
     * specifying case-sensitivity.
     *
     * @param prefix  the prefix to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the prefix is null
     * @since 1.4
     */
    public PrefixFileFilter(final String prefix, final IOCase caseSensitivity) {
        if (prefix == null) {
            throw new IllegalArgumentException("The prefix must not be null");
        }
        this.prefixes = new String[] {prefix};
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new Prefix file filter for any of an array of prefixes.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     *
     * @param prefixes  the prefixes to allow, must not be null
     * @throws IllegalArgumentException if the prefix array is null
     */
    public PrefixFileFilter(final String[] prefixes) {
        this(prefixes, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Prefix file filter for any of an array of prefixes
     * specifying case-sensitivity.
     *
     * @param prefixes  the prefixes to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the prefix is null
     * @since 1.4
     */
    public PrefixFileFilter(final String[] prefixes, final IOCase caseSensitivity) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The array of prefixes must not be null");
        }
        this.prefixes = new String[prefixes.length];
        System.arraycopy(prefixes, 0, this.prefixes, 0, prefixes.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new Prefix file filter for a list of prefixes.
     *
     * @param prefixes  the prefixes to allow, must not be null
     * @throws IllegalArgumentException if the prefix list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public PrefixFileFilter(final List<String> prefixes) {
        this(prefixes, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Prefix file filter for a list of prefixes
     * specifying case-sensitivity.
     *
     * @param prefixes  the prefixes to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the prefix list is null
     * @throws ClassCastException if the list does not contain Strings
     * @since 1.4
     */
    public PrefixFileFilter(final List<String> prefixes, final IOCase caseSensitivity) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The list of prefixes must not be null");
        }
        this.prefixes = prefixes.toArray(new String[prefixes.size()]);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Checks to see if the filename starts with the prefix.
     *
     * @param file  the File to check
     * @return true if the filename starts with one of our prefixes
     */
    @Override
    public boolean accept(final File file) {
        final String name = file.getName();
        for (final String prefix : this.prefixes) {
            if (caseSensitivity.checkStartsWith(name, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the filename starts with the prefix.
     *
     * @param file  the File directory
     * @param name  the filename
     * @return true if the filename starts with one of our prefixes
     */
    @Override
    public boolean accept(final File file, final String name) {
        for (final String prefix : prefixes) {
            if (caseSensitivity.checkStartsWith(name, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        if (prefixes != null) {
            for (int i = 0; i < prefixes.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(prefixes[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
