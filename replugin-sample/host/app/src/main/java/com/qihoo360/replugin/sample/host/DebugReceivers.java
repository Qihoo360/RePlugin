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

package com.qihoo360.replugin.sample.host;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

/**
 * 用来接收一些Debug有关的Receivers，如快速安装插件等 <p>
 * 第三方App可直接Copy代码到您们工程中，以实现同样的逻辑 <p>
 * 注意：请不要用于【发布环境】，以免出现严重的安全事故
 *
 * @author RePlugin Team
 */

public class DebugReceivers {
    private static final String TAG = "DebugReceivers";

    // 安装"纯APK"插件
    // adb shell am broadcast -a com.qihoo360.replugin.sample.host.replugin.install -e path [Path_In_SDCard]
    private static final String ACTION_INSTALL =  BuildConfig.APPLICATION_ID + ".replugin.install";

    // 安装"p-n-"开头的插件
    // adb shell am broadcast -a com.qihoo360.replugin.sample.host.replugin.install_with_pn -e path [Path_In_SDCard]
    private static final String ACTION_INSTALL_WITH_PN = BuildConfig.APPLICATION_ID + ".replugin.install_with_pn";

    // 启动插件的Activity
    // adb shell am broadcast -a com.qihoo360.replugin.sample.host.replugin.start_activity -e plugin [Name] -e activity [Class]
    private static final String ACTION_START_ACTIVITY = BuildConfig.APPLICATION_ID + ".replugin.start_activity";

    private static BroadcastReceiver sDebugReceiver;

    // 注册一系列用于快速调试的广播
    public static void registerReceivers(Context context) {
        if (sDebugReceiver != null) {
            return;
        }
        sDebugReceiver = new DebugReceiver();
        IntentFilter itf = new IntentFilter();
        itf.addAction(ACTION_INSTALL);
        itf.addAction(ACTION_INSTALL_WITH_PN);
        itf.addAction(ACTION_START_ACTIVITY);
        context.registerReceiver(sDebugReceiver, itf);
    }

    static class DebugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (TextUtils.isEmpty(act)) {
                return;
            }
            // 只在常驻进程中处理Debug逻辑
            if (!RePlugin.isCurrentPersistentProcess()) {
                return;
            }

            switch (act) {
                case ACTION_INSTALL: {
                    String path = intent.getStringExtra("path");
                    String immediatelyText = intent.getStringExtra("immediately");
                    boolean immediately = false;
                    if (TextUtils.equals(immediatelyText, "true")) {
                        immediately = true;
                    }

                    onInstallByApk(path, immediately);
                    break;
                }
                case ACTION_INSTALL_WITH_PN: {
                    String path = intent.getStringExtra("path");
                    String immediatelyText = intent.getStringExtra("immediately");
                    boolean immediately = false;
                    if (TextUtils.equals(immediatelyText, "true")) {
                        immediately = true;
                    }

                    onInstallByPn(path, immediately);
                    break;
                }
                case ACTION_START_ACTIVITY: {
                    String plugin = intent.getStringExtra("plugin");
                    String activity = intent.getStringExtra("activity");

                    onStartActivity(context, plugin, activity);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        private void onInstallByApk(String path, boolean immediately) {
            onInstall(path, immediately);
        }

        private void onInstallByPn(String path, boolean imeediately) {
            path = RePlugin.convertToPnFile(path);
            if (TextUtils.isEmpty(path)) {
                Log.e(TAG, "onInstallByPn: Error! path=" + path);
                return;
            }
            onInstall(path, imeediately);
        }

        private void onInstall(String path, boolean immediately) {
            PluginInfo pi = RePlugin.install(path);
            // Okay
            if (pi != null) {
                Log.i(TAG, "onInstall: Install Success! inst=" + pi);
                Log.i(TAG, "onInstall: Install Success! cur=" + RePlugin.getPluginInfo(pi.getName()));

                if (immediately) {
                    if (RePlugin.preload(pi)) {
                        Log.i(TAG, "onInstall: Preload Success! pn=" + pi.getName());
                    } else {
                        Log.e(TAG, "onInstall: Preload Error! pn=" + pi.getName());
                    }
                }
            } else {
                Log.e(TAG, "onInstall: Install Error! path=" + path);
            }
        }

        private void onStartActivity(Context context, String plugin, String activity) {
            if (TextUtils.isEmpty(activity)) {
                // TODO 可能要用主界面，暂时不支持
                return;
            }

            RePlugin.startActivity(context, RePlugin.createIntent(plugin, activity));
        }
    }
}
