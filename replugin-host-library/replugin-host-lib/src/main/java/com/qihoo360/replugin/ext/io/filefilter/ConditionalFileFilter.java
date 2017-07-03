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

import java.util.List;

/**
 * Defines operations for conditional file filters.
 *
 * @since 1.1
 * @version $Id: ConditionalFileFilter.java 1307462 2012-03-30 15:13:11Z ggregory $
 */
public interface ConditionalFileFilter {

    /**
     * Adds the specified file filter to the list of file filters at the end of
     * the list.
     *
     * @param ioFileFilter the filter to be added
     * @since 1.1
     */
    void addFileFilter(IOFileFilter ioFileFilter);

    /**
     * Returns this conditional file filter's list of file filters.
     *
     * @return the file filter list
     * @since 1.1
     */
    List<IOFileFilter> getFileFilters();

    /**
     * Removes the specified file filter.
     *
     * @param ioFileFilter filter to be removed
     * @return {@code true} if the filter was found in the list,
     * {@code false} otherwise
     * @since 1.1
     */
    boolean removeFileFilter(IOFileFilter ioFileFilter);

    /**
     * Sets the list of file filters, replacing any previously configured
     * file filters on this filter.
     *
     * @param fileFilters the list of filters
     * @since 1.1
     */
    void setFileFilters(List<IOFileFilter> fileFilters);

}
