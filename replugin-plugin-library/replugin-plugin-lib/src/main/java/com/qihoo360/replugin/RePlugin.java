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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.i.IPluginManager;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginRunningList;
import com.qihoo360.replugin.utils.ParcelUtils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * RePlugin的对外入口类 <p>
 * 宿主App可直接调用此类中的方法，来使用插件化的几乎全部的逻辑。
 *
 * @author RePlugin Team
 */

public class RePlugin {

    static final String TAG = "RePlugin";

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

    /**
     * 安装此插件 <p>
     * 注意： <p>
     * 1、这里只将APK移动（或复制）到“插件路径”下，不释放优化后的Dex和Native库，不会加载插件 <p>
     * 2、支持“纯APK”和“p-n”（旧版，即将废弃）插件 <p>
     * 3、此方法是【同步】的，耗时较少
     *
     * @param path 插件安装的地址。必须是“绝对路径”。通常可以用context.getFilesDir()来做
     * @return 安装成功的插件信息，外界可直接读取
     * @since 2.0.0 （1.x版本为installDelayed）
     */
    public static PluginInfo install(String path) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            Object obj = ProxyRePluginVar.install.call(null, path);
            if (obj != null) {
                // 跨ClassLoader进行parcel对象的构造
                Parcel p = ParcelUtils.createFromParcelable((Parcelable) obj);
                return PluginInfo.CREATOR.createFromParcel(p);
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.preload.call(null, pluginName);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 预加载此插件。此方法会立即释放优化后的Dex和Native库，但不会运行插件代码。 <p>
     * 使用场景：在“安装”完成后“提前释放Dex”（时间算在“安装过程”中）。这样下次启动插件时则速度飞快 <p>
     * 注意： <p>
     * 1、该方法非必须调用（见“使用场景”）。换言之，只要涉及到插件加载，就会自动完成preload操作，无需开发者关心 <p>
     * 2、Dex和Native库会占用大量的“内部存储空间”。故除非插件是“确定要用的”，否则不必在安装完成后立即调用此方法 <p>
     * 3、该方法为【同步】调用，且耗时较久（尤其是dex2oat的过程），建议在线程中使用
     *
     * @param pi 要加载的插件信息
     * @return 预加载是否成功
     * @hide
     * @see #install(String)
     * @since 2.0.0
     */
    public static boolean preload(PluginInfo pi) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            // 跨classloader创建PluginInfo对象
            // TODO 如果有更优雅的方式，可优化
            Object p = ParcelUtils.createFromParcelable(pi, RePluginEnv.getHostCLassLoader(), "com.qihoo360.replugin.model.PluginInfo");
            Object obj = ProxyRePluginVar.preload2.call(null, p);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.startActivity.call(null, context, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.startActivity2.call(null, context, intent, pluginName, activity);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.startActivityForResult.call(null, activity, intent, requestCode);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.startActivityForResult2.call(null, activity, intent, requestCode, options);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (Intent) ProxyRePluginVar.createIntent.call(null, pluginName, cls);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (ComponentName) ProxyRePluginVar.createComponentName.call(null, pluginName, cls);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isForDev.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 获取当前版本
     *
     * @return 版本号，如2.2.2等
     * @since 2.2.2
     */
    public static String getVersion() {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (String) ProxyRePluginVar.getVersion.call(null);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取SDK的版本信息
     *
     * @return SDK的版本，如2.0.0等
     * @since 2.0.0
     * @deprecated 已废弃，请使用 getVersion() 方法
     */
    public static String getSDKVersion() {
        return getVersion();
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (PackageInfo) ProxyRePluginVar.fetchPackageInfo.call(null, pluginName);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (Resources) ProxyRePluginVar.fetchResources.call(null, pluginName);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (ClassLoader) ProxyRePluginVar.fetchClassLoader.call(null, pluginName);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (Context) ProxyRePluginVar.fetchContext.call(null, pluginName);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (IBinder) ProxyRePluginVar.fetchBinder2.call(null, pluginName, module, process);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
     * @since 1.0.0
     */
    public static IBinder fetchBinder(String pluginName, String module) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (IBinder) ProxyRePluginVar.fetchBinder.call(null, pluginName, module);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (String) ProxyRePluginVar.fetchPluginNameByClassLoader.call(null, cl);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
     * @since 2.2.0 (老的host-lib版本也能使用)
     */
    public static int fetchResourceIdByName(String pluginName, String resTypeAndName) {
        if (!RePluginFramework.mHostInitialized) {
            return 0;
        }
        return RePluginCompat.fetchResourceIdByName(pluginName, resTypeAndName);
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
     * @since 2.2.0 (老的host-lib版本也能使用)
     */
    public static <T extends View> T fetchViewByLayoutName(String pluginName, String layoutName, ViewGroup root) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }
        return RePluginCompat.fetchViewByLayoutName(pluginName, layoutName, root);
    }

    /**
     * 获取所有插件的列表（指已安装的）
     *
     * @return PluginInfo的表
     * @since 2.0.0（1.x版本为getExistPlugins）
     */
    public static List<PluginInfo> getPluginInfoList() {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            List list = (List) ProxyRePluginVar.getPluginInfoList.call(null);
            if (list != null && list.size() > 0) {
                List<PluginInfo> ret = new ArrayList<>();
                for (Object o : list) {
                    // 跨ClassLoader进行parcel对象的构造
                    Parcel p = ParcelUtils.createFromParcelable((Parcelable) o);
                    PluginInfo nPi = PluginInfo.CREATOR.createFromParcel(p);
                    ret.add(nPi);
                }

                return ret;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取指定插件的信息
     *
     * @param name 插件名
     * @return PluginInfo对象
     * @since 1.2.0
     */
    public static PluginInfo getPluginInfo(String name) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            Object obj = ProxyRePluginVar.getPluginInfo.call(null, name);
            if (obj != null) {
                // 跨ClassLoader进行parcel对象的构造
                Parcel p = ParcelUtils.createFromParcelable((Parcelable) obj);
                return PluginInfo.CREATOR.createFromParcel(p);
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取当前插件的版本号，可以是VersionCode，也可以是meta-data中的ver。
     *
     * @param name 插件名
     * @return 插件版本号。若为-1则表示插件不存在
     * @since 2.0.0
     */
    public static int getPluginVersion(String name) {
        if (!RePluginFramework.mHostInitialized) {
            return -1;
        }

        try {
            Object obj = ProxyRePluginVar.getPluginVersion.call(null, name);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isPluginInstalled.call(null, pluginName);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isPluginUsed.call(null, pluginName);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 判断当前插件是否已释放了Dex、Native库等
     *
     * @param pluginName 插件名
     * @return 是否已被使用过
     * @since 2.0.0 （原为isPluginInstalled）
     */
    public static boolean isPluginDexExtracted(String pluginName) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isPluginDexExtracted.call(null, pluginName);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 当前插件是否在运行。只要任意进程在，就都属于此情况
     *
     * @param pluginName 插件名
     * @return 插件是否正在被运行
     * @since 2.0.0
     */
    public static boolean isPluginRunning(String pluginName) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isPluginRunning.call(null, pluginName);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isPluginRunningInProcess.call(null, pluginName, process);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 获取所有正在运行的插件列表
     *
     * @return 所有正在运行的插件的List
     * @see PluginRunningList
     * @since 2.0.0
     */
    public static PluginRunningList getRunningPlugins() {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            Object obj = ProxyRePluginVar.getRunningPlugins.call(null);
            if (obj != null) {
                // 跨ClassLoader创建parcelable对象
                Parcel p = ParcelUtils.createFromParcelable((Parcelable) obj);
                PluginRunningList.CREATOR.createFromParcel(p);
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
        // FIXME
        return null;
    }

    /**
     * 获取正在运行此插件的进程名列表 <p>
     * 若要获取PID，可在拿到列表后，通过IPC.getPidByProcessName来反查
     *
     * @param pluginName 要查询的插件名
     * @return 正在运行此插件的进程名列表。一定不会为Null
     * @since 2.0.0
     */
    public static String[] getRunningProcessesByPlugin(String pluginName) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (String[]) ProxyRePluginVar.getRunningProcessesByPlugin.call(null, pluginName);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 当前是否处于"常驻进程"？
     *
     * @return 是否处于常驻进程
     * @since 1.1.0
     */
    public static boolean isCurrentPersistentProcess() {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isCurrentPersistentProcess.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginVar.registerInstalledReceiver.call(null, context, r);
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

        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        Object obj = ProxyRePluginVar.registerGlobalBinder.call(null, name, binder);

        if (obj != null) {
            return (Boolean) obj;
        }

        return false;
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
     * @since 2.1.0 前面的sdk版本没有keepIBinderGetter
     */
    public static boolean registerGlobalBinderDelayed(String name, IBinderGetter getter) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.registerGlobalBinderDelayed.call(null, name, getter);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.unregisterGlobalBinder.call(null, name);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static IBinder getGlobalBinder(String name) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (IBinder) ProxyRePluginVar.getGlobalBinder.call(null, name);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 注册一个“跳转”类。一旦系统或自身想调用指定类时，将自动跳转到插件里的另一个类。 <p>
     * 例如，系统想访问CallShowService类，但此类在宿主中不存在，只在CallShow中有，则： <p>
     * 未注册“跳转类”时：直接到宿主中寻找CallShowService类，找到后就加载，找不到就崩溃（若不Catch） <p>
     * 注册“挑转类”后，直接将CallShowService的调用“跳转到”插件的CallShowService类中（名字可以不同）。这种情况下，需要调用： <p>
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
     * @since 1.0.0
     */
    public static void registerHookingClass(String source, ComponentName target, Class defClass) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginVar.registerHookingClass.call(null, source, target, defClass);
    }

    /**
     * 查询某个 Component 是否是“跳转”类
     *
     * @param component 要查询的组件信息，其中 packageName 为插件名称，className 为要查询的类名称
     * @since 2.0.0
     */
    public static boolean isHookingClass(ComponentName component) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginVar.isHookingClass.call(null, component);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
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
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginVar.unregisterHookingClass.call(null, source);
    }

    /**
     * 注册一个可供其他模块调用的IBinder，供IPlugin.query使用
     *
     * @param name 注册的IBinder名
     * @param binder 注册的IBinder对象
     */
    public static void registerPluginBinder(String name, IBinder binder) {
        RePluginServiceManager.getInstance().addService(name, binder);
    }

    /**
     * 获取宿主的Context
     * @return 宿主的Context
     */
    public static Context getHostContext() {
        return RePluginEnv.getHostContext();
    }

    /**
     * 获取宿主的ClassLoader
     * @return 宿主的ClassLoader
     */
    public static ClassLoader getHostClassLoader() {
        return RePluginEnv.getHostCLassLoader();
    }

    /**
     * 获取该插件的PluginContext
     * @return
     */
    public static Context getPluginContext() {
        return RePluginEnv.getPluginContext();
    }

    /**
     * 判断是否运行在宿主环境中
     * @return 是否运行在宿主环境
     */
    public static boolean isHostInitialized() {
        return RePluginFramework.isHostInitialized();
    }

    /**
     * dump RePlugin框架运行时的详细信息，包括：Activity 坑位映射表，正在运行的 Service，以及详细的插件信息
     *
     * @param fd
     * @param writer
     * @param args
     * @since 2.2.2
     */
    public static void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        try {
            ProxyRePluginVar.dump.call(null, fd, writer, args);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
    }

    static class ProxyRePluginVar {

        private static MethodInvoker install;

        private static MethodInvoker preload;

        private static MethodInvoker preload2;

        private static MethodInvoker startActivity;

        private static MethodInvoker startActivity2;

        private static MethodInvoker startActivityForResult;

        private static MethodInvoker startActivityForResult2;

        private static MethodInvoker createIntent;

        private static MethodInvoker createComponentName;

        private static MethodInvoker isForDev;

        private static MethodInvoker getVersion;

        private static MethodInvoker fetchPackageInfo;

        private static MethodInvoker fetchResources;

        private static MethodInvoker fetchClassLoader;

        private static MethodInvoker fetchContext;

        private static MethodInvoker fetchBinder;

        private static MethodInvoker fetchBinder2;

        private static MethodInvoker fetchPluginNameByClassLoader;

        private static MethodInvoker getPluginInfoList;

        private static MethodInvoker getPluginInfo;

        private static MethodInvoker getPluginVersion;

        private static MethodInvoker isPluginInstalled;

        private static MethodInvoker isPluginUsed;

        private static MethodInvoker isPluginDexExtracted;

        private static MethodInvoker isPluginRunning;

        private static MethodInvoker isPluginRunningInProcess;

        private static MethodInvoker getRunningPlugins;

        private static MethodInvoker getRunningProcessesByPlugin;

        private static MethodInvoker isCurrentPersistentProcess;

        private static MethodInvoker registerInstalledReceiver;

        private static MethodInvoker registerGlobalBinder;

        private static MethodInvoker registerGlobalBinderDelayed;

        private static MethodInvoker unregisterGlobalBinder;

        private static MethodInvoker getGlobalBinder;

        private static MethodInvoker registerHookingClass;

        private static MethodInvoker isHookingClass;

        private static MethodInvoker unregisterHookingClass;

        private static MethodInvoker dump;

        static void initLocked(final ClassLoader classLoader) {

            // 初始化Replugin的相关方法
            final String rePlugin = "com.qihoo360.replugin.RePlugin";
            install = new MethodInvoker(classLoader, rePlugin, "install", new Class<?>[]{String.class});
            preload = new MethodInvoker(classLoader, rePlugin, "preload", new Class<?>[]{String.class});

            // 这里的参数类型PluginInfo是主程序ClassLoader中的PluginInfo
            try {
                Class hostPluginInfo = classLoader.loadClass("com.qihoo360.replugin.model.PluginInfo");
                preload2 = new MethodInvoker(classLoader, rePlugin, "preload", new Class<?>[]{PluginInfo.class});
            } catch (ClassNotFoundException e) {
                //
            }


            startActivity = new MethodInvoker(classLoader, rePlugin, "startActivity", new Class<?>[]{Context.class, Intent.class});
            startActivity2 = new MethodInvoker(classLoader, rePlugin, "startActivity", new Class<?>[]{Context.class, Intent.class, String.class, String.class});
            startActivityForResult = new MethodInvoker(classLoader, rePlugin, "startActivityForResult", new Class<?>[]{Activity.class, Intent.class, int.class});
            startActivityForResult2 = new MethodInvoker(classLoader, rePlugin, "startActivityForResult", new Class<?>[]{Context.class, Intent.class, int.class, Bundle.class});
            createIntent = new MethodInvoker(classLoader, rePlugin, "createIntent", new Class<?>[]{String.class, String.class});
            createComponentName = new MethodInvoker(classLoader, rePlugin, "createComponentName", new Class<?>[]{String.class, String.class});
            isForDev = new MethodInvoker(classLoader, rePlugin, "isForDev", new Class<?>[]{});
            getVersion = new MethodInvoker(classLoader, rePlugin, "getVersion", new Class<?>[]{});
            fetchPackageInfo = new MethodInvoker(classLoader, rePlugin, "fetchPackageInfo", new Class<?>[]{String.class});
            fetchResources = new MethodInvoker(classLoader, rePlugin, "fetchResources", new Class<?>[]{String.class});
            fetchClassLoader = new MethodInvoker(classLoader, rePlugin, "fetchClassLoader", new Class<?>[]{String.class});
            fetchContext = new MethodInvoker(classLoader, rePlugin, "fetchContext", new Class<?>[]{String.class});
            fetchBinder = new MethodInvoker(classLoader, rePlugin, "fetchBinder", new Class<?>[]{String.class, String.class});
            fetchBinder2 = new MethodInvoker(classLoader, rePlugin, "fetchBinder", new Class<?>[]{String.class, String.class, String.class});
            fetchPluginNameByClassLoader = new MethodInvoker(classLoader, rePlugin, "fetchPluginNameByClassLoader", new Class<?>[]{ClassLoader.class});
            getPluginInfoList = new MethodInvoker(classLoader, rePlugin, "getPluginInfoList", new Class<?>[]{});
            getPluginInfo = new MethodInvoker(classLoader, rePlugin, "getPluginInfo", new Class<?>[]{String.class});
            getPluginVersion = new MethodInvoker(classLoader, rePlugin, "getPluginVersion", new Class<?>[]{String.class});
            isPluginInstalled = new MethodInvoker(classLoader, rePlugin, "isPluginInstalled", new Class<?>[]{String.class});
            isPluginUsed = new MethodInvoker(classLoader, rePlugin, "isPluginUsed", new Class<?>[]{String.class});
            isPluginDexExtracted = new MethodInvoker(classLoader, rePlugin, "isPluginDexExtracted", new Class<?>[]{String.class});
            isPluginRunning = new MethodInvoker(classLoader, rePlugin, "isPluginRunning", new Class<?>[]{String.class});
            isPluginRunningInProcess = new MethodInvoker(classLoader, rePlugin, "isPluginRunningInProcess", new Class<?>[]{String.class, String.class});
            getRunningPlugins = new MethodInvoker(classLoader, rePlugin, "getRunningPlugins", new Class<?>[]{});
            getRunningProcessesByPlugin = new MethodInvoker(classLoader, rePlugin, "getRunningProcessesByPlugin", new Class<?>[]{String.class});
            isCurrentPersistentProcess = new MethodInvoker(classLoader, rePlugin, "isCurrentPersistentProcess", new Class<?>[]{});
            registerInstalledReceiver = new MethodInvoker(classLoader, rePlugin, "registerInstalledReceiver", new Class<?>[]{Context.class, BroadcastReceiver.class});
            registerGlobalBinder = new MethodInvoker(classLoader, rePlugin, "registerGlobalBinder", new Class<?>[]{String.class, IBinder.class});

            Class cGetter = null;
            try {
                cGetter = classLoader.loadClass("com.qihoo360.replugin.IBinderGetter");
            } catch (Exception e) {
                // ignore
            }
            registerGlobalBinderDelayed = new MethodInvoker(classLoader, rePlugin, "registerGlobalBinderDelayed", new Class<?>[]{String.class, cGetter});

            unregisterGlobalBinder = new MethodInvoker(classLoader, rePlugin, "unregisterGlobalBinder", new Class<?>[]{String.class});
            getGlobalBinder = new MethodInvoker(classLoader, rePlugin, "getGlobalBinder", new Class<?>[]{String.class});
            registerHookingClass = new MethodInvoker(classLoader, rePlugin, "registerHookingClass", new Class<?>[]{String.class, ComponentName.class, Class.class});
            isHookingClass = new MethodInvoker(classLoader, rePlugin, "isHookingClass", new Class<?>[]{ComponentName.class});
            unregisterHookingClass = new MethodInvoker(classLoader, rePlugin, "unregisterHookingClass", new Class<?>[]{String.class});
            dump = new MethodInvoker(classLoader, rePlugin, "dump", new Class<?>[]{FileDescriptor.class, PrintWriter.class, (new String[0]).getClass()});
        }
    }
}
