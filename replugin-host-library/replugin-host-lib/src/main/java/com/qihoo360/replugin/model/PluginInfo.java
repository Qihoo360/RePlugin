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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qihoo360.loader2.Constant;
import com.qihoo360.loader2.PluginNativeLibsHelper;
import com.qihoo360.loader2.V5FileInfo;
import com.qihoo360.loader2.VMRuntimeCompat;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.JSONHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;


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
            low = Constant.ADAPTER_COMPATIBLE_VERSION;
        }
        if (high <= 0) {
            high = Constant.ADAPTER_COMPATIBLE_VERSION;
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
     * 通过插件APK的MetaData来初始化PluginInfo <p>
     * 注意：框架内部接口，外界请不要直接使用
     */
    public static PluginInfo parseFromPackageInfo(PackageInfo pi, String path) {
        ApplicationInfo ai = pi.applicationInfo;
        String pn = pi.packageName;
        String alias = null;
        int low = 0;
        int high = 0;
        int ver = 0;

        Bundle metaData = ai.metaData;

        // 优先读取MetaData中的内容（如有），并覆盖上面的默认值
        if (metaData != null) {
            // 获取插件别名（如有），如无则将"包名"当做插件名
            alias = metaData.getString("com.qihoo360.plugin.name");

            // 获取最低/最高协议版本（默认为应用的最小支持版本，以保证一定能在宿主中运行）
            low = metaData.getInt("com.qihoo360.plugin.version.low");
            high = metaData.getInt("com.qihoo360.plugin.version.high");

            // 获取插件的版本号。优先从metaData中读取，如无则使用插件的VersionCode
            ver = metaData.getInt("com.qihoo360.plugin.version.ver");
        }

        // 针对有问题的字段做除错处理
        if (low <= 0) {
            low = Constant.ADAPTER_COMPATIBLE_VERSION;
        }
        if (high <= 0) {
            high = Constant.ADAPTER_COMPATIBLE_VERSION;
        }
        if (ver <= 0) {
            ver = pi.versionCode;
        }

        PluginInfo pli = new PluginInfo(pn, alias, low, high, ver, path, PluginInfo.TYPE_NOT_INSTALL);

        // 获取插件的框架版本号
        pli.setFrameworkVersionByMeta(metaData);

        return pli;
    }

    /**
     * （框架内部接口）通过传入的JSON的字符串来创建PluginInfo对象 <p>
     * 注意：框架内部接口，外界请不要直接使用
     */
    public static PluginInfo parseFromJsonText(String joText) {
        JSONObject jo;
        try {
            jo = new JSONObject(joText);
        } catch (JSONException e) {
            if (LOG) {
                e.printStackTrace();
            }
            return null;
        }

        // 三个字段是必备的，其余均可
        if (jo.has("pkgname") && jo.has("type") && jo.has("ver")) {
            return new PluginInfo(jo);
        } else {
            return null;
        }
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
        return f.exists() && FileUtils.sizeOf(f) > 0;
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
        Context context = RePluginInternal.getAppContext();
        File dir;
        if (isPnPlugin()) {
            dir = context.getDir(Constant.LOCAL_PLUGIN_SUB_DIR, 0);
        } else if (getIsPendingCover()) {
            dir = context.getDir(Constant.LOCAL_PLUGIN_APK_COVER_DIR, 0);
        } else {
            dir = context.getDir(Constant.LOCAL_PLUGIN_APK_SUB_DIR, 0);
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
        return getDexDir(getDexParentDir(), Constant.LOCAL_PLUGIN_INDEPENDENT_EXTRA_DEX_SUB_DIR);
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
        return getDexDir(getDexParentDir(), Constant.LOCAL_PLUGIN_INDEPENDENT_EXTRA_ODEX_SUB_DIR);
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
        Context context = RePluginInternal.getAppContext();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            return new File(getApkDir() + File.separator + "oat" + File.separator + VMRuntimeCompat.getArtOatCpuType());
        } else {
            if (isPnPlugin()) {
                return context.getDir(Constant.LOCAL_PLUGIN_ODEX_SUB_DIR, 0);
            } else if (getIsPendingCover()) {
                return context.getDir(Constant.LOCAL_PLUGIN_APK_COVER_DIR, 0);
            } else {
                return context.getDir(Constant.LOCAL_PLUGIN_APK_ODEX_SUB_DIR, 0);
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
        Context context = RePluginInternal.getAppContext();
        File dir;
        if (isPnPlugin()) {
            dir = context.getDir(Constant.LOCAL_PLUGIN_DATA_LIB_DIR, 0);
        } else if (getIsPendingCover()) {
            dir = context.getDir(Constant.LOCAL_PLUGIN_APK_COVER_DIR, 0);
        } else {
            dir = context.getDir(Constant.LOCAL_PLUGIN_APK_LIB_DIR, 0);
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
     * 设置插件的更新信息。此信息有可能等到下次才能被使用 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param info 插件的更新信息
     */
    public void setPendingUpdate(PluginInfo info) {
        mPendingUpdate = info;
        if (info != null) {
            JSONHelper.putNoThrows(mJson, "upinfo", info.getJSON());
        } else {
            mJson.remove("upinfo");
        }
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
     * 设置插件的卸载信息。此信息有可能等到下次才能被使用 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param info 插件的卸载信息
     */
    public void setPendingDelete(PluginInfo info) {
        mPendingDelete = info;
        if (info != null) {
            JSONHelper.putNoThrows(mJson, "delinfo", info.getJSON());
        } else {
            mJson.remove("delinfo");
        }
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
     * 设置插件的覆盖更新信息。此信息有可能等到下次才能被使用 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param info 插件覆盖安装信息
     */
    public void setPendingCover(PluginInfo info) {
        mPendingCover = info;
        if (info != null) {
            JSONHelper.putNoThrows(mJson, "coverinfo", info.getJSON());
        } else {
            mJson.remove("coverinfo");
        }
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
     * 设置PluginInfo的同版本覆盖的字段 <p>
     * 注意：仅框架内部使用
     */
    public void setIsPendingCover(boolean coverInfo) {
        mIsPendingCover = coverInfo;
        if (mIsPendingCover) {
            JSONHelper.putNoThrows(mJson, "cover", mIsPendingCover);
        } else {
            mJson.remove("cover");
        }
    }

    /**
     * 获取最小支持宿主API的版本
     */
    public int getLowInterfaceApi() {
        return mJson.optInt("low", Constant.ADAPTER_COMPATIBLE_VERSION);
    }

    /**
     * 获取最高支持宿主API的版本
     *
     * @deprecated 可能会废弃
     */
    public int getHighInterfaceApi() {
        return mJson.optInt("high", Constant.ADAPTER_COMPATIBLE_VERSION);
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
     * 根据MetaData来设置框架版本号 <p>
     * 注意：若为“纯APK”方案所用，则修改后需调用PluginInfoList.save来保存，否则会无效
     *
     * @param meta MetaData数据
     */
    public void setFrameworkVersionByMeta(Bundle meta) {
        int dfv = RePlugin.getConfig().getDefaultFrameworkVersion();
        int frameVer = 0;
        if (meta != null) {
            frameVer = meta.getInt("com.qihoo360.framework.ver", dfv);
        }
        if (frameVer < 1) {
            frameVer = dfv;
        }
        setFrameworkVersion(frameVer);
    }

    // @hide
    public JSONObject getJSON() {
        return mJson;
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
     * 更新插件信息。通常是在安装完新插件后调用此方法 <p>
     * 只更新一些必要的方法，如插件版本、路径、时间等。
     *
     * @param info 新版本插件信息
     */
    public void update(PluginInfo info) {
        // TODO low high
        setVersion(info.getVersion());
        setPath(info.getPath());
        setType(info.getType());
        setPackageName(info.getPackageName());
        setAlias(info.getAlias());
    }

    /**
     * 若此Info为“新PluginInfo”，则这里返回的是“其父Info”的内容。通常和PendingUpdate有关
     *
     * @return 父PluginInfo
     */
    public PluginInfo getParentInfo() {
        return mParentInfo;
    }

    // @hide
    public void setParentInfo(PluginInfo parent) {
        mParentInfo = parent;
    }

    static PluginInfo createByJO(JSONObject jo) {
        PluginInfo pi = new PluginInfo(jo);
        // 必须有包名或别名
        if (TextUtils.isEmpty(pi.getName())) {
            return null;
        }

        return pi;
    }

    private void setPackageName(String pkgName) {
        if (!TextUtils.equals(pkgName, getPackageName())) {
            JSONHelper.putNoThrows(mJson, "pkgname", pkgName);
        }
    }

    private void setAlias(String alias) {
        if (!TextUtils.equals(alias, getAlias())) {
            JSONHelper.putNoThrows(mJson, "ali", alias);
        }
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
        synchronized (this) {
            StringBuilder b = new StringBuilder();

            b.append("PInfo { ");
            toContentString(b);
            b.append(" }");

            return b.toString();
        }
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


    // -------------------------
    // FIXME 兼容老的P-n插件
    // -------------------------

    public static final String QUERY_COLUMNS[] = {
            "name", "low", "high", "ver", "type", "v5type", "path", "v5index", "v5offset", "v5length", "v5md5"
    };

    private static final Pattern REGEX;

    static {
        REGEX = Pattern.compile(Constant.LOCAL_PLUGIN_FILE_PATTERN);
    }

    /**
     *
     */
    public static final Comparator<PluginInfo> VERSION_COMPARATOR = new Comparator<PluginInfo>() {

        @Override
        public int compare(PluginInfo lhs, PluginInfo rhs) {
            long diff = lhs.getVersionValue() - rhs.getVersionValue();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            }
            return 0;
        }
    };

    public static final String format(String name, int low, int high, int ver) {
        return name + "-" + low + "-" + high + "-" + ver;
    }

    public static final PluginInfo build(File f) {
        Matcher m = REGEX.matcher(f.getName());
        if (m == null || !m.matches()) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginInfo.build: skip, no match1, file=" + f.getAbsolutePath());
            }
            return null;
        }
        MatchResult r = m.toMatchResult();
        if (r == null || r.groupCount() != 4) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginInfo.build: skip, no match2, file=" + f.getAbsolutePath());
            }
            return null;
        }
        String name = r.group(1);
        int low = Integer.parseInt(r.group(2));
        int high = Integer.parseInt(r.group(3));
        int ver = Integer.parseInt(r.group(4));
        String path = f.getPath();
        PluginInfo info = new PluginInfo(name, low, high, ver, TYPE_PN_INSTALLED, V5FileInfo.NONE_PLUGIN, path, -1, -1, -1, null);
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PluginInfo.build: found plugin, name=" + info.getName()
                    + " low=" + info.getLowInterfaceApi() + " high=" + info.getHighInterfaceApi()
                    + " ver=" + info.getVersion());
        }
        return info;
    }

    public static final PluginInfo buildFromBuiltInJson(JSONObject jo) {
        String pkgName = jo.optString("pkg");
        String name = jo.optString("name");
        String assetName = jo.optString("path");
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(assetName)) {
            if (LogDebug.LOG) {
                LogDebug.d(TAG, "buildFromBuiltInJson: Invalid json. j=" + jo);
            }
            return null;
        }
        int low = jo.optInt("low", Constant.ADAPTER_COMPATIBLE_VERSION);    // Low应指向最低兼容版本
        int high = jo.optInt("high", Constant.ADAPTER_COMPATIBLE_VERSION);  // High同上
        int ver = jo.optInt("ver");
        PluginInfo info = new PluginInfo(pkgName, name, low, high, ver, assetName, TYPE_BUILTIN);

        // 从 json 中读取 frameVersion（可选）
        int frameVer = jo.optInt("frm");
        if (frameVer < 1) {
            frameVer = RePlugin.getConfig().getDefaultFrameworkVersion();
        }
        info.setFrameworkVersion(frameVer);

        return info;
    }

    public static final PluginInfo buildV5(String name, int low, int high, int ver, int v5Type, String v5Path, int v5index, int v5offset, int v5length, String v5md5) {
        return new PluginInfo(name, low, high, ver, TYPE_PN_JAR, v5Type, v5Path, v5index, v5offset, v5length, v5md5);
    }

    public static final PluginInfo build(Cursor cursor) {
        String name = cursor.getString(0);
        int v1 = cursor.getInt(1);
        int v2 = cursor.getInt(2);
        int v3 = cursor.getInt(3);
        int type = cursor.getInt(4);
        int v5Type = cursor.getInt(5);
        String path = cursor.getString(6);
        int v5index = cursor.getInt(7);
        int v5offset = cursor.getInt(8);
        int v5length = cursor.getInt(9);
        String v5md5 = cursor.getString(10);
        return new PluginInfo(name, v1, v2, v3, type, v5Type, path, v5index, v5offset, v5length, v5md5);
    }

    public static final PluginInfo build(String name, int low, int high, int ver) {
        return new PluginInfo(name, low, high, ver);
    }

    // Old Version
    private PluginInfo(String name, int low, int high, int ver, int type, int v5Type, String path, int v5index, int v5offset, int v5length, String v5md5) {
        this(name, name, low, high, ver, path, type);

        JSONHelper.putNoThrows(mJson, "v5type", v5Type);
        JSONHelper.putNoThrows(mJson, "v5index", v5index);
        JSONHelper.putNoThrows(mJson, "v5offset", v5offset);
        JSONHelper.putNoThrows(mJson, "v5length", v5length);
        JSONHelper.putNoThrows(mJson, "v5md5", v5md5);
    }

    private String formatName() {
        return format(getName(), getLowInterfaceApi(), getHighInterfaceApi(), getVersion());
    }

    final void to(MatrixCursor cursor) {
        cursor.newRow().add(getName()).add(getLowInterfaceApi()).add(getHighInterfaceApi())
                .add(getVersion()).add(getType()).add(getV5Type()).add(getPath())
                .add(getV5Index()).add(getV5Offset()).add(getV5Length()).add(getV5MD5());
    }

    public final void to(Intent intent) {
        intent.putExtra("name", getName());
        intent.putExtra("low", getLowInterfaceApi());
        intent.putExtra("high", getHighInterfaceApi());
        intent.putExtra("ver", getVersion());
        intent.putExtra("type", getType());
        intent.putExtra("v5type", getV5Type());
        intent.putExtra("path", getPath());
        intent.putExtra("v5index", getV5Index());
        intent.putExtra("v5offset", getV5Offset());
        intent.putExtra("v5length", getV5Length());
        intent.putExtra("v5md5", getV5MD5());
    }

    public final boolean deleteObsolote(Context context) {
        if (getType() != TYPE_PN_INSTALLED) {
            return true;
        }
        if (TextUtils.isEmpty(getPath())) {
            return true;
        }
        boolean rc = new File(getPath()).delete();

        // 同时清除旧的SO库文件
        // Added by Jiongxuan Zhang
        File libDir = getNativeLibsDir();
        PluginNativeLibsHelper.clear(libDir);
        return rc;
    }

    /**
     * 判断是否可以替换（将NOT_INSTALLED变为INSTALLED） <p>
     * 条件：目前是BUILT_IN或NOT_INSTALLED，插件基本信息相同
     *
     * @param info 要替换的Info对象
     * @return 是否可以替换
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public final boolean canReplaceForPn(PluginInfo info) {
        if (getType() != TYPE_PN_INSTALLED
                && info.getType() == TYPE_PN_INSTALLED
                && getName().equals(info.getName())
                && getLowInterfaceApi() == info.getLowInterfaceApi()
                && getHighInterfaceApi() == info.getHighInterfaceApi()
                && getVersion() == info.getVersion()) {
            return true;
        }
        return false;
    }

    public final boolean match() {

        boolean isBlocked = RePlugin.getConfig().getCallbacks().isPluginBlocked(this);

        if (LOG) {
            if (isBlocked) {
                LogDebug.d(PLUGIN_TAG, "match result: plugin is blocked");
            }
        }

        return !isBlocked;
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

    /**
     * V5类型
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public int getV5Type() {
        return mJson.optInt("v5type", V5FileInfo.NONE_PLUGIN);
    }

    /**
     * V5类型：复合插件文件索引
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public int getV5Index() {
        return mJson.optInt("v5index", -1);
    }

    /**
     * V5类型：复合插件文件偏移
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public int getV5Offset() {
        return mJson.optInt("v5offset", -1);
    }

    /**
     * V5类型：复合插件文件长度
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public int getV5Length() {
        return mJson.optInt("v5length", -1);
    }

    /**
     * V5类型：复合插件文件MD5
     *
     * @deprecated 只用于旧的P-n插件，可能会废弃
     */
    public String getV5MD5() {
        return mJson.optString("v5md5");
    }

}
