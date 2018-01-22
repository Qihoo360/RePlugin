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
import android.content.Intent;
import android.content.SharedPreferences;

import com.qihoo360.loader2.PluginContext;
import com.qihoo360.replugin.utils.pkg.PackageFilesUtil;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.InputStream;

/**
 * 插件框架对外回调接口集
 * <p>
 * 宿主需继承DefaultPluginCallbacks，并复写相应的方法来自定义插件框架
 *
 * @author RePlugin Team
 */
public class RePluginCallbacks {

    protected final Context mContext;

    public RePluginCallbacks(Context context) {
        mContext = context;
    }

    /**
     * 创建【宿主用的】 RePluginClassLoader 对象以支持大多数插件化特征。默认为：RePluginClassLoader的实例
     * <p>
     * 子类可复写此方法，来创建自己的ClassLoader，做相应的事情（如Hook宿主的ClassLoader做一些特殊的事）
     *
     * @param parent   该ClassLoader的父亲，通常为BootClassLoader
     * @param original 宿主的原ClassLoader，通常为PathClassLoader
     * @return 支持插件化方案的ClassLoader对象，可直接返回RePluginClassLoader
     * @see RePluginClassLoader
     */
    public RePluginClassLoader createClassLoader(ClassLoader parent, ClassLoader original) {
        return new RePluginClassLoader(parent, original);
    }

    /**
     * 插件【插件用的】 ClassLoader对象。默认为：PluginDexClassLoader的实例
     * <p>
     * 子类可复写此方法（虽然不建议），来创建插件自己需要用的DexClassLoader对象
     * <p>
     * 注意：四个参数务必【透传】到要创建的ClassLoader中，以免出现意外。如应该这样做：
     * <code>
     * return new MyDexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent);
     * </code>
     *
     * @param pi                 插件信息
     * @param dexPath            插件APK所在路径
     * @param optimizedDirectory 插件释放odex/oat的路径
     * @param librarySearchPath  插件SO库所在路径
     * @param parent             插件ClassLoader的父亲
     * @return 插件自己可用的PluginDexClassLoader对象。
     */
    public PluginDexClassLoader createPluginClassLoader(PluginInfo pi, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        return new PluginDexClassLoader(pi, dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    /**
     * 当要打开的Activity所对应的插件不存在时触发。通常在这里会触发“下载”逻辑
     * <p>
     * 其中Intent信息很关键，当插件下载完成后，应使用其Intent来打开Activity。如为Null则只是下载，不打开Activity
     *
     * @param context Context对象
     * @param plugin  要打开Activity的插件名，这样可知道要“下载”哪个插件
     * @param intent  要打开的Activity的Intent信息
     * @param process 要打开的Activity所在进程
     * @return 若为true，则表示“我们已弹出下载界面”，则不会走后面的逻辑。若返回false则直接抛出ActivityNotFoundException
     */
    public boolean onPluginNotExistsForActivity(Context context, String plugin, Intent intent, int process) {
        // Nothing
        return false;
    }

    /**
     * 当要打开的Activity所对应的插件过大，需要弹Loading窗提示时触发
     * <p>
     * 其中Intent信息很关键，当插件安装完成后，应使用其Intent来打开Activity。如为Null则只是安装，不打开Activity
     *
     * @param context Context对象
     * @param plugin  要打开Activity的插件名，这样可知道要“下载”哪个插件
     * @param intent  要打开的Activity的Intent信息
     * @param process 要打开的Activity所在进程
     */
    public boolean onLoadLargePluginForActivity(Context context, String plugin, Intent intent, int process) {
        // Nothing
        return false;
    }

    /**
     * 获取SharedPreferences对象
     * <p>
     * 绝大多数情况下直接返回系统的即可，但如360手机卫士是实现了“跨进程SP”的功能，则需复写此方法
     *
     * @param name Desired preferences file. If a preferences file by this name
     *             does not exist, it will be created when you retrieve an
     *             editor (SharedPreferences.edit()) and then commit changes (Editor.commit()).
     * @param mode Operating mode.  Use 0 or {@link Context#MODE_PRIVATE} for the
     *             default operation.
     * @return The single {@link SharedPreferences} instance that can be used
     * to retrieve and modify the preference values.
     */
    public SharedPreferences getSharedPreferences(Context context, String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }

    /**
     * 打开一个可被云控的，插件框架方面的文件（如plugin-list.json等）
     * <p>
     * 既然文件可以被云控，那么通常会在Files和Assets上都有此文件
     * <p>
     * 因此我们会和时间戳文件（或Prefs）进行对比，谁新就用谁的
     * <p>
     * <p>
     * 宿主也可以自行实现相应逻辑。总之要实现“打开最新的文件”的逻辑即可
     *
     * @param context  Context对象
     * @param filename 要打开的文件名
     * @return InputStream对象
     */
    public InputStream openLatestFile(Context context, String filename) {
        return PackageFilesUtil.openLatestInputFile(context, filename);
    }

    /**
     * 获取业务层定义的ContextInjector实现对象，允许业务层对PluginContext中的startActivity等接口处进行自定义操作
     * <p>
     * PluginContext {@link PluginContext} 是插件中使用的Context对象（Activity.mBase 和 Application.mBase）
     *
     * @return 可以允许返回null 表示无需注入自定义逻辑
     *
     * @since 2.0.0
     */
    public ContextInjector createContextInjector() {
        // Nothing
        return null;
    }

    /**
     * 判断当前插件是否已经处于“禁用”状态，允许业务层自定义该禁用逻辑
     *
     * @param pluginInfo 插件的信息
     * @return 是否被禁用
     * @since 2.1.0
     */
    public boolean isPluginBlocked(PluginInfo pluginInfo) {
        // Nothing, allow all
        return false;
    }

    /**
     * 为了p-n插件初始化PluginOverride逻辑，只有老插件方案使用
     * 可以通过该回调，在进程初始化时，设置插件的override逻辑（每个进程都会调到）
     *
     * @since 2.2.2
     */
    public void initPnPluginOverride() {
        // default, do Nothing
    }
}