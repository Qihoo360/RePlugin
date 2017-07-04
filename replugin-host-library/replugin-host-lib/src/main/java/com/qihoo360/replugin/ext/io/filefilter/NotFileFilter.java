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

/**
 * This filter produces a logical NOT of the filters specified.
 *
 * @since 1.0
 * @version $Id: NotFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 * @see FileFilterUtils#notFileFilter(IOFileFilter)
 */
public class NotFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 6131563330944994230L;
    /** The filter */
    private final IOFileFilter filter;

    /**
     * Constructs a new file filter that NOTs the result of another filter.
     *
     * @param filter  the filter, must not be null
     * @throws IllegalArgumentException if the filter is null
     */
    public NotFileFilter(final IOFileFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The filter must not be null");
        }
        this.filter = filter;
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same File.
     *
     * @param file  the File to check
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file) {
        return ! filter.accept(file);
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same arguments.
     *
     * @param file  the File directory
     * @param name  the filename
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file, final String name) {
        return ! filter.accept(file, name);
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        return super.toString() + "(" + filter.toString()  + ")";
    }

}
