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

package com.qihoo360.loader2.alc;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.RemoteException;

import com.qihoo360.replugin.helper.LogDebug;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;

/**
 * @author RePlugin Team
 */
public final class ActivityController {

    private static IActivityWatcher.Stub sStub;

    private static InvocationHandler sHandler;

    private static Map<?, ?> sActivityThreadActivities;

    private static Map<?, ?> sActivityThreadServices;

    private static ArrayList<WeakReference<Activity>> sActivities;

    /**
     * TODO 优化
     */
    private static IActivityUpdate sListener;

    /**
     *
     */
    public static final void init() {
        loadVar();
    }

    public static final void install(Application application) {
        if (Build.VERSION.SDK_INT < 14) {
            if (LOG) {
                LogDebug.d(MAIN_TAG, "install activity watcher");
            }
            install2x();
            return;
        }

        if (LOG) {
            LogDebug.d(MAIN_TAG, "install activity lifecycle callbacks");
        }
        install4x(application);
    }

    /**
     * TODO 优化
     */
    public static final void setListener(IActivityUpdate listener) {
        sListener = listener;
    }

    public static final int sumActivities() {
        int sum = -1;
        if (sActivities != null) {
            sum = sActivities.size();
        } else if (sActivityThreadActivities != null) {
            sum = sActivityThreadActivities.size();
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "process sumActivities = " + sum);
        }
        return sum;
    }

    private static final void install2x() {
        //
        sStub = new IActivityWatcher.Stub() {

            @Override
            public void closingSystemDialogs(String reason) throws RemoteException {
            }

            @Override
            public void activityResuming(int activityId) throws RemoteException {
                int activityCount = -1;
                if (sActivityThreadActivities != null) {
                    activityCount = sActivityThreadActivities.size();
                }
                int serviceCount = -1;
                if (sActivityThreadServices != null) {
                    serviceCount = sActivityThreadServices.size();
                }
                if (LOG) {
                    LogDebug.d(MAIN_TAG, "activityResuming: activities=" + activityCount + " services=" + serviceCount);
                }
                if (sListener != null) {
                    sListener.handleActivityUpdate();
                }
            }
        };

        Class<?> clsAMN = null;
        Class<?> clsIAW = null;
        try {
            clsAMN = Class.forName("android.app.ActivityManagerNative");
            clsIAW = Class.forName("android.app.IActivityWatcher");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "AMN=" + clsAMN + " IAW=" + clsIAW);
        }

        Method m1 = null;
        Method m2 = null;
        try {
            m1 = clsAMN.getDeclaredMethod("getDefault");
            m2 = clsAMN.getMethod("registerActivityWatcher", clsIAW);
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "getDefault=" + m1 + " registerActivityWatcher=" + m2);
        }

        Object oAMN = null;
        try {
            oAMN = m1.invoke(null);
            m2.invoke(oAMN, sStub);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "oAMN=" + oAMN);
        }
    }

    private static final void install4x(Application application) {
        //
        sHandler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("onActivityCreated".equals(method.getName())) {
                    if (args.length > 0 && args[0] instanceof Activity) {
                        Activity activity = (Activity) args[0];
                        WeakReference<Activity> ref = new WeakReference<Activity>(activity);
                        sActivities.add(ref);
                        if (LOG) {
                            LogDebug.d(MAIN_TAG, "onActivityCreated: a=" + activity + " total=" + sActivities.size());
                        }
                    }
                } else if ("onActivityDestroyed".equals(method.getName())) {
                    if (args.length > 0 && args[0] instanceof Activity) {
                        Activity activity = (Activity) args[0];
                        for (int index = sActivities.size() - 1; index >= 0; index--) {
                            Activity a = sActivities.get(index).get();
                            if (a == activity || a == null) {
                                sActivities.remove(index);
                            }
                        }
                        if (LOG) {
                            LogDebug.d(MAIN_TAG, "onActivityDestroyed: a=" + activity + " total=" + sActivities.size());
                        }
                        if (sListener != null) {
                            sListener.handleActivityUpdate();
                        }
                    }
                }
                return null;
            }
        };

        //
        sActivities = new ArrayList<WeakReference<Activity>>();

        Class<?> appClass = null;
        Class<?> cbClass = null;
        try {
            appClass = Class.forName("android.app.Application");
            cbClass = Class.forName("android.app.Application$ActivityLifecycleCallbacks");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "install activity lifecycle callbacks: class=" + cbClass);
        }

        Method m = null;
        try {
            m = appClass.getDeclaredMethod("registerActivityLifecycleCallbacks", cbClass);
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "install activity lifecycle callbacks: m=" + m);
        }

        Object cb  = Proxy.newProxyInstance(ActivityController.class.getClassLoader(), new Class<?>[]{cbClass}, sHandler);
        if (LOG) {
            LogDebug.d(MAIN_TAG, "install activity lifecycle callbacks: cb=" + cb);
        }

        try {
            m.invoke(application, cb);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "install activity lifecycle callbacks: ok");
        }
    }

    private static final void loadVar() {
        Class<?> clsAT = null;
        try {
            clsAT = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "clsAT=" + clsAT);
        }

        Method m0 = null;
        try {
            m0 = clsAT.getDeclaredMethod("currentActivityThread");
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "currentActivityThread=" + m0);
        }

        Object oAT = null;
        try {
            oAT = m0.invoke(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "oAT=" + oAT);
        }

        Field f1 = null;
        Field f2 = null;
        try {
            f1 = clsAT.getDeclaredField("mActivities");
            f2 = clsAT.getDeclaredField("mServices");
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "AT f1=" + f1 + " f2=" + f2);
        }

        Object o1 = null;
        Object o2 = null;
        try {
            f1.setAccessible(true);
            o1 = f1.get(oAT);
            f2.setAccessible(true);
            o2 = f2.get(oAT);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        if (LOG) {
            LogDebug.d(MAIN_TAG, "AT activities=" + o1 + " services=" + o2);
        }

        try {
            sActivityThreadActivities = (Map<?, ?>) o1;
            sActivityThreadServices = (Map<?, ?>) o2;
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        if (LOG) {
            LogDebug.d(MAIN_TAG, "converted: activities=" + sActivityThreadActivities + " services=" + sActivityThreadServices);
        }
    }

    public interface IActivityUpdate {

        void handleActivityUpdate();
    }
}
