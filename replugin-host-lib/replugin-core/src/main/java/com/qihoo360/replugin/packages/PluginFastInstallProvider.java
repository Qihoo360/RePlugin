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

package com.qihoo360.replugin.packages;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.qihoo360.loader2.PMF;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

/**
 * 在UI进程中安装插件的Provider。有关具体说明，请参见PluginInstallProviderProxy的说明
 *
 * @author RePlugin Team
 * @see PluginFastInstallProviderProxy
 */

public class PluginFastInstallProvider extends ContentProvider {

    private static final String TAG = "PluginFastInstallPv";

    public static final String AUTHORITY = IPC.getPackageName() + ".loader.p.pip";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String SELECTION_INSTALL = "inst";

    public static final String KEY_PLUGIN_INFO = "pi";

    // 此类不是个标准的Provider（只是命令接受），不需要URI_MATCHER这么复杂的处理形式
    // private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static ContentValues makeInstallValues(PluginInfo pi) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_PLUGIN_INFO, pi.getJSON().toString());
        return cv;
    }

    @Override
    public boolean onCreate() {
        // Nothing
        return true;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "update: cv=" + values);
        }

        if (TextUtils.isEmpty(selection)) {
            return 0;
        }
        switch (selection) {
            case SELECTION_INSTALL: {
                return install(values);
            }
        }
        return 0;
    }

    private int install(ContentValues cv) {
        if (cv == null) {
            return 0;
        }

        String pit = cv.getAsString(KEY_PLUGIN_INFO);
        if (TextUtils.isEmpty(pit)) {
            return 0;
        }
        PluginInfo pi = PluginInfo.parseFromJsonText(pit);

        // 开始加载ClassLoader
        ClassLoader cl = PMF.getLocal().loadPluginClassLoader(pi);
        if (cl != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Nothing
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Nothing
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Nothing
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Nothing
        return 0;
    }
}
