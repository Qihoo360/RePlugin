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
import java.util.HashMap;

/**
 * Translates a value using a lookup table.
 */
class LookupTranslator extends CharSequenceTranslator {

    private final HashMap<String, CharSequence> lookupMap;
    private final int shortest;
    private final int longest;

    /**
     * Define the lookup table to be used in translation
     * <p>
     * Note that, as of Lang 3.1, the key to the lookup table is converted to a
     * java.lang.String, while the value remains as a java.lang.CharSequence.
     * This is because we need the key to support hashCode and equals(Object),
     * allowing it to be the key for a HashMap. See LANG-882.
     *
     * @param lookup CharSequence[][] table of size [*][2]
     */
    public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<>();
        int shortest = Integer.MAX_VALUE;
        int longest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                this.lookupMap.put(seq[0].toString(), seq[1]);
                final int sz = seq[0].length();
                if (sz < shortest) {
                    shortest = sz;
                }
                if (sz > longest) {
                    longest = sz;
                }
            }
        }
        this.shortest = shortest;
        this.longest = longest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        int max = longest;
        if (index + longest > input.length()) {
            max = input.length() - index;
        }
        // descend so as to get a greedy algorithm
        for (int i = max; i >= shortest; i--) {
            final CharSequence subSeq = input.subSequence(index, index + i);
            final CharSequence result = lookupMap.get(subSeq.toString());
            if (result != null) {
                out.write(result.toString());
                return i;
            }
        }
        return 0;
    }
}
