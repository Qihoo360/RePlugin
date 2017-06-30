/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo360.loader2.mgr;

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * （该类仅为兼容360手机卫士的旧插件而存在，因涉及到反射而保留此类）
 *
 * @deprecated 请使用新类
 * @see com.qihoo360.replugin.component.provider.PluginProviderClient
 * @author RePlugin Team
 */
public class PluginProviderClient {

    /**
     * @deprecated
     */
    public static ContentProviderClient acquireContentProviderClient(Context c, String name) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.acquireContentProviderClient(c, name);
    }

    /**
     * @deprecated
     */
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.query(c, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * @deprecated
     */
    @TargetApi(16)
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.query(c, uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    /**
     * @deprecated
     */
    public static String getType(Context c, Uri uri) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.getType(c, uri);
    }

    /**
     * @deprecated
     */
    public static Uri insert(Context c, Uri uri, ContentValues values) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.insert(c, uri, values);
    }

    /**
     * @deprecated
     */
    public static int bulkInsert(Context c, Uri uri, ContentValues[] values) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.bulkInsert(c, uri, values);
    }

    /**
     * @deprecated
     */
    public static int delete(Context c, Uri uri, String selection, String[] selectionArgs) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.delete(c, uri, selection, selectionArgs);
    }

    /**
     * @deprecated
     */
    public static int update(Context c, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.update(c, uri, values, selection, selectionArgs);
    }

    /**
     * @deprecated
     */
    public static InputStream openInputStream(Context c, Uri uri) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.openInputStream(c, uri);
    }

    /**
     * @deprecated
     */
    public static OutputStream openOutputStream(Context c, Uri uri) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.openOutputStream(c, uri);
    }

    /**
     * @deprecated
     */
    public static OutputStream openOutputStream(Context c, Uri uri, String mode) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.openOutputStream(c, uri, mode);
    }

    /**
     * @deprecated
     */
    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.openFileDescriptor(c, uri, mode);
    }

    /**
     * @deprecated
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode, CancellationSignal cancellationSignal) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.openFileDescriptor(c, uri, mode, cancellationSignal);
    }

    /**
     * @deprecated
     */
    public static void registerContentObserver(Context c, Uri uri, boolean notifyForDescendents, ContentObserver observer) {
        com.qihoo360.replugin.component.provider.PluginProviderClient.registerContentObserver(c, uri, notifyForDescendents, observer);
    }

    /**
     * @deprecated
     */
    public static void notifyChange(Context c, Uri uri, ContentObserver observer) {
        com.qihoo360.replugin.component.provider.PluginProviderClient.notifyChange(c, uri, observer);
    }

    /**
     * @deprecated
     */
    public static void notifyChange(Context c, Uri uri, ContentObserver observer, boolean b) {
        com.qihoo360.replugin.component.provider.PluginProviderClient.notifyChange(c, uri, observer, b);
    }

    /**
     * @deprecated
     */
    public static Uri toCalledUri(Context c, Uri uri) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.toCalledUri(c, uri);
    }

    /**
     * @deprecated
     */
    public static Uri toCalledUri(Context context, String plugin, Uri uri, int process) {
        return com.qihoo360.replugin.component.provider.PluginProviderClient.toCalledUri(context, plugin, uri, process);
    }
}
