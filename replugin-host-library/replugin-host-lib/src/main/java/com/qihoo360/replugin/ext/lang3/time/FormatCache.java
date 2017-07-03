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
package com.qihoo360.replugin.ext.lang3.time;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>FormatCache is a cache and factory for {@link Format}s.</p>
 *
 * @since 3.0
 */
// TODO: Before making public move from getDateTimeInstance(Integer,...) to int; or some other approach.
abstract class FormatCache<F extends Format> {

    /**
     * No date or no time.  Used in same parameters as DateFormat.SHORT or DateFormat.LONG
     */
    static final int NONE= -1;

    private final ConcurrentMap<MultipartKey, F> cInstanceCache
        = new ConcurrentHashMap<>(7);

    private static final ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache
        = new ConcurrentHashMap<>(7);

    /**
     * <p>Gets a formatter instance using the default pattern in the
     * default timezone and locale.</p>
     *
     * @return a date/time formatter
     */
    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * <p>Gets a formatter instance using the specified pattern, time zone
     * and locale.</p>
     *
     * @param pattern  {@link SimpleDateFormat} compatible
     *  pattern, non-null
     * @param timeZone  the time zone, null means use the default TimeZone
     * @param locale  the locale, null means use the default Locale
     * @return a pattern based date/time formatter
     * @throws IllegalArgumentException if pattern is invalid
     *  or <code>null</code>
     */
    public F getInstance(final String pattern, TimeZone timeZone, Locale locale) {
        Validate.notNull(pattern, "pattern must not be null");
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final MultipartKey key = new MultipartKey(pattern, timeZone, locale);
        F format = cInstanceCache.get(key);
        if (format == null) {
            format = createInstance(pattern, timeZone, locale);
            final F previousValue= cInstanceCache.putIfAbsent(key, format);
            if (previousValue != null) {
                // another thread snuck in and did the same work
                // we should return the instance that is in ConcurrentMap
                format= previousValue;
            }
        }
        return format;
    }

    /**
     * <p>Create a format instance using the specified pattern, time zone
     * and locale.</p>
     *
     * @param pattern  {@link SimpleDateFormat} compatible pattern, this will not be null.
     * @param timeZone  time zone, this will not be null.
     * @param locale  locale, this will not be null.
     * @return a pattern based date/time formatter
     * @throws IllegalArgumentException if pattern is invalid
     *  or <code>null</code>
     */
    protected abstract F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * <p>Gets a date/time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle  date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle  time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param timeZone  optional time zone, overrides time zone of
     *  formatted date, null means use default Locale
     * @param locale  optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *  pattern defined
     */
    // This must remain private, see LANG-884
    private F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    /**
     * <p>Gets a date/time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle  date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle  time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *  formatted date, null means use default Locale
     * @param locale  optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>Gets a date formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle  date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *  formatted date, null means use default Locale
     * @param locale  optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateInstance(final int dateStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(Integer.valueOf(dateStyle), null, timeZone, locale);
    }

    /**
     * <p>Gets a time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param timeStyle  time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *  formatted date, null means use default Locale
     * @param locale  optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getTimeInstance(final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(null, Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>Gets a date/time format for the specified styles and locale.</p>
     *
     * @param dateStyle  date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle  time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param locale  The non-null locale of the desired format
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from test code; do not make public or protected
    static String getPatternForStyle(final Integer dateStyle, final Integer timeStyle, final Locale locale) {
        final MultipartKey key = new MultipartKey(dateStyle, timeStyle, locale);

        String pattern = cDateTimeInstanceCache.get(key);
        if (pattern == null) {
            try {
                DateFormat formatter;
                if (dateStyle == null) {
                    formatter = DateFormat.getTimeInstance(timeStyle.intValue(), locale);
                } else if (timeStyle == null) {
                    formatter = DateFormat.getDateInstance(dateStyle.intValue(), locale);
                } else {
                    formatter = DateFormat.getDateTimeInstance(dateStyle.intValue(), timeStyle.intValue(), locale);
                }
                pattern = ((SimpleDateFormat)formatter).toPattern();
                final String previous = cDateTimeInstanceCache.putIfAbsent(key, pattern);
                if (previous != null) {
                    // even though it doesn't matter if another thread put the pattern
                    // it's still good practice to return the String instance that is
                    // actually in the ConcurrentMap
                    pattern= previous;
                }
            } catch (final ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    // ----------------------------------------------------------------------
    /**
     * <p>Helper class to hold multi-part Map keys</p>
     */
    private static class MultipartKey {
        private final Object[] keys;
        private int hashCode;

        /**
         * Constructs an instance of <code>MultipartKey</code> to hold the specified objects.
         * @param keys the set of objects that make up the key.  Each key may be null.
         */
        MultipartKey(final Object... keys) {
            this.keys = keys;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            // Eliminate the usual boilerplate because
            // this inner static class is only used in a generic ConcurrentHashMap
            // which will not compare against other Object types
            return Arrays.equals(keys, ((MultipartKey)obj).keys);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            if(hashCode==0) {
                int rc= 0;
                for(final Object key : keys) {
                    if(key!=null) {
                        rc= rc*7 + key.hashCode();
                    }
                }
                hashCode= rc;
            }
            return hashCode;
        }
    }

}
