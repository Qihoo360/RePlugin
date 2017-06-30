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

package com.qihoo360.replugin.component.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.qihoo360.i.Factory;
import com.qihoo360.loader2.PMF;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.service.server.IPluginServiceServer;
import com.qihoo360.replugin.component.utils.PluginClientHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 一种能够对【插件】的服务进行：启动、停止、绑定、解绑等功能的类
 * 所有针对插件命令的操作，均从此类开始。
 *
 * @author RePlugin Team
 */
public class PluginServiceClient {
    static final int PROCESS_UNKNOWN = Integer.MAX_VALUE;

    private static final String TAG = "PluginServiceClient";

    private static PluginServiceServerFetcher sServerFetcher = new PluginServiceServerFetcher();
    private static PluginServiceDispatcherManager sDispatcherManager = new PluginServiceDispatcherManager();

    private static Handler sClientHandler = new Handler(Looper.getMainLooper());
    private static Messenger sClientMessenger = new Messenger(sClientHandler);

    /**
     * 开启指定插件的服务。近似于Context.startService方法
     *
     * @param context Context对象
     * @param intent  要打开的服务名。如何填写请参见类的说明
     * @return 最终打开哪个服务的ComponentName
     * @see android.content.Context#startService(Intent)
     */
    public static ComponentName startService(Context context, Intent intent) {
        return startService(context, intent, false);
    }

    // @hide
    public static ComponentName startService(Context context, Intent intent, boolean throwOnFail) {

        // 从 Intent 中获取 ComponentName
        ComponentName cn = getServiceComponentFromIntent(context, intent);

        // 获取将要使用服务的进程ID，并在稍后获取PSS对象
        int process = getProcessByComponentName(cn);
        if (process == PROCESS_UNKNOWN) {
            // 有可能是不支持的插件，也有可能本意就是想调用Main工程的服务。则直接调用系统方法来做
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PSS.startService(): Call SystemAPI: in=" + intent);
            }
            if (throwOnFail) {
                throw new PluginClientHelper.ShouldCallSystem();
            } else {
                return context.startService(intent);
            }
        }

        // 既然确认是插件，则将之前获取的插件信息填入
        intent.setComponent(cn);

        IPluginServiceServer pss = sServerFetcher.fetchByProcess(process);
        if (pss == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.ss: pss n");
            }
            return null;
        }

        // 开启服务
        try {
            return pss.startService(intent, sClientMessenger);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.ss: pss e", e);
            }
        }
        return null;
    }

    /**
     * 停止指定插件的服务。近似于Context.stopService
     *
     * @param context Context对象
     * @param intent  要打开的服务名。如何填写请参见类的说明
     * @return 是否成功停止服务。大于0表示成功
     * @see android.content.Context#stopService(Intent)
     */
    public static boolean stopService(Context context, Intent intent) {
        return stopService(context, intent, false);
    }

    // @hide
    public static boolean stopService(Context context, Intent intent, boolean throwOnFail) {
        // 根据Context所带的插件信息，来填充Intent的ComponentName对象。具体见方法说明
        ComponentName cn = PluginClientHelper.getComponentNameByContext(context, intent.getComponent());

        // 获取将要使用服务的进程ID，并在稍后获取PSS对象
        int process = getProcessByComponentName(cn);
        if (process == PROCESS_UNKNOWN) {
            // 有可能是不支持的插件，也有可能本意就是想调用Main工程的服务。则直接调用系统方法来做
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PSS.stopService(): Call SystemAPI: in=" + intent);
            }
            if (throwOnFail) {
                throw new PluginClientHelper.ShouldCallSystem();
            } else {
                return context.stopService(intent);
            }
        }

        // 既然确认是插件，则将之前获取的插件信息填入
        intent.setComponent(cn);

        IPluginServiceServer pss = sServerFetcher.fetchByProcess(process);
        if (pss == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.sts: pss n");
            }
            return false;
        }

        // 停止服务
        try {
            return pss.stopService(intent, sClientMessenger) != 0;
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.sts: pss e", e);
            }
        }
        return false;
    }

    /**
     * 绑定插件服务，获取其AIDL。近似于Context.bindService
     *
     * @param context Context对象
     * @param intent  要打开的服务名。如何填写请参见类的说明
     * @param sc      ServiceConnection对象（等同于系统）
     * @param flags   flags对象。目前仅支持BIND_AUTO_CREATE标志
     * @return 是否成功绑定服务。大于0表示成功
     * @see android.content.Context#bindService(Intent, ServiceConnection, int)
     */
    public static boolean bindService(Context context, Intent intent, ServiceConnection sc, int flags) {
        return bindService(context, intent, sc, flags, false);
    }

    // @hide
    public static boolean bindService(Context context, Intent intent, ServiceConnection sc, int flags, boolean throwOnFail) {
        // 根据Context所带的插件信息，来填充Intent的ComponentName对象。具体见方法说明
        ComponentName cn = PluginClientHelper.getComponentNameByContext(context, intent.getComponent());

        // 获取将要使用服务的进程ID，并在稍后获取PSS对象
        int process = getProcessByComponentName(cn);
        if (process == PROCESS_UNKNOWN) {
            // 有可能是不支持的插件，也有可能本意就是想调用Main工程的服务。则直接调用系统方法来做
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PSS.bindService(): Call SystemAPI: in=" + intent);
            }
            if (throwOnFail) {
                throw new PluginClientHelper.ShouldCallSystem();
            } else {
                return context.bindService(intent, sc, flags);
            }
        }

        // 既然确认是插件，则将之前获取的插件信息填入
        intent.setComponent(cn);

        IPluginServiceServer pss = sServerFetcher.fetchByProcess(process);
        if (pss == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.bs: pss n");
            }
            return false;
        }

        // 开始绑定服务
        try {
            ServiceDispatcher sd = sDispatcherManager.get(sc, context, sClientHandler, flags, process);
            int r = pss.bindService(intent, sd.getIServiceConnection(), flags, sClientMessenger);
            // TODO 返回值处理
            return r != 0;
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.bs: pss e", e);
            }
        }
        return false;
    }

    /**
     * 解除对插件服务的绑定
     *
     * @param context Context对象
     * @param sc      ServiceConnection对象（等同于系统）
     * @return 是否成功解除绑定
     */
    public static boolean unbindService(Context context, ServiceConnection sc) {
        return unbindService(context, sc, true);
    }

    // @hide
    public static boolean unbindService(Context context, ServiceConnection sc, boolean callSysFirst) {
        // 由于我们不确定ServiceConnection是否在系统中注册，所以先尝试走系统unbind流程，然后再走我们的
        if (callSysFirst) {
            try {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PSS.unbindService(): First, We call SystemAPI: sc=" + sc);
                }
                context.unbindService(sc);
            } catch (Throwable e) {
                // Ignore
            }
        }

        // 获取服务分发器
        ServiceDispatcher sd = sDispatcherManager.forget(context, sc);
        if (sd == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.us: sd n");
            }
            return false;
        }
        IPluginServiceServer pss = sServerFetcher.fetchByProcess(sd.getProcess());
        if (pss == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.us: pss n");
            }
            return false;
        }
        try {
            return pss.unbindService(sd.getIServiceConnection());
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.us: pss e", e);
            }
        }
        return false;
    }

    /**
     * 在插件服务中停止服务。近似于Service.stopSelf
     * 注意：此方法应在插件服务中被调用
     *
     * @param s 要停止的插件服务
     * @see android.app.Service#stopSelf()
     */
    public static void stopSelf(Service s) {
        Intent intent = new Intent(s, s.getClass());
        try {
            PMF.stopService(intent);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "pss.ss: pf f", e);
            }
        }
    }

    private static int getProcessByComponentName(ComponentName cn) {
        if (cn == null) {
            // 如果Intent里面没有带ComponentName，则我们不支持此特性，直接返回null
            // 外界会直接调用其系统的对应方法
            return PROCESS_UNKNOWN;
        }
        String pn = cn.getPackageName();
        // 开始尝试获取插件的ServiceInfo
        ComponentList col = Factory.queryPluginComponentList(pn);
        if (col == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "getProcessByComponentName(): Fetch Component List Error! pn=" + pn);
            }
            return PROCESS_UNKNOWN;
        }
        ServiceInfo si = col.getService(cn.getClassName());
        if (si == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "getProcessByComponentName(): Not register! pn=" + pn);
            }
            return PROCESS_UNKNOWN;
        }
        int p = PluginClientHelper.getProcessInt(si.processName);
        if (LogDebug.LOG) {
            Log.d(TAG, "getProcessByComponentName(): Okay! Process=" + p + "; pn=" + pn);
        }
        return p;
    }

    /**
     * 启动 Service 时，从 Intent 中获取 ComponentName
     */
    private static ComponentName getServiceComponentFromIntent(Context context, Intent intent) {
        ClassLoader cl = context.getClassLoader();
        String plugin = Factory.fetchPluginName(cl);

        /* Intent 中已指定 ComponentName */
        if (intent.getComponent() != null) {
            // 根据 Context 所带的插件信息，来填充 Intent 的 ComponentName 对象。具体见方法说明
            return PluginClientHelper.getComponentNameByContext(context, intent.getComponent());

        } else {
            /* Intent 中已指定 Action，根据 action 解析出 ServiceInfo */
            if (!TextUtils.isEmpty(intent.getAction())) {
                ComponentList componentList = Factory.queryPluginComponentList(plugin);
                if (componentList != null) {
                    // 返回 ServiceInfo 和 Service 所在的插件
                    Pair<ServiceInfo, String> pair = componentList.getServiceAndPluginByIntent(context, intent);
                    if (pair != null) {
                        return new ComponentName(pair.second, pair.first.name);
                    }
                }
            } else {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PSS.startService(): No Component and no Action");
                }
            }
        }
        return null;
    }
}

