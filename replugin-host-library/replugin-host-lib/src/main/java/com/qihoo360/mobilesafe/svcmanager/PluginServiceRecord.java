
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

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.loader2.MP;
import com.qihoo360.loader2.MP.PluginBinder;
import com.qihoo360.mobilesafe.core.BuildConfig;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 每对plugin name和service name的组合（保证唯一性）都对应一个PluginServiceRecord实例。
 * 此类提供了获取service的实现，并且在内部维护了一个请求过该服务的进程记录（ProcessRecord）的列表，
 * 当该列表为空时，通常是所有对此service的引用都已经释放了。
 *
 * @author RePlugin Team
 */
class PluginServiceRecord extends ReentrantLock {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "PluginServiceRecord" : PluginServiceRecord.class.getSimpleName();

    private static final long serialVersionUID = 1964598149985081920L;

    /**
     * 每个ProcessRecord实例对应一个进程对此service的引用记录，同时监视着进程的死亡事件。
     * 一个使用该service的进程可能有多份互不相干的引用，因此内部使用引用计数来实现。
     */
    class ProcessRecord implements IBinder.DeathRecipient {
        final int pid;

        final IBinder deathMonitor;

        private int refCount;

        private ProcessRecord(int pid, IBinder deathMonitor) {
            this.pid = pid;
            this.deathMonitor = deathMonitor;
            try {
                deathMonitor.linkToDeath(this, 0);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.d(TAG, "Error when linkToDeath: ");
                }
            }
            refCount = 1;
        }

        private int incrementRef() {
            return ++refCount;
        }

        private int decrementRef() {
            return --refCount;
        }

        @Override
        public void binderDied() {
            PluginServiceManager.onRefProcessDied(mPluginName, mServiceName, pid);
        }
    }

    final String mPluginName;

    final String mServiceName;

    PluginBinder mPluginBinder;

    // 多个进程同时使用一个plugin service的概率比较小，4个基本上足够，使用4为初始值以节省内存
    ArrayList<ProcessRecord> processRecords = new ArrayList<ProcessRecord>(4);

    PluginServiceRecord(String pluginName, String serviceName) {
        mPluginName = pluginName;
        mServiceName = serviceName;
    }

    IBinder getService(int pid, IBinder deathMonitor) {
        lock();

        try {
            if (mPluginBinder == null) {
                mPluginBinder = MP.fetchPluginBinder(mPluginName, IPluginManager.PROCESS_AUTO, mServiceName);
            }

            // MP.fetchPluginBinder有可能出现：目标进程被系统回收的情况，进而导致DeadObjectException的情况
            // 这样其方法的返回值自然就是Null了
            // Edited by Jiongxuan Zhang
            if (mPluginBinder == null) {
                return null;
            }
            addNewRecordInternal(pid, deathMonitor);
            return mPluginBinder.binder;
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Error getting plugin service: ", e);
            }
        } finally {
            unlock();
        }

        return null;
    }

    int decrementProcessRef(int pid) {
        lock();

        try {
            ProcessRecord record = getProcessRecordInternal(pid);
            if (record != null) {
                int processRefCount = record.decrementRef();
                if (processRefCount <= 0) {
                    processRecords.remove(record);
                }
            }

            if (DEBUG) {
                Log.d(TAG, "[decrementProcessRef] remaining ref count: " + getTotalRefCountInternal());
            }

            return getTotalRefCountInternal();
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Error decrement reference: ", e);
            }
        } finally {
            unlock();
        }

        return -1;
    }

    int refProcessDied(int pid) {
        lock();

        try {
            ProcessRecord record = getProcessRecordInternal(pid);
            if (record != null) {
                processRecords.remove(record);
            }

            return getTotalRefCountInternal();
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Error decrement reference: ", e);
            }
        } finally {
            unlock();
        }

        return -1;
    }

    boolean isServiceAlive() {
        return mPluginBinder != null && mPluginBinder.binder != null && mPluginBinder.binder.isBinderAlive() && mPluginBinder.binder.pingBinder();
    }

    // ==================================Internal=Methods===================================

    private void addNewRecordInternal(int pid, IBinder deathMonitor) {
        ProcessRecord record = getProcessRecordInternal(pid);
        if (record != null) {
            // Already exists
            record.incrementRef();
        } else {
            // New creation
            ProcessRecord pr = new ProcessRecord(pid, deathMonitor);
            processRecords.add(pr);
        }

        if (DEBUG) {
            Log.d(TAG, "[addNewRecordInternal] remaining ref count: " + getTotalRefCountInternal());
        }
    }

    private ProcessRecord getProcessRecordInternal(int pid) {
        for (ProcessRecord record : processRecords) {
            if (record.pid == pid) {
                return record;
            }
        }
        return null;
    }

    private int getTotalRefCountInternal() {
        int totalRefCount = 0;
        for (ProcessRecord pr : processRecords) {
            totalRefCount += pr.refCount;
        }
        return totalRefCount;
    }
}
