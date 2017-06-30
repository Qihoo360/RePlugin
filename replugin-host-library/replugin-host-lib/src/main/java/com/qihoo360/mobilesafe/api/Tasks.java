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

package com.qihoo360.mobilesafe.api;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * 一些任务：需要运行在进程的UI主线程，或者运行在进程的HandlerThread
 *
 * @author RePlugin Team
 */
public final class Tasks {

    private static Handler sMainHandler;

    private static Object sLock = new Object();

    private static Handler sThreadHandler;

    /**
     * @param r
     * @return
     */
    public static final boolean post2UI(Runnable r) {
        return sMainHandler.post(r);
    }

    /**
     * @param r
     * @param delayMillis
     * @return
     */
    public static final boolean postDelayed2UI(Runnable r, long delayMillis) {
        return sMainHandler.postDelayed(r, delayMillis);
    }

    /**
     * 取消UI线程任务
     * @param r
     */
    public static final void cancelTask(Runnable r) {
        initThread();
        sMainHandler.removeCallbacks(r);
    }

    /**
     * @param r
     * @return
     */
    public static final boolean post2Thread(Runnable r) {
        initThread();
        return sThreadHandler.post(r);
    }

    /**
     * @param r
     * @param delayMillis
     * @return
     */
    public static final boolean postDelayed2Thread(Runnable r, long delayMillis) {
        initThread();
        return sThreadHandler.postDelayed(r, delayMillis);
    }

    /**
     * 取消后台线程任务
     * @param r
     */
    public static final void cancelThreadTask(Runnable r) {
        initThread();
        sThreadHandler.removeCallbacks(r);
    }

    /**
     * @hide 内部接口
     */
    public static final void init() {
        sMainHandler = new Handler(Looper.getMainLooper());
    }

    public static final void init(Handler handler) {
        sMainHandler = handler;
    }

    private static final void initThread() {
        synchronized (sLock) {
            if (sThreadHandler == null) {
                HandlerThread t = new HandlerThread("daemon-handler-thread");
                t.start();
                sThreadHandler = new Handler(t.getLooper());
            }
        }
    }
}
