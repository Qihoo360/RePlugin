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

package com.qihoo360.loader.utils;

import android.content.Context;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 进程锁
 *
 * @author RePlugin Team
 */
public final class ProcessLocker {

    private static final String TAG = LogDebug.PLUGIN_TAG;

    private final Context mContext;

    private FileOutputStream mFileOutputStream;

    private FileChannel mFileChannel;

    private FileLock mFileLock;

    private File mFile;

    /**
     * @param context
     * @param filename
     */
    public ProcessLocker(Context context, String filename) {
        mContext = context;
        try {
            mFile = new File(filename);
            mFileOutputStream = mContext.openFileOutput(filename, 0);
            if (mFileOutputStream != null) {
                mFileChannel = mFileOutputStream.getChannel();
            }
            if (mFileChannel == null) {
                if (LOGR) {
                    LogRelease.e(MAIN_TAG, "channel is null");
                }
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MAIN_TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 允许传递绝对路径
     *
     * @param context
     * @param dir
     * @param filename
     */
    public ProcessLocker(Context context, String dir, String filename) {
        mContext = context;
        try {
            mFile = new File(dir, filename);
            if (!mFile.exists()) {
                FileUtils.forceMkdirParent(mFile);
                mFile.createNewFile();
            }
            mFileOutputStream = new FileOutputStream(mFile, false);
            mFileChannel = mFileOutputStream.getChannel();
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MAIN_TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 查看文件是否已经被上锁
     *
     * @return
     */
    public final synchronized boolean isLocked() {
        boolean ret = tryLock();

        // 加锁成功说明文件还未被上锁
        // 在退出之前一定要进行unlock
        if (ret) {
            unlock();
        }

        return !ret;
    }

    /**
     * 加锁
     *
     * @return
     */
    public final synchronized boolean tryLock() {
        if (mFileChannel == null) {
            return false;
        }
        try {
            mFileLock = mFileChannel.tryLock();
            if (mFileLock != null) {
                return true;
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MAIN_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 加锁
     *
     * @param ms       毫秒
     * @param interval 间隔
     * @return
     */
    public final synchronized boolean tryLockTimeWait(int ms, int interval) {
        if (mFileChannel == null) {
            return false;
        }
        // 自动修正到最小值，避免死锁
        if (ms <= 0) {
            ms = 1;
        }
        if (interval <= 0) {
            interval = 1;
        }
        try {
            for (int i = 0; i < ms; i += interval) {
                try {
                    mFileLock = mFileChannel.tryLock();
                } catch (IOException e) {
                    // 获取锁失败会抛异常，此处忽略
                    // java.io.IOException: fcntl failed: EAGAIN (Try again)
                }
                if (mFileLock != null) {
                    return true;
                }
                // 每秒钟输出一次日志，防止“刷屏”
                if (LOGR) {
                    if (i % 1000 == 0) {
                        LogRelease.i(TAG, "wait process lock: " + i + "/" + ms);
                    }
                }
                Thread.sleep(interval, 0);
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MAIN_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 加锁
     *
     * @return
     */
    public final synchronized boolean lock() {
        if (mFileChannel == null) {
            return false;
        }
        try {
            mFileLock = mFileChannel.lock();
            if (mFileLock != null) {
                return true;
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(MAIN_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 释放并且删除该锁文件
     */
    public final synchronized void unlock() {
        if (mFileLock != null) {
            try {
                mFileLock.release();
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(TAG, e.getMessage(), e);
                }
            }
        }
        if (mFileChannel != null) {
            try {
                mFileChannel.close();
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(TAG, e.getMessage(), e);
                }
            }
        }
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.close();
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(TAG, e.getMessage(), e);
                }
            }
        }

        // 删除锁文件
        if (mFile != null && mFile.exists()) {
            mFile.delete();
        }
    }
}
