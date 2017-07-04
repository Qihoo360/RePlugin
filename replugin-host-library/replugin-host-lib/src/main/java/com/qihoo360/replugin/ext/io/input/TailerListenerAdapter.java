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
package com.qihoo360.replugin.ext.io.input;

/**
 * {@link TailerListener} Adapter.
 *
 * @version $Id: TailerListenerAdapter.java 1714076 2015-11-12 16:06:41Z krosenvold $
 * @since 2.0
 */
public class TailerListenerAdapter implements TailerListener {

    /**
     * The tailer will call this method during construction,
     * giving the listener a method of stopping the tailer.
     * @param tailer the tailer.
     */
    public void init(final Tailer tailer) {
    }

    /**
     * This method is called if the tailed file is not found.
     */
    public void fileNotFound() {
    }

    /**
     * Called if a file rotation is detected.
     *
     * This method is called before the file is reopened, and fileNotFound may
     * be called if the new file has not yet been created.
     */
    public void fileRotated() {
    }

    /**
     * Handles a line from a Tailer.
     * @param line the line.
     */
    public void handle(final String line) {
    }

    /**
     * Handles an Exception .
     * @param ex the exception.
     */
    public void handle(final Exception ex) {
    }

    /**
     * Called each time the Tailer reaches the end of the file.
     *
     * <b>Note:</b> this is called from the tailer thread.
     *
     * Note: a future version of commons-io will pull this method up to the TailerListener interface,
     * for now clients must subclass this class to use this feature.
     *
     * @since 2.5
     */
    public void endOfFileReached() {
    }
}
