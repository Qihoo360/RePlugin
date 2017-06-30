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

/**
 * Utils method to escape xml string, copied from apache commons lang3
 *
 * @author Liu Dong {@literal <im@dongliu.net>}
 */
public class XmlEscaper {

    public static final CharSequenceTranslator ESCAPE_XML10 =
            new AggregateTranslator(
                    new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
                    new LookupTranslator(EntityArrays.APOS_ESCAPE()),
                    new LookupTranslator(
                            new String[][]{
                                    {"\u0000", ""},
                                    {"\u0001", ""},
                                    {"\u0002", ""},
                                    {"\u0003", ""},
                                    {"\u0004", ""},
                                    {"\u0005", ""},
                                    {"\u0006", ""},
                                    {"\u0007", ""},
                                    {"\u0008", ""},
                                    {"\u000b", ""},
                                    {"\u000c", ""},
                                    {"\u000e", ""},
                                    {"\u000f", ""},
                                    {"\u0010", ""},
                                    {"\u0011", ""},
                                    {"\u0012", ""},
                                    {"\u0013", ""},
                                    {"\u0014", ""},
                                    {"\u0015", ""},
                                    {"\u0016", ""},
                                    {"\u0017", ""},
                                    {"\u0018", ""},
                                    {"\u0019", ""},
                                    {"\u001a", ""},
                                    {"\u001b", ""},
                                    {"\u001c", ""},
                                    {"\u001d", ""},
                                    {"\u001e", ""},
                                    {"\u001f", ""},
                                    {"\ufffe", ""},
                                    {"\uffff", ""}
                            }),
                    NumericEntityEscaper.between(0x7f, 0x84),
                    NumericEntityEscaper.between(0x86, 0x9f),
                    new UnicodeUnpairedSurrogateRemover()
            );

    /**
     * <p>Escapes the characters in a {@code String} using XML entities.</p>
     */
    public static String escapeXml10(final String input) {
        return ESCAPE_XML10.translate(input);
    }
}
