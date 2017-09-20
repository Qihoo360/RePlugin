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
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qihoo360.i.Factory;
import com.qihoo360.i.Factory2;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.loader2.CertUtils;
import com.qihoo360.loader2.DumpUtils;
import com.qihoo360.loader2.MP;
import com.qihoo360.loader2.PMF;
import com.qihoo360.loader2.PluginStatusController;
import com.qihoo360.mobilesafe.api.AppVar;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.mobilesafe.svcmanager.QihooServiceManager;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.app.PluginApplicationClient;
import com.qihoo360.replugin.debugger.DebuggerReceivers;
import com.qihoo360.replugin.helper.HostConfigHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginFastInstallProviderProxy;
import com.qihoo360.replugin.packages.PluginInfoUpdater;
import com.qihoo360.replugin.packages.PluginManagerProxy;
import com.qihoo360.replugin.packages.PluginRunningList;
import com.qihoo360.replugin.packages.RePluginInstaller;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * RePlugin的对外入口类 <p>
 * 宿主App可直接调用此类中的方法，来使用插件化的几乎全部的逻辑。
 *
 * @author RePlugin Team
 */

public class RePlugin {

    private static final String TAG = "RePlugin";

    /**
     * 插件名为“宿主”。这样插件可以直接通过一些方法来使用“宿主”的接口
     */
    public static final String PLUGIN_NAME_MAIN = "main";

    /**
     * 表示目标进程根据实际情况自动调配
     */
    public static final String PROCESS_AUTO = "" + IPluginManager.PROCESS_AUTO;

    /**
     * 表示目标为UI进程
     */
    public static final String PROCESS_UI = "" + IPluginManager.PROCESS_UI;

    /**
     * 表示目标为常驻进程（名字可变，见BuildConfig内字段）
     */
    public static final String PROCESS_PERSIST = "" + IPluginManager.PROCESS_PERSIST;

    private static RePluginConfig sConfig;

    /**
     * 安装或升级此插件 <p>
     * 注意： <p>
     * 1、这里只将APK移动（或复制）到“插件路径”下，不释放优化后的Dex和Native库，不会加载插件 <p>
     * 2、支持“纯APK”和“p-n”（旧版，即将废弃）插件 <p>
     * 3、此方法是【同步】的，耗时较少 <p>
     * 4、不会触发插件“启动”逻辑，因此只要插件“当前没有被使用”，再次调用此方法则新插件立即生效
     *
     * @param path 插件安装的地址。必须是“绝对路径”。通常可以用context.getFilesDir()来做
     * @return 安装成功的插件信息，外界可直接读取
     * @since 2.0.0 （1.x版本为installDelayed）
     */
    public static PluginInfo install(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException();
        }

        // 判断文件合法性
        File file = new File(path);
        if (!file.exists()) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "install: File not exists. path=" + path);
            }
            return null;
        } else if (!file.isFile()) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "install: Not a valid file. path=" + path);
            }
            return null;
        }

        // 若为p-n开头的插件，则必须是从宿主设置的“插件安装路径”上（默认为files目录）才能安装，其余均不允许
        if (path.startsWith("p-n-")) {
            String installPath = RePlugin.getConfig().getPnInstallDir().getAbsolutePath();
            if (!path.startsWith(installPath)) {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "install: Must be installed from the specified path. Path=" + path + "; Allowed=" + installPath);
                }
                return null;
            }
        }

        return MP.pluginDownloaded(path);
    }

    /**
     * 卸载此插件 <p>
     * 注意： <p>
     * 1、此卸载功能只针对"纯APK"插件方案 <p>
     * 2、若插件正在运行，则直到下次重启进程后才生效
     *
     * @param pluginName 待卸载插件名字
     * @return 插件卸载是否成功
     * @since 2.1.0
     */
    public static boolean uninstall(String pluginName) {
        if (TextUtils.isEmpty(pluginName)) {
            throw new IllegalArgumentException();
        }
        return MP.pluginUninstall(pluginName);
    }

    /**
     * 预加载此插件。此方法会立即释放优化后的Dex和Native库，但不会运行插件代码。 <p>
     * 具体用法可参见preload(PluginInfo)的说明
     *
     * @param pluginName 要加载的插件名
     * @return 预加载是否成功
     * @see #preload(PluginInfo)
     * @since 2.0.0
     */
    public static boolean preload(String pluginName) {
        PluginInfo pi = getPluginInfo(pluginName);
        if (pi == null) {
            // 插件不存在，无法加载Dex
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "preload: Plugin not found! pn=" + pluginName);
            }
            return false;
        }
        return preload(pi);
    }

    /**
     * 预加载此插件。此方法会立即释放优化后的Dex和Native库，但不会运行插件代码。 <p>
     * 使用场景：在“安装”完成后“提前释放Dex”（时间算在“安装过程”中）。这样下次启动插件时则速度飞快 <p>
     * 注意： <p>
     * 1、该方法非必须调用（见“使用场景”）。换言之，只要涉及到插件加载，就会自动完成preload操作，无需开发者关心 <p>
     * 2、Dex和Native库会占用大量的“内部存储空间”。故除非插件是“确定要用的”，否则不必在安装完成后立即调用此方法 <p>
     * 3、该方法为【同步】调用，且耗时较久（尤其是dex2oat的过程），建议在线程中使用 <p>
     * 4、调用后将“启动”此插件，若再次升级，则必须重启进程后才生效
     *
     * @param pi 要加载的插件信息
     * @return 预加载是否成功
     * @see #install(String)
     * @since 2.0.0
     */
    public static boolean preload(PluginInfo pi) {
        if (pi == null) {
            return false;
        }

        // 借助“UI进程”来快速释放Dex（见PluginFastInstallProviderProxy的说明）
        return PluginFastInstallProviderProxy.install(RePluginInternal.getAppContext(), pi);
    }

    /**
     * 是否启用调试器,Debug阶段建议开启,Release阶段建议关闭,默认为关闭状态
     *
     * @param context Context对象
     * @param enable  true=开启
     * @return 是否执行成功
     * @since 2.0.0
     */
    public static boolean enableDebugger(Context context, boolean enable) {
        if ((null != context) && enable) {
            DebuggerReceivers debuggerReceivers = new DebuggerReceivers();
            debuggerReceivers.registerReceivers(context);
        }
        return true;
    }

    /**
     * 开启一个插件的Activity <p>
     * 其中Intent的ComponentName的Key应为插件名（而不是包名），可使用createIntent方法来创建Intent对象
     *
     * @param context Context对象
     * @param intent  要打开Activity的Intent，其中ComponentName的Key必须为插件名
     * @return 插件Activity是否被成功打开？
     * FIXME 是否需要Exception来做？
     * @see #createIntent(String, String)
     * @since 1.0.0
     */
    public static boolean startActivity(Context context, Intent intent) {
        // TODO 先用旧的开启Activity方案，以后再优化
        ComponentName cn = intent.getComponent();
        if (cn == null) {
            // TODO 需要支持Action方案
            return false;
        }
        String plugin = cn.getPackageName();
        String cls = cn.getClassName();
        return Factory.startActivityWithNoInjectCN(context, intent, plugin, cls, IPluginManager.PROCESS_AUTO);
    }

    /**
     * 开启一个插件的Activity，无需调用createIntent或设置ComponentName来修改Intent
     *
     * @param context    Context对象
     * @param intent     要打开Activity的Intent，其中ComponentName的Key必须为插件名
     * @param pluginName 插件名。稍后会填充到Intent中
     * @param activity   插件的Activity。稍后会填充到Intent中
     * @see #startActivity(Context, Intent)
     * @since 1.0.0
     */
    public static boolean startActivity(Context context, Intent intent, String pluginName, String activity) {
        // TODO 先用旧的开启Activity方案，以后再优化
        return Factory.startActivity(context, intent, pluginName, activity, IPluginManager.PROCESS_AUTO);
    }

    /**
     * 通过 forResult 方式启动一个插件的 Activity
     *
     * @param activity    源 Activity
     * @param intent      要打开 Activity 的 Intent，其中 ComponentName 的 Key 必须为插件名
     * @param requestCode 请求码
     * @see #startActivityForResult(Activity, Intent, int, Bundle)
     * @since 2.1.3
     */
    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode) {
        return Factory.startActivityForResult(activity, intent, requestCode, null);
    }

    /**
     * 通过 forResult 方式启动一个插件的 Activity
     *
     * @param activity    源 Activity
     * @param intent      要打开 Activity 的 Intent，其中 ComponentName 的 Key 必须为插件名
     * @param requestCode 请求码
     * @param options     附加的数据
     * @see #startActivityForResult(Activity, Intent, int, Bundle)
     * @since 2.1.3
     */
    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode, Bundle options) {
        return Factory.startActivityForResult(activity, intent, requestCode, options);
    }

    /**
     * 创建一个用来定向到插件组件的Intent <p>
     * <p>
     * 推荐用法： <p>
     * <code>
     * Intent in = RePlugin.createIntent("clean", "com.qihoo360.mobilesafe.clean.CleanActivity");
     * </code> <p>
     * 当然，也可以用标准的Android创建方法： <p>
     * <code>
     * Intent in = new Intent(); <p>
     * in.setComponent(new ComponentName("clean", "com.qihoo360.mobilesafe.clean.CleanActivity"));
     * </code>
     *
     * @param pluginName 插件名
     * @param cls        目标全名
     * @return 可以被RePlugin识别的Intent
     * @since 1.0.0
     */
    public static Intent createIntent(String pluginName, String cls) {
        Intent in = new Intent();
        in.setComponent(createComponentName(pluginName, cls));
        return in;
    }

    /**
     * 创建一个用来定向到插件组件的ComponentName，其Key为插件名，Value为目标组件的类全名
     *
     * @param pluginName 插件名
     * @param cls        目标组件全名
     * @return 一个修改过的ComponentName对象
     * @since 1.0.0
     */
    public static ComponentName createComponentName(String pluginName, String cls) {
        return new ComponentName(pluginName, cls);
    }

    /**
     * 添加允许插件使用的签名指纹。一旦添加进来，则通过该签名制作的插件将允许被执行，否则将不能被执行 <p>
     * 注意：请不要从Prefs中“缓存”签名信息，防止别他人篡改后，导致恶意插件被校验通过
     *
     * @param sign 签名指纹
     * @since 1.0.0
     */
    public static void addCertSignature(String sign) {
        if (TextUtils.isEmpty(sign)) {
            throw new IllegalArgumentException("arg is null");
        }
        CertUtils.SIGNATURES.add(sign.toUpperCase());
    }

    /**
     * 是否使用Dev版AAR？可支持一些"调试特性"，但该AAR【千万不要用于发布环境】 <p>
     * Dev版的AAR可支持如下特性： <p>
     * 1、插件签名不正确时仍允许被安装进来，这样利于调试（发布环境上则容易导致严重安全隐患） <p>
     * 2、可以打出一些完整的日志（发布环境上则容易被逆向，进而对框架稳定性、私密性造成严重影响）
     *
     * @return 是否使用Dev版的AAR？
     * @since 1.0.0
     */
    public static boolean isForDev() {
        return RePluginInternal.FOR_DEV;
    }

    /**
     * 获取当前版本
     *
     * @return 版本号，如2.0.0等
     * @since 2.0.0
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取插件的组件列表（ComponentList） <p>
     * 注意：这里会尝试安装插件，但不会加载资源和代码，因此会有一些耗时的情况 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo(This) < Resources < ClassLoader < Context < Binder
     *
     * @param pluginName 要获取的插件名
     * @return ComponentList对象
     * @see ComponentList
     * @since 1.0.0
     */
    public static ComponentList fetchComponentList(String pluginName) {
        return Factory.queryPluginComponentList(pluginName);
    }

    /**
     * 加载插件，并获取插件的包信息 <p>
     * 注意：这里会尝试加载插件，并释放其Jar包。但不会读取资源，也不会释放oat/odex <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo(This) < Resources < ClassLoader < Context < Binder
     *
     * @param pluginName 插件名
     * @return PackageInfo对象
     * @see PackageInfo
     * @since 1.0.0
     */
    public static PackageInfo fetchPackageInfo(String pluginName) {
        return Factory.queryPluginPackageInfo(pluginName);
    }

    /**
     * 加载插件，并获取插件的资源信息 <p>
     * 注意：这里会尝试安装插件，并释放其Jar包，读取资源，但不会释放oat/odex。 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo < Resources(This) < ClassLoader < Context < Binder
     *
     * @param pluginName 插件名
     * @return Resources对象
     * @see Resources
     * @since 1.0.0
     */
    public static Resources fetchResources(String pluginName) {
        return Factory.queryPluginResouces(pluginName);
    }

    /**
     * 加载插件，并获取插件自身的ClassLoader对象，以调用插件内部的类 <p>
     * 注意：这里会尝试安装插件，并同时加载资源和代码，耗时可能较久 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo < Resources < ClassLoader(This) < Context < Binder
     *
     * @param pluginName 插件名
     * @return 插件的ClassLoader对象
     * @since 1.0.0
     */
    public static ClassLoader fetchClassLoader(String pluginName) {
        return Factory.queryPluginClassLoader(pluginName);
    }

    /**
     * 加载插件，并获取插件自身的Context对象，以获取资源等信息 <p>
     * 注意：这里会尝试安装插件，并同时加载资源和代码，耗时可能较久 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo < Resources < ClassLoader < Context(This) < Binder
     *
     * @param pluginName 插件名
     * @return 插件的Context对象
     * @since 1.0.0
     */
    public static Context fetchContext(String pluginName) {
        return Factory.queryPluginContext(pluginName);
    }

    /**
     * 加载插件，并通过插件里的Plugin类，获取插件定义的IBinder <p>
     * 注意：这里会尝试安装插件，并同时加载资源和代码，耗时可能较久 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo < Resources < Context/ClassLoader < Binder(This) <p>
     * <p>
     * PluginBinder（如使用使用本方法）和GlobalBinder类方法（如getGlobalBinder）的不同： <p>
     * 1、PluginBinder需要指定插件；GlobalBinder无需指定 <p>
     * 2、PluginBinder获取的是插件内部已定义好的Binder；GlobalBinder在获取时必须先在代码中注册
     *
     * @param pluginName 插件名
     * @param module     要加载的插件模块
     * @param process    进程名 TODO 现阶段只能使用IPluginManager中的值，请务必使用它们，否则会出现问题
     * @return 返回插件定义的IBinder对象，供外界使用
     * @see #getGlobalBinder(String)
     * @since 1.0.0
     */
    public static IBinder fetchBinder(String pluginName, String module, String process) {
        return Factory.query(pluginName, module, Integer.parseInt(process));
    }

    /**
     * 在当前进程加载插件，并通过插件里的Plugin类，获取插件定义的IBinder <p>
     * 注意：这里会尝试安装插件，并同时加载资源和代码，耗时可能较久 <p>
     * 性能消耗（小 → 大）：ComponentList/PackageInfo < Resources < Context/ClassLoader < Binder(This) <p>
     * <p>
     * PluginBinder（如使用使用本方法）和GlobalBinder类方法（如getGlobalBinder）的不同： <p>
     * 1、PluginBinder需要指定插件；GlobalBinder无需指定 <p>
     * 2、PluginBinder获取的是插件内部已定义好的Binder；GlobalBinder在获取时必须先在代码中注册
     *
     * @param pluginName 插件名
     * @param module     要加载的插件模块
     * @return 返回插件定义的IBinder对象，供外界使用
     * @see #getGlobalBinder(String)
     * @since 2.1.0
     */
    public static IBinder fetchBinder(String pluginName, String module) {
        return Factory.query(pluginName, module);
    }

    /**
     * 通过ClassLoader对象来获取该ClassLoader应属于哪个插件
     * <p>
     * 该方法消耗非常小，可直接使用
     *
     * @param cl ClassLoader对象
     * @return 插件名
     * @since 1.0.0
     */
    public static String fetchPluginNameByClassLoader(ClassLoader cl) {
        return Factory.fetchPluginName(cl);
    }

    /**
     * 通过资源名（包括前缀和具体名字），来获取指定插件里的资源的ID
     * <p>
     * 性能消耗：等同于 fetchResources
     *
     * @param pluginName     插件名
     * @param resTypeAndName 要获取的“资源类型+资源名”，格式为：“[type]/[name]”。例如： <p>
     *                       → layout/common_title → 从“布局”里获取common_title的ID <p>
     *                       → drawable/common_bg → 从“可绘制图片”里获取common_bg的ID <p>
     *                       详细见Android官方的说明
     * @return 资源的ID。若为0，则表示资源没有找到，无法使用
     * @since 2.2.0
     */
    public static int fetchResourceIdByName(String pluginName, String resTypeAndName) {
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
     * 通过Layout名，来获取插件内的View，并自动做“强制类型转换”（也可直接使用View类型） <p>
     * 注意：若使用的是公共库，则务必按照Provided的形式引入，否则会出现“不同ClassLoader”导致的ClassCastException <p>
     * 当然，非公共库不受影响，但请务必使用Android Framework内的View（例如WebView、ViewGroup等），或索性直接使用View
     *
     * @param pluginName 插件名
     * @param layoutName Layout名字
     * @param root Optional view to be the parent of the generated hierarchy.
     * @return 插件的View。若为Null则表示获取失败
     * @throws ClassCastException 若不是想要的那个View类型，或者ClassLoader不同，则可能会出现此异常。应确保View类型正确
     * @since 2.2.0
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

    /**
     * 获取所有插件的列表（指已安装的）
     *
     * @return PluginInfo的表
     * @since 2.0.0（1.x版本为getExistPlugins）
     */
    public static List<PluginInfo> getPluginInfoList() {
        return MP.getPlugins(true);
    }

    /**
     * 获取指定插件的信息
     *
     * @param name 插件名
     * @return PluginInfo对象
     * @since 1.2.0
     */
    public static PluginInfo getPluginInfo(String name) {
        return MP.getPlugin(name, true);
    }

    /**
     * 获取当前插件的版本号，可以是VersionCode，也可以是meta-data中的ver。
     *
     * @param name 插件名
     * @return 插件版本号。若为-1则表示插件不存在
     * @since 2.0.0
     */
    public static int getPluginVersion(String name) {
        PluginInfo pi = MP.getPlugin(name, false);
        if (pi == null) {
            return -1;
        }

        return pi.getVersion();
    }

    /**
     * 判断插件是否已被安装（但不一定被使用过，如可能不会释放Dex、Native库等） <p>
     * 注意：RePlugin 1.x版本中，isPluginInstalled方法等于现在的isPluginUsed，故含义有变
     *
     * @param pluginName 插件名
     * @return 是否被安装
     * @since 2.0.0 （1.x版本为isPluginExists）
     */
    public static boolean isPluginInstalled(String pluginName) {
        PluginInfo pi = MP.getPlugin(pluginName, false);
        return pi != null;
    }

    /**
     * 判断插件是否曾被使用过。只要释放过Dex、Native的，就认为是“使用过”的 <p>
     * 和isPluginDexExtracted的区别：插件会在升级完成后，会删除旧Dex。其isPluginDexExtracted为false，而isPluginUsed仍为true
     *
     * @param pluginName 插件名
     * @return 插件是否已被使用过
     * @since 2.0.0
     */
    public static boolean isPluginUsed(String pluginName) {
        PluginInfo pi = MP.getPlugin(pluginName, false);
        return pi != null && pi.isUsed();
    }

    /**
     * 判断当前插件是否已释放了Dex、Native库等
     *
     * @param pluginName 插件名
     * @return 是否已被使用过
     * @since 2.0.0 （原为isPluginInstalled）
     */
    public static boolean isPluginDexExtracted(String pluginName) {
        PluginInfo pi = MP.getPlugin(pluginName, false);
        return pi != null && pi.isDexExtracted();
    }

    /**
     * 当前插件是否在运行。只要任意进程在，就都属于此情况
     *
     * @param pluginName 插件名
     * @return 插件是否正在被运行
     * @since 2.0.0
     */
    public static boolean isPluginRunning(String pluginName) {
        try {
            return PluginManagerProxy.isPluginRunning(pluginName);
        } catch (RemoteException e) {
            // 常驻进程中断，且当前进程也没有运行。先返回False
            if (LogRelease.LOGR) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 当前插件是否在指定进程中运行
     *
     * @param pluginName 插件名
     * @param process    指定的进程名，必须为全名
     * @return 插件是否在指定进程中运行
     * @since 2.0.0
     */
    public static boolean isPluginRunningInProcess(String pluginName, String process) {
        try {
            return PluginManagerProxy.isPluginRunningInProcess(pluginName, process);
        } catch (RemoteException e) {
            // 常驻进程中断，且当前进程也没有运行。先返回False
            if (LogRelease.LOGR) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 获取所有正在运行的插件列表
     *
     * @return 所有正在运行的插件的List
     * @see PluginRunningList
     * @since 2.0.0
     */
    public static PluginRunningList getRunningPlugins() {
        return PluginManagerProxy.getRunningPluginsNoThrows();
    }

    /**
     * 获取正在运行此插件的进程名列表 <p>
     * 若要获取PID，可在拿到列表后，通过IPC.getPidByProcessName来反查
     *
     * @param pluginName 要查询的插件名
     * @return 正在运行此插件的进程名列表。一定不会为Null
     * @see IPC#getPidByProcessName(String)
     * @since 2.0.0
     */
    public static String[] getRunningProcessesByPlugin(String pluginName) {
        return PluginManagerProxy.getRunningProcessesByPluginNoThrows(pluginName);
    }

    /**
     * 当前是否处于"常驻进程"？
     *
     * @return 是否处于常驻进程
     * @since 1.1.0
     */
    public static boolean isCurrentPersistentProcess() {
        return IPC.isPersistentProcess();
    }

    /**
     * 获取RePlugin的Config对象。请参见RePluginConfig类的说明
     *
     * @return RePluginConfig对象。注意，即便没有在attachBaseContext中自定义，也仍会有个默认的对象
     * @see RePluginConfig
     * @see App#attachBaseContext(Application, RePluginConfig)
     * @since 1.2.0
     */
    public static RePluginConfig getConfig() {
        return sConfig;
    }

    /**
     * 注册“安装完成后的通知”广播 <p>
     * 此为“本地”广播，插件内也可以接收到。开发者也可以自行注册，做法： <p>
     * <code>
     * IntentFilter itf = new IntentFilter(MP.ACTION_NEW_PLUGIN); <p>
     * LocalBroadcastManager.getInstance(context).registerReceiver(r, itf);
     * </code>
     *
     * @param context Context对象
     * @param r       要绑定的BroadcastReceiver对象
     * @since 1.0.0
     */
    public static void registerInstalledReceiver(Context context, BroadcastReceiver r) {
        IntentFilter itf = new IntentFilter(RePluginConstants.ACTION_NEW_PLUGIN);
        LocalBroadcastManager.getInstance(context).registerReceiver(r, itf);
    }

    /**
     * 在宿主内注册一个可被其它插件所获取的Binder的对象
     *
     * @param hbf 一个IHostBinderFetcher对象
     * @see #fetchBinder(String, String, String)
     * @since 1.0.0
     */
    public static void registerHostBinder(IHostBinderFetcher hbf) {
        MP.installBuiltinPlugin("main", hbf);
    }

    /**
     * 注册一个无需插件名，可被全局使用的Binder对象。Binder对象必须事先创建好 <p>
     * 有关GlobalBinder的详细介绍，请参见getGlobalBinder的说明 <p>
     * 有关此方法和registerGlobalBinderDelayed的区别，请参见其方法说明。
     *
     * @param name   Binder的描述名
     * @param binder Binder对象
     * @return 是否注册成功
     * @see #getGlobalBinder(String)
     * @see #registerGlobalBinderDelayed(String, IBinderGetter)
     * @since 1.2.0
     */
    public static boolean registerGlobalBinder(String name, IBinder binder) {
        return QihooServiceManager.addService(RePluginInternal.getAppContext(), name, binder);
    }

    /**
     * 注册一个无需插件名，可被全局使用的Binder对象，但Binder对象只有在“用到时”才会被创建 <p>
     * 有关GlobalBinder的详细介绍，请参见getGlobalBinder的说明 <p>
     * <p>
     * 和registerGlobalBinder不同的是： <p>
     * 1、前者的binder对象必须事先创建好并传递到参数中 <p>
     * 　　适用于Binder在注册时就立即创建（性能消耗小），或未来使用频率非常多的情况。如“用户账号服务”、“基础服务”等 <p>
     * 2、后者会在getGlobalBinder指定的name被首次调用后，才会尝试获取Binder对象 <p>
     * 　　适用于Binder只在使用时才被创建（确保启动性能快），或未来调用频率较少的情况。如“Root服务”、“特色功能服务”等 <p>
     *
     * @param name   Binder的描述名
     * @param getter 当getGlobalBinder调用时匹配到name后，会调用getter.get()方法来获取IBinder对象
     * @return 是否延迟注册成功
     * @since 1.2.0
     */
    public static boolean registerGlobalBinderDelayed(String name, IBinderGetter getter) {
        return QihooServiceManager.addService(RePluginInternal.getAppContext(), name, getter);
    }

    /**
     * 取消全局Binder对象的注册。这样当调用getGlobalBinder时将不再返回结果 <p>
     * 有关globalBinder的详细介绍，请参见registerGlobalBinder的说明
     *
     * @param name Binder的描述名
     * @return 是否取消成功
     * @see #getGlobalBinder(String)
     * @since 1.2.0
     */
    public static boolean unregisterGlobalBinder(String name) {
        return QihooServiceManager.removeService(RePluginInternal.getAppContext(), name, null);
    }

    /**
     * 获取一个无需插件名，可被全局使用的Binder对象。必须先调用registerGlobalBinder注册后才能获得 <p>
     * PluginBinder（如使用fetchBinder）和GlobalBinder类方法的不同： <p>
     * 1、PluginBinder需要指定插件；GlobalBinder无需指定 <p>
     * 2、PluginBinder获取的是插件内部已定义好的Binder；GlobalBinder在获取时必须先在代码中注册
     *
     * @param name Binder的描述名
     * @return Binder对象。若之前从未注册过，或已被取消注册，则返回Null。
     * @see #registerGlobalBinder(String, IBinder)
     * @see #unregisterGlobalBinder(String)
     * @see #fetchBinder(String, String, String)
     * @since 1.2.0
     */
    public static IBinder getGlobalBinder(String name) {
        return QihooServiceManager.getService(RePluginInternal.getAppContext(), name);
    }

    /**
     * 注册一个“跳转”类。一旦系统或自身想调用指定类时，将自动跳转到插件里的另一个类。 <p>
     * 例如，系统想访问CallShowService类，但此类在宿主中不存在，只在callshow插件中有，则： <p>
     * 未注册“跳转类”时：直接到宿主中寻找CallShowService类，找到后就加载，找不到就崩溃（若不Catch） <p>
     * 注册“挑转类”后，直接将CallShowService的调用“跳转到”callshow插件的CallShowService2类中（名字可以不同）。这种情况下，需要调用： <p>
     * <code>
     * RePlugin.registerHookingClass("com.qihoo360.mobilesafe.CallShowService", <p>
     * 　　　　　　　　　　　　RePlugin.createComponentName("callshow", "com.qihoo360.callshow.CallShowService2"), <p>
     * 　　　　　　　　　　　　DummyService.class);
     * </code>
     * <p> <p>
     * 该方法可以玩出很多【新花样】。如，可用于以下场景： <p>
     * <b>* 已在宿主Manifest中声明了插件的四大组件，只是想借助插件化框架来找到该类并加载进来（如前述例子）。</b> <p>
     * <b>* 某些不方便（不好用，或需要云控）的类，想借机替换成插件里的。</b> <p>
     * 　　如我们有一个LaunchUtils类，现在想使用Utils插件中的同样的类来替代。
     *
     * @param source   要替换的类的全名
     * @param target   要替换的类的目标，需要使用 createComponentName 方法来创建
     * @param defClass 若要替换的类不存在，或插件不可用，则应该使用一个默认的Class。
     *                 <p>
     *                 可替换成如下的形式，也可以传Null。但若访问的是四大组件，传Null可能会导致出现App崩溃（且无法被Catch）
     *                 <p>
     *                 DummyService.class
     *                 <p>
     *                 DummyActivity.class
     *                 <p>
     *                 DummyProvider.class
     *                 <p>
     *                 DummyReceiver.class
     * @see com.qihoo360.replugin.component.dummy.DummyActivity
     * @see com.qihoo360.replugin.component.dummy.DummyService
     * @see com.qihoo360.replugin.component.dummy.DummyReceiver
     * @see com.qihoo360.replugin.component.dummy.DummyProvider
     * @since 1.0.0
     */
    public static void registerHookingClass(String source, ComponentName target, Class defClass) {
        Factory2.registerDynamicClass(source, target.getPackageName(), target.getClassName(), defClass);
    }

    /**
     * 查询某个 Component 是否是“跳转”类
     *
     * @param component 要查询的组件信息，其中 packageName 为插件名称，className 为要查询的类名称
     * @since 2.0.0
     */
    public static boolean isHookingClass(ComponentName component) {
        return Factory2.isDynamicClass(component.getPackageName(), component.getClassName());
    }

    /**
     * 取消对某个“跳转”类的注册，恢复原状。<p>
     * 请参见 registerHookingClass 的详细说明
     *
     * @param source   要替换的类的全名
     * @see #registerHookingClass(String, ComponentName, Class)
     * @since 2.1.6
     */
    public static void unregisterHookingClass(String source) {
        Factory2.unregisterDynamicClass(source);
    }

    /**
     * 支持将APK转化成p-n-开头的插件（已经在360手机卫士80+个插件验证通过的方案），放入files目录，并返回其路径 <p>
     * 注：由于目前卫士绝大多数插件还是p-n开头的，"纯APK"方案还没有经过大量测试，故这里将加入此接口。 <p>
     * 具体做法： <p>
     * <code>
     * String pnPath = RePlugin.convertToPnFile(apkPath);
     * RePlugin.install(pnPath);
     * </code>
     *
     * @param path 要复制的路径
     * @return 安装后的p-n的路径。如为Null表示转化成p-n时出现了问题
     * @since 1.0.0
     * @deprecated 临时方案，为360手机助手的早期而设计。正常情况下，请要么使用p-n方案，要么使用全新"纯APK"方案
     */
    public static String convertToPnFile(String path) {
        File f = RePluginInstaller.covertToPnFile(RePluginInternal.getAppContext(), path);
        if (f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }

    /**
     * dump RePlugin框架运行时的详细信息，包括：Activity 坑位映射表，正在运行的 Service，以及详细的插件信息
     *
     * @param fd
     * @param writer
     * @param args
     */
    public static void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        DumpUtils.dump(fd, writer, args);
    }

    /**
     * RePlugin中，针对Application的入口类 <p>
     * 所有针对Application的调用应从此类开始
     *
     * @author RePlugin Team
     */
    public static class App {

        static boolean sAttached;

        /**
         * 当Application的attachBaseContext调用时需调用此方法 <p>
         * 使用插件框架默认的方案
         *
         * @param app Application对象
         * @see Application#attachBaseContext(Context)
         */
        public static void attachBaseContext(Application app) {
            attachBaseContext(app, new RePluginConfig());
        }

        /**
         * 当Application的attachBaseContext调用时触发 <p>
         * 可自定义插件框架的回调行为。参见RePluginCallbacks类的说明
         * 此方法的定制性不如RePluginConfig的版本
         *
         * @param app Application对象
         * @param pc  可供外界使用的回调
         * @see Application#attachBaseContext(Context)
         * @see RePluginCallbacks
         */
        public static void attachBaseContext(Application app, RePluginCallbacks pc) {
            attachBaseContext(app, new RePluginConfig().setCallbacks(pc));
        }

        /**
         * （推荐）当Application的attachBaseContext调用时需调用此方法 <p>
         * 可自定义插件框架的行为。参见RePluginConfig类的说明
         *
         * @param app Application对象
         * @see Application#attachBaseContext(Context)
         * @see RePluginConfig
         * @since 1.2.0
         */
        public static void attachBaseContext(Application app, RePluginConfig config) {
            if (sAttached) {
                if (LogDebug.LOG) {
                    LogDebug.d(TAG, "attachBaseContext: Already called");
                }
                return;
            }

            RePluginInternal.init(app);
            sConfig = config;
            sConfig.initDefaults(app);

            IPC.init(app);

            // 打印当前内存占用情况
            // 只有开启“详细日志”才会输出，防止“消耗性能”
            if (LOG && RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.printMemoryStatus(LogDebug.TAG, "act=, init, flag=, Start, pn=, framework, func=, attachBaseContext, lib=, RePlugin");
            }

            // 初始化HostConfigHelper（通过反射HostConfig来实现）
            // NOTE 一定要在IPC类初始化之后才使用
            HostConfigHelper.init();

            // FIXME 此处需要优化掉
            AppVar.sAppContext = app;

            // Plugin Status Controller
            PluginStatusController.setAppContext(app);

            PMF.init(app);
            PMF.callAttach();

            sAttached = true;
        }

        /**
         * 当Application的onCreate调用时触发。 <p>
         * 务必先调用attachBaseContext后，才能调用此方法
         *
         * @throws IllegalStateException 若没有调用attachBaseContext，则抛出此异常
         * @see Application#onCreate()
         */
        public static void onCreate() {
            if (!sAttached) {
                throw new IllegalStateException();
            }

            Tasks.init();

            PMF.callAppCreate();

            // 注册监听PluginInfo变化的广播以接受来自常驻进程的更新
            if (!IPC.isPersistentProcess()) {
                PluginInfoUpdater.register(RePluginInternal.getAppContext());
            }

            // 打印当前内存占用情况
            // 只有开启“详细日志”才会输出，防止“消耗性能”
            if (LOG && RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.printMemoryStatus(LogDebug.TAG, "act=, init, flag=, End, pn=, framework, func=, onCreate, lib=, RePlugin");
            }
        }

        /**
         * 当Application的onLowMemory调用时触发 <p>
         * 除了插件化框架本身会做一些事情外，该方法也来通知插件onLowMemory的行为
         * <p>
         * 如果App的minSdkVersion >= 14，该方法不用调用
         *
         * @see Application#onLowMemory()
         */
        public static void onLowMemory() {
            // API>14采用注册回调的方式执行插件中该方法
            if (Build.VERSION.SDK_INT >= 14) {
                return;
            }

            // 遍历插件的Application对象，并调用其onLowMemory
            PluginApplicationClient.notifyOnLowMemory();
        }

        /**
         * 当Application的onTrimMemory调用时触发 <p>
         * 除了插件化框架本身会做一些事情外，该方法也来通知插件onTrimMemory的行为
         * <p>
         * 如果App的minSdkVersion >= 14，该方法不用调用
         *
         * @see Application#onTrimMemory(int)
         */
        public static void onTrimMemory(int level) {
            // API>14采用注册回调的方式执行插件中该方法
            if (Build.VERSION.SDK_INT >= 14) {
                return;
            }

            // 遍历插件的Application对象，并调用其onTrimMemory
            PluginApplicationClient.notifyOnTrimMemory(level);
        }

        /**
         * 当Application的onConfigurationChanged调用时触发 <p>
         * 除了插件化框架本身会做一些事情外，该方法也来通知插件onConfigurationChanged的行为
         * <p>
         * 如果App的minSdkVersion >= 14，该方法不用调用
         *
         * @see Application#onConfigurationChanged(Configuration)
         */
        public static void onConfigurationChanged(Configuration newConfig) {
            // API>14采用注册回调的方式执行插件中该方法
            if (Build.VERSION.SDK_INT >= 14) {
                return;
            }

            // 遍历插件的Application对象，并调用其onConfigurationChanged
            PluginApplicationClient.notifyOnConfigurationChanged(newConfig);
        }
    }
}
