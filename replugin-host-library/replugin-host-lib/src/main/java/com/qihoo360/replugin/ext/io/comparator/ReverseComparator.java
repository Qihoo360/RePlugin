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
package com.qihoo360.replugin.ext.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Reverses the result of comparing two objects using
 * the delegate {@link Comparator}.
 *
 * @version $Id: ReverseComparator.java 1642757 2014-12-01 21:09:30Z sebb $
 * @since 1.4
 */
class ReverseComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = -4808255005272229056L;
    private final Comparator<File> delegate;

    /**
     * Construct an instance with the sepecified delegate {@link Comparator}.
     *
     * @param delegate The comparator to delegate to
     */
    public ReverseComparator(final Comparator<File> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate comparator is missing");
        }
        this.delegate = delegate;
    }

    /**
     * Compare using the delegate Comparator, but reversing the result.
     * 
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return the result from the delegate {@link Comparator#compare(Object, Object)}
     * reversing the value (i.e. positive becomes negative and vice versa)
     */
    public int compare(final File file1, final File file2) {
        return delegate.compare(file2, file1); // parameters switched round
    }

    /**
     * String representation of this file comparator.
     *
     * @return String representation of this file comparator
     */
    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

}
