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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 所有正在运行的插件列表
 *
 * @author RePlugin Team
 */

public class PluginRunningList implements Parcelable, Iterable<String>, Cloneable {

    private final ArrayList<String> mList;

    String mProcessName;
    int mPid = Integer.MIN_VALUE;

    PluginRunningList() {
        mList = new ArrayList<>();
    }

    PluginRunningList(PluginRunningList list) {
        // 复制一份，而不是复用以前的，避免外界影响到这里
        mProcessName = list.mProcessName;
        mPid = list.mPid;
        mList = new ArrayList<>(list.getList());
    }

    void setProcessInfo(String processName, int pid) {
        mProcessName = processName;
        mPid = pid;
    }

    void add(String s) {
        synchronized (this) {
            if (!isRunning(s)) {
                mList.add(s);
            }
        }
    }

    boolean isRunning(String pluginName) {
        return mList.contains(pluginName);
    }

    boolean hasRunning() {
        return !mList.isEmpty();
    }

    // 获取List内部对象。使用此方法时务必小心，确保外界不会影响这里后才能使用
    List<String> getList() {
        return mList;
    }

    @Override
    public Iterator<String> iterator() {
        return mList.iterator();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("PRunningL{ ");

        // 进程名+PID
        if (mPid == Integer.MIN_VALUE) {
            b.append("<UNKNOWN_PID>");
        } else {
            b.append('<');
            b.append(mProcessName);
            b.append(':');
            b.append(mPid);
            b.append("> ");
        }

        // 进程运行列表
        b.append(mList);
        b.append(" }");

        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginRunningList strings = (PluginRunningList) o;

        if (mPid != strings.mPid) return false;
        if (!mList.equals(strings.mList)) return false;

        return mProcessName != null ? mProcessName.equals(strings.mProcessName) : strings.mProcessName == null;
    }

    @Override
    public int hashCode() {
        int result = mList.hashCode();
        result = 31 * result + (mProcessName != null ? mProcessName.hashCode() : 0);
        result = 31 * result + mPid;
        return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new PluginRunningList(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProcessName);
        dest.writeInt(mPid);
        dest.writeSerializable(mList);
    }

    public static final Parcelable.Creator<PluginRunningList> CREATOR
            = new Parcelable.Creator<PluginRunningList>() {
        public PluginRunningList createFromParcel(Parcel in) {
            return new PluginRunningList(in);
        }

        public PluginRunningList[] newArray(int size) {
            return new PluginRunningList[size];
        }
    };

    private PluginRunningList(Parcel in) {
        mProcessName = in.readString();
        mPid = in.readInt();
        mList = (ArrayList<String>) in.readSerializable();
    }
}
