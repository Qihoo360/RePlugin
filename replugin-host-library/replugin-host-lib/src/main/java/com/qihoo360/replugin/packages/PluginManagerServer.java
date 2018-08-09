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

package com.qihoo360.replugin.packages;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qihoo360.loader2.CertUtils;
import com.qihoo360.loader2.MP;
import com.qihoo360.loader2.PluginNativeLibsHelper;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginEventCallbacks;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.model.PluginInfoList;
import com.qihoo360.replugin.utils.FileUtils;
import com.qihoo360.replugin.utils.pkg.PackageFilesUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * 插件管理器。用来控制插件的安装、卸载、获取等。运行在常驻进程中 <p>
 * 补充：涉及到插件交互、运行机制有关的管理器，在IPluginHost中 <p>
 * TODO 待p-n型插件逐渐变少后，将涉及到存储等逻辑，从PmHostSvc中重构后移到这里 <p>
 * <p>
 * 注意：插件框架内部使用，外界请不要调用。
 *
 * @author RePlugin Team
 */
public class PluginManagerServer {

    private static final String TAG = "PluginManagerServer";
    /*** 非法类型的插件*/
    public static final int PLUGIN_TYPE_INVALID = 0;
    /*** PN类型的插件*/
    public static final int PLUGIN_TYPE_PN = 1;
    /*** APK类型的插件*/
    public static final int PLUGIN_TYPE_APK = 2;

    private static final byte[] LOCKER_PROCESS_KILLED = new byte[0];
    private static final byte[] LOCKER = new byte[0];

    private Context mContext;

    // 存储所有插件的信息
    // TODO 目前这里只存新插件信息，不做额外的处理。除此之外，在PmHostSvc和PmBase中存放着所有插件信息，将来会优化这里
    private PluginInfoList mList = new PluginInfoList();

    private Map<String, PluginRunningList> mProcess2PluginsMap = new ConcurrentHashMap<>();

    private IPluginManagerServer mStub;

    public PluginManagerServer(Context context) {
        mContext = context;
        mStub = new Stub();
    }

    public IPluginManagerServer getService() {
        return mStub;
    }

    /**
     * 若某个客户端进程（除常驻进程外的进程）被干掉时调用此方法
     *
     * @param processName 被干掉的进程名
     */
    public void onClientProcessKilled(String processName) {
        synchronized (LOCKER_PROCESS_KILLED) {
            mProcess2PluginsMap.remove(processName);

            if (LogDebug.LOG) {
                LogDebug.d(TAG, "onClientProcessKilled: Killed! process=" + processName + "; remains=" + mProcess2PluginsMap);
            }
        }
    }

    private List<PluginInfo> loadLocked() {
        if (!mList.load(mContext)) {
            return null;
        }

        // 执行“更新或删除Pending”插件，并返回结果
        return updateAllLocked();
    }

    private List<PluginInfo> updateAllLocked() {
        // 判断是否需要更新插件（只有未运行的才可以）
        updateAllIfNeeded();

        // TODO 扫描一下，看看文件在不在
        return mList.cloneList();
    }

    private PluginInfo installLocked(String path) {
        final boolean verifySignEnable = RePlugin.getConfig().getVerifySign();
        final int flags = verifySignEnable ? PackageManager.GET_META_DATA | PackageManager.GET_SIGNATURES : PackageManager.GET_META_DATA;

        // 1. 读取APK内容
        PackageInfo pi = mContext.getPackageManager().getPackageArchiveInfo(path, flags);
        if (pi == null) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "installLocked: Not a valid apk. path=" + path);
            }

            RePlugin.getConfig().getEventCallbacks().onInstallPluginFailed(path, RePluginEventCallbacks.InstallResult.READ_PKG_INFO_FAIL);
            return null;
        }

        // 2. 校验插件签名
        if (verifySignEnable) {
            if (!verifySignature(pi, path)) {
                return null;
            }
        }

        // 3. 解析出名字和三元组
        PluginInfo instPli = PluginInfo.parseFromPackageInfo(pi, path);
        if (LogDebug.LOG) {
            LogDebug.i(TAG, "installLocked: Info=" + instPli);
        }
        instPli.setType(PluginInfo.TYPE_NOT_INSTALL);

        // 若要安装的插件版本小于或等于当前版本，则安装失败
        // NOTE 绝大多数情况下，应该在调用RePlugin.install方法前，根据云端回传的信息来判断，以防止下载旧插件，浪费流量
        // NOTE 这里仅做双保险，或通过特殊渠道安装时会有用

        // 注意：这里必须用“非Clone过的”PluginInfo，因为要修改里面的内容
        PluginInfo curPli = MP.getPlugin(instPli.getName(), false);
        if (curPli != null) {
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "installLocked: Has installed plugin. current=" + curPli);
            }

            // 版本较老？直接返回
            final int checkResult = checkVersion(instPli, curPli);
            if (checkResult < 0) {
                RePlugin.getConfig().getEventCallbacks().onInstallPluginFailed(path, RePluginEventCallbacks.InstallResult.VERIFY_VER_FAIL);
                return null;
            } else if (checkResult == 0) {
                instPli.setIsPendingCover(true);
            }
        }

        // 4. 将合法的APK改名后，移动（或复制，见RePluginConfig.isMoveFileWhenInstalling）到新位置
        // 注意：不能和p-n的最终释放位置相同，因为管理方式不一样
        if (!copyOrMoveApk(path, instPli)) {
            RePlugin.getConfig().getEventCallbacks().onInstallPluginFailed(path, RePluginEventCallbacks.InstallResult.COPY_APK_FAIL);
            return null;
        }

        // 5. 从插件中释放 So 文件
        PluginNativeLibsHelper.install(instPli.getPath(), instPli.getNativeLibsDir());

        // 6. 若已经安装旧版本插件，则尝试更新插件信息，否则直接加入到列表中
        if (curPli != null) {
            updateOrLater(curPli, instPli);
        } else {
            mList.add(instPli);
        }

        // 7. 保存插件信息到文件中，下次可直接使用
        mList.save(mContext);

        return instPli;
    }

    private boolean verifySignature(PackageInfo pi, String path) {
        if (!CertUtils.isPluginSignatures(pi)) {
            if (LogDebug.LOG) {
                LogDebug.d(TAG, "verifySignature: invalid cert: " + " name=" + pi);
            }

            RePlugin.getConfig().getEventCallbacks().onInstallPluginFailed(path, RePluginEventCallbacks.InstallResult.VERIFY_SIGN_FAIL);
            return false;
        }
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "verifySignature: valid cert: " + " name=" + pi);
        }
        return true;
    }

    private int checkVersion(PluginInfo instPli, PluginInfo curPli) {
        // 支持插件同版本覆盖安装？
        // 若现在要安装的，与之前的版本相同，则覆盖掉之前的版本；
        if (instPli.getVersion() == curPli.getVersion() && getPluginType(instPli) == getPluginType(curPli)) {
            if (LogDebug.LOG) {
                LogDebug.d(TAG, "isSameVersion: same version. " +
                        "inst_ver=" + instPli.getVersion() + "; cur_ver=" + curPli.getVersion());
            }
            return 0;
        }

        // 若现在要安装的，比之前的版本还要旧，则忽略更新；
        if (instPli.getVersion() < curPli.getVersion()) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "checkVersion: Older than current, install fail. pn=" + curPli.getName() +
                        "; inst_ver=" + instPli.getVersion() + "; cur_ver=" + curPli.getVersion());
            }
            return -1;
        }

        // 已有“待更新版本”？
        // 若现在要安装的，比“待更新版本”还要旧，则也可以忽略
        PluginInfo curUpdatePli = curPli.getPendingUpdate();
        if (curUpdatePli != null && instPli.getVersion() < curUpdatePli.getVersion()) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "checkVersion: Older than updating plugin. Ignore. pn=" + curPli.getName() + "; " +
                        "cur_ver=" + curPli.getVersion() + "; old_ver=" + curUpdatePli.getVersion() + "; new_ver=" + instPli.getVersion());
            }
            return -1;
        }
        return 1;
    }

    private boolean copyOrMoveApk(String path, PluginInfo instPli) {
        File srcFile = new File(path);
        File newFile = instPli.getApkFile();

        // 插件已被释放过一次？通常“同版本覆盖安装”时，覆盖次数超过2次的会出现此问题
        // 此时，直接删除安装路径下的文件即可，这样就可以直接Move/Copy了
        if (newFile.exists()) {
            FileUtils.deleteQuietly(newFile);
        }

        // 将源APK文件移动/复制到安装路径下
        try {
            if (RePlugin.getConfig().isMoveFileWhenInstalling()) {
                FileUtils.moveFile(srcFile, newFile);
            } else {
                FileUtils.copyFile(srcFile, newFile);
            }
        } catch (IOException e) {
            if (LogRelease.LOGR) {
                LogRelease.e(TAG, "copyOrMoveApk: Copy/Move Failed! src=" + srcFile + "; dest=" + newFile, e);
            }
            return false;
        }

        instPli.setPath(newFile.getAbsolutePath());
        instPli.setType(PluginInfo.TYPE_EXTRACTED);
        return true;
    }

    private void updateOrLater(PluginInfo curPli, PluginInfo instPli) {
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "updateOrLater: Need update. pn=" + curPli.getName() +
                    "; cur_ver=" + curPli.getVersion() + "; update_ver=" + instPli.getVersion());
        }
        // 既然要更新到新的"纯APK"方案，自然需要把旧p-n的信息迁移到新列表中
        // FIXME 看看有没有别的兼容问题，尤其是和两个List表之间的
        if (curPli.isPnPlugin()) {
            mList.add(curPli);
        }

        // 已有“待更新版本”？
        PluginInfo curUpdatePli = curPli.getPendingUpdate();
        if (curUpdatePli != null) {
            updatePendingUpdate(curPli, instPli, curUpdatePli);

            // 由于"打算要更新"的前提是插件正在被运行，且下次重启时会清空这个信息，既然这次只是替换"打算要更新"的插件信息
            // 则不必再做后面诸如"插件是否存在"等判断，直接返回即可
            return;
        }

        // 正在运行？Later到下次使用时再释放。否则直接开始更新
        if (RePlugin.isPluginRunning(curPli.getName())) {
            if (LogDebug.LOG) {
                LogDebug.w(TAG, "updateOrLater: Plugin is running. Later. pn=" + curPli.getName());
            }
            if (instPli.getVersion() > curPli.getVersion() ||
                    instPli.getVersion() == curPli.getVersion() && getPluginType(instPli) != getPluginType(curPli)) {
                // 高版本升级
                curPli.setPendingUpdate(instPli);
                curPli.setPendingDelete(null);
                curPli.setPendingCover(null);
                if (LogDebug.LOG) {
                    LogDebug.w(TAG, "updateOrLater: Plugin need update high version. clear PendingDelete and PendingCover.");
                }
            } else if (instPli.getVersion() == curPli.getVersion()) {
                // 同版本覆盖
                curPli.setPendingCover(instPli);
                curPli.setPendingDelete(null);
                // 注意：并不需要对PendingUpdate信息做处理，因为此前的updatePendingUpdate方法时就已经返回了
                if (LogDebug.LOG) {
                    LogDebug.w(TAG, "updateOrLater: Plugin need update same version. clear PendingDelete.");
                }
            }

            // 设置其Parent为curPli，在PmBase.newPluginFound时会用到
            instPli.setParentInfo(curPli);
        } else {
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "updateOrLater: Not running. Update now! pn=" + curPli.getName());
            }
            updateNow(curPli, instPli);
        }
    }

    private static int getPluginType(PluginInfo pluginInfo) {
        return pluginInfo == null ? PLUGIN_TYPE_INVALID : pluginInfo.isPnPlugin() ? PLUGIN_TYPE_PN : PLUGIN_TYPE_APK;
    }

    private void updatePendingUpdate(PluginInfo curPli, PluginInfo instPli, PluginInfo curUpdatePli) {
        if (curUpdatePli.getVersion() < instPli.getVersion()) {
            // 现在的版本比之前"打算要更新"的版本还要新（形象的称之为“夹心层”），则删除掉该“夹心层”的版本，然后换成这个更新的
            // 例如：原插件版本为101，原本打算更新的是102，现在遇到了103，显然102的就变成了“夹心层”版本了，直接用103的更好
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "updatePendingUpdate: Found newer plugin, replace. pn=" + curPli.getName() + "; " +
                        "cur_ver=" + curPli.getVersion() + "; old_ver=" + curUpdatePli.getVersion() + "; new_ver=" + instPli.getVersion());
            }

            // 设置待更新版本至最大版本
            curPli.setPendingUpdate(instPli);

            // 删除“夹心层”插件文件
            try {
                FileUtils.forceDelete(new File(curUpdatePli.getPath()));
            } catch (IOException e) {
                if (LogRelease.LOGR) {
                    e.printStackTrace();
                }
            }
        } else {
            // 由于已经在installLocked中做过判断了，故不太可能走到这里。不过仍可以忽略
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "updatePendingUpdate: Older than updating plugin. But...");
            }
        }
    }

    private void updateAllIfNeeded() {
        // FIXME 务必保证sync方法被调用，否则有可能判断不准确
        int updateNum = 0;
        for (PluginInfo pi : mList) {
            if (updateIfNeeded(pi)) {
                updateNum++;
            }
        }

        if (LogDebug.LOG) {
            LogDebug.d(TAG, "updateAllIfNeeded: Updated " + updateNum + " plugins");
        }
        if (updateNum > 0) {
            mList.save(mContext);
        }
    }

    // NOTE 调用此方法后，务必最终调用sList.save()，不然会丢失改动
    private boolean updateIfNeeded(PluginInfo curInfo) {
        if (isPluginRunningLocked(curInfo.getName(), null)) {
            // 插件正在被使用，不能贸然升级或者卸载
            if (LogDebug.LOG) {
                LogDebug.w(TAG, "updateIfNeeded: Plugin is running. pn=" + curInfo.getName());
            }
            return false;
        }

        // 更新插件前，首先判断该插件是否需要卸载，若是则直接删除，不再做更新操作
        if (curInfo.isNeedUninstall()) {
            if (LogDebug.LOG) {
                LogDebug.d(TAG, "updateIfNeeded: delete plugin. pn=" + curInfo.getName());
            }

            // 移除插件及其已释放的Dex、Native库等文件并向各进程发送广播，同步更新
            return uninstallNow(curInfo.getPendingDelete());

        } else if (curInfo.isNeedUpdate()) {
            // 需要更新插件？那就直接来
            updateNow(curInfo, curInfo.getPendingUpdate());
            return true;
        } else if (curInfo.isNeedCover()) {
            updateNow(curInfo, curInfo.getPendingCover());
            return true;
        } else {
            // 既不需要删除也不需要更新
            if (LogDebug.LOG) {
                LogDebug.d(TAG, "updateIfNeeded: Not need to update. pn=" + curInfo.getName());
            }
            return false;
        }
    }

    private void updateNow(PluginInfo curInfo, PluginInfo newInfo) {
        final boolean covered = newInfo.getIsPendingCover();
        if (covered) {
            move(curInfo, newInfo);
        } else {
            // 删除旧版本插件，不管是不是p-n的，且清掉Dex和Native目录
            delete(curInfo);
        }

        newInfo.setType(PluginInfo.TYPE_EXTRACTED);
        if (LogDebug.LOG) {
            LogDebug.i(TAG, "updateNow: Update. pn=" + curInfo.getVersion() +
                    "; cur_ver=" + curInfo.getVersion() + "; update_ver=" + newInfo.getVersion());
        }

        if (covered) {
            curInfo.setPendingCover(null);
            newInfo.setIsPendingCover(false);
            //修改isPendingCover属性后必须同时修改json中的path路径
            newInfo.setPath(newInfo.getApkFile().getPath());
        } else {
            curInfo.update(newInfo);
            curInfo.setPendingUpdate(null);
        }
    }

    private void move(@NonNull PluginInfo curPi, @NonNull PluginInfo newPi) {
        if (LogDebug.LOG) {
            LogDebug.i(TAG, "move. curPi=" + curPi.getPath() + "; newPi=" + newPi.getPath());
        }
        try {
            FileUtils.copyFile(newPi.getApkFile(), curPi.getApkFile());

            if (newPi.getDexFile().exists()) {
                FileUtils.copyFile(newPi.getDexFile(), curPi.getDexFile());
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                FileUtils.copyDir(newPi.getExtraOdexDir(), curPi.getExtraOdexDir());
            }

            if (newPi.getNativeLibsDir().exists()) {
                FileUtils.copyDir(newPi.getNativeLibsDir(), curPi.getNativeLibsDir());
            }

        } catch (IOException e) {
            if (LogRelease.LOGR) {
                e.printStackTrace();
            }
        } finally {
            try {
                File parentDir = newPi.getApkFile().getParentFile();
                FileUtils.forceDelete(parentDir);
            } catch (IOException e) {
                if (LogRelease.LOGR) {
                    e.printStackTrace();
                }
            } catch (IllegalArgumentException e2) {
                if (LogRelease.LOGR) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void delete(@NonNull PluginInfo pi) {
        try {
            FileUtils.forceDelete(new File(pi.getPath()));
            FileUtils.forceDelete(pi.getDexFile());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                FileUtils.forceDelete(pi.getExtraOdexDir());
            }
            FileUtils.forceDelete(pi.getNativeLibsDir());
        } catch (IOException e) {
            if (LogRelease.LOGR) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e2) {
            if (LogRelease.LOGR) {
                e2.printStackTrace();
            }
        }
    }

    private void updateUsedLocked(String pluginName, boolean used) {
        PluginInfo pi = MP.getPlugin(pluginName, false);
        if (pi == null) {
            return;
        }

        // 1. 设置状态并保存
        pi.setIsUsed(used);
        mList.save(mContext);

        // 2. 给各进程发送广播，要求更新Used状态（同步）
        PluginInfoUpdater.updateIsUsed(RePluginInternal.getAppContext(), pluginName, used);
    }

    private boolean uninstallLocked(PluginInfo pi) {
        if (pi == null) {
            return false;
        }

        // 插件正在运行？ 记录“卸载状态”，推迟到到常驻进程重启的时执行卸载
        if (RePlugin.isPluginRunning(pi.getName())) {
            return uninstallLater(pi);
        }

        // 插件未在运行，直接卸载
        return uninstallNow(pi);
    }

    private boolean uninstallLater(PluginInfo info) {
        if (LOG) {
            LogDebug.d(TAG, "Is running. Uninstall later! pn=" + info.getName());
        }
        PluginInfo pi = MP.getPlugin(info.getName(), false);
        if (pi == null) {
            return false;
        }
        pi.setPendingDelete(info);

        // 保存插件卸载状态到文件中，下次可直接使用
        mList.save(mContext);
        return false;
    }

    private boolean uninstallNow(PluginInfo info) {
        if (LOG) {
            LogDebug.i(TAG, "Not running. Uninstall now! pn=" + info.getName());
        }

        // 1. 移除插件及其已释放的Dex、Native库等文件
        PackageFilesUtil.forceDelete(info);

        // 2. 保存插件信息到文件中
        mList.remove(info.getName());
        mList.save(mContext);

        return true;
    }

    private PluginRunningList getRunningPluginsLocked() {
        PluginRunningList l = new PluginRunningList();
        for (PluginRunningList ps : mProcess2PluginsMap.values()) {
            for (String p : ps) {
                if (!l.isRunning(p)) {
                    l.add(p);
                }
            }
        }
        return l;
    }

    private boolean isPluginRunningLocked(String pluginName, String process) {
        if (TextUtils.isEmpty(process)) {
            // 没有明确目标进程，只要找到了就返回
            for (PluginRunningList ps : mProcess2PluginsMap.values()) {
                if (ps.isRunning(pluginName)) {
                    return true;
                }
            }
        } else {
            // 有明确目标，则直接获取即可
            PluginRunningList ps = mProcess2PluginsMap.get(process);
            if (ps != null) {
                if (ps.isRunning(pluginName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void syncRunningPluginsLocked(PluginRunningList list) {
        // 复制一份List，这样无论是否为跨进程，都不会因客户端对List的修改而产生影响
        PluginRunningList newList = new PluginRunningList(list);
        mProcess2PluginsMap.put(list.mProcessName, newList);

        if (LogDebug.LOG) {
            LogDebug.d(TAG, "syncRunningPluginsLocked: Synced! pl=" + list + "; map=" + mProcess2PluginsMap);
        }
    }

    private void addToRunningPluginsLocked(String processName, int pid, String pluginName) {
        PluginRunningList l = mProcess2PluginsMap.get(processName);
        if (l == null) {
            l = new PluginRunningList();
            mProcess2PluginsMap.put(processName, l);
        }

        // 不管是从缓存中获取，还是新创建的，都应该重新“刷新”一下进程信息，再将其Add到表中
        l.setProcessInfo(processName, pid);
        l.add(pluginName);

        if (LogDebug.LOG) {
            LogDebug.d(TAG, "addToRunningPluginsLocked: Added! pl =" + l + "; map=" + mProcess2PluginsMap);
        }
    }

    private String[] getRunningProcessesByPluginLocked(String pluginName) {
        ArrayList<String> l = new ArrayList<>();
        for (PluginRunningList prl : mProcess2PluginsMap.values()) {
            if (prl.isRunning(pluginName)) {
                l.add(prl.mProcessName);
            }
        }
        return l.toArray(new String[0]);
    }

    private class Stub extends IPluginManagerServer.Stub {

        @Override
        public PluginInfo install(String path) throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.installLocked(path);
            }
        }

        @Override
        public List<PluginInfo> load() throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.loadLocked();
            }
        }

        @Override
        public List<PluginInfo> updateAll() throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.updateAllLocked();
            }
        }

        @Override
        public void updateUsed(String pluginName, boolean used) throws RemoteException {
            synchronized (LOCKER) {
                PluginManagerServer.this.updateUsedLocked(pluginName, used);
            }
        }

        @Override
        public boolean uninstall(PluginInfo info) throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.uninstallLocked(info);
            }
        }

        @Override
        public PluginRunningList getRunningPlugins() throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.getRunningPluginsLocked();
            }
        }

        @Override
        public boolean isPluginRunning(String pluginName, String process) throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.isPluginRunningLocked(pluginName, process);
            }
        }

        @Override
        public void syncRunningPlugins(PluginRunningList list) throws RemoteException {
            synchronized (LOCKER) {
                PluginManagerServer.this.syncRunningPluginsLocked(list);
            }
        }

        @Override
        public void addToRunningPlugins(String processName, int pid, String pluginName) throws RemoteException {
            synchronized (LOCKER) {
                PluginManagerServer.this.addToRunningPluginsLocked(processName, pid, pluginName);
            }
        }

        @Override
        public String[] getRunningProcessesByPlugin(String pluginName) throws RemoteException {
            synchronized (LOCKER) {
                return PluginManagerServer.this.getRunningProcessesByPluginLocked(pluginName);
            }
        }
    }
}
