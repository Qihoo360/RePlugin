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

/**
 * An abstract class which implements the Java FileFilter and FilenameFilter 
 * interfaces via the IOFileFilter interface.
 * <p>
 * Note that a subclass <b>must</b> override one of the accept methods,
 * otherwise your class will infinitely loop.
 *
 * @since 1.0
 * @version $Id: AbstractFileFilter.java 1415850 2012-11-30 20:51:39Z ggregory $
 */
public abstract class AbstractFileFilter implements IOFileFilter {

    /**
     * Checks to see if the File should be accepted by this filter.
     * 
     * @param file  the File to check
     * @return true if this file matches the test
     */
    public boolean accept(final File file) {
        return accept(file.getParentFile(), file.getName());
    }

    /**
     * Checks to see if the File should be accepted by this filter.
     * 
     * @param dir  the directory File to check
     * @param name  the filename within the directory to check
     * @return true if this file matches the test
     */
    public boolean accept(final File dir, final String name) {
        return accept(new File(dir, name));
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
