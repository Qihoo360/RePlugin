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

package com.qihoo360.replugin.component.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import com.qihoo360.replugin.base.IPC;

/**
 * 所有插件的Provider均由此处分发
 *
 * @author RePlugin Team
 */
public abstract class PluginPitProviderBase extends ContentProvider {

    PluginProviderHelper mHelper;

    public static final String AUTHORITY_PREFIX = IPC.getPackageName() + ".Plugin.NP.";

    protected PluginPitProviderBase(String authority) {
        mHelper = new PluginProviderHelper(authority);
    }

    @Override
    public boolean onCreate() {
        // Nothing，不需要做任何事情
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return null;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return null;
        }
        return cp.query(pu.transferredUri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    @TargetApi(16)
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder,
                        CancellationSignal cancellationSignal) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return null;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return null;
        }
        return cp.query(pu.transferredUri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return null;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return null;
        }
        return cp.getType(pu.transferredUri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return null;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return null;
        }
        return cp.insert(pu.transferredUri, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return -1;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return -1;
        }
        return cp.bulkInsert(pu.transferredUri, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return -1;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return -1;
        }
        return cp.delete(pu.transferredUri, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        PluginProviderHelper.PluginUri pu = mHelper.toPluginUri(uri);
        if (pu == null) {
            return -1;
        }
        ContentProvider cp = mHelper.getProvider(pu);
        if (cp == null) {
            return -1;
        }
        return cp.update(pu.transferredUri, values, selection, selectionArgs);
    }

    @Override
    public void onLowMemory() {
        for (ContentProvider cp : mHelper.mProviderAuthorityMap.values()) {
            cp.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    @TargetApi(14)
    public void onTrimMemory(int level) {
        for (ContentProvider cp : mHelper.mProviderAuthorityMap.values()) {
            cp.onTrimMemory(level);
        }
        super.onTrimMemory(level);
    }
}
