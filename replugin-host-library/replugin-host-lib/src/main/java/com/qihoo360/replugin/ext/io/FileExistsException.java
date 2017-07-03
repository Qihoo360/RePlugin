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
package com.qihoo360.replugin.ext.io;

import java.io.File;
import java.io.IOException;

/**
 * Indicates that a file already exists.
 * 
 * @version $Id: FileExistsException.java 1415850 2012-11-30 20:51:39Z ggregory $
 * @since 2.0
 */
public class FileExistsException extends IOException {

    /**
     * Defines the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor.
     */
    public FileExistsException() {
        super();
    }

    /**
     * Construct an instance with the specified message.
     *
     * @param message The error message
     */
    public FileExistsException(final String message) {
        super(message);
    }

    /**
     * Construct an instance with the specified file.
     *
     * @param file The file that exists
     */
    public FileExistsException(final File file) {
        super("File " + file + " exists");
    }

}
