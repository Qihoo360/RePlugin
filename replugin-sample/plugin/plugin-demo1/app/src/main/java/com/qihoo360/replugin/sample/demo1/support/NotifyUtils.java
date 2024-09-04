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
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

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

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String channelId = getSilenceChannelId(ctx);
                builder.setChannelId(channelId);
            }

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

            notification.icon = 0x7f0c0002; // 注意此处是Host宿主的通知栏图标ID，需要宿主keep该资源ID
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

    public static String getSilenceChannelId(Context context) {
        if (context == null) return null;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String id = context.getPackageName() + "_" + "silence_channel";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = nm.getNotificationChannel(id);
            if (channel == null) {
                CharSequence name = "channel_silent_notification";
                channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);

                channel.enableVibration(false);
                channel.setVibrationPattern(new long[]{0});
                channel.setSound(null, null);

                String groupId = getChannelGroupId(context);
                if (!TextUtils.isEmpty(groupId)) {
                    channel.setGroup(groupId);
                }

                nm.createNotificationChannel(channel);
            }
        }

        return id;
    }

    private static String getChannelGroupId(Context context) {
        if (context == null) return null;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String groupId = context.getPackageName() + "_" + "default_channel_group";
        NotificationChannelGroup group = null;

        List<NotificationChannelGroup> channelGroupsList = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelGroupsList = nm.getNotificationChannelGroups();

            if (channelGroupsList != null) {
                for (int i = channelGroupsList.size() - 1; i >= 0; i--) {
                    NotificationChannelGroup channelGroup = channelGroupsList.get(i);
                    if (channelGroup != null) {
                        String id = channelGroup.getId();
                        if (groupId.equals(id)) {
                            group = channelGroup;
                            break;
                        }
                    }
                }
            }
            if (group == null) {
                CharSequence name = "Replugin";

                group = new NotificationChannelGroup(groupId, name);
                nm.createNotificationChannelGroup(group);
            }
        }

        return groupId;
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

        PendingIntent mPendingPollIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPendingPollIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPendingPollIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mPendingPollIntent;
    }
}
