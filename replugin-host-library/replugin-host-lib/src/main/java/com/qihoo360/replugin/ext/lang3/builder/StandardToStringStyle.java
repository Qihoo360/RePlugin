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
package com.qihoo360.replugin.ext.lang3.builder;

/**
 * <p>Works with {@link ToStringBuilder} to create a <code>toString</code>.</p>
 *
 * <p>This class is intended to be used as a singleton.
 * There is no need to instantiate a new style each time.
 * Simply instantiate the class once, customize the values as required, and
 * store the result in a public static final variable for the rest of the
 * program to access.</p>
 *
 * @since 1.0
 */
public class StandardToStringStyle extends ToStringStyle {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor.</p>
     */
    public StandardToStringStyle() {
        super();
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to use the class name.</p>
     *
     * @return the current useClassName flag
     */
    @Override
    public boolean isUseClassName() { // NOPMD as this is implementing the abstract class
        return super.isUseClassName();
    }

    /**
     * <p>Sets whether to use the class name.</p>
     *
     * @param useClassName  the new useClassName flag
     */
    @Override
    public void setUseClassName(final boolean useClassName) { // NOPMD as this is implementing the abstract class
        super.setUseClassName(useClassName);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to output short or long class names.</p>
     *
     * @return the current useShortClassName flag
     * @since 2.0
     */
    @Override
    public boolean isUseShortClassName() { // NOPMD as this is implementing the abstract class
        return super.isUseShortClassName();
    }

    /**
     * <p>Sets whether to output short or long class names.</p>
     *
     * @param useShortClassName  the new useShortClassName flag
     * @since 2.0
     */
    @Override
    public void setUseShortClassName(final boolean useShortClassName) { // NOPMD as this is implementing the abstract class
        super.setUseShortClassName(useShortClassName);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to use the identity hash code.</p>
     * @return the current useIdentityHashCode flag
     */
    @Override
    public boolean isUseIdentityHashCode() { // NOPMD as this is implementing the abstract class
        return super.isUseIdentityHashCode();
    }

    /**
     * <p>Sets whether to use the identity hash code.</p>
     *
     * @param useIdentityHashCode  the new useIdentityHashCode flag
     */
    @Override
    public void setUseIdentityHashCode(final boolean useIdentityHashCode) { // NOPMD as this is implementing the abstract class
        super.setUseIdentityHashCode(useIdentityHashCode);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to use the field names passed in.</p>
     *
     * @return the current useFieldNames flag
     */
    @Override
    public boolean isUseFieldNames() { // NOPMD as this is implementing the abstract class
        return super.isUseFieldNames();
    }

    /**
     * <p>Sets whether to use the field names passed in.</p>
     *
     * @param useFieldNames  the new useFieldNames flag
     */
    @Override
    public void setUseFieldNames(final boolean useFieldNames) { // NOPMD as this is implementing the abstract class
        super.setUseFieldNames(useFieldNames);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to use full detail when the caller doesn't
     * specify.</p>
     *
     * @return the current defaultFullDetail flag
     */
    @Override
    public boolean isDefaultFullDetail() { // NOPMD as this is implementing the abstract class
        return super.isDefaultFullDetail();
    }

    /**
     * <p>Sets whether to use full detail when the caller doesn't
     * specify.</p>
     *
     * @param defaultFullDetail  the new defaultFullDetail flag
     */
    @Override
    public void setDefaultFullDetail(final boolean defaultFullDetail) { // NOPMD as this is implementing the abstract class
        super.setDefaultFullDetail(defaultFullDetail);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether to output array content detail.</p>
     *
     * @return the current array content detail setting
     */
    @Override
    public boolean isArrayContentDetail() { // NOPMD as this is implementing the abstract class
        return super.isArrayContentDetail();
    }

    /**
     * <p>Sets whether to output array content detail.</p>
     *
     * @param arrayContentDetail  the new arrayContentDetail flag
     */
    @Override
    public void setArrayContentDetail(final boolean arrayContentDetail) { // NOPMD as this is implementing the abstract class
        super.setArrayContentDetail(arrayContentDetail);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the array start text.</p>
     *
     * @return the current array start text
     */
    @Override
    public String getArrayStart() { // NOPMD as this is implementing the abstract class
        return super.getArrayStart();
    }

    /**
     * <p>Sets the array start text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param arrayStart  the new array start text
     */
    @Override
    public void setArrayStart(final String arrayStart) { // NOPMD as this is implementing the abstract class
        super.setArrayStart(arrayStart);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the array end text.</p>
     *
     * @return the current array end text
     */
    @Override
    public String getArrayEnd() { // NOPMD as this is implementing the abstract class
        return super.getArrayEnd();
    }

    /**
     * <p>Sets the array end text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param arrayEnd  the new array end text
     */
    @Override
    public void setArrayEnd(final String arrayEnd) { // NOPMD as this is implementing the abstract class
        super.setArrayEnd(arrayEnd);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the array separator text.</p>
     *
     * @return the current array separator text
     */
    @Override
    public String getArraySeparator() { // NOPMD as this is implementing the abstract class
        return super.getArraySeparator();
    }

    /**
     * <p>Sets the array separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param arraySeparator  the new array separator text
     */
    @Override
    public void setArraySeparator(final String arraySeparator) { // NOPMD as this is implementing the abstract class
        super.setArraySeparator(arraySeparator);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the content start text.</p>
     *
     * @return the current content start text
     */
    @Override
    public String getContentStart() { // NOPMD as this is implementing the abstract class
        return super.getContentStart();
    }

    /**
     * <p>Sets the content start text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param contentStart  the new content start text
     */
    @Override
    public void setContentStart(final String contentStart) { // NOPMD as this is implementing the abstract class
        super.setContentStart(contentStart);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the content end text.</p>
     *
     * @return the current content end text
     */
    @Override
    public String getContentEnd() { // NOPMD as this is implementing the abstract class
        return super.getContentEnd();
    }

    /**
     * <p>Sets the content end text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param contentEnd  the new content end text
     */
    @Override
    public void setContentEnd(final String contentEnd) { // NOPMD as this is implementing the abstract class
        super.setContentEnd(contentEnd);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the field name value separator text.</p>
     *
     * @return the current field name value separator text
     */
    @Override
    public String getFieldNameValueSeparator() { // NOPMD as this is implementing the abstract class
        return super.getFieldNameValueSeparator();
    }

    /**
     * <p>Sets the field name value separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param fieldNameValueSeparator  the new field name value separator text
     */
    @Override
    public void setFieldNameValueSeparator(final String fieldNameValueSeparator) { // NOPMD as this is implementing the abstract class
        super.setFieldNameValueSeparator(fieldNameValueSeparator);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the field separator text.</p>
     *
     * @return the current field separator text
     */
    @Override
    public String getFieldSeparator() { // NOPMD as this is implementing the abstract class
        return super.getFieldSeparator();
    }

    /**
     * <p>Sets the field separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param fieldSeparator  the new field separator text
     */
    @Override
    public void setFieldSeparator(final String fieldSeparator) { // NOPMD as this is implementing the abstract class
        super.setFieldSeparator(fieldSeparator);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether the field separator should be added at the start
     * of each buffer.</p>
     *
     * @return the fieldSeparatorAtStart flag
     * @since 2.0
     */
    @Override
    public boolean isFieldSeparatorAtStart() { // NOPMD as this is implementing the abstract class
        return super.isFieldSeparatorAtStart();
    }

    /**
     * <p>Sets whether the field separator should be added at the start
     * of each buffer.</p>
     *
     * @param fieldSeparatorAtStart  the fieldSeparatorAtStart flag
     * @since 2.0
     */
    @Override
    public void setFieldSeparatorAtStart(final boolean fieldSeparatorAtStart) { // NOPMD as this is implementing the abstract class
        super.setFieldSeparatorAtStart(fieldSeparatorAtStart);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets whether the field separator should be added at the end
     * of each buffer.</p>
     *
     * @return fieldSeparatorAtEnd flag
     * @since 2.0
     */
    @Override
    public boolean isFieldSeparatorAtEnd() { // NOPMD as this is implementing the abstract class
        return super.isFieldSeparatorAtEnd();
    }

    /**
     * <p>Sets whether the field separator should be added at the end
     * of each buffer.</p>
     *
     * @param fieldSeparatorAtEnd  the fieldSeparatorAtEnd flag
     * @since 2.0
     */
    @Override
    public void setFieldSeparatorAtEnd(final boolean fieldSeparatorAtEnd) { // NOPMD as this is implementing the abstract class
        super.setFieldSeparatorAtEnd(fieldSeparatorAtEnd);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the text to output when <code>null</code> found.</p>
     *
     * @return the current text to output when <code>null</code> found
     */
    @Override
    public String getNullText() { // NOPMD as this is implementing the abstract class
        return super.getNullText();
    }

    /**
     * <p>Sets the text to output when <code>null</code> found.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param nullText  the new text to output when <code>null</code> found
     */
    @Override
    public void setNullText(final String nullText) { // NOPMD as this is implementing the abstract class
        super.setNullText(nullText);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the text to output when a <code>Collection</code>,
     * <code>Map</code> or <code>Array</code> size is output.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * @return the current start of size text
     */
    @Override
    public String getSizeStartText() { // NOPMD as this is implementing the abstract class
        return super.getSizeStartText();
    }

    /**
     * <p>Sets the start text to output when a <code>Collection</code>,
     * <code>Map</code> or <code>Array</code> size is output.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param sizeStartText  the new start of size text
     */
    @Override
    public void setSizeStartText(final String sizeStartText) { // NOPMD as this is implementing the abstract class
        super.setSizeStartText(sizeStartText);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the end text to output when a <code>Collection</code>,
     * <code>Map</code> or <code>Array</code> size is output.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * @return the current end of size text
     */
    @Override
    public String getSizeEndText() { // NOPMD as this is implementing the abstract class
        return super.getSizeEndText();
    }

    /**
     * <p>Sets the end text to output when a <code>Collection</code>,
     * <code>Map</code> or <code>Array</code> size is output.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted
     * to an empty String.</p>
     *
     * @param sizeEndText  the new end of size text
     */
    @Override
    public void setSizeEndText(final String sizeEndText) { // NOPMD as this is implementing the abstract class
        super.setSizeEndText(sizeEndText);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <P>This is output before the size value.</p>
     *
     * @return the current start of summary text
     */
    @Override
    public String getSummaryObjectStartText() { // NOPMD as this is implementing the abstract class
        return super.getSummaryObjectStartText();
    }

    /**
     * <p>Sets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param summaryObjectStartText  the new start of summary text
     */
    @Override
    public void setSummaryObjectStartText(final String summaryObjectStartText) { // NOPMD as this is implementing the abstract class
        super.setSummaryObjectStartText(summaryObjectStartText);
    }

    //---------------------------------------------------------------------

    /**
     * <p>Gets the end text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * @return the current end of summary text
     */
    @Override
    public String getSummaryObjectEndText() { // NOPMD as this is implementing the abstract class
        return super.getSummaryObjectEndText();
    }

    /**
     * <p>Sets the end text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param summaryObjectEndText  the new end of summary text
     */
    @Override
    public void setSummaryObjectEndText(final String summaryObjectEndText) { // NOPMD as this is implementing the abstract class
        super.setSummaryObjectEndText(summaryObjectEndText);
    }

    //---------------------------------------------------------------------

}
