package com.qihoo360.loader.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.qihoo360.replugin.utils.ReflectUtils;

public class LocalBroadcastManager {

    private static String V4_MANAGER = "android.support.v4.content.LocalBroadcastManager";
    private static String ANDROIDX_MANAGER = "androidx.localbroadcastmanager.content.LocalBroadcastManager";
    private static LocalBroadcastManager instance;

    private Context context;
    private boolean init;
    private static Object managerObj;

    public static LocalBroadcastManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LocalBroadcastManager.class) {
                if (instance == null) {
                    instance = new LocalBroadcastManager(context);
                }
            }
        }
        return instance;
    }

    private LocalBroadcastManager(Context context) {
        this.context = context.getApplicationContext();
        loadClass();
    }

    private void loadClass() {
        try {
            Class cls = null;
            try {
                cls = ReflectUtils.getClass(ANDROIDX_MANAGER);
            } catch (Exception e) {
                cls = ReflectUtils.getClass(V4_MANAGER);
            }
            if (cls == null) {
                return;
            }
            managerObj = ReflectUtils.getMethod(cls, "getInstance", new Class<?>[]{Context.class}).invoke(null, context);
            init = true;
        } catch (Exception e) {
        }
    }

    public boolean registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (!init || managerObj == null) {
            return false;
        }
        try {
            ReflectUtils.getMethod(managerObj.getClass(), "registerReceiver",
                    BroadcastReceiver.class, IntentFilter.class).invoke(managerObj, receiver, filter);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean unregisterReceiver(BroadcastReceiver receiver) {
        if (!init || managerObj == null) {
            return false;
        }
        try {
            ReflectUtils.getMethod(managerObj.getClass(), "unregisterReceiver",
                    BroadcastReceiver.class).invoke(managerObj, receiver);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean sendBroadcast(Intent intent) {
        if (!init || managerObj == null) {
            return false;
        }
        try {
            ReflectUtils.getMethod(managerObj.getClass(), "sendBroadcast",
                    Intent.class).invoke(managerObj, intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean sendBroadcastSync(Intent intent) {
        if (!init || managerObj == null) {
            return false;
        }
        try {
            ReflectUtils.getMethod(managerObj.getClass(), "sendBroadcastSync",
                    Intent.class).invoke(managerObj, intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }
}
