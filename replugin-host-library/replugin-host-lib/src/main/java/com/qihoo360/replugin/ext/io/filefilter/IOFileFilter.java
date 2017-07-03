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
package com.qihoo360.replugin.ext.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An interface which brings the FileFilter and FilenameFilter
 * interfaces together.
 *
 * @since 1.0
 * @version $Id: IOFileFilter.java 1471767 2013-04-24 23:24:19Z sebb $
 */
public interface IOFileFilter extends FileFilter, FilenameFilter {

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link FileFilter}.
     *
     * @param file  the File to check
     * @return true if this file matches the test
     */
    boolean accept(File file);

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link FilenameFilter}.
     *
     * @param dir  the directory File to check
     * @param name  the filename within the directory to check
     * @return true if this file matches the test
     */
    boolean accept(File dir, String name);

}
