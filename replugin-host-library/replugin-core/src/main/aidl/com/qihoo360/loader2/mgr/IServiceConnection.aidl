// IServiceConnection.aidl
// Same as android.app.IServiceConnection
package com.qihoo360.loader2.mgr;

import android.content.ComponentName;

/** @hide */
oneway interface IServiceConnection {
    void connected(in ComponentName name, IBinder service);
}
