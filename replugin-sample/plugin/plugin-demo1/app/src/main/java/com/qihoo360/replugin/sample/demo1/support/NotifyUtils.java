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

package com.qihoo360.replugin.sample.demo1.support;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

/**
 * @author RePlugin Team
 */
public class NotifyUtils {

    public static final String TAG = "NotifyUtils";

    private static final int NOTIFICATION_ID = 360;

    public static final String ACTION_START_NOTIFY_UI = "com.qihoo360.replugin.sample.demo1.NOTIFTY";

    public static final String NOTIFY_KEY = "notify_key";

    public static void sendNotification(Context ctx) {
        if (ctx == null) {
            return;
        }

        try {
            String notiTitle = "来自Demo1插件的通知";
            String contentText = "此处添加文案";

            Notification.Builder builder = new Notification.Builder(ctx)
                    .setContentTitle(notiTitle)
                    .setContentText(contentText)
                    .setLargeIcon(getAppIcon(ctx));

            Notification notification;
            if (Build.VERSION.SDK_INT <= 15) {
                notification = builder.getNotification();
            } else {
                notification = builder.build();
            }

            if (notification == null) {
                Log.e(TAG, "notification is null, SDK_INT = " + Build.VERSION.SDK_INT);
                return;
            }

            notification.icon = 0x7f030002; // 注意此处是Host宿主的通知栏图标ID，需要宿主keep该资源ID
            notification.tickerText = notiTitle;
            notification.when = System.currentTimeMillis();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.contentIntent = getNotificationIntent(ctx);
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    private static Bitmap getAppIcon(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(ctx.getPackageName(), 0);
            return ((BitmapDrawable) info.loadIcon(pm)).getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PendingIntent getNotificationIntent(Context context) {
        Intent i = new Intent(ACTION_START_NOTIFY_UI);
        i.putExtra(NOTIFY_KEY, "来自Demo1插件的通知栏点击");
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
