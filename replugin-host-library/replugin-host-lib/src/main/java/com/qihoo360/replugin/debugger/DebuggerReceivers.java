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

package com.qihoo360.replugin.debugger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

/**
 * 用来接收一些Debug有关的Receivers，如快速安装插件等 <p>
 *
 * @author RePlugin Team
 */

public class DebuggerReceivers {

    private static final String TAG = "DebugReceivers";


    private final String BR_LOGO = ".replugin";

    private final String BR_POSTFIX_INSTALL = ".install";
    private final String BR_POSTFIX_INSTALL_WITH_PN = ".install_with_pn";

    private final String BR_POSTFIX_UNINSTALL = ".uninstall";

    private final String BR_POSTFIX_ACTIVITY = ".start_activity";

    private final String PARAM_PATH = "path";

    private final String PARAM_IMMEDIATELY = "immediately";

    private final String PARAM_PLUGIN = "plugin";

    private final String PARAM_ACTIVITY = "activity";

    private String packageName;

    // 安装"纯APK"插件
    // 举例:adb shell am broadcast -a com.qihoo360.repluginapp.replugin.install -e path [Path_In_SDCard]
    private String actionInstall;

    // 卸载"纯APK"插件
    // 举例:adb shell am broadcast -a com.qihoo360.repluginapp.replugin.uninstall -e plugin [Name]
    private String actionUninstall;

    // 安装"p-n-"开头的插件
    // 举例:adb shell am broadcast -a com.qihoo360.repluginapp.replugin.install_with_pn -e path [Path_In_SDCard]
    private String actionInstallWithPN;

    // 启动插件的Activity,如果activity为空,则启动默认Activity
    // 举例:adb shell am broadcast -a com.qihoo360.repluginapp.replugin.start_activity -e plugin [Name] -e activity [Class]
    private String actionStartActivity;

    private BroadcastReceiver sDebugerReceiver;

    /**
     * 注册一系列用于快速调试的广播
     *
     * @param context
     * @return 是否注册成功
     */
    public boolean registerReceivers(Context context) {

        if (sDebugerReceiver != null) {
            return true;
        }

        if (null == context) {
            return false;
        }

        packageName = context.getPackageName();

        actionInstall = packageName + BR_LOGO + BR_POSTFIX_INSTALL;

        actionUninstall = packageName + BR_LOGO + BR_POSTFIX_UNINSTALL;

        actionInstallWithPN = packageName + BR_LOGO + BR_POSTFIX_INSTALL_WITH_PN;

        actionStartActivity = packageName + BR_LOGO + BR_POSTFIX_ACTIVITY;

        sDebugerReceiver = new DebugerReceiver();
        IntentFilter itf = new IntentFilter();
        itf.addAction(actionInstall);
        itf.addAction(actionUninstall);
        itf.addAction(actionInstallWithPN);
        itf.addAction(actionStartActivity);
        context.registerReceiver(sDebugerReceiver, itf);

        return true;
    }

    class DebugerReceiver extends BroadcastReceiver {


        /**
         * 安装"纯APK"插件
         *
         * @param context
         * @param intent
         * @return 执行是否成功
         */
        private boolean doActionInstall(final Context context, final Intent intent) {
            String path = intent.getStringExtra(PARAM_PATH);
            String immediatelyText = intent.getStringExtra(PARAM_IMMEDIATELY);
            boolean immediately = false;
            if (TextUtils.equals(immediatelyText, "true")) {
                immediately = true;
            }

            onInstallByApk(path, immediately);

            return true;
        }

        /**
         * 卸载"纯APK"插件
         *
         * @param context
         * @param intent
         * @return 执行是否成功
         */
        private boolean doActionUninstall(final Context context, final Intent intent) {

            String plugin = intent.getStringExtra(PARAM_PLUGIN);

            if (TextUtils.isEmpty(plugin)){
                return false;
            }

            return RePlugin.uninstall(plugin);
        }


        /**
         * 安装"p-n-"开头的插件
         *
         * @param context
         * @param intent
         * @return 执行是否成功
         */
        private boolean doActionInstallWithPN(final Context context, final Intent intent) {

            String path = intent.getStringExtra(PARAM_PATH);
            String immediatelyText = intent.getStringExtra(PARAM_IMMEDIATELY);
            boolean immediately = false;
            if (TextUtils.equals(immediatelyText, "true")) {
                immediately = true;
            }

            onInstallByPn(path, immediately);

            return true;
        }

        /**
         * @param context
         * @param intent
         * @return 执行是否成功
         */
        private boolean doActionStartActivity(final Context context, final Intent intent) {

            String plugin = intent.getStringExtra(PARAM_PLUGIN);

            if (TextUtils.isEmpty(plugin)) {
                return false;
            }

            String activity = intent.getStringExtra(PARAM_ACTIVITY);

            return onStartActivity(context, plugin, activity);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (TextUtils.isEmpty(act)) {
                return;
            }
            // 只在常驻进程中处理Debug逻辑
            if (!RePlugin.isCurrentPersistentProcess()) {
                //留待扩展其他功能,暂时直接返回
                return;
            }

            if (act.equals(actionInstall)) {
                doActionInstall(context, intent);
            } else if (act.equals(actionUninstall)) {
                doActionUninstall(context, intent);
            } else if (act.equals(actionInstallWithPN)) {
                doActionInstallWithPN(context, intent);
            } else if (act.equals(actionStartActivity)) {
                doActionStartActivity(context, intent);
            }

        }

        private boolean onInstallByApk(String path, boolean immediately) {
            return onInstall(path, immediately);
        }

        private boolean onInstallByPn(String path, boolean imeediately) {
            path = RePlugin.convertToPnFile(path);
            if (TextUtils.isEmpty(path)) {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "onInstallByPn: Error! path=" + path);
                }
                return false;
            }
            return onInstall(path, imeediately);
        }

        private boolean onInstall(String path, boolean immediately) {
            PluginInfo pi = RePlugin.install(path);

            // Okay
            if (pi != null) {
                if (LogDebug.LOG) {
                    LogDebug.i(TAG, "onInstall: Install Success! cur=" + RePlugin.getPluginInfo(pi.getName()));
                }

                if (immediately) {
                    if (RePlugin.preload(pi)) {
                        if (LogDebug.LOG) {
                            LogDebug.i(TAG, "onInstall: Preload Success! pn=" + pi.getName());
                        }
                        return true;
                    } else {
                        if (LogDebug.LOG) {
                            LogDebug.e(TAG, "onInstall: Preload Error! pn=" + pi.getName());
                        }
                    }
                }
            } else {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "onInstall: Install Error! path=" + path);
                }
            }
            return false;
        }

        private boolean onStartActivity(Context context, String plugin, String activity) {
            if (TextUtils.isEmpty(activity)) {
                //启动默认的activity
                Intent intent1 = new Intent(Intent.ACTION_MAIN);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return RePlugin.startActivity(context, intent1, plugin, null);
            }
            return RePlugin.startActivity(context, RePlugin.createIntent(plugin, activity));
        }
    }
}

