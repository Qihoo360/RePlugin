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

package com.qihoo360.loader2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 插件框架内部接口
 *
 * @hide 内部框架使用
 * @author RePlugin Team
 */
public final class PluginBinderInfo implements Parcelable {

    public static final int NONE_REQUEST = 0;

    public static final int ACTIVITY_REQUEST = 1;

    public static final int SERVICE_REQUEST = 2;

    public static final int PROVIDER_REQUEST = 3;

    public static final int BINDER_REQUEST = 4;

    public int request;

    public int pid;

    public int index;

    public static final Parcelable.Creator<PluginBinderInfo> CREATOR = new Parcelable.Creator<PluginBinderInfo>() {

        @Override
        public PluginBinderInfo createFromParcel(Parcel source) {
            return new PluginBinderInfo(source);
        }

        @Override
        public PluginBinderInfo[] newArray(int size) {
            return new PluginBinderInfo[size];
        }
    };

    PluginBinderInfo() {
        request = NONE_REQUEST;
        pid = -1;
        index = -1;
    }

    // @hide
    public PluginBinderInfo(int req) {
        request = req;
        pid = -1;
        index = -1;
    }

    PluginBinderInfo(Parcel source) {
        readFromParcel(source);
    }

    final void readFromParcel(Parcel source) {
        request = source.readInt();
        pid = source.readInt();
        index = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(request);
        dest.writeInt(pid);
        dest.writeInt(index);
    }
}
