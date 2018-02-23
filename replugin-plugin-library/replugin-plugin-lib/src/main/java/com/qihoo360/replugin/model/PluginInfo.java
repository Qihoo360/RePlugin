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

package com.qihoo360.replugin.model;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginEnv;
import com.qihoo360.replugin.helper.JSONHelper;
import com.qihoo360.replugin.helper.LogDebug;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 用来描述插件的描述信息。以Json来封装
 *
 * @author RePlugin Team
 */

public class PluginInfo implements Serializable, Parcelable, Cloneable {

    private static final long serialVersionUID = -6531475023210445876L;

    private static final String TAG = "PluginInfo";

    /**
     * 表示一个尚未安装的"纯APK"插件，其path指向下载完成后APK所在位置
     */
    public static final int TYPE_NOT_INSTALL = 10;

    /**
     * 表示一个释放过的"纯APK"插件，其path指向释放后的那个APK包 <p>
     * <p>
     * 注意：此时可能还并未安装，仅仅是将APK拷贝到相应目录而已。例如，若是通过RePlugin.installDelayed时即为如此
     */
    public static final int TYPE_EXTRACTED = 11;

    /**
     * 表示为P-n已安装，其path指向释放后的那个Jar包（存在于app_plugins_v3，如clean-10-10-102.jar，一个APK）
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public static final int TYPE_PN_INSTALLED = 1;

    /**
     * 内置插件
     */
    public static final int TYPE_BUILTIN = 2;

    /**
     * 表示为P-n还未安装，其path指向释放前的那个Jar包（在Files目录下，如p-n-clean.jar，有V5文件头）
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public static final int TYPE_PN_JAR = 3;

    /**
     * 表示“不确定的框架版本号”，只有旧P-n插件才需要在Load期间得到框架版本号
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public static final int FRAMEWORK_VERSION_UNKNOWN = 0;

    /**
     * HOST向下兼容版本
     */
    private static final int ADAPTER_COMPATIBLE_VERSION = 10;

    /**
     * HOST版本
     */
    public static final int ADAPTER_CURRENT_VERSION = 12;

    /**
     * 插件存放目录
     */
    private static final String LOCAL_PLUGIN_SUB_DIR = "plugins_v3";

    /**
     * 插件ODEX存放目录
     */
    private static final String LOCAL_PLUGIN_ODEX_SUB_DIR = "plugins_v3_odex";

    /**
     * 插件data存放目录
     */
    public static final String LOCAL_PLUGIN_DATA_SUB_DIR = "plugins_v3_data";

    /**
     * 插件Native（SO库）存放目录
     */
    private static final String LOCAL_PLUGIN_DATA_LIB_DIR = "plugins_v3_libs";

    /**
     * "纯APK"插件存放目录
     */
    private static final String LOCAL_PLUGIN_APK_SUB_DIR = "p_a";

    /**
     * "纯APK"中释放Odex的目录
     */
    private static final String LOCAL_PLUGIN_APK_ODEX_SUB_DIR = "p_od";

    /**
     * 纯"APK"插件的Native（SO库）存放目录
     */
    private static final String LOCAL_PLUGIN_APK_LIB_DIR = "p_n";

    /**
     * "纯APK"插件同版本升级时插件、Odex、Native（SO库）的用于覆盖的存放目录
     */
    private static final String LOCAL_PLUGIN_APK_COVER_DIR = "p_c";

    /**
     * 插件extra dex（优化前）释放的以插件名独立隔离的子目录
     * 适用于 android 5.0 以下，5.0以上不会用到该目录
     */
    private static final String LOCAL_PLUGIN_INDEPENDENT_EXTRA_DEX_SUB_DIR = "_ed";

    /**
     * 插件extra dex（优化后）释放的以插件名独立隔离的子目录
     */
    private static final String LOCAL_PLUGIN_INDEPENDENT_EXTRA_ODEX_SUB_DIR = "_eod";

    private transient JSONObject mJson;
    private String mJsonText;

    // 若插件需要更新，则会有此值
    private PluginInfo mPendingUpdate;

    // 若插件需要卸载，则会有此值
    private PluginInfo mPendingDelete;

    // 若插件需要同版本覆盖安装更新，则会有此值
    private PluginInfo mPendingCover;
    private boolean mIsPendingCover;    // 若当前为“新的PluginInfo”且为“同版本覆盖”，则为了能区分路径，则需要将此字段同步到Json文件中

    // 若当前为“新的PluginInfo”，则其“父Info”是什么？
    // 通常当前这个Info会包裹在“mPendingUpdate/mPendingDelete/mPendingCover”内
    // 此信息【不会】做持久化工作。下次重启进程后会消失
    private PluginInfo mParentInfo;

    private PluginInfo(JSONObject jo) {
        initPluginInfo(jo);
    }

    private PluginInfo(String name, int low, int high, int ver) {
        mJson = new JSONObject();
        JSONHelper.putNoThrows(mJson, "name", name);
        JSONHelper.putNoThrows(mJson, "low", low);
        JSONHelper.putNoThrows(mJson, "high", high);
        JSONHelper.putNoThrows(mJson, "ver", ver);
    }

    private PluginInfo(String pkgName, String alias, int low, int high, int version, String path, int type) {
        // 如Low、High不正确，则给个默认值（等于应用的“最小支持协议版本”）
        if (low <= 0) {
            low = ADAPTER_COMPATIBLE_VERSION;
        }
        if (high <= 0) {
            high = ADAPTER_COMPATIBLE_VERSION;
        }

        mJson = new JSONObject();
        JSONHelper.putNoThrows(mJson, "pkgname", pkgName);
        JSONHelper.putNoThrows(mJson, "ali", alias);
        JSONHelper.putNoThrows(mJson, "name", makeName(pkgName, alias));
        JSONHelper.putNoThrows(mJson, "low", low);
        JSONHelper.putNoThrows(mJson, "high", high);

        setVersion(version);
        setPath(path);
        setType(type);
    }

    private void initPluginInfo(JSONObject jo) {
        mJson = jo;

        // 缓存“待更新”的插件信息
        JSONObject ujo = jo.optJSONObject("upinfo");
        if (ujo != null) {
            mPendingUpdate = new PluginInfo(ujo);
        }

        // 缓存“待卸载”的插件信息
        JSONObject djo = jo.optJSONObject("delinfo");
        if (djo != null) {
            mPendingDelete = new PluginInfo(djo);
        }

        // 缓存"待覆盖安装"的插件信息
        JSONObject cjo = jo.optJSONObject("coverinfo");
        if (cjo != null) {
            mPendingCover = new PluginInfo(cjo);
        }

        // 缓存"待覆盖安装"的插件覆盖字段
        mIsPendingCover = jo.optBoolean("cover");
    }

    // 通过别名和包名来最终确认插件名
    // 注意：老插件会用到"name"字段，同时出于性能考虑，故必须写在Json中。见调用此方法的地方
    private String makeName(String pkgName, String alias) {
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }
        if (!TextUtils.isEmpty(pkgName)) {
            return pkgName;
        }
        return "";
    }

    /**
     * 获取插件名，如果有别名，则返回别名，否则返回插件包名 <p>
     * （注意：旧插件"p-n"的"别名"就是插件名）
     */
    public String getName() {
        return mJson.optString("name");
    }

    /**
     * 获取插件包名
     */
    public String getPackageName() {
        return mJson.optString("pkgname");
    }

    /**
     * 获取插件别名
     */
    public String getAlias() {
        return mJson.optString("ali");
    }

    /**
     * 获取插件的版本
     */
    public int getVersion() {
        return mJson.optInt("ver");
    }

    /**
     * 获取最新的插件，目前所在的位置
     */
    public String getPath() {
        return mJson.optString("path");
    }

    /**
     * 设置最新的插件，目前所在的位置 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     */
    public void setPath(String path) {
        JSONHelper.putNoThrows(mJson, "path", path);
    }

    /**
     * 插件是否被使用过？只要释放过Dex的都认为是true
     *
     * @return 插件是否使用过？
     */
    public boolean isUsed() {
        // 注意：该方法不单纯获取JSON中的值，而是根据插件类型（p-n、纯APK）、所处环境（新插件、当前插件）而定
        if (isPnPlugin()) {
            // 为兼容以前逻辑，p-n仍是判断dex是否存在
            return isDexExtracted();
        } else if (getParentInfo() != null) {
            // 若PluginInfo是其它PluginInfo中的PendingUpdate，则返回那个PluginInfo的Used即可
            return getParentInfo().isUsed();
        } else {
            // 若是纯APK，且不是PendingUpdate，则直接从Json中获取
            return mJson.optBoolean("used");
        }
    }

    /**
     * 设置插件是否被使用过 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param used 插件是否被使用过
     */
    public void setIsUsed(boolean used) {
        JSONHelper.putNoThrows(mJson, "used", used);
    }

    /**
     * 获取Long型的，可用来对比的版本号
     */
    public long getVersionValue() {
        return mJson.optLong("verv");
    }

    /**
     * 插件的Dex是否已被优化（释放）了？
     *
     * @return
     */
    public boolean isDexExtracted() {
        File f = getDexFile();
        // 文件存在，且大小不为 0 时，才返回 true。
        return f.exists() && f.length() > 0;
    }

    /**
     * 获取APK存放的文件信息 <p>
     * 若为"纯APK"插件，则会位于app_p_a中；若为"p-n"插件，则会位于"app_plugins_v3"中 <p>
     * 注意：若支持同版本覆盖安装的话，则会位于app_p_c中； <p>
     *
     * @return Apk所在的File对象
     */
    public File getApkFile() {
        return new File(getApkDir(), makeInstalledFileName() + ".jar");
    }

    /**
     * 获取APK存放目录
     *
     * @return
     */
    public String getApkDir() {
        // 必须使用宿主的Context对象，防止出现“目录定位到插件内”的问题
        Context context = RePluginEnv.getHostContext();
        File dir;
        if (isPnPlugin()) {
            dir = context.getDir(LOCAL_PLUGIN_SUB_DIR, 0);
        } else if (getIsPendingCover()) {
            dir = context.getDir(LOCAL_PLUGIN_APK_COVER_DIR, 0);
        } else {
            dir = context.getDir(LOCAL_PLUGIN_APK_SUB_DIR, 0);
        }

        return dir.getAbsolutePath();
    }

    /**
     * 获取或创建（如果需要）某个插件的Dex目录，用于放置dex文件
     * 注意：仅供框架内部使用;仅适用于Android 4.4.x及以下
     *
     * @param dirSuffix 目录后缀
     * @return 插件的Dex所在目录的File对象
     */
    @NonNull
    private File getDexDir(File dexDir, String dirSuffix) {

        File dir = new File(dexDir, makeInstalledFileName() + dirSuffix);

        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    /**
     * 获取Extra Dex（优化前）生成时所在的目录 <p>
     * 若为"纯APK"插件，则会位于app_p_od/xx_ed中；若为"p-n"插件，则会位于"app_plugins_v3_odex/xx_ed"中 <p>
     * 若支持同版本覆盖安装的话，则会位于app_p_c/xx_ed中； <p>
     * 注意：仅供框架内部使用;仅适用于Android 4.4.x及以下
     *
     * @return 优化前Extra Dex所在目录的File对象
     */
    public File getExtraDexDir() {
        return getDexDir(getDexParentDir(), LOCAL_PLUGIN_INDEPENDENT_EXTRA_DEX_SUB_DIR);
    }

    /**
     * 获取Extra Dex（优化后）生成时所在的目录 <p>
     * 若为"纯APK"插件，则会位于app_p_od/xx_eod中；若为"p-n"插件，则会位于"app_plugins_v3_odex/xx_eod"中 <p>
     * 若支持同版本覆盖安装的话，则会位于app_p_c/xx_eod中； <p>
     * 注意：仅供框架内部使用;仅适用于Android 4.4.x及以下
     *
     * @return 优化后Extra Dex所在目录的File对象
     */
    public File getExtraOdexDir() {
        return getDexDir(getDexParentDir(), LOCAL_PLUGIN_INDEPENDENT_EXTRA_ODEX_SUB_DIR);
    }

    /**
     * 获取Dex（优化后）生成时所在的目录 <p>
     *
     * Android O之前：
     * 若为"纯APK"插件，则会位于app_p_od中；若为"p-n"插件，则会位于"app_plugins_v3_odex"中 <p>
     * 若支持同版本覆盖安装的话，则会位于app_p_c中； <p>
     *
     * Android O：
     * APK存放目录/oat/{cpuType}
     *
     * 注意：仅供框架内部使用
     * @return 优化后Dex所在目录的File对象
     */
    public File getDexParentDir() {

        // 必须使用宿主的Context对象，防止出现“目录定位到插件内”的问题
        Context context = RePluginEnv.getHostContext();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            return new File(getApkDir() + File.separator + "oat" + File.separator + VMRuntimeCompat.getArtOatCpuType());
        } else {
            if (isPnPlugin()) {
                return context.getDir(LOCAL_PLUGIN_ODEX_SUB_DIR, 0);
            } else if (getIsPendingCover()) {
                return context.getDir(LOCAL_PLUGIN_APK_COVER_DIR, 0);
            } else {
                return context.getDir(LOCAL_PLUGIN_APK_ODEX_SUB_DIR, 0);
            }
        }
    }

    /**
     * 获取Dex（优化后）所在的文件信息 <p>
     *
     * Android O 之前：
     * 若为"纯APK"插件，则会位于app_p_od中；若为"p-n"插件，则会位于"app_plugins_v3_odex"中 <p>
     *
     * Android O：
     * APK存放目录/oat/{cpuType}/XXX.odex
     *
     * 注意：仅供框架内部使用
     *
     * @return 优化后Dex所在文件的File对象
     */
    public File getDexFile() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            File dir = getDexParentDir();
            return new File(dir, makeInstalledFileName() + ".odex");
        } else {
            File dir = getDexParentDir();
            return new File(dir, makeInstalledFileName() + ".dex");
        }
    }

    /**
     * 根据类型来获取SO释放的路径 <p>
     * 若为"纯APK"插件，则会位于app_p_n中；若为"p-n"插件，则会位于"app_plugins_v3_libs"中 <p>
     * 若支持同版本覆盖安装的话，则会位于app_p_c中； <p>
     * 注意：仅供框架内部使用
     *
     * @return SO释放路径所在的File对象
     */
    public File getNativeLibsDir() {
        // 必须使用宿主的Context对象，防止出现“目录定位到插件内”的问题
        Context context = RePluginEnv.getHostContext();
        File dir;
        if (isPnPlugin()) {
            dir = context.getDir(LOCAL_PLUGIN_DATA_LIB_DIR, 0);
        } else if (getIsPendingCover()) {
            dir = context.getDir(LOCAL_PLUGIN_APK_COVER_DIR, 0);
        } else {
            dir = context.getDir(LOCAL_PLUGIN_APK_LIB_DIR, 0);
        }
        return new File(dir, makeInstalledFileName());
    }

    /**
     * 获取插件当前所处的类型。详细见TYPE_XXX常量
     */
    public int getType() {
        return mJson.optInt("type");
    }

    /**
     * 设置插件当前所处的类型。详细见TYPE_XXX常量 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     */
    public void setType(int type) {
        JSONHelper.putNoThrows(mJson, "type", type);
    }

    /**
     * 是否已准备好了新版本？
     *
     * @return 是否已准备好
     */
    public boolean isNeedUpdate() {
        return mPendingUpdate != null;
    }

    /**
     * 获取将来要更新的插件的信息，将会在下次启动时才能被使用
     *
     * @return 插件更新信息
     */
    public PluginInfo getPendingUpdate() {
        return mPendingUpdate;
    }

    /**
     * 是否需要删除插件？
     *
     * @return 是否需要卸载插件
     */
    public boolean isNeedUninstall() {
        return mPendingDelete != null;
    }

    /**
     * 获取将来要卸载的插件的信息，将会在下次启动时才能被使用
     *
     * @return 插件卸载信息
     */
    public PluginInfo getPendingDelete() {
        return mPendingDelete;
    }

    /**
     * 是否已准备好了新待覆盖的版本？
     *
     * @return 是否已准备好
     */
    public boolean isNeedCover() {
        return mPendingCover != null;
    }

    /**
     * 获取将来要覆盖更新的插件的信息，将会在下次启动时才能被使用
     *
     * @return 插件覆盖安装信息
     */
    public PluginInfo getPendingCover() {
        return mPendingCover;
    }

    /**
     * 此PluginInfo是否包含同版本覆盖的字段？只在调用RePlugin.install方法才能看到 <p>
     * 注意：仅框架内部使用
     *
     * @return 是否包含同版本覆盖字段
     */
    public boolean getIsPendingCover() {
        return mIsPendingCover;
    }

    /**
     * 获取最小支持宿主API的版本
     */
    public int getLowInterfaceApi() {
        return mJson.optInt("low", ADAPTER_COMPATIBLE_VERSION);
    }

    /**
     * 获取最高支持宿主API的版本
     *
     * @deprecated 可能会废弃
     */
    public int getHighInterfaceApi() {
        return mJson.optInt("high", ADAPTER_COMPATIBLE_VERSION);
    }

    /**
     * 获取框架的版本号 <p>
     * 此版本号不同于“协议版本”。这直接关系到四大组件和其它模块的加载情况
     */
    public int getFrameworkVersion() {
        // 仅p-n插件在用
        // 之所以默认为FRAMEWORK_VERSION_UNKNOWN，是因为在这里还只是读取p-n文件头，框架版本需要在loadDex阶段获得
        return mJson.optInt("frm_ver", FRAMEWORK_VERSION_UNKNOWN);
    }

    /**
     * 设置框架的版本号 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param version 框架版本号
     */
    public void setFrameworkVersion(int version) {
        JSONHelper.putNoThrows(mJson, "frm_ver", version);
    }

    /**
     * 生成用于放入app_plugin_v3（app_p_n）等目录下的插件的文件名，其中：<p>
     * 1、“纯APK”方案：得到混淆后的文件名（规则见代码内容） <p>
     * 2、“旧p-n”和“内置插件”（暂定）方案：得到类似 shakeoff_10_10_103 这样的比较规范的文件名 <p>
     * 3、只获取文件名，其目录和扩展名仍需在外面定义
     *
     * @return 文件名（不含扩展名）
     */
    public String makeInstalledFileName() {
        if (isPnPlugin() || getType() == TYPE_BUILTIN) {
            return formatName();
        } else {
            // 混淆插件名字，做法：
            // 1. 生成最初的名字：[插件包名（小写）][协议最低版本][协议最高版本][插件版本][ak]
            //    必须用小写和数字、无特殊字符，否则hashCode后会有一定的重复率
            // 2. 将其生成出hashCode
            // 3. 将整体数字 - 88
            String n = getPackageName().toLowerCase() + getLowInterfaceApi() + getHighInterfaceApi() + getVersion() + "ak";
            int h = n.hashCode() - 88;
            return Integer.toString(h);
        }
    }

    /**
     * 若此Info为“新PluginInfo”，则这里返回的是“其父Info”的内容。通常和PendingUpdate有关
     *
     * @return 父PluginInfo
     */
    public PluginInfo getParentInfo() {
        return mParentInfo;
    }

    private void setVersion(int version) {
        JSONHelper.putNoThrows(mJson, "ver", version);
        JSONHelper.putNoThrows(mJson, "verv", buildCompareValue());
    }

    // -------------------------
    // Parcelable and Cloneable
    // -------------------------

    public static final Creator<PluginInfo> CREATOR = new Creator<PluginInfo>() {

        @Override
        public PluginInfo createFromParcel(Parcel source) {
            return new PluginInfo(source);
        }

        @Override
        public PluginInfo[] newArray(int size) {
            return new PluginInfo[size];
        }
    };

    private PluginInfo(Parcel source) {
        JSONObject jo = null;
        String txt = null;
        try {
            txt = source.readString();
            jo = new JSONObject(txt);
        } catch (JSONException e) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "PluginInfo: mJson error! s=" + txt, e);
            }
            jo = new JSONObject();
        }
        initPluginInfo(jo);
    }

    @Override
    public Object clone() {
        PluginInfo pluginInfo = null;

        // 通过 transient 和 Serializable 配合实现深拷贝
        this.mJsonText = this.mJson != null ? this.mJson.toString() : null;

        // Object -> Stream -> clone Object
        try {
            ByteArrayOutputStream byteArrOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteArrOut);

            objOut.writeObject(this);

            ByteArrayInputStream byteArrIn = new ByteArrayInputStream(byteArrOut.toByteArray());
            ObjectInputStream objIn = new ObjectInputStream(byteArrIn);

            pluginInfo = (PluginInfo) objIn.readObject();

            if (pluginInfo != null && !TextUtils.isEmpty(this.mJsonText)) {
                pluginInfo.mJson = new JSONObject(this.mJsonText);

                JSONObject ujo = pluginInfo.mJson.optJSONObject("upinfo");
                if (ujo != null) {
                    pluginInfo.mPendingUpdate = new PluginInfo(ujo);
                }

                JSONObject djo = pluginInfo.mJson.optJSONObject("delinfo");
                if (djo != null) {
                    pluginInfo.mPendingDelete = new PluginInfo(djo);
                }

                JSONObject cjo = pluginInfo.mJson.optJSONObject("coverinfo");
                if (cjo != null) {
                    pluginInfo.mPendingCover = new PluginInfo(cjo);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pluginInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mJson.toString());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("PInfo { ");
        toContentString(b);
        b.append(" }");

        return b.toString();
    }

    private void toContentString(StringBuilder b) {
        // 插件名 + 版本 + 框架版本
        {
            b.append('<');
            b.append(getName()).append(':').append(getVersion());
            b.append('(').append(getFrameworkVersion()).append(')');
            b.append("> ");
        }

        // 当前是否为PendingUpdate的信息
        if (mParentInfo != null) {
            b.append("[HAS_PARENT] ");
        }

        // 插件类型
        {
            if (getType() == TYPE_BUILTIN) {
                b.append("[BUILTIN] ");
            } else if (isPnPlugin()) {
                b.append("[P-N] ");
            } else {
                b.append("[APK] ");
            }
        }

        // 插件是否已释放
        if (isDexExtracted()) {
            b.append("[DEX_EXTRACTED] ");
        }

        // 插件是否“正在使用”
        if (RePlugin.isPluginRunning(getName())) {
            b.append("[RUNNING] ");
        }

        // 哪些进程使用
        String[] processes = RePlugin.getRunningProcessesByPlugin(getName());
        if (processes != null) {
            b.append("processes=").append(Arrays.toString(processes)).append(' ');
        }

        // 插件基本信息
        if (mJson != null) {
            b.append("js=").append(mJson).append(' ');
        }

        // 和插件路径有关（除APK路径以外）
        {
            b.append("dex=").append(getDexFile()).append(' ');
            b.append("nlib=").append(getNativeLibsDir());
        }
    }

    @Override
    public int hashCode() {
        return mJson.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        PluginInfo pluginInfo = (PluginInfo) obj;

        try {
            return pluginInfo.mJson.toString().equals(mJson.toString());
        } catch (Exception e) {
            return false;
        }
    }


    public static final String format(String name, int low, int high, int ver) {
        return name + "-" + low + "-" + high + "-" + ver;
    }

    private String formatName() {
        return format(getName(), getLowInterfaceApi(), getHighInterfaceApi(), getVersion());
    }

    private final long buildCompareValue() {
        long x;
        x = getHighInterfaceApi() & 0x7fff;
        long v1 = x << (32 + 16);
        x = getLowInterfaceApi() & 0xffff;
        long v2 = x << 32;
        long v3 = getVersion();
        return v1 | v2 | v3;
    }

    /**
     * 判断是否为p-n类型的插件？
     *
     * @return 是否为p-n类型的插件
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public boolean isPnPlugin() {
        int type = getType();
        return type == TYPE_PN_INSTALLED || type == TYPE_PN_JAR || type == TYPE_BUILTIN;
    }

    private static class VMRuntimeCompat {

        public static final String ARM = "arm";

        public static final String ARM64 = "arm64";

        private static final byte[] GET_LOCKER = new byte[0];

        private static volatile Boolean sIs64Bit;

        /**
         * 精确判断是否为64位
         */
        public static boolean is64Bit() {
            // 最终使用下列方法：
            // VMRuntime.getRuntime().is64Bit();
            if (sIs64Bit != null) {
                return sIs64Bit;
            }
            synchronized (GET_LOCKER) {
                if (sIs64Bit != null) {
                    return sIs64Bit;
                }

                // 确保只获取一次。但不排除个别手机一上来获取会有问题（没遇到）
                sIs64Bit = is64BitImpl();
                return sIs64Bit;
            }
        }

        private static boolean is64BitImpl() {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Android API 21之前不支持64位CPU
                    return false;
                }

                Class<?> clzVMRuntime = Class.forName("dalvik.system.VMRuntime");
                if (clzVMRuntime == null) {
                    return false;
                }
                Method mthVMRuntimeGet = clzVMRuntime.getDeclaredMethod("getRuntime");
                if (mthVMRuntimeGet == null) {
                    return false;
                }
                Object objVMRuntime = mthVMRuntimeGet.invoke(null);
                if (objVMRuntime == null) {
                    return false;
                }
                Method sVMRuntimeIs64BitMethod = clzVMRuntime.getDeclaredMethod("is64Bit");
                if (sVMRuntimeIs64BitMethod == null) {
                    return false;
                }
                Object objIs64Bit = sVMRuntimeIs64BitMethod.invoke(objVMRuntime);
                if (objIs64Bit instanceof Boolean) {
                    return (boolean) objIs64Bit;
                }
            } catch (Throwable e) {
                if (LogDebug.LOG) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        /**
         * Art虚拟机，引入AOT编译后，读取oat目录下当前正在使用的目录
         * TODO 目前仅支持arm
         *
         * @return
         */
        public static String getArtOatCpuType() {
            return VMRuntimeCompat.is64Bit() ? ARM64 : ARM;
        }
    }
}