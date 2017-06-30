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
package com.qihoo360.replugin.ext.parser.utils.xml;

import java.io.IOException;
import java.io.Writer;

/**
 * Translates codepoints to their XML numeric entity escaped value.
 */
class NumericEntityEscaper extends CodePointTranslator {

    private final int below;
    private final int above;
    private final boolean between;

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> for the specified range. This is
     * the underlying method for the other constructors/builders. The <code>below</code>
     * and <code>above</code> boundaries are inclusive when <code>between</code> is
     * <code>true</code> and exclusive when it is <code>false</code>. </p>
     *
     * @param below   int value representing the lowest codepoint boundary
     * @param above   int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    private NumericEntityEscaper(final int below, final int above, final boolean between) {
        this.below = below;
        this.above = above;
        this.between = between;
    }

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> for all characters. </p>
     */
    public NumericEntityEscaper() {
        this(0, Integer.MAX_VALUE, true);
    }

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> below the specified value (exclusive). </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code NumericEntityEscaper} instance
     */
    public static NumericEntityEscaper below(final int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> above the specified value (exclusive). </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code NumericEntityEscaper} instance
     */
    public static NumericEntityEscaper above(final int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> between the specified values (inclusive). </p>
     *
     * @param codepointLow  above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code NumericEntityEscaper} instance
     */
    public static NumericEntityEscaper between(final int codepointLow, final int codepointHigh) {
        return new NumericEntityEscaper(codepointLow, codepointHigh, true);
    }

    /**
     * <p>Constructs a <code>NumericEntityEscaper</code> outside of the specified values (exclusive). </p>
     *
     * @param codepointLow  below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code NumericEntityEscaper} instance
     */
    public static NumericEntityEscaper outsideOf(final int codepointLow, final int codepointHigh) {
        return new NumericEntityEscaper(codepointLow, codepointHigh, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean translate(final int codepoint, final Writer out) throws IOException {
        if (between) {
            if (codepoint < below || codepoint > above) {
                return false;
            }
        } else {
            if (codepoint >= below && codepoint <= above) {
                return false;
            }
        }

        out.write("&#");
        out.write(Integer.toString(codepoint, 10));
        out.write(';');
        return true;
    }
}
