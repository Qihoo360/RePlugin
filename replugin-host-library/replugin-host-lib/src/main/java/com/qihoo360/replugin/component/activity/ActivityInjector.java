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

package com.qihoo360.replugin.component.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.qihoo360.loader2.MP;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * 根据需要来Inject一些Activity的特性，使其更像一个App中的Activity
 *
 * @author RePlugin Team
 */

public class ActivityInjector {

    public static final String TAG = "activity-injector";

    /**
     * 填充一些必要的东西到Activity中
     *
     * @param activity     Activity对象
     * @param plugin       插件名
     * @param realActivity 真实的（非坑位的）Activity名字
     * @return 是否Inject成功
     */
    public static boolean inject(Activity activity, String plugin, String realActivity) {
        // 根据传进的参数来获取ActivityInfo
        PluginInfo pi = MP.getPlugin(plugin, false);
        if (pi == null) {
            return false;
        }
        ComponentList cl = RePlugin.fetchComponentList(plugin);
        if (cl == null) {
            return false;
        }

        ActivityInfo ai = cl.getActivity(realActivity);
        return ai != null && inject(activity, ai, pi.getFrameworkVersion());
    }

    private static boolean inject(Activity activity, ActivityInfo ai, int frameworkVer) {
        // 可根据插件Activity的描述（android:label、android:icon）来设置Task在“最近应用程序”中的显示
        // 注意：框架版本需 >= 4，否则仍沿用Application的Label和Icon
        if (frameworkVer >= 4) {
            injectTaskDescription(activity, ai);
        }
        return true;
    }

    /**
     * 可根据插件Activity的描述（android:label、android:icon）来设置Task在“最近应用程序”中的显示 <p>
     * 注意：Android 4.x及以下暂不支持 <p>
     * Author: Jiongxuan Zhang
     */
    private static void injectTaskDescription(Activity activity, ActivityInfo ai) {
        // Android 4.x及以下暂不支持
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (activity == null || ai == null) {
            return;
        }

        if (LOG) {
            LogDebug.d(TAG, "activity = " + activity);
            LogDebug.d(TAG, "ai = " + ai);
        }

        // 获取 activity label
        String label = getLabel(activity, ai);
        // 如果获取 label 失败（可能性极小），则不修改 TaskDescription
        if (TextUtils.isEmpty(label)) {
            return;
        }

        // 获取 ICON
        Bitmap bitmap = getIcon(activity, ai);

        // FIXME color的透明度需要在Theme中的colorPrimary中获取，先不实现
        ActivityManager.TaskDescription td;
        if (bitmap != null) {
            td = new ActivityManager.TaskDescription(label, bitmap);
        } else {
            td = new ActivityManager.TaskDescription(label);
        }

        if (LOG) {
            LogDebug.d(TAG, "td = " + td);
        }

        activity.setTaskDescription(td);
    }

    /**
     * 获取 activity 的 label 属性
     */
    private static String getLabel(Activity activity, ActivityInfo ai) {
        String label;
        Resources res = activity.getResources();

        // 获取 Activity label（如有）
        label = getLabelById(res, ai.labelRes);

        // 获取插件 Application Label（如有）
        if (TextUtils.isEmpty(label)) {
            label = getLabelById(res, ai.applicationInfo.labelRes);
        }

        // 获取宿主 App label
        if (TextUtils.isEmpty(label)) {
            Context appContext = RePluginInternal.getAppContext();
            Resources appResource = appContext.getResources();
            ApplicationInfo appInfo = appContext.getApplicationInfo();
            label = getLabelById(appResource, appInfo.labelRes);
        }

        if (LOG) {
            LogDebug.d(TAG, "label = " + label);
        }
        return label;
    }

    private static String getLabelById(Resources res, int id) {
        if (id == 0) {
            return null;
        }
        try {
            return res.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取 activity 的 icon 属性
     */
    private static Bitmap getIcon(Activity activity, ActivityInfo ai) {
        Drawable iconDrawable;
        Resources res = activity.getResources();

        // 获取 Activity icon
        iconDrawable = getIconById(res, ai.icon);

        // 获取插件 Application Icon
        if (iconDrawable == null) {
            iconDrawable = getIconById(res, ai.applicationInfo.icon);
        }

        // 获取 App(Host) Icon
        if (iconDrawable == null) {
            Context appContext = RePluginInternal.getAppContext();
            Resources appResource = appContext.getResources();
            ApplicationInfo appInfo = appContext.getApplicationInfo();
            iconDrawable = getIconById(appResource, appInfo.icon);
        }

        Bitmap bitmap = null;
        if (iconDrawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
        }

        if (LOG) {
            LogDebug.d(TAG, "bitmap = " + bitmap);
        }
        return bitmap;
    }

    private static Drawable getIconById(Resources res, int id) {
        if (id == 0) {
            return null;
        }
        try {
            return res.getDrawable(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
