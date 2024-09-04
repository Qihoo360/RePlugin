package com.qihoo360.replugin.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;

import com.qihoo360.replugin.helper.LogDebug;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Description: Only fullscreen opaque activities can request orientation
 */
public class FixOTranslucentOrientation {
    private static final String TAG = "FixOTranslucentOri";
    public static void fix(Activity activity) {

        if (android.os.Build.VERSION.SDK_INT != 26){
            return;
        }

        try {
            int[] styleableRes = (int[]) ReflectUtils.getClass("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = activity.obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            boolean isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);

            LogDebug.d(TAG, "activity create before: " + activity.getClass().getName() + " isTranslucentOrFloating =" + isTranslucentOrFloating);

            if (!isTranslucentOrFloating) {
                return;
            }

            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(activity);
            o.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            field.setAccessible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

