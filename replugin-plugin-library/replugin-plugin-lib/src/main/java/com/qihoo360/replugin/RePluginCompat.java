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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qihoo360.replugin.helper.LogDebug;

import static com.qihoo360.replugin.RePlugin.TAG;
import static com.qihoo360.replugin.RePlugin.fetchContext;
import static com.qihoo360.replugin.RePlugin.fetchPackageInfo;
import static com.qihoo360.replugin.RePlugin.fetchResources;

/**
 * 为了兼容旧版本RePlugin而做的类 <p>
 * 必须和host-lib中的方法“近乎完全一样”才可以，避免出现行为不一致的问题 <p>
 * <p>
 * 注意：依赖host-lib内部调用的（例如调用了Loader的逻辑等）、或者行为高度和宿主一致的，则还是应该走正常的反射流程
 *
 * @author RePlugin Team
 * @since 2.2.0
 */

class RePluginCompat {

    /**
     * @see RePlugin#fetchResourceIdByName(String, String)
     */
    static int fetchResourceIdByName(String pluginName, String resTypeAndName) {
        PackageInfo pi = fetchPackageInfo(pluginName);
        if (pi == null) {
            // 插件没有找到
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "fetchResourceIdByName: Plugin not found. pn=" + pluginName + "; resName=" + resTypeAndName);
            }
            return 0;
        }
        Resources res = fetchResources(pluginName);
        if (res == null) {
            // 不太可能出现此问题，同样为插件没有找到
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "fetchResourceIdByName: Plugin not found (fetchResources). pn=" + pluginName + "; resName=" + resTypeAndName);
            }
            return 0;
        }

        // Identifier的第一个参数想要的是：
        // [包名]:[类型名]/[资源名]。其中[类型名]/[资源名]就是 resTypeAndName 参数
        // 例如：com.qihoo360.replugin.sample.demo2:layout/from_demo1
        String idKey = pi.packageName + ":" + resTypeAndName;
        return res.getIdentifier(idKey, null, null);
    }

    /**
     * @see RePlugin#fetchViewByLayoutName(String, String, ViewGroup)
     */
    public static <T extends View> T fetchViewByLayoutName(String pluginName, String layoutName, ViewGroup root) {
        Context context = fetchContext(pluginName);
        if (context == null) {
            // 插件没有找到
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "fetchViewByLayoutName: Plugin not found. pn=" + pluginName + "; layoutName=" + layoutName);
            }
        }

        String resTypeAndName = "layout/" + layoutName;
        int id = fetchResourceIdByName(pluginName, resTypeAndName);
        if (id <= 0) {
            // 无法拿到资源，可能是资源没有找到
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "fetchViewByLayoutName: fetch failed! pn=" + pluginName + "; layoutName=" + layoutName);
            }
            return null;
        }

        // TODO 可能要考虑WebView在API 19以上的特殊性

        // 强制转换到T类型，一旦转换出错就抛出ClassCastException异常并告诉外界
        // noinspection unchecked
        return (T) LayoutInflater.from(context).inflate(id, root);
    }
}
