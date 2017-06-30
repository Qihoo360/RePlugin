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

package com.qihoo360.mobilesafe.api;

/**
 * @author RePlugin Team
 */
public class Intents {

    public static final String SCREEN_ON = "com.qihoo360.mobilesafe.api.SCREEN_ON";

    public static final String SCREEN_OFF = "com.qihoo360.mobilesafe.api.SCREEN_OFF";

    public static final String ACTIVITY_EVENT = "com.qihoo360.mobilesafe.api.ACTIVITY_EVENT";

    /**
     * 当应用被安装时会收到此通知
     * 该值有个Extra，参见 PACKAGE_KEY_INTENT的说明
     */
    public static final String PACKAGE_ADDED = "com.qihoo360.mobilesafe.api.PACKAGE_ADDED";

    /**
     * 当应用被卸载时会收到此通知
     * 该值有个Extra，参见 PACKAGE_KEY_INTENT的说明
     */
    public static final String PACKAGE_REMOVED = "com.qihoo360.mobilesafe.api.PACKAGE_REMOVED";

    /**
     * 当应用被修改时（包括安装、卸载、升级等）会收到此通知
     * 该值有个Extra，参见 PACKAGE_KEY_INTENT的说明
     */
    public static final String PACKAGE_ALL = "com.qihoo360.mobilesafe.api.PACKAGE_ALL";

    /**
     * 针对PACKAGE_*这三个事件，可直接通过intent.getParcelableExtra来获取系统的Intent
     * 进而获取到如intent.getData（包名信息）、Intent.EXTRA_REPLACING（是否升级的）等的值。
     *
     * @since 6.5.0
     */
    public static final String PACKAGE_KEY_INTENT = "intent";

    /**
     * TopActivity 改变
     * 这个Action不能随便修改, 因为插件也在使用.
     *
     * @since 6.5.0
     */
    public static final String ACTIVITY_CHANGE_EVENT = "com.qihoo360.mobilesafe.api.ACTIVITY_CHANGE_EVENT";

    /**
     * 修改 ActivityMonitor 轮询间隔
     *
     * @since 7.0.0
     */
    public static final String CHANGE_ACTIVITY_MONITOR_INTERVAL = "com.qihoo360.mobilesafe.api.CHANGE_ACTIVITY_MONITOR_INTERVAL";

    /**
     * 修改 ActivityMonitor 轮询间隔时, 传递过来的时间间隔, 单位毫秒
     *
     * @since 7.0.0
     */
    public static final String CHANGE_ACTIVITY_MONITOR_KEY_INTERVAL = "interval";

    /**
     * 手机内存占用 改变
     * 这个Action不能随便修改, 因为插件也在使用.
     */
    public static final String MEM_CHANGE_EVENT = "com.qihoo360.mobilesafe.api.MEM_CHANGE_EVENT";

    /**
     * 引导界面显示时间结束，可以进行权限弹窗的任务（用户点击完成，或者超过显示超过一定时间如10分钟，界面没在后台，特定机型）
     * 通过LocalBroadcast注册
     */
    public static final String PERM_TASK_ALLOWED = "com.qihoo360.mobilesafe.api.PERM_TASK_ALLOWED";
}
