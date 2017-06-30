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

package com.qihoo360.replugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.qihoo360.mobilesafe.utils.pkg.PackageFilesUtil;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.InputStream;

/**
 * 插件框架对外回调接口集，其中有一套默认的实现，宿主可直接继承此类，并复写要自定义的接口，来修改插件化框架的逻辑
 * <p>
 * 注意：可以只使用默认的Callbacks，但默认实现不支持插件的下载。下载部分需要实现自己的一套逻辑
 * <p>
 * 要了解每个接口的用途，请参见PluginCallbacks接口的说明
 *
 * @author RePlugin Team
 * @see RePluginCallbacks
 */
public class DefaultRePluginCallbacks implements RePluginCallbacks {

    private final Context mContext;

    public DefaultRePluginCallbacks(Context context) {
        mContext = context;
    }

    /**
     * @see RePluginCallbacks#createClassLoader(ClassLoader, ClassLoader)
     */
    @Override
    public RePluginClassLoader createClassLoader(ClassLoader parent, ClassLoader original) {
        return new RePluginClassLoader(parent, original);
    }

    /**
     * @see RePluginCallbacks#createPluginClassLoader(String, String, String, ClassLoader)
     */
    @Override
    public PluginDexClassLoader createPluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        return new PluginDexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    /**
     * @see RePluginCallbacks#onPluginNotExistsForActivity(Context, String, Intent, int)
     */
    @Override
    public boolean onPluginNotExistsForActivity(Context context, String plugin, Intent intent, int process) {
        // Nothing
        return false;
    }

    /**
     * @see RePluginCallbacks#onLoadLargePluginForActivity(Context, String, Intent, int)
     */
    @Override
    public boolean onLoadLargePluginForActivity(Context context, String plugin, Intent intent, int process) {
        // Nothing
        return false;
    }

    /**
     * @see RePluginCallbacks#onPrepareAllocPitActivity(Intent)
     */
    @Override
    public void onPrepareAllocPitActivity(Intent intent) {
        // Nothing
    }

    /**
     * @see RePluginCallbacks#onPrepareStartPitActivity(Context, Intent, Intent)
     */
    @Override
    public void onPrepareStartPitActivity(Context context, Intent intent, Intent pittedIntent) {
        // Nothing
    }

    /**
     * @see RePluginCallbacks#onActivityDestroyed(Activity)
     */
    @Override
    public void onActivityDestroyed(Activity activity) {
        // Nothing
    }

    /**
     * @see RePluginCallbacks#onBinderReleased()
     */
    @Override
    public void onBinderReleased() {
        // Nothing
    }

    /**
     * @see RePluginCallbacks#getSharedPreferences(Context, String, int)
     */
    @Override
    public SharedPreferences getSharedPreferences(Context context, String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }

    /**
     * @see RePluginCallbacks#openLatestFile(Context, String)
     */
    @Override
    public InputStream openLatestFile(Context context, String filename) {
        return PackageFilesUtil.openLatestInputFile(context, filename);
    }

    @Override
    public ContextInjector createContextInjector() {
        return null;
    }

    @Override
    public boolean isPluginBlocked(PluginInfo pluginInfo) {
        return false;
    }

}
