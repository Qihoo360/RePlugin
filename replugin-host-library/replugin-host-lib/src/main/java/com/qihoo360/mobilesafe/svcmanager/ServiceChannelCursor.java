
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

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.IBinder;

/**
 * A Matrix Cursor implementation that a binder object is embedded in its extra
 * data.
 *
 * @author RePlugin Team
 */
/* PACKAGE */class ServiceChannelCursor extends MatrixCursor {

    public static final String SERVER_CHANNEL_BUNDLE_KEY = "servicechannel";

    /* PACKAGE */static final String[] DEFAULT_COLUMNS = {
        "s"
    };

    static final ServiceChannelCursor makeCursor(IBinder binder) {
        return new ServiceChannelCursor(DEFAULT_COLUMNS, binder);
    }

    static final IBinder getBinder(Cursor cursor) {
        Bundle bundle = cursor.getExtras();
        bundle.setClassLoader(ParcelBinder.class.getClassLoader());
        ParcelBinder parcelBinder = bundle.getParcelable(SERVER_CHANNEL_BUNDLE_KEY);
        return parcelBinder.getIbinder();
    }

    Bundle mBinderExtra = new Bundle();

    public ServiceChannelCursor(String[] columnNames, IBinder binder) {
        super(columnNames);

        mBinderExtra.putParcelable(SERVER_CHANNEL_BUNDLE_KEY, new ParcelBinder(binder));
    }

    @Override
    public Bundle getExtras() {
        return mBinderExtra;
    }
}
