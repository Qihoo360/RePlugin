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

package com.qihoo360.replugin.base;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.Callable;

/**
 * 和LocalBroadcastManager有关的帮助类
 *
 * @author RePlugin Team
 * @see androidx.localbroadcastmanager.content.LocalBroadcastManager
 */

public class LocalBroadcastHelper {

    /**
     * 和LocalBroadcastManager.sendBroadcastSync类似，唯一的区别是执行所在线程：前者只在调用所在线程，本方法则在UI线程。
     * <p>
     * 可防止onReceiver在其它线程中被调用到。特别适用于AIDL、没有Looper的线程中调用此方法。
     *
     * @param intent 要发送的Intent信息
     * @see LocalBroadcastManager#sendBroadcastSync(Intent)
     */
    public static void sendBroadcastSyncUi(final Context context, final Intent intent) {
        try {
            ThreadUtils.syncToMainThread(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                    return null;
                }
            }, 10000);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
