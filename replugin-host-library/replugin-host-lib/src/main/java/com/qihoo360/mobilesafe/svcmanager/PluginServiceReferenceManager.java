
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

package com.qihoo360.mobilesafe.svcmanager;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import com.qihoo360.mobilesafe.core.BuildConfig;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;

/**
 * 注意此类的对象仅存活于请求plugin service的进程内，其作用是监视请求来的远端service引用何时被释放， 使用到了幽灵引用。
 *
 * @author RePlugin Team
 */
class PluginServiceReferenceManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "PluginServiceReferenceManager" : PluginServiceReferenceManager.class.getSimpleName();

    private static class ServicePhantomRef extends PhantomReference<IBinder> {

        final String pluginName;

        final String serviceName;

        public ServicePhantomRef(String pluginName, String serviceName, IBinder r, ReferenceQueue<? super IBinder> q) {
            super(r, q);
            this.pluginName = pluginName;
            this.serviceName = serviceName;
        }
    }

    private static Context sAppContext = null;

    private static ArrayList<ServicePhantomRef> sRefList = new ArrayList<ServicePhantomRef>();

    private static ReferenceQueue<IBinder> sRefQueue = new ReferenceQueue<IBinder>();

    private static Thread sMonitorThread = null;

    /**
     * 一个新的plugin service引用被请求
     */
    static synchronized void onPluginServiceObtained(Context context, String pluginName, String serviceName, IBinder service) {
        sAppContext = context.getApplicationContext();

        synchronized (sRefList) {
            sRefList.add(new ServicePhantomRef(pluginName, serviceName, service, sRefQueue));
        }

        if (sMonitorThread == null) {
            startMonitoring();
        }
    }

    /**
     * 启动一个后台线程见识是否还有未被释放的service引用，一旦没有了线程会推出。
     */
    private static synchronized void startMonitoring() {
        sMonitorThread = new Thread() {
            @Override
            public void run() {
                boolean quit = false;
                while (!quit) {
                    synchronized (sRefList) {
                        int remainedQueueCount = sRefList.size();
                        if (remainedQueueCount > 0) {
                            ServicePhantomRef ref = (ServicePhantomRef) sRefQueue.poll();
                            while (ref != null) {
                                if (DEBUG) {
                                    Log.d(TAG, "Plugin service ref released: " + ref.serviceName);
                                }

                                sRefList.remove(ref);
                                remainedQueueCount--;

                                QihooServiceManager.onPluginServiceReleased(sAppContext, ref.pluginName, ref.serviceName);

                                ref = (ServicePhantomRef) sRefQueue.poll();
                            }
                        }

                        if (remainedQueueCount <= 0) {
                            sMonitorThread = null;
                            quit = true;
                        }
                    }

                    if (!quit) {
                        // 扫描间隔为5S
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            if (DEBUG) {
                                Log.d(TAG, "Thread sleeping interrupted: ", e);
                            }
                        }
                    }
                }

                if (DEBUG) {
                    Log.d(TAG, "sMonitorThread quits... ");
                }
            }
        };

        if (DEBUG) {
            Log.d(TAG, "Start monitoring...");
        }
        sMonitorThread.setPriority(Thread.NORM_PRIORITY);
        sMonitorThread.start();
    }
}
