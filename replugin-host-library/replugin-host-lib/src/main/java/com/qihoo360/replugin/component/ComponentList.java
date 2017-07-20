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

package com.qihoo360.replugin.component;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Pair;

import com.qihoo360.i.Factory;
import com.qihoo360.mobilesafe.parser.manifest.ManifestParser;
import com.qihoo360.replugin.component.utils.ApkCommentReader;
import com.qihoo360.replugin.component.utils.IntentMatcherHelper;
import com.qihoo360.replugin.ext.parser.ApkParser;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * 用来快速获取四大组件和Application的系统Info的List
 * <p>
 * NOTE 每个Plugin对象维护一份ComponentList，且在第一次加载PackageInfo时被生成
 *
 * @author RePlugin Team
 * @see android.content.pm.ActivityInfo
 * @see android.content.pm.ServiceInfo
 * @see android.content.pm.ProviderInfo
 * @see android.content.pm.ApplicationInfo
 */
public class ComponentList {
    /**
     * Class类名 - Activity的Map表
     */
    final HashMap<String, ActivityInfo> mActivities = new HashMap<>();

    /**
     * Class类名 - Provider的Map表
     */
    final HashMap<String, ProviderInfo> mProvidersByName = new HashMap<>();

    /**
     * Authority - Provider的Map表
     */
    final HashMap<String, ProviderInfo> mProvidersByAuthority = new HashMap<>();

    /**
     * Class类名 - Service的Map表
     */
    final HashMap<String, ServiceInfo> mServices = new HashMap<>();

    /**
     * Application对象
     */
    ApplicationInfo mApplication = null;

    /**
     * Class类名 - BroadcastReceiver的Map表
     * 注意：是的，你没有看错，系统缓存Receiver就是用的ActivityInfo
     */
    final HashMap<String, ActivityInfo> mReceivers = new HashMap<>();

    /**
     * 初始化ComponentList对象 <p>
     * 注意：仅框架内部使用
     */
    public ComponentList(PackageInfo pi, String path, PluginInfo pli) {
        if (pi.activities != null) {
            for (ActivityInfo ai : pi.activities) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "activity=" + ai.name);
                }
                ai.applicationInfo.sourceDir = path;
                // todo extract to function
                if (ai.processName == null) {
                    ai.processName = ai.applicationInfo.processName;
                }
                if (ai.processName == null) {
                    ai.processName = ai.packageName;
                }
                mActivities.put(ai.name, ai);
            }
        }
        if (pi.providers != null) {
            for (ProviderInfo ppi : pi.providers) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "provider=" + ppi.name + "; auth=" + ppi.authority);
                }
                if (ppi.processName == null) {
                    ppi.processName = ppi.applicationInfo.processName;
                }
                if (ppi.processName == null) {
                    ppi.processName = ppi.packageName;
                }
                mProvidersByName.put(ppi.name, ppi);
                mProvidersByAuthority.put(ppi.authority, ppi);
            }
        }
        if (pi.services != null) {
            for (ServiceInfo si : pi.services) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "service=" + si.name);
                }
                if (si.processName == null) {
                    si.processName = si.applicationInfo.processName;
                }
                if (si.processName == null) {
                    si.processName = si.packageName;
                }
                mServices.put(si.name, si);
            }
        }
        if (pi.receivers != null) {
            for (ActivityInfo ri : pi.receivers) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "receiver=" + ri.name);
                }
                if (ri.processName == null) {
                    ri.processName = ri.applicationInfo.processName;
                }
                if (ri.processName == null) {
                    ri.processName = ri.packageName;
                }
                mReceivers.put(ri.name, ri);
            }
        }

        // 解析 Apk 中的 AndroidManifest.xml
        String manifest = getManifestFromApk(path);

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "\n解析插件 " + pli.getName() + " : " + path + "\nAndroidManifest: \n" + manifest);
        }

        // 生成组件与 IntentFilter 的对应关系
        ManifestParser.INS.parse(pli, manifest);

        mApplication = pi.applicationInfo;

        if (mApplication.dataDir == null) {
            mApplication.dataDir = Environment.getDataDirectory() + File.separator + "data" + File.separator + mApplication.packageName;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "mApplication: " + mApplication);
        }
    }

    /**
     * 从 APK 中获取 Manifest 内容
     *
     * @param apkFile apk 文件路径
     * @return apk 中 AndroidManifest 中的内容
     */
    private static String getManifestFromApk(String apkFile) {

        // 先从 Apk comment 中解析 AndroidManifest
        String manifest = ApkCommentReader.readComment(apkFile);
        if (!TextUtils.isEmpty(manifest)) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "从 apk comment 中解析 xml:\n " + manifest);
            }
            return manifest;
        }

        // 解析失败时，再从 apk 中解析
        ApkParser parser = null;
        try {
            parser = new ApkParser(apkFile);
            if (LOG) {
                long begin = System.currentTimeMillis();
                manifest = parser.getManifestXml();
                long end = System.currentTimeMillis();
                LogDebug.d(PLUGIN_TAG, "从 apk 中解析 xml 耗时 " + (end - begin) + " 毫秒");
            } else {
                manifest = parser.getManifestXml();
            }
            return manifest;

        } catch (IOException t) {
            t.printStackTrace();
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 获取ServiceInfo对象
     */
    public ServiceInfo getService(String className) {
        return mServices.get(className);
    }

    /**
     * 获取该插件所有的ServiceInfo列表
     */
    public ServiceInfo[] getServices() {
        return mServices.values().toArray(new ServiceInfo[0]);
    }

    /**
     * 获取ServiceInfo对象
     */
    public ActivityInfo getActivity(String className) {
        return mActivities.get(className);
    }

    /**
     * 获取该插件所有的ActivityInfo列表
     */
    public ActivityInfo[] getActivities() {
        return mActivities.values().toArray(new ActivityInfo[0]);
    }

    /**
     * 获取 Receiver
     */
    public ActivityInfo getReveiver(String className) {
        return mReceivers.get(className);
    }

    /**
     * 获取该插件所有的 Receiver 列表
     */
    public ActivityInfo[] getReceivers() {
        return mReceivers.values().toArray(new ActivityInfo[0]);
    }

    /**
     * 根据 Intent 匹配 Service
     * <p>
     * 遍历 plugin 插件中，所有保存的 Service 的 IntentFilter 数据，进行匹配，
     * 返回第一个符合条件的 ServiceInfo 对象.
     *
     * @param context Context
     * @param intent  调用方传来的 Intent
     * @return 匹配到的 ServiceInfo
     */
    public Pair<ServiceInfo, String> getServiceAndPluginByIntent(Context context, Intent intent) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            Set<String> plugins = ManifestParser.INS.getPluginsByActionWhenStartService(action);
            if (plugins != null) {

                for (String plugin : plugins) {
                    // 获取 plugin 插件中，所有的 Service 和 IntentFilter 的对应关系
                    Map<String, List<IntentFilter>> filters = ManifestParser.INS.getServiceFilterMap(plugin);
                    // 找到 plugin 插件中，IntentFilter 匹配成功的 Service
                    String service = IntentMatcherHelper.doMatchIntent(context, intent, filters);
                    ServiceInfo info = Factory.queryServiceInfo(plugin, service);
                    if (info != null) {
                        return new Pair<>(info, plugin);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 通过类名获取ProviderInfo对象
     */
    public ProviderInfo getProvider(String className) {
        return mProvidersByName.get(className);
    }

    /**
     * 通过Authority获取ProviderInfo对象
     */
    public ProviderInfo getProviderByAuthority(String authority) {
        return mProvidersByAuthority.get(authority);
    }

    /**
     * 获取该插件所有的ProviderInfo列表
     */
    public ProviderInfo[] getProviders() {
        return mProvidersByName.values().toArray(new ProviderInfo[0]);
    }

    /**
     * 获取Application对象
     */
    public ApplicationInfo getApplication() {
        return mApplication;
    }

    /**
     * 获取存储该插件所有的 ActivityInfo 的 Map
     */
    public HashMap<String, ActivityInfo> getActivityMap() {
        return mActivities;
    }

    /**
     * 获取存储该插件所有的 ServiceInfo 的 Map
     */
    public HashMap<String, ServiceInfo> getServiceMap() {
        return mServices;
    }

    /**
     * 获取存储该插件所有的 Receiver 的 Map
     */
    public HashMap<String, ActivityInfo> getReceiverMap() {
        return mReceivers;
    }

    /**
     * 获取存储该插件所有的 Provider 的 Map
     */
    public HashMap<String, ProviderInfo> getProviderMap() {
        return mProvidersByAuthority;
    }
}
