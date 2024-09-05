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

package com.qihoo360.loader2;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.IModule;
import com.qihoo360.i.IPlugin;
import com.qihoo360.loader.utils.ProcessLocker;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.app.PluginApplicationClient;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginManagerProxy;
import com.qihoo360.replugin.utils.AssetsUtils;
import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.TAG_NO_PN;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
class Plugin {

    private static final String TAG = "Plugin";

    // 只加载Service/Activity/ProviderInfo信息（包含ComponentList）
    static final int LOAD_INFO = 0;

    // 加载插件信息和资源
    static final int LOAD_RESOURCES = 1;

    // 加载插件信息、资源和Dex
    static final int LOAD_DEX = 2;

    // 加载插件信息、资源、Dex，并运行Entry类
    static final int LOAD_APP = 3;

    /**
     * 专门针对LoadEntry（见方法）的锁
     */
    private static final byte[] LOCK_LOAD_ENTRY = new byte[0];

    /**
     * 保存插件 pkgName 至 pluginName 的映射
     */
    static final HashMap<String, String> PKG_NAME_2_PLUGIN_NAME = new HashMap<>();

    /**
     * 保存插件 pluginName 至 fileName 的映射
     */
    static final HashMap<String, String> PLUGIN_NAME_2_FILENAME = new HashMap<>();

    /**
     *
     */
    static final HashMap<String, ClassLoader> FILENAME_2_DEX = new HashMap<>();

    /**
     *
     */
    static final HashMap<String, Resources> FILENAME_2_RESOURCES = new HashMap<>();

    /**
     *
     */
    static final HashMap<String, PackageInfo> FILENAME_2_PACKAGE_INFO = new HashMap<>();

    /**
     *
     */
    static final HashMap<String, ComponentList> FILENAME_2_COMPONENT_LIST = new HashMap<>();

    /**
     * 调试用
     */
    static volatile ArrayList<String> sLoadedReasons;

    /**
     *
     */
    PluginInfo mInfo;
    /**
     * sync
     */
    private final Object LOCK = new Object();

    private final ConditionVariable APPLICATION_LOCK = new ConditionVariable();

    /**
     * 没有IPlugin对象
     */
    boolean mDummyPlugin;

    /**
     *
     */
    Context mContext;

    /**
     *
     */
    ClassLoader mParent;

    /**
     *
     */
    PluginCommImpl mPluginManager;

    /**
     *
     */
   volatile boolean mInitialized;


    /**
     *
     */
    Loader mLoader;

    /**
     * 跑在UI线程里的Handler对象
     */
    final Handler mMainH = new Handler(Looper.getMainLooper());

    /**
     * 用来控制插件里的Application对象
     */
    volatile PluginApplicationClient mApplicationClient;

    private static class UpdateInfoTask implements Runnable {

        PluginInfo mInfo;

        UpdateInfoTask(PluginInfo info) {
            mInfo = info;
        }

        @Override
        public void run() {
            try {
                PluginProcessMain.getPluginHost().updatePluginInfo(mInfo);
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "ph u p i: " + e.getMessage(), e);
                }
            }
        }
    }

    static final Plugin build(PluginInfo info) {
        return new Plugin(info);
    }

    static final Plugin cloneAndReattach(Context c, Plugin p, ClassLoader parent, PluginCommImpl pm) {
        if (p == null) {
            return null;
        }
        p = build(p.mInfo);
        p.attach(c, parent, pm);
        return p;
    }

    /**
     * 根据插件 pkgName 取 pluginName
     */
    static final String queryPluginNameByPkgName(String pkgName) {
        String pluginName;
        synchronized (PKG_NAME_2_PLUGIN_NAME) {
            pluginName = PKG_NAME_2_PLUGIN_NAME.get(pkgName);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "cached pluginName: " + pkgName + " -> " + pluginName);
            }
        }
        return pluginName;
    }

    static final String queryCachedFilename(String name) {
        String filename = null;
        synchronized (PLUGIN_NAME_2_FILENAME) {
            filename = PLUGIN_NAME_2_FILENAME.get(name);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "cached filename: " + name + " -> " + filename);
            }
        }
        return filename;
    }

    static final Map<String, String> queryCachedFilenames() {
        return PLUGIN_NAME_2_FILENAME;
    }

    static final ClassLoader queryCachedClassLoader(String filename) {
        ClassLoader dex = null;
        if (!TextUtils.isEmpty(filename)) {
            synchronized (FILENAME_2_DEX) {
                return FILENAME_2_DEX.get(filename);
            }
        }
        return dex;
    }

    static final Resources queryCachedResources(String filename) {
        Resources resources = null;
        if (!TextUtils.isEmpty(filename)) {
            synchronized (FILENAME_2_RESOURCES) {
                return FILENAME_2_RESOURCES.get(filename);
            }
        }
        return resources;
    }

    static final PackageInfo queryCachedPackageInfo(String filename) {
        PackageInfo packageInfo = null;
        if (!TextUtils.isEmpty(filename)) {
            synchronized (FILENAME_2_PACKAGE_INFO) {
                return FILENAME_2_PACKAGE_INFO.get(filename);
            }
        }
        return packageInfo;
    }

    static final ComponentList queryCachedComponentList(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            synchronized (FILENAME_2_COMPONENT_LIST) {
                return FILENAME_2_COMPONENT_LIST.get(filename);
            }
        } else {
            return null;
        }
    }


    static final List<ComponentList> queryCachedComponentListAll() {
        List<ComponentList> result = new ArrayList<>();
        synchronized (FILENAME_2_COMPONENT_LIST) {
            for (String key : FILENAME_2_COMPONENT_LIST.keySet()) {
                result.add(FILENAME_2_COMPONENT_LIST.get(key));
            }
        }
        return result;
    }

    static final void clearCachedPlugin(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return;
        }

        ClassLoader dex = null;
        synchronized (FILENAME_2_DEX) {
                FILENAME_2_DEX.remove(filename);
        }

        Resources resources = null;
        synchronized (FILENAME_2_RESOURCES) {
                FILENAME_2_RESOURCES.remove(filename);
        }

        PackageInfo packageInfo = null;
        synchronized (FILENAME_2_PACKAGE_INFO) {
                FILENAME_2_PACKAGE_INFO.remove(filename);
        }

        ComponentList cl = null;
        synchronized (FILENAME_2_COMPONENT_LIST) {
                FILENAME_2_COMPONENT_LIST.remove(filename);
        }

    }

    static final void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (LogDebug.DUMP_ENABLED) {
            writer.println("--- cached plugin filename ---");
            // 懒得锁了
            for (String name : PLUGIN_NAME_2_FILENAME.keySet()) {
                writer.println(name + ": " + PLUGIN_NAME_2_FILENAME.get(name));
            }
            writer.println("--- cached plugin Resources ---");
            // 懒得锁了
            for (String name : FILENAME_2_RESOURCES.keySet()) {
                writer.println(name + ": " + FILENAME_2_RESOURCES.get(name));
            }
            writer.println("--- cached plugin PackageInfo ---");
            // 懒得锁了
            for (String name : FILENAME_2_PACKAGE_INFO.keySet()) {
                writer.println(name + ": " + FILENAME_2_PACKAGE_INFO.get(name));
            }
            writer.println("--- cached plugin ComponentList ---");
            // 懒得锁了
            for (String name : FILENAME_2_COMPONENT_LIST.keySet()) {
                writer.println(name + ": " + FILENAME_2_COMPONENT_LIST.get(name));
            }
        }
    }

    private Plugin(PluginInfo info) {
        mInfo = info;
    }

    @Override
    public String toString() {
        if (LOG) {
            return super.toString() + " {info=" + mInfo + "}";
        }
        return super.toString();
    }

    final void attach(Context context, ClassLoader parent, PluginCommImpl manager) {
        mContext = context;
        mParent = parent;
        mPluginManager = manager;
    }

    /**
     * @return
     */
    final ClassLoader getClassLoader() {
        if (mLoader == null) {
            return null;
        }
        return mLoader.mClassLoader;
    }

    /**
     * @return
     */
    final boolean isInitialized() {
        return mInitialized;
    }

    /**
     * @return
     */
    final boolean isLoaded() {
        if (mLoader == null) {
            return false;
        }
        return mLoader.isAppLoaded();
    }

    /**
     * @return
     */
    final boolean isPackageInfoLoaded() {
        if (mLoader == null) {
            return false;
        }
        return mLoader.isPackageInfoLoaded();
    }

    /**
     *
     */
    final boolean load(int load, boolean useCache) {
        PluginInfo info = mInfo;
        boolean rc = loadLocked(load, useCache);
        // 尝试在此处调用Application.onCreate方法
        // Added by Jiongxuan Zhang
        if (load == LOAD_APP && rc) {
            callApp();
        }
        // 如果info改了，通知一下常驻
        // 只针对P-n的Type转化来处理，一定要通知，这样Framework_Version也会得到更新
        if (rc && mInfo != info) {
            UpdateInfoTask task = new UpdateInfoTask((PluginInfo) mInfo.clone());
            Tasks.post2Thread(task);
        }
        return rc;
    }

    final void replaceInfo(PluginInfo info) {
        boolean rc = false;
        if (mInfo.canReplaceForPn(info)) {
            mInfo = info;
            rc = true;
        }
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "replace plugin info: info=" + info + " rc=" + rc);
        }
    }

    /**
     * 从缓存中读取Loader信息
     * @param load 加载类型
     * @return true: 缓存命中 false: 没有缓存
     */
    private boolean loadByCache(int load) {
        if (load == LOAD_INFO) {
            // 提取PackageInfo对象
            String filename = queryCachedFilename(mInfo.getName());
            PackageInfo pi = queryCachedPackageInfo(filename);
            ComponentList cl = queryCachedComponentList(filename);
            if (pi != null && cl != null) {
                mLoader = new Loader(mContext, mInfo.getName(), null, this);
                mLoader.mPackageInfo = pi;
                mLoader.mComponents = cl;
                if (LOG) {
                    LogDebug.i(MAIN_TAG, "loadLocked(): Cached, pkgInfo loaded");
                }
                return true;
            }
        }
        if (load == LOAD_RESOURCES) {
            // 提取PackageInfo和Resources对象
            String filename = queryCachedFilename(mInfo.getName());
            Resources r = queryCachedResources(filename);
            PackageInfo pi = queryCachedPackageInfo(filename);
            ComponentList cl = queryCachedComponentList(filename);
            if (r != null && pi != null && cl != null) {
                mLoader = new Loader(mContext, mInfo.getName(), null, this);
                mLoader.mPkgResources = r;
                mLoader.mPackageInfo = pi;
                mLoader.mComponents = cl;
                if (LOG) {
                    LogDebug.i(MAIN_TAG, "loadLocked(): Cached, resource loaded");
                }
                return true;
            }
        }
        if (load == LOAD_DEX) {
            // 提取PackageInfo、Resources和DexClassLoader对象
            String filename = queryCachedFilename(mInfo.getName());
            Resources r = queryCachedResources(filename);
            PackageInfo pi = queryCachedPackageInfo(filename);
            ComponentList cl = queryCachedComponentList(filename);
            ClassLoader clzl = queryCachedClassLoader(filename);
            if (r != null && pi != null && cl != null && clzl != null) {
                mLoader = new Loader(mContext, mInfo.getName(), null, this);
                mLoader.mPkgResources = r;
                mLoader.mPackageInfo = pi;
                mLoader.mComponents = cl;
                mLoader.mClassLoader = clzl;
                if (LOG) {
                    LogDebug.i(MAIN_TAG, "loadLocked(): Cached, dex loaded");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param load
     * @return
     */
    private boolean loadLocked(int load, boolean useCache) {
        // 若插件被“禁用”，则即便上次加载过（且进程一直活着），这次也不能再次使用了
        // Added by Jiongxuan Zhang
        int status = PluginStatusController.getStatus(mInfo.getName(), mInfo.getVersion());
        if (status < PluginStatusController.STATUS_OK) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "loadLocked(): Disable in=" + mInfo.getName() + ":" + mInfo.getVersion() + "; st=" + status);
            }
            return false;
        }
        synchronized (LOCK) {
            if (mInitialized) {
                if (mLoader == null) {
                    if (LOG) {
                        LogDebug.i(MAIN_TAG, "loadLocked(): Initialized but mLoader is Null");
                    }
                    return false;
                }
                if (load == LOAD_INFO) {
                    boolean rl = mLoader.isPackageInfoLoaded();
                    if (LOG) {
                        LogDebug.i(MAIN_TAG, "loadLocked(): Initialized, pkginfo loaded = " + rl);
                    }
                    return rl;
                }
                if (load == LOAD_RESOURCES) {
                    boolean rl = mLoader.isResourcesLoaded();
                    if (LOG) {
                        LogDebug.i(MAIN_TAG, "loadLocked(): Initialized, resource loaded = " + rl);
                    }
                    return rl;
                }
                if (load == LOAD_DEX) {
                    boolean rl = mLoader.isDexLoaded();
                    if (LOG) {
                        LogDebug.i(MAIN_TAG, "loadLocked(): Initialized, dex loaded = " + rl);
                    }
                    return rl;
                }
                boolean il = mLoader.isAppLoaded();
                if (LOG) {
                    LogDebug.i(MAIN_TAG, "loadLocked(): Initialized, is loaded = " + il);
                }
                return il;
            }
            mInitialized = true;
        }

        // 若开启了“打印详情”则打印调用栈，便于观察
        if (RePlugin.getConfig().isPrintDetailLog()) {
            String reason = "";
            reason += "--- plugin: " + mInfo.getName() + " ---\n";
            reason += "load=" + load + "\n";
            StackTraceElement elements[] = Thread.currentThread().getStackTrace();
            for (StackTraceElement item : elements) {
                if (item.isNativeMethod()) {
                    continue;
                }
                String cn = item.getClassName();
                String mn = item.getMethodName();
                String filename = item.getFileName();
                int line = item.getLineNumber();
                if (LOG) {
                    LogDebug.i(PLUGIN_TAG, cn + "." + mn + "(" + filename + ":" + line + ")");
                }
                reason += cn + "." + mn + "(" + filename + ":" + line + ")" + "\n";
            }
            if (sLoadedReasons == null) {
                sLoadedReasons = new ArrayList<String>();
            }
            sLoadedReasons.add(reason);
        }

        // 这里先处理一下，如果cache命中，省了后面插件提取（如释放Jar包等）操作
        if (useCache) {
            boolean result = loadByCache(load);
            // 如果缓存命中，则直接返回
            if (result) {
                return true;
            }
        }

        Context context = mContext;
        ClassLoader parent = mParent;
        PluginCommImpl manager = mPluginManager;

        //
        String logTag = "try1";
        String lockFileName = String.format(Constant.LOAD_PLUGIN_LOCK, mInfo.getApkFile().getName());
        ProcessLocker lock = new ProcessLocker(context, lockFileName);
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "loadLocked(): Ready to lock! logtag = " + logTag + "; pn = " + mInfo.getName());
        }
        if (!lock.tryLockTimeWait(5000, 10)) {
            // 此处仅仅打印错误
            if (LOGR) {
                LogRelease.w(PLUGIN_TAG, logTag + ": failed to lock: can't wait plugin ready");
            }
        }
        //
        long t1 = System.currentTimeMillis();
        boolean rc = doLoad(logTag, context, parent, manager, load);
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "load " + mInfo.getPath() + " " + hashCode() + " c=" + load + " rc=" + rc + " delta=" + (System.currentTimeMillis() - t1));
        }
        //
        lock.unlock();
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "loadLocked(): Unlock! logtag = " + logTag + "; pn = " + mInfo.getName());
        }
        if (!rc) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, logTag + ": loading fail1");
            }
        }
        if (rc) {
            // 打印当前内存占用情况，只针对Dex和App加载做输出
            // 只有开启“详细日志”才会输出，防止“消耗性能”
            if (LOG && RePlugin.getConfig().isPrintDetailLog()) {
                if (load == LOAD_DEX || load == LOAD_APP) {
                    LogDebug.printPluginInfo(mInfo, load);
                    LogDebug.printMemoryStatus(LogDebug.TAG, "act=, loadLocked, flag=, End-1, pn=, " + mInfo.getName() + ", type=, " + load);
                }
            }
            try {
                // 至此，该插件已开始运行
                PluginManagerProxy.addToRunningPluginsNoThrows(mInfo.getName());
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "p.u.1: " + e.getMessage(), e);
                }
            }

            return true;
        }

        //
        logTag = "try2";
        lock = new ProcessLocker(context, lockFileName);
        if (!lock.tryLockTimeWait(5000, 10)) {
            // 此处仅仅打印错误
            if (LOGR) {
                LogRelease.w(PLUGIN_TAG, logTag + ": failed to lock: can't wait plugin ready");
            }
        }

        // 删除优化dex文件
        File odex = mInfo.getDexFile();
        if (odex.exists()) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, logTag + ": delete exist odex=" + odex.getAbsolutePath());
            }
            odex.delete();
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // support for multidex below LOLLIPOP:delete Extra odex,if need
            try {
                FileUtils.forceDelete(mInfo.getExtraOdexDir());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        }

        t1 = System.currentTimeMillis();
        // 尝试再次加载该插件
        rc = tryLoadAgain(logTag, context, parent, manager, load);
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "load2 " + mInfo.getPath() + " " + hashCode() + " c=" + load + " rc=" + rc + " delta=" + (System.currentTimeMillis() - t1));
        }
        //
        lock.unlock();
        if (!rc) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, logTag + ": loading fail2");
            }
            return false;
        }

        // 打印当前内存占用情况，只针对Dex和App加载做输出
        // 只有开启“详细日志”才会输出，防止“消耗性能”
        if (LOG && RePlugin.getConfig().isPrintDetailLog()) {
            if (load == LOAD_DEX || load == LOAD_APP) {
                LogDebug.printPluginInfo(mInfo, load);
                LogDebug.printMemoryStatus(LogDebug.TAG, "act=, loadLocked, flag=, End-2, pn=, " + mInfo.getName() + ", type=, " + load);
            }
        }

        try {
            // 至此，该插件已开始运行
            PluginManagerProxy.addToRunningPluginsNoThrows(mInfo.getName());
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p.u.2: " + e.getMessage(), e);
            }
        }

        return true;
    }

    final IModule query(Class<? extends IModule> c) {
        return mLoader.mPlugin.query(c);
    }

    final IBinder query(String binder) {
        try {
            return mLoader.mBinderPlugin.mPlugin.query(binder);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "q.b.e.m" + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 抽出方法
     */
    private boolean tryLoadAgain(String tag, Context context, ClassLoader parent, PluginCommImpl manager, int load) {
        mLoader = null;
        return doLoad(tag, context, parent, manager, load);
    }

    private final boolean doLoad(String tag, Context context, ClassLoader parent, PluginCommImpl manager, int load) {
        if (mLoader == null) {
            // 试图释放文件
            PluginInfo info = null;
            if (mInfo.getType() == PluginInfo.TYPE_BUILTIN) {
                //内置插件，首次加载的时候，把数据写到p.l中，然后把文件拷贝到对应目录
                File dir = new File(mInfo.getApkDir());
                File dexdir = mInfo.getDexParentDir();
                String dstName = mInfo.getApkFile().getName();
                boolean rc = AssetsUtils.quickExtractTo(context, mInfo, dir.getAbsolutePath(), dstName, dexdir.getAbsolutePath());
                if (!rc) {
                    // extract built-in plugin failed: plugin=
                    if (LOGR) {
                        LogRelease.e(PLUGIN_TAG, "p e b i p f " + mInfo);
                    }
                    return false;
                }
                File file = new File(dir, dstName);
                info = (PluginInfo) mInfo.clone();
                info.setPath(file.getPath());
                info.setType(PluginInfo.TYPE_EXTRACTED);

            } else if(mInfo.getType() == PluginInfo.TYPE_PN_INSTALLED || mInfo.getType() == PluginInfo.TYPE_EXTRACTED){
                try {
                    //针对升级上来的用户，重新释放已安装插件的so，更换路径
                    File oldSoLibDir = mInfo.getOldNativeLibsDir();
                    File soLibDir = mInfo.getNativeLibsDir();
                    if (oldSoLibDir.exists() && oldSoLibDir.listFiles() != null && oldSoLibDir.listFiles().length > 0
                            && (!soLibDir.exists() || soLibDir.listFiles() == null || soLibDir.listFiles().length == 0)){
                        PluginNativeLibsHelper.install(mInfo.getPath(), soLibDir);
                    }
                }catch (Exception e){
                    return false;
                }
            }
            //
            if (info != null) {
                // 替换
                mInfo = info;
            }

            //
            mLoader = new Loader(context, mInfo.getName(), mInfo.getPath(), this);
            if (!mLoader.loadDex(parent, load)) {
                //内置插件加载失败后，需要把释放的文件路径和类型写入到p.l中去。
                try {
                    PluginManagerProxy.updateTP(mInfo.getName(), mInfo.getType(), mInfo.getPath());
                } catch (RemoteException e) {
                }
                return false;
            }

            // 设置插件为“使用过的”
            // 注意，需要重新获取当前的PluginInfo对象，而非使用“可能是新插件”的mInfo
            try {
                long start = System.currentTimeMillis();
                PluginManagerProxy.updateUsedIfNeeded(mInfo.getName(), mInfo.getPath(), mInfo.getType(), true);
                mInfo.setIsUsed(true);
                if (LOG) {
                    Log.d(TAG_NO_PN, "update " + mInfo.getName() + " time=" + (System.currentTimeMillis() - start));
                }
            } catch (RemoteException e) {
                // 同步出现问题，但仍继续进行
                if (LOGR) {
                    e.printStackTrace();
                }
            }

            long startApp = System.currentTimeMillis();
            // 若需要加载Dex，则还同时需要初始化插件里的Entry对象
            if (load == LOAD_APP) {
                // NOTE Entry对象是可以在任何线程中被调用到
                if (!loadEntryLocked(manager)) {
                    return false;
                }
                // NOTE 在此处调用则必须Post到UI，但此时有可能Activity已被加载
                //      会出现Activity.onCreate比Application更早的情况，故应放在load外面立即调用
                // callApp();
            }
            if (LOG) {
                Log.d(TAG_NO_PN, "load entry for  " + mInfo.getName() + " time=" + (System.currentTimeMillis() - startApp));
            }
        }

        if (load == LOAD_INFO) {
            return mLoader.isPackageInfoLoaded();
        } else if (load == LOAD_RESOURCES) {
            return mLoader.isResourcesLoaded();
        } else if (load == LOAD_DEX) {
            return mLoader.isDexLoaded();
        } else {
            return mLoader.isAppLoaded();
        }
    }

    private boolean loadEntryLocked(PluginCommImpl manager) {
        if (mDummyPlugin) {
            if (LOGR) {
                LogRelease.w(PLUGIN_TAG, "p.lel dm " + mInfo.getName());
            }
            mLoader.mPlugin = new IPlugin() {
                @Override
                public IModule query(Class<? extends IModule> c) {
                    return null;
                }
            };
        } else {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "Plugin.loadEntryLocked(): Load entry, info=" + mInfo);
            }
            if (mLoader.loadEntryMethod2()) {
                if (!mLoader.invoke2(manager)) {
                    return false;
                }
            } else if (mLoader.loadEntryMethod(false)) {
                if (!mLoader.invoke(manager)) {
                    return false;
                }
            } else if (mLoader.loadEntryMethod3()) {
                if (!mLoader.invoke2(manager)) {
                    return false;
                }
            } else {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "p.lel f " + mInfo.getName());
                }
                return false;
            }
        }
        return true;
    }

    // 确保在UI线程中调用
    // ATTENTION 必须在LOCK锁之外调用此方法
    //           否则一旦LOCK锁内任一位置再次调用Plugin.doLoad（如打开另一插件）时会造成循环锁
    // Added by Jiongxuan Zhang
    private void callApp() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callAppLocked();
        } else {
            // 确保一定在UI的最早消息处调用
            mMainH.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    callAppLocked();
                }
            });
            if(!APPLICATION_LOCK.block(3 * 1000)){
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "p.cal timeout " + mInfo.getName());
                }
            }
        }
    }

    private synchronized void callAppLocked() {
        // 获取并调用Application的几个核心方法
        if (!mDummyPlugin) {
            // NOTE 不排除A的Application中调到了B，B又调回到A，或在同一插件内的onCreate开启Service/Activity，而内部逻辑又调用fetchContext并再次走到这里
            // NOTE 因此需要对mApplicationClient做判断，确保永远只执行一次，无论是否成功
            if (mApplicationClient != null) {
                // 已经初始化过，无需再次处理
                APPLICATION_LOCK.open();
                return;
            }

            mApplicationClient = PluginApplicationClient.getOrCreate(
                    mInfo.getName(), mLoader.mClassLoader, mLoader.mComponents, mLoader.mPluginObj.mInfo);

            if (mApplicationClient != null) {
                mApplicationClient.callAttachBaseContext(mLoader.mPkgContext);
                mApplicationClient.callOnCreate();
            }
        } else {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p.cal dm " + mInfo.getName());
            }
        }
        APPLICATION_LOCK.open();
    }
}
