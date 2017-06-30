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

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 和线程有关的帮助类
 *
 * @author RePlugin Team
 */

public class ThreadUtils {

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    /**
     * 确保一定在主线程中使用
     * <p>
     * 若当前处于主线程，则直接调用。若当前处于其它线程，则Post到主线程后等待结果
     *
     * @param callable Callable对象
     * @param wait 最长等待主线程的时间
     * @param <T> 任何Object子类均可以
     * @return 主线程执行完方法后，返回的结果
     */
    public static <T> T syncToMainThread(final Callable<T> callable, int wait) throws Throwable {
        if (sHandler.getLooper() == Looper.myLooper()) {
            // 已在UI线程中使用，则直接调用它
            return callable.call();

        } else {
            // 不在UI线程，需尝试Post到UI线程并等待
            return syncToMainThreadByOthers(callable, wait);
        }
    }

    private static <T> T syncToMainThreadByOthers(final Callable<T> callable, int wait) throws Throwable {
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Throwable> ex = new AtomicReference<>();

        // 异步转同步
        final CountDownLatch latch = new CountDownLatch(1);

        // 必须在主线程进行
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    result.set(callable.call());
                } catch (Throwable e) {
                    ex.set(e);
                }
                latch.countDown();
            }
        });

        try {
            latch.await(wait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }

        // 若方法体有异常？直接抛出
        Throwable exo = ex.get();
        if (exo != null) {
            throw exo;
        }

        // 没有问题？则直接返回结果
        return result.get();
    }
}
