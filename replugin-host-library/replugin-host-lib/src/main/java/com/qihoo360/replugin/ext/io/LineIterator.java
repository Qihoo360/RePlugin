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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator over the lines in a <code>Reader</code>.
 * <p>
 * <code>LineIterator</code> holds a reference to an open <code>Reader</code>.
 * When you have finished with the iterator you should close the reader
 * to free internal resources. This can be done by closing the reader directly,
 * or by calling the {@link #close()} or {@link #closeQuietly(LineIterator)}
 * method on the iterator.
 * <p>
 * The recommended usage pattern is:
 * <pre>
 * LineIterator it = FileUtils.lineIterator(file, "UTF-8");
 * try {
 *   while (it.hasNext()) {
 *     String line = it.nextLine();
 *     // do something with line
 *   }
 * } finally {
 *   it.close();
 * }
 * </pre>
 *
 * @version $Id: LineIterator.java 1471767 2013-04-24 23:24:19Z sebb $
 * @since 1.2
 */
public class LineIterator implements Iterator<String> {

    // N.B. This class deliberately does not implement Iterable, see https://issues.apache.org/jira/browse/IO-181

    /** The reader that is being read. */
    private final BufferedReader bufferedReader;
    /** The current line. */
    private String cachedLine;
    /** A flag indicating if the iterator has been fully read. */
    private boolean finished = false;

    /**
     * Constructs an iterator of the lines for a <code>Reader</code>.
     *
     * @param reader the <code>Reader</code> to read from, not null
     * @throws IllegalArgumentException if the reader is null
     */
    public LineIterator(final Reader reader) throws IllegalArgumentException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null");
        }
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Indicates whether the <code>Reader</code> has more lines.
     * If there is an <code>IOException</code> then {@link #close()} will
     * be called on this instance.
     *
     * @return {@code true} if the Reader has more lines
     * @throws IllegalStateException if an IO exception occurs
     */
    public boolean hasNext() {
        if (cachedLine != null) {
            return true;
        } else if (finished) {
            return false;
        } else {
            try {
                while (true) {
                    final String line = bufferedReader.readLine();
                    if (line == null) {
                        finished = true;
                        return false;
                    } else if (isValidLine(line)) {
                        cachedLine = line;
                        return true;
                    }
                }
            } catch(final IOException ioe) {
                close();
                throw new IllegalStateException(ioe);
            }
        }
    }

    /**
     * Overridable method to validate each line that is returned.
     * This implementation always returns true.
     * @param line  the line that is to be validated
     * @return true if valid, false to remove from the iterator
     */
    protected boolean isValidLine(final String line) {
        return true;
    }

    /**
     * Returns the next line in the wrapped <code>Reader</code>.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     */
    public String next() {
        return nextLine();
    }

    /**
     * Returns the next line in the wrapped <code>Reader</code>.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     */
    public String nextLine() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        final String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;
    }

    /**
     * Closes the underlying <code>Reader</code> quietly.
     * This method is useful if you only want to process the first few
     * lines of a larger file. If you do not close the iterator
     * then the <code>Reader</code> remains open.
     * This method can safely be called multiple times.
     */
    public void close() {
        finished = true;
        IOUtils.closeQuietly(bufferedReader);
        cachedLine = null;
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException always
     */
    public void remove() {
        throw new UnsupportedOperationException("Remove unsupported on LineIterator");
    }

    //-----------------------------------------------------------------------
    /**
     * Closes the iterator, handling null and ignoring exceptions.
     *
     * @param iterator  the iterator to close
     */
    public static void closeQuietly(final LineIterator iterator) {
        if (iterator != null) {
            iterator.close();
        }
    }

}
