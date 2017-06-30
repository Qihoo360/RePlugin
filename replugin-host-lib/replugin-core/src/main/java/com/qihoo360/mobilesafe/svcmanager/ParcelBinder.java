
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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author RePlugin Team
 */
class ParcelBinder implements Parcelable {

    private final IBinder mBinder;

    private ParcelBinder(Parcel source) {
        mBinder = source.readStrongBinder();
    }

    public ParcelBinder(IBinder binder) {
        this.mBinder = binder;
    }

    public IBinder getIbinder() {
        return mBinder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mBinder);
    }

    public static final Parcelable.Creator<ParcelBinder> CREATOR = new Parcelable.Creator<ParcelBinder>() {

        @Override
        public ParcelBinder createFromParcel(Parcel source) {
            return new ParcelBinder(source);
        }

        @Override
        public ParcelBinder[] newArray(int size) {
            return new ParcelBinder[size];
        }

    };

}
