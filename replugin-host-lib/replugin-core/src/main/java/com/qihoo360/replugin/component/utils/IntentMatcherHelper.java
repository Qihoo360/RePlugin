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

package com.qihoo360.replugin.component.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;

import com.qihoo360.i.Factory;
import com.qihoo360.mobilesafe.parser.manifest.ManifestParser;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.qihoo360.mobilesafe.parser.manifest.ManifestParser.TAG;
import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * 和Intent匹配有关的类
 *
 * @author RePlugin Team
 */

public class IntentMatcherHelper {

    /**
     * 根据 Intent 以及 plugin 匹配 Activity
     * <p>
     * 遍历plugin插件中，保存的 Activity 的 IntentFilter 数据，进行匹配，
     * 返回第一个符合条件的 ActivityInfo 对象.
     *
     * @param context Context
     * @param plugin  插件名称
     * @param intent  调用方传来的 Intent
     * @return 匹配到的 ActivityInfo 和 插件名称
     */
    public static ActivityInfo getActivityInfo(Context context, String plugin, Intent intent) {
        if (plugin == null) {
            return null;
        }

        String activity = doMatchIntent(context, intent, ManifestParser.INS.getActivityFilterMap(plugin));
        return Factory.queryActivityInfo(plugin, activity);
    }

    /**
     * 根据 Intent 匹配组件
     *
     * @param context    Context
     * @param intent     调用方传来的 Intent
     * @param filtersMap 插件中声明的所有组件和 IntentFilter
     * @return ComponentInfo
     */
    public static String doMatchIntent(Context context, Intent intent, Map<String, List<IntentFilter>> filtersMap) {
        if (filtersMap == null) {
            return null;
        }

        final String action = intent.getAction();
        final String type = intent.resolveTypeIfNeeded(context.getContentResolver());
        final Uri data = intent.getData();
        final String scheme = intent.getScheme();
        final Set<String> categories = intent.getCategories();

        for (Map.Entry<String, List<IntentFilter>> entry : filtersMap.entrySet()) {
            String pluginName = entry.getKey();
            List<IntentFilter> filters = entry.getValue();
            if (filters == null) {
                continue;
            }

            for (IntentFilter filter : filters) {
                int match = filter.match(action, type, scheme, data, categories, "ComponentList");
                if (match >= 0) {
                    if (LOG) {
                        LogDebug.d(TAG, "IntentFilter 匹配成功: " + entry.getKey());
                    }
                    return pluginName;
                } else {
                    if (LOG) {
                        String reason;
                        switch (match) {
                            case IntentFilter.NO_MATCH_ACTION:
                                reason = "action";
                                break;
                            case IntentFilter.NO_MATCH_CATEGORY:
                                reason = "category";
                                break;
                            case IntentFilter.NO_MATCH_DATA:
                                reason = "data";
                                break;
                            case IntentFilter.NO_MATCH_TYPE:
                                reason = "type";
                                break;
                            default:
                                reason = "unknown reason";
                                break;
                        }
                        LogDebug.d(TAG, "  Filter did not match: " + reason);
                    }
                }
            }
        }
        return "";
    }
}
