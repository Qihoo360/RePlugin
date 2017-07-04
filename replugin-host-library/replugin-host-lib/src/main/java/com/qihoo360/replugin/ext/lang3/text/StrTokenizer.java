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
package com.qihoo360.replugin.ext.lang3.text;

import com.qihoo360.replugin.ext.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.qihoo360.replugin.ext.lang3.StringUtils;

/**
 * Tokenizes a string based based on delimiters (separators)
 * and supporting quoting and ignored character concepts.
 * <p>
 * This class can split a String into many smaller strings. It aims
 * to do a similar job to {@link java.util.StringTokenizer StringTokenizer},
 * however it offers much more control and flexibility including implementing
 * the <code>ListIterator</code> interface. By default, it is set up
 * like <code>StringTokenizer</code>.
 * <p>
 * The input String is split into a number of <i>tokens</i>.
 * Each token is separated from the next String by a <i>delimiter</i>.
 * One or more delimiter characters must be specified.
 * <p>
 * Each token may be surrounded by quotes.
 * The <i>quote</i> matcher specifies the quote character(s).
 * A quote may be escaped within a quoted section by duplicating itself.
 * <p>
 * Between each token and the delimiter are potentially characters that need trimming.
 * The <i>trimmer</i> matcher specifies these characters.
 * One usage might be to trim whitespace characters.
 * <p>
 * At any point outside the quotes there might potentially be invalid characters.
 * The <i>ignored</i> matcher specifies these characters to be removed.
 * One usage might be to remove new line characters.
 * <p>
 * Empty tokens may be removed or returned as null.
 * <pre>
 * "a,b,c"         - Three tokens "a","b","c"   (comma delimiter)
 * " a, b , c "    - Three tokens "a","b","c"   (default CSV processing trims whitespace)
 * "a, ", b ,", c" - Three tokens "a, " , " b ", ", c" (quoted text untouched)
 * </pre>
 * <p>
 *
 * This tokenizer has the following properties and options:
 *
 * <table summary="Tokenizer Properties">
 *  <tr>
 *   <th>Property</th><th>Type</th><th>Default</th>
 *  </tr>
 *  <tr>
 *   <td>delim</td><td>CharSetMatcher</td><td>{ \t\n\r\f}</td>
 *  </tr>
 *  <tr>
 *   <td>quote</td><td>NoneMatcher</td><td>{}</td>
 *  </tr>
 *  <tr>
 *   <td>ignore</td><td>NoneMatcher</td><td>{}</td>
 *  </tr>
 *  <tr>
 *   <td>emptyTokenAsNull</td><td>boolean</td><td>false</td>
 *  </tr>
 *  <tr>
 *   <td>ignoreEmptyTokens</td><td>boolean</td><td>true</td>
 *  </tr>
 * </table>
 *
 * @since 2.2
 * @deprecated as of 3.6, use commons-text
 * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/StrTokenizer.html">
 * StrTokenizer</a> instead
 */
@Deprecated
public class StrTokenizer implements ListIterator<String>, Cloneable {

    private static final StrTokenizer CSV_TOKENIZER_PROTOTYPE;
    private static final StrTokenizer TSV_TOKENIZER_PROTOTYPE;
    static {
        CSV_TOKENIZER_PROTOTYPE = new StrTokenizer();
        CSV_TOKENIZER_PROTOTYPE.setDelimiterMatcher(StrMatcher.commaMatcher());
        CSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        CSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        CSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        CSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        CSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);

        TSV_TOKENIZER_PROTOTYPE = new StrTokenizer();
        TSV_TOKENIZER_PROTOTYPE.setDelimiterMatcher(StrMatcher.tabMatcher());
        TSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        TSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        TSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        TSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        TSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);
    }

    /** The text to work on. */
    private char chars[];
    /** The parsed tokens */
    private String tokens[];
    /** The current iteration position */
    private int tokenPos;

    /** The delimiter matcher */
    private StrMatcher delimMatcher = StrMatcher.splitMatcher();
    /** The quote matcher */
    private StrMatcher quoteMatcher = StrMatcher.noneMatcher();
    /** The ignored matcher */
    private StrMatcher ignoredMatcher = StrMatcher.noneMatcher();
    /** The trimmer matcher */
    private StrMatcher trimmerMatcher = StrMatcher.noneMatcher();

    /** Whether to return empty tokens as null */
    private boolean emptyAsNull = false;
    /** Whether to ignore empty tokens */
    private boolean ignoreEmptyTokens = true;

    //-----------------------------------------------------------------------

    /**
     * Returns a clone of <code>CSV_TOKENIZER_PROTOTYPE</code>.
     *
     * @return a clone of <code>CSV_TOKENIZER_PROTOTYPE</code>.
     */
    private static StrTokenizer getCSVClone() {
        return (StrTokenizer) CSV_TOKENIZER_PROTOTYPE.clone();
    }

    /**
     * Gets a new tokenizer instance which parses Comma Separated Value strings
     * initializing it with the given input.  The default for CSV processing
     * will be trim whitespace from both ends (which can be overridden with
     * the setTrimmer method).
     * <p>
     * You must call a "reset" method to set the string which you want to parse.
     * @return a new tokenizer instance which parses Comma Separated Value strings
     */
    public static StrTokenizer getCSVInstance() {
        return getCSVClone();
    }

    /**
     * Gets a new tokenizer instance which parses Comma Separated Value strings
     * initializing it with the given input.  The default for CSV processing
     * will be trim whitespace from both ends (which can be overridden with
     * the setTrimmer method).
     *
     * @param input  the text to parse
     * @return a new tokenizer instance which parses Comma Separated Value strings
     */
    public static StrTokenizer getCSVInstance(final String input) {
        final StrTokenizer tok = getCSVClone();
        tok.reset(input);
        return tok;
    }

    /**
     * Gets a new tokenizer instance which parses Comma Separated Value strings
     * initializing it with the given input.  The default for CSV processing
     * will be trim whitespace from both ends (which can be overridden with
     * the setTrimmer method).
     *
     * @param input  the text to parse
     * @return a new tokenizer instance which parses Comma Separated Value strings
     */
    public static StrTokenizer getCSVInstance(final char[] input) {
        final StrTokenizer tok = getCSVClone();
        tok.reset(input);
        return tok;
    }

    /**
     * Returns a clone of <code>TSV_TOKENIZER_PROTOTYPE</code>.
     *
     * @return a clone of <code>TSV_TOKENIZER_PROTOTYPE</code>.
     */
    private static StrTokenizer getTSVClone() {
        return (StrTokenizer) TSV_TOKENIZER_PROTOTYPE.clone();
    }


    /**
     * Gets a new tokenizer instance which parses Tab Separated Value strings.
     * The default for CSV processing will be trim whitespace from both ends
     * (which can be overridden with the setTrimmer method).
     * <p>
     * You must call a "reset" method to set the string which you want to parse.
     * @return a new tokenizer instance which parses Tab Separated Value strings.
     */
    public static StrTokenizer getTSVInstance() {
        return getTSVClone();
    }

    /**
     * Gets a new tokenizer instance which parses Tab Separated Value strings.
     * The default for CSV processing will be trim whitespace from both ends
     * (which can be overridden with the setTrimmer method).
     * @param input  the string to parse
     * @return a new tokenizer instance which parses Tab Separated Value strings.
     */
    public static StrTokenizer getTSVInstance(final String input) {
        final StrTokenizer tok = getTSVClone();
        tok.reset(input);
        return tok;
    }

    /**
     * Gets a new tokenizer instance which parses Tab Separated Value strings.
     * The default for CSV processing will be trim whitespace from both ends
     * (which can be overridden with the setTrimmer method).
     * @param input  the string to parse
     * @return a new tokenizer instance which parses Tab Separated Value strings.
     */
    public static StrTokenizer getTSVInstance(final char[] input) {
        final StrTokenizer tok = getTSVClone();
        tok.reset(input);
        return tok;
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs a tokenizer splitting on space, tab, newline and formfeed
     * as per StringTokenizer, but with no text to tokenize.
     * <p>
     * This constructor is normally used with {@link #reset(String)}.
     */
    public StrTokenizer() {
        super();
        this.chars = null;
    }

    /**
     * Constructs a tokenizer splitting on space, tab, newline and formfeed
     * as per StringTokenizer.
     *
     * @param input  the string which is to be parsed
     */
    public StrTokenizer(final String input) {
        super();
        if (input != null) {
            chars = input.toCharArray();
        } else {
            chars = null;
        }
    }

    /**
     * Constructs a tokenizer splitting on the specified delimiter character.
     *
     * @param input  the string which is to be parsed
     * @param delim  the field delimiter character
     */
    public StrTokenizer(final String input, final char delim) {
        this(input);
        setDelimiterChar(delim);
    }

    /**
     * Constructs a tokenizer splitting on the specified delimiter string.
     *
     * @param input  the string which is to be parsed
     * @param delim  the field delimiter string
     */
    public StrTokenizer(final String input, final String delim) {
        this(input);
        setDelimiterString(delim);
    }

    /**
     * Constructs a tokenizer splitting using the specified delimiter matcher.
     *
     * @param input  the string which is to be parsed
     * @param delim  the field delimiter matcher
     */
    public StrTokenizer(final String input, final StrMatcher delim) {
        this(input);
        setDelimiterMatcher(delim);
    }

    /**
     * Constructs a tokenizer splitting on the specified delimiter character
     * and handling quotes using the specified quote character.
     *
     * @param input  the string which is to be parsed
     * @param delim  the field delimiter character
     * @param quote  the field quoted string character
     */
    public StrTokenizer(final String input, final char delim, final char quote) {
        this(input, delim);
        setQuoteChar(quote);
    }

    /**
     * Constructs a tokenizer splitting using the specified delimiter matcher
     * and handling quotes using the specified quote matcher.
     *
     * @param input  the string which is to be parsed
     * @param delim  the field delimiter matcher
     * @param quote  the field quoted string matcher
     */
    public StrTokenizer(final String input, final StrMatcher delim, final StrMatcher quote) {
        this(input, delim);
        setQuoteMatcher(quote);
    }

    /**
     * Constructs a tokenizer splitting on space, tab, newline and formfeed
     * as per StringTokenizer.
     *
     * @param input  the string which is to be parsed, not cloned
     */
    public StrTokenizer(final char[] input) {
        super();
        this.chars = ArrayUtils.clone(input);
    }

    /**
     * Constructs a tokenizer splitting on the specified character.
     *
     * @param input  the string which is to be parsed, not cloned
     * @param delim the field delimiter character
     */
    public StrTokenizer(final char[] input, final char delim) {
        this(input);
        setDelimiterChar(delim);
    }

    /**
     * Constructs a tokenizer splitting on the specified string.
     *
     * @param input  the string which is to be parsed, not cloned
     * @param delim the field delimiter string
     */
    public StrTokenizer(final char[] input, final String delim) {
        this(input);
        setDelimiterString(delim);
    }

    /**
     * Constructs a tokenizer splitting using the specified delimiter matcher.
     *
     * @param input  the string which is to be parsed, not cloned
     * @param delim  the field delimiter matcher
     */
    public StrTokenizer(final char[] input, final StrMatcher delim) {
        this(input);
        setDelimiterMatcher(delim);
    }

    /**
     * Constructs a tokenizer splitting on the specified delimiter character
     * and handling quotes using the specified quote character.
     *
     * @param input  the string which is to be parsed, not cloned
     * @param delim  the field delimiter character
     * @param quote  the field quoted string character
     */
    public StrTokenizer(final char[] input, final char delim, final char quote) {
        this(input, delim);
        setQuoteChar(quote);
    }

    /**
     * Constructs a tokenizer splitting using the specified delimiter matcher
     * and handling quotes using the specified quote matcher.
     *
     * @param input  the string which is to be parsed, not cloned
     * @param delim  the field delimiter character
     * @param quote  the field quoted string character
     */
    public StrTokenizer(final char[] input, final StrMatcher delim, final StrMatcher quote) {
        this(input, delim);
        setQuoteMatcher(quote);
    }

    // API
    //-----------------------------------------------------------------------
    /**
     * Gets the number of tokens found in the String.
     *
     * @return the number of matched tokens
     */
    public int size() {
        checkTokenized();
        return tokens.length;
    }

    /**
     * Gets the next token from the String.
     * Equivalent to {@link #next()} except it returns null rather than
     * throwing {@link NoSuchElementException} when no tokens remain.
     *
     * @return the next sequential token, or null when no more tokens are found
     */
    public String nextToken() {
        if (hasNext()) {
            return tokens[tokenPos++];
        }
        return null;
    }

    /**
     * Gets the previous token from the String.
     *
     * @return the previous sequential token, or null when no more tokens are found
     */
    public String previousToken() {
        if (hasPrevious()) {
            return tokens[--tokenPos];
        }
        return null;
    }

    /**
     * Gets a copy of the full token list as an independent modifiable array.
     *
     * @return the tokens as a String array
     */
    public String[] getTokenArray() {
        checkTokenized();
        return tokens.clone();
    }

    /**
     * Gets a copy of the full token list as an independent modifiable list.
     *
     * @return the tokens as a String array
     */
    public List<String> getTokenList() {
        checkTokenized();
        final List<String> list = new ArrayList<>(tokens.length);
        for (final String element : tokens) {
            list.add(element);
        }
        return list;
    }

    /**
     * Resets this tokenizer, forgetting all parsing and iteration already completed.
     * <p>
     * This method allows the same tokenizer to be reused for the same String.
     *
     * @return this, to enable chaining
     */
    public StrTokenizer reset() {
        tokenPos = 0;
        tokens = null;
        return this;
    }

    /**
     * Reset this tokenizer, giving it a new input string to parse.
     * In this manner you can re-use a tokenizer with the same settings
     * on multiple input lines.
     *
     * @param input  the new string to tokenize, null sets no text to parse
     * @return this, to enable chaining
     */
    public StrTokenizer reset(final String input) {
        reset();
        if (input != null) {
            this.chars = input.toCharArray();
        } else {
            this.chars = null;
        }
        return this;
    }

    /**
     * Reset this tokenizer, giving it a new input string to parse.
     * In this manner you can re-use a tokenizer with the same settings
     * on multiple input lines.
     *
     * @param input  the new character array to tokenize, not cloned, null sets no text to parse
     * @return this, to enable chaining
     */
    public StrTokenizer reset(final char[] input) {
        reset();
        this.chars = ArrayUtils.clone(input);
        return this;
    }

    // ListIterator
    //-----------------------------------------------------------------------
    /**
     * Checks whether there are any more tokens.
     *
     * @return true if there are more tokens
     */
    @Override
    public boolean hasNext() {
        checkTokenized();
        return tokenPos < tokens.length;
    }

    /**
     * Gets the next token.
     *
     * @return the next String token
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public String next() {
        if (hasNext()) {
            return tokens[tokenPos++];
        }
        throw new NoSuchElementException();
    }

    /**
     * Gets the index of the next token to return.
     *
     * @return the next token index
     */
    @Override
    public int nextIndex() {
        return tokenPos;
    }

    /**
     * Checks whether there are any previous tokens that can be iterated to.
     *
     * @return true if there are previous tokens
     */
    @Override
    public boolean hasPrevious() {
        checkTokenized();
        return tokenPos > 0;
    }

    /**
     * Gets the token previous to the last returned token.
     *
     * @return the previous token
     */
    @Override
    public String previous() {
        if (hasPrevious()) {
            return tokens[--tokenPos];
        }
        throw new NoSuchElementException();
    }

    /**
     * Gets the index of the previous token.
     *
     * @return the previous token index
     */
    @Override
    public int previousIndex() {
        return tokenPos - 1;
    }

    /**
     * Unsupported ListIterator operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is unsupported");
    }

    /**
     * Unsupported ListIterator operation.
     * @param obj this parameter ignored.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void set(final String obj) {
        throw new UnsupportedOperationException("set() is unsupported");
    }

    /**
     * Unsupported ListIterator operation.
     * @param obj this parameter ignored.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void add(final String obj) {
        throw new UnsupportedOperationException("add() is unsupported");
    }

    // Implementation
    //-----------------------------------------------------------------------
    /**
     * Checks if tokenization has been done, and if not then do it.
     */
    private void checkTokenized() {
        if (tokens == null) {
            if (chars == null) {
                // still call tokenize as subclass may do some work
                final List<String> split = tokenize(null, 0, 0);
                tokens = split.toArray(new String[split.size()]);
            } else {
                final List<String> split = tokenize(chars, 0, chars.length);
                tokens = split.toArray(new String[split.size()]);
            }
        }
    }

    /**
     * Internal method to performs the tokenization.
     * <p>
     * Most users of this class do not need to call this method. This method
     * will be called automatically by other (public) methods when required.
     * <p>
     * This method exists to allow subclasses to add code before or after the
     * tokenization. For example, a subclass could alter the character array,
     * offset or count to be parsed, or call the tokenizer multiple times on
     * multiple strings. It is also be possible to filter the results.
     * <p>
     * <code>StrTokenizer</code> will always pass a zero offset and a count
     * equal to the length of the array to this method, however a subclass
     * may pass other values, or even an entirely different array.
     *
     * @param srcChars  the character array being tokenized, may be null
     * @param offset  the start position within the character array, must be valid
     * @param count  the number of characters to tokenize, must be valid
     * @return the modifiable list of String tokens, unmodifiable if null array or zero count
     */
    protected List<String> tokenize(final char[] srcChars, final int offset, final int count) {
        if (srcChars == null || count == 0) {
            return Collections.emptyList();
        }
        final StrBuilder buf = new StrBuilder();
        final List<String> tokenList = new ArrayList<>();
        int pos = offset;

        // loop around the entire buffer
        while (pos >= 0 && pos < count) {
            // find next token
            pos = readNextToken(srcChars, pos, count, buf, tokenList);

            // handle case where end of string is a delimiter
            if (pos >= count) {
                addToken(tokenList, StringUtils.EMPTY);
            }
        }
        return tokenList;
    }

    /**
     * Adds a token to a list, paying attention to the parameters we've set.
     *
     * @param list  the list to add to
     * @param tok  the token to add
     */
    private void addToken(final List<String> list, String tok) {
        if (StringUtils.isEmpty(tok)) {
            if (isIgnoreEmptyTokens()) {
                return;
            }
            if (isEmptyTokenAsNull()) {
                tok = null;
            }
        }
        list.add(tok);
    }

    /**
     * Reads character by character through the String to get the next token.
     *
     * @param srcChars  the character array being tokenized
     * @param start  the first character of field
     * @param len  the length of the character array being tokenized
     * @param workArea  a temporary work area
     * @param tokenList  the list of parsed tokens
     * @return the starting position of the next field (the character
     *  immediately after the delimiter), or -1 if end of string found
     */
    private int readNextToken(final char[] srcChars, int start, final int len, final StrBuilder workArea, final List<String> tokenList) {
        // skip all leading whitespace, unless it is the
        // field delimiter or the quote character
        while (start < len) {
            final int removeLen = Math.max(
                    getIgnoredMatcher().isMatch(srcChars, start, start, len),
                    getTrimmerMatcher().isMatch(srcChars, start, start, len));
            if (removeLen == 0 ||
                getDelimiterMatcher().isMatch(srcChars, start, start, len) > 0 ||
                getQuoteMatcher().isMatch(srcChars, start, start, len) > 0) {
                break;
            }
            start += removeLen;
        }

        // handle reaching end
        if (start >= len) {
            addToken(tokenList, StringUtils.EMPTY);
            return -1;
        }

        // handle empty token
        final int delimLen = getDelimiterMatcher().isMatch(srcChars, start, start, len);
        if (delimLen > 0) {
            addToken(tokenList, StringUtils.EMPTY);
            return start + delimLen;
        }

        // handle found token
        final int quoteLen = getQuoteMatcher().isMatch(srcChars, start, start, len);
        if (quoteLen > 0) {
            return readWithQuotes(srcChars, start + quoteLen, len, workArea, tokenList, start, quoteLen);
        }
        return readWithQuotes(srcChars, start, len, workArea, tokenList, 0, 0);
    }

    /**
     * Reads a possibly quoted string token.
     *
     * @param srcChars  the character array being tokenized
     * @param start  the first character of field
     * @param len  the length of the character array being tokenized
     * @param workArea  a temporary work area
     * @param tokenList  the list of parsed tokens
     * @param quoteStart  the start position of the matched quote, 0 if no quoting
     * @param quoteLen  the length of the matched quote, 0 if no quoting
     * @return the starting position of the next field (the character
     *  immediately after the delimiter, or if end of string found,
     *  then the length of string
     */
    private int readWithQuotes(final char[] srcChars, final int start, final int len, final StrBuilder workArea,
                               final List<String> tokenList, final int quoteStart, final int quoteLen) {
        // Loop until we've found the end of the quoted
        // string or the end of the input
        workArea.clear();
        int pos = start;
        boolean quoting = quoteLen > 0;
        int trimStart = 0;

        while (pos < len) {
            // quoting mode can occur several times throughout a string
            // we must switch between quoting and non-quoting until we
            // encounter a non-quoted delimiter, or end of string
            if (quoting) {
                // In quoting mode

                // If we've found a quote character, see if it's
                // followed by a second quote.  If so, then we need
                // to actually put the quote character into the token
                // rather than end the token.
                if (isQuote(srcChars, pos, len, quoteStart, quoteLen)) {
                    if (isQuote(srcChars, pos + quoteLen, len, quoteStart, quoteLen)) {
                        // matched pair of quotes, thus an escaped quote
                        workArea.append(srcChars, pos, quoteLen);
                        pos += quoteLen * 2;
                        trimStart = workArea.size();
                        continue;
                    }

                    // end of quoting
                    quoting = false;
                    pos += quoteLen;
                    continue;
                }

                // copy regular character from inside quotes
                workArea.append(srcChars[pos++]);
                trimStart = workArea.size();

            } else {
                // Not in quoting mode

                // check for delimiter, and thus end of token
                final int delimLen = getDelimiterMatcher().isMatch(srcChars, pos, start, len);
                if (delimLen > 0) {
                    // return condition when end of token found
                    addToken(tokenList, workArea.substring(0, trimStart));
                    return pos + delimLen;
                }

                // check for quote, and thus back into quoting mode
                if (quoteLen > 0 && isQuote(srcChars, pos, len, quoteStart, quoteLen)) {
                    quoting = true;
                    pos += quoteLen;
                    continue;
                }

                // check for ignored (outside quotes), and ignore
                final int ignoredLen = getIgnoredMatcher().isMatch(srcChars, pos, start, len);
                if (ignoredLen > 0) {
                    pos += ignoredLen;
                    continue;
                }

                // check for trimmed character
                // don't yet know if its at the end, so copy to workArea
                // use trimStart to keep track of trim at the end
                final int trimmedLen = getTrimmerMatcher().isMatch(srcChars, pos, start, len);
                if (trimmedLen > 0) {
                    workArea.append(srcChars, pos, trimmedLen);
                    pos += trimmedLen;
                    continue;
                }

                // copy regular character from outside quotes
                workArea.append(srcChars[pos++]);
                trimStart = workArea.size();
            }
        }

        // return condition when end of string found
        addToken(tokenList, workArea.substring(0, trimStart));
        return -1;
    }

    /**
     * Checks if the characters at the index specified match the quote
     * already matched in readNextToken().
     *
     * @param srcChars  the character array being tokenized
     * @param pos  the position to check for a quote
     * @param len  the length of the character array being tokenized
     * @param quoteStart  the start position of the matched quote, 0 if no quoting
     * @param quoteLen  the length of the matched quote, 0 if no quoting
     * @return true if a quote is matched
     */
    private boolean isQuote(final char[] srcChars, final int pos, final int len, final int quoteStart, final int quoteLen) {
        for (int i = 0; i < quoteLen; i++) {
            if (pos + i >= len || srcChars[pos + i] != srcChars[quoteStart + i]) {
                return false;
            }
        }
        return true;
    }

    // Delimiter
    //-----------------------------------------------------------------------
    /**
     * Gets the field delimiter matcher.
     *
     * @return the delimiter matcher in use
     */
    public StrMatcher getDelimiterMatcher() {
        return this.delimMatcher;
    }

    /**
     * Sets the field delimiter matcher.
     * <p>
     * The delimitier is used to separate one token from another.
     *
     * @param delim  the delimiter matcher to use
     * @return this, to enable chaining
     */
    public StrTokenizer setDelimiterMatcher(final StrMatcher delim) {
        if (delim == null) {
            this.delimMatcher = StrMatcher.noneMatcher();
        } else {
            this.delimMatcher = delim;
        }
        return this;
    }

    /**
     * Sets the field delimiter character.
     *
     * @param delim  the delimiter character to use
     * @return this, to enable chaining
     */
    public StrTokenizer setDelimiterChar(final char delim) {
        return setDelimiterMatcher(StrMatcher.charMatcher(delim));
    }

    /**
     * Sets the field delimiter string.
     *
     * @param delim  the delimiter string to use
     * @return this, to enable chaining
     */
    public StrTokenizer setDelimiterString(final String delim) {
        return setDelimiterMatcher(StrMatcher.stringMatcher(delim));
    }

    // Quote
    //-----------------------------------------------------------------------
    /**
     * Gets the quote matcher currently in use.
     * <p>
     * The quote character is used to wrap data between the tokens.
     * This enables delimiters to be entered as data.
     * The default value is '"' (double quote).
     *
     * @return the quote matcher in use
     */
    public StrMatcher getQuoteMatcher() {
        return quoteMatcher;
    }

    /**
     * Set the quote matcher to use.
     * <p>
     * The quote character is used to wrap data between the tokens.
     * This enables delimiters to be entered as data.
     *
     * @param quote  the quote matcher to use, null ignored
     * @return this, to enable chaining
     */
    public StrTokenizer setQuoteMatcher(final StrMatcher quote) {
        if (quote != null) {
            this.quoteMatcher = quote;
        }
        return this;
    }

    /**
     * Sets the quote character to use.
     * <p>
     * The quote character is used to wrap data between the tokens.
     * This enables delimiters to be entered as data.
     *
     * @param quote  the quote character to use
     * @return this, to enable chaining
     */
    public StrTokenizer setQuoteChar(final char quote) {
        return setQuoteMatcher(StrMatcher.charMatcher(quote));
    }

    // Ignored
    //-----------------------------------------------------------------------
    /**
     * Gets the ignored character matcher.
     * <p>
     * These characters are ignored when parsing the String, unless they are
     * within a quoted region.
     * The default value is not to ignore anything.
     *
     * @return the ignored matcher in use
     */
    public StrMatcher getIgnoredMatcher() {
        return ignoredMatcher;
    }

    /**
     * Set the matcher for characters to ignore.
     * <p>
     * These characters are ignored when parsing the String, unless they are
     * within a quoted region.
     *
     * @param ignored  the ignored matcher to use, null ignored
     * @return this, to enable chaining
     */
    public StrTokenizer setIgnoredMatcher(final StrMatcher ignored) {
        if (ignored != null) {
            this.ignoredMatcher = ignored;
        }
        return this;
    }

    /**
     * Set the character to ignore.
     * <p>
     * This character is ignored when parsing the String, unless it is
     * within a quoted region.
     *
     * @param ignored  the ignored character to use
     * @return this, to enable chaining
     */
    public StrTokenizer setIgnoredChar(final char ignored) {
        return setIgnoredMatcher(StrMatcher.charMatcher(ignored));
    }

    // Trimmer
    //-----------------------------------------------------------------------
    /**
     * Gets the trimmer character matcher.
     * <p>
     * These characters are trimmed off on each side of the delimiter
     * until the token or quote is found.
     * The default value is not to trim anything.
     *
     * @return the trimmer matcher in use
     */
    public StrMatcher getTrimmerMatcher() {
        return trimmerMatcher;
    }

    /**
     * Sets the matcher for characters to trim.
     * <p>
     * These characters are trimmed off on each side of the delimiter
     * until the token or quote is found.
     *
     * @param trimmer  the trimmer matcher to use, null ignored
     * @return this, to enable chaining
     */
    public StrTokenizer setTrimmerMatcher(final StrMatcher trimmer) {
        if (trimmer != null) {
            this.trimmerMatcher = trimmer;
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets whether the tokenizer currently returns empty tokens as null.
     * The default for this property is false.
     *
     * @return true if empty tokens are returned as null
     */
    public boolean isEmptyTokenAsNull() {
        return this.emptyAsNull;
    }

    /**
     * Sets whether the tokenizer should return empty tokens as null.
     * The default for this property is false.
     *
     * @param emptyAsNull  whether empty tokens are returned as null
     * @return this, to enable chaining
     */
    public StrTokenizer setEmptyTokenAsNull(final boolean emptyAsNull) {
        this.emptyAsNull = emptyAsNull;
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets whether the tokenizer currently ignores empty tokens.
     * The default for this property is true.
     *
     * @return true if empty tokens are not returned
     */
    public boolean isIgnoreEmptyTokens() {
        return ignoreEmptyTokens;
    }

    /**
     * Sets whether the tokenizer should ignore and not return empty tokens.
     * The default for this property is true.
     *
     * @param ignoreEmptyTokens  whether empty tokens are not returned
     * @return this, to enable chaining
     */
    public StrTokenizer setIgnoreEmptyTokens(final boolean ignoreEmptyTokens) {
        this.ignoreEmptyTokens = ignoreEmptyTokens;
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the String content that the tokenizer is parsing.
     *
     * @return the string content being parsed
     */
    public String getContent() {
        if (chars == null) {
            return null;
        }
        return new String(chars);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a new instance of this Tokenizer. The new instance is reset so
     * that it will be at the start of the token list.
     * If a {@link CloneNotSupportedException} is caught, return <code>null</code>.
     *
     * @return a new instance of this Tokenizer which has been reset.
     */
    @Override
    public Object clone() {
        try {
            return cloneReset();
        } catch (final CloneNotSupportedException ex) {
            return null;
        }
    }

    /**
     * Creates a new instance of this Tokenizer. The new instance is reset so that
     * it will be at the start of the token list.
     *
     * @return a new instance of this Tokenizer which has been reset.
     * @throws CloneNotSupportedException if there is a problem cloning
     */
    Object cloneReset() throws CloneNotSupportedException {
        // this method exists to enable 100% test coverage
        final StrTokenizer cloned = (StrTokenizer) super.clone();
        if (cloned.chars != null) {
            cloned.chars = cloned.chars.clone();
        }
        cloned.reset();
        return cloned;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the String content that the tokenizer is parsing.
     *
     * @return the string content being parsed
     */
    @Override
    public String toString() {
        if (tokens == null) {
            return "StrTokenizer[not tokenized yet]";
        }
        return "StrTokenizer" + getTokenList();
    }

}
