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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link java.io.FileFilter} providing conditional OR logic across a list of
 * file filters. This filter returns {@code true} if any filters in the
 * list return {@code true}. Otherwise, it returns {@code false}.
 * Checking of the file filter list stops when the first filter returns
 * {@code true}.
 *
 * @since 1.0
 * @version $Id: OrFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 * @see FileFilterUtils#or(IOFileFilter...)
 */
public class OrFileFilter
        extends AbstractFileFilter
        implements ConditionalFileFilter, Serializable {

    private static final long serialVersionUID = 5767770777065432721L;
    /** The list of file filters. */
    private final List<IOFileFilter> fileFilters;

    /**
     * Constructs a new instance of <code>OrFileFilter</code>.
     *
     * @since 1.1
     */
    public OrFileFilter() {
        this.fileFilters = new ArrayList<IOFileFilter>();
    }

    /**
     * Constructs a new instance of <code>OrFileFilter</code>
     * with the specified filters.
     *
     * @param fileFilters  the file filters for this filter, copied, null ignored
     * @since 1.1
     */
    public OrFileFilter(final List<IOFileFilter> fileFilters) {
        if (fileFilters == null) {
            this.fileFilters = new ArrayList<IOFileFilter>();
        } else {
            this.fileFilters = new ArrayList<IOFileFilter>(fileFilters);
        }
    }

    /**
     * Constructs a new file filter that ORs the result of two other filters.
     * 
     * @param filter1  the first filter, must not be null
     * @param filter2  the second filter, must not be null
     * @throws IllegalArgumentException if either filter is null
     */
    public OrFileFilter(final IOFileFilter filter1, final IOFileFilter filter2) {
        if (filter1 == null || filter2 == null) {
            throw new IllegalArgumentException("The filters must not be null");
        }
        this.fileFilters = new ArrayList<IOFileFilter>(2);
        addFileFilter(filter1);
        addFileFilter(filter2);
    }

    /**
     * {@inheritDoc}
     */
    public void addFileFilter(final IOFileFilter ioFileFilter) {
        this.fileFilters.add(ioFileFilter);
    }

    /**
     * {@inheritDoc}
     */
    public List<IOFileFilter> getFileFilters() {
        return Collections.unmodifiableList(this.fileFilters);
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeFileFilter(final IOFileFilter ioFileFilter) {
        return this.fileFilters.remove(ioFileFilter);
    }

    /**
     * {@inheritDoc}
     */
    public void setFileFilters(final List<IOFileFilter> fileFilters) {
        this.fileFilters.clear();
        this.fileFilters.addAll(fileFilters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file) {
        for (final IOFileFilter fileFilter : fileFilters) {
            if (fileFilter.accept(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file, final String name) {
        for (final IOFileFilter fileFilter : fileFilters) {
            if (fileFilter.accept(file, name)) {
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
        if (fileFilters != null) {
            for (int i = 0; i < fileFilters.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                final Object filter = fileFilters.get(i);
                buffer.append(filter == null ? "null" : filter.toString());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
