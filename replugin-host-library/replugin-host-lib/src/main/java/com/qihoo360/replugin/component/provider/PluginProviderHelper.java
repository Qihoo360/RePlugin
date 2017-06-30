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

import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.Factory;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 和安装、加载、缓存插件Provider有关的帮助类。
 *
 * @author RePlugin Team
 */
public class PluginProviderHelper {
    private static final String TAG = "PluginProviderHelper";
    private static final String SCHEME_AND_SSP = "content://";

    private final String mAuthority;

    PluginProviderHelper(String authority) {
        mAuthority = authority;
    }

    Map<String, ContentProvider> mProviderAuthorityMap = new HashMap<>();

    // 将从系统传过来的URI转化成插件里的URI。例如：
    // Before: content://com.qihoo360.mobilesafe.PluginTransferP/contacts/com.qihoo360.contacts.abc/people
    // After : content://                                                 com.qihoo360.contacts.abc/people （从contacts插件中解析并寻找）
    public PluginUri toPluginUri(Uri uri) {
        if (LogDebug.LOG) {
            Log.i(TAG, "toPluginUri(): Start... Uri=" + uri);
        }
        // Authority正确
        if (!TextUtils.equals(uri.getAuthority(), mAuthority)) {
            if (LogDebug.LOG) {
                Log.e(TAG, "toPluginUri(): Authority error! auth=" + uri.getAuthority());
            }
            return null;
        }
        // 至少两个元素（排除掉authority）
        List<String> fs = uri.getPathSegments();
        if (fs.size() < 2) {
            if (LogDebug.LOG) {
                Log.e(TAG, "toPluginUri(): Less than 2 fragments, size=" + fs.size());
            }
            return null;
        }

        // 获取插件名（第一个元素）
        String pn = fs.get(0);

        // 看这个Plugin是否可以被打开
        if (!RePlugin.isPluginInstalled(pn)) {
            if (LogDebug.LOG) {
                Log.e(TAG, "toPluginUri(): Plugin not exists! pn=" + pn);
            }
            return null;
        }

        // 剔除Uri中开头的内容
        String ut = uri.toString();
        String tut = removeHostAuthorityAndInfo(ut, pn);

        PluginUri pu = new PluginUri();
        pu.plugin = pn;
        pu.transferredUri = Uri.parse(tut);
        if (LogDebug.LOG) {
            Log.i(TAG, "toPluginUri(): End! t-uri=" + pu);
        }
        return pu;
    }

    public ContentProvider getProvider(PluginUri pu) {
        if (LogDebug.LOG) {
            Log.i(TAG, "getProvider(): Start... pu=" + pu);
        }

        String auth = pu.transferredUri.getAuthority();

        // 已有缓存？直接返回！
        ContentProvider cp = mProviderAuthorityMap.get(auth);
        if (cp != null) {
            if (LogDebug.LOG) {
                Log.i(TAG, "getProvider(): Exists! Return now. cp=" + cp);
            }
            return cp;
        }

        // 开始构建插件里的ContentProvider对象
        cp = installProvider(pu, auth);
        if (cp == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "getProvider(): Install fail!");
            }
            return null;
        }

        // 加入列表。下次直接读缓存
        mProviderAuthorityMap.put(auth, cp);

        if (LogDebug.LOG) {
            Log.i(TAG, "getProvider(): Okay! pu=" + pu + "; cp=" + cp);
        }
        return cp;
    }

    private String removeHostAuthorityAndInfo(String uri, String plugin) {
        // content:// --- com.qihoo360.mobilesafe.PluginTransferP --- /(1) --- shakeoff --- /(1) ---
        int startsWith = SCHEME_AND_SSP.length() + mAuthority.length() + 1 + plugin.length() + 1;
        return SCHEME_AND_SSP + uri.substring(startsWith, uri.length());
    }

    private ContentProvider installProvider(PluginUri pu, String auth) {
        // 开始尝试获取插件的ProviderInfo
        ComponentList col = Factory.queryPluginComponentList(pu.plugin);
        if (col == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): Fetch Component List Error! auth=" + auth);
            }
            return null;
        }
        ProviderInfo pi = col.getProviderByAuthority(auth);
        if (pi == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): Not register! auth=" + auth);
            }
            return null;
        }

        // 通过ProviderInfo创建ContentProvider对象
        Context plgc = Factory.queryPluginContext(pu.plugin);
        if (plgc == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): Fetch Context Error! auth=" + auth);
            }
            return null;
        }
        ClassLoader cl = plgc.getClassLoader();
        if (cl == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): ClassLoader is Null!");
            }
            return null;
        }
        ContentProvider cp;
        try {
            cp = (ContentProvider) cl.loadClass(pi.name).newInstance();
        } catch (Throwable e) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): New instance fail!", e);
            }
            return null;
        }

        // 调用attachInfo方法（内部会调用onCreate）
        try {
            cp.attachInfo(plgc, pi);
        } catch (Throwable e) {
            // 有两种可能：
            // 1、第三方ROM修改了ContentProvider.attachInfo的实现
            // 2、开发者自己覆写了attachInfo方法，其中有Bug
            // 故暂时先Try-Catch，这样若插件的Provider没有使用Context对象，则也不会出现问题
            if (LogDebug.LOG) {
                Log.e(TAG, "installProvider(): Attach info fail!", e);
            }
            return null;
        }
        return cp;
    }

    static class PluginUri {
        Uri transferredUri;
        String plugin;

        @Override
        public String toString() {
            return transferredUri + " [" + plugin + "]";
        }
    }
}
