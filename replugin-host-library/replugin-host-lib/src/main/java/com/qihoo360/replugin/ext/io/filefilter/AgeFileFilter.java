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
import java.util.Date;

import com.qihoo360.replugin.ext.io.FileUtils;

/**
 * Filters files based on a cutoff time, can filter either newer
 * files or files equal to or older.
 * <p>
 * For example, to print all files and directories in the
 * current directory older than one day:
 *
 * <pre>
 * File dir = new File(".");
 * // We are interested in files older than one day
 * long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * String[] files = dir.list( new AgeFileFilter(cutoff) );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @version $Id: AgeFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 * @see FileFilterUtils#ageFileFilter(Date)
 * @see FileFilterUtils#ageFileFilter(File)
 * @see FileFilterUtils#ageFileFilter(long)
 * @see FileFilterUtils#ageFileFilter(Date, boolean)
 * @see FileFilterUtils#ageFileFilter(File, boolean)
 * @see FileFilterUtils#ageFileFilter(long, boolean)
 * @since 1.2
 */
public class AgeFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -2132740084016138541L;

    /** The cutoff time threshold. */
    private final long cutoff;
    /** Whether the files accepted will be older or newer. */
    private final boolean acceptOlder;

    /**
     * Constructs a new age file filter for files equal to or older than
     * a certain cutoff
     *
     * @param cutoff  the threshold age of the files
     */
    public AgeFileFilter(final long cutoff) {
        this(cutoff, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain cutoff.
     *
     * @param cutoff  the threshold age of the files
     * @param acceptOlder  if true, older files (at or before the cutoff)
     * are accepted, else newer ones (after the cutoff).
     */
    public AgeFileFilter(final long cutoff, final boolean acceptOlder) {
        this.acceptOlder = acceptOlder;
        this.cutoff = cutoff;
    }

    /**
     * Constructs a new age file filter for files older than (at or before)
     * a certain cutoff date.
     *
     * @param cutoffDate  the threshold age of the files
     */
    public AgeFileFilter(final Date cutoffDate) {
        this(cutoffDate, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain cutoff date.
     *
     * @param cutoffDate  the threshold age of the files
     * @param acceptOlder  if true, older files (at or before the cutoff)
     * are accepted, else newer ones (after the cutoff).
     */
    public AgeFileFilter(final Date cutoffDate, final boolean acceptOlder) {
        this(cutoffDate.getTime(), acceptOlder);
    }

    /**
     * Constructs a new age file filter for files older than (at or before)
     * a certain File (whose last modification time will be used as reference).
     *
     * @param cutoffReference  the file whose last modification
     *        time is usesd as the threshold age of the files
     */
    public AgeFileFilter(final File cutoffReference) {
        this(cutoffReference, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain File (whose last modification time will be used as
     * reference).
     *
     * @param cutoffReference  the file whose last modification
     *        time is usesd as the threshold age of the files
     * @param acceptOlder  if true, older files (at or before the cutoff)
     * are accepted, else newer ones (after the cutoff).
     */
    public AgeFileFilter(final File cutoffReference, final boolean acceptOlder) {
        this(cutoffReference.lastModified(), acceptOlder);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the last modification of the file matches cutoff
     * favorably.
     * <p>
     * If last modification time equals cutoff and newer files are required,
     * file <b>IS NOT</b> selected.
     * If last modification time equals cutoff and older files are required,
     * file <b>IS</b> selected.
     *
     * @param file  the File to check
     * @return true if the filename matches
     */
    @Override
    public boolean accept(final File file) {
        final boolean newer = FileUtils.isFileNewer(file, cutoff);
        return acceptOlder ? !newer : newer;
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        final String condition = acceptOlder ? "<=" : ">";
        return super.toString() + "(" + condition + cutoff + ")";
    }
}
