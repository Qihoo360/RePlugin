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

package com.qihoo360.replugin.sample.demo2.support;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author RePlugin Team
 */
public class LogX {

    private static final int THREAD_STR_LENGTH = 20;
    private static final int STACK_STR_LENGTH = 40;

    private static final String LOG_FORMATTER = "❖ %s %s ❖   %s";
    private static final String TAG_STRUCTURE = "v_structure";
    private static SimpleDateFormat sFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());


    public static void logDebug(String tag, String msg) {
        Log.d(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), msg));
    }

    public static void logDebug(String tag, String fmt, Object... obj) {
        Log.d(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), fmt), obj));
    }

    public static void logWarn(String tag, String msg) {
        Log.w(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), msg));
    }

    public static void logWarn(String tag, String fmt, Object... obj) {
        Log.w(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), fmt), obj));
    }

    public static void logInfo(String tag, String msg) {
        Log.i(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), msg));
    }

    public static void logInfo(String tag, String fmt, Object... obj) {
        Log.i(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), fmt), obj));
    }

    public static void logError(String tag, String msg) {
        Log.e(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), msg));
    }

    public static void logError(String tag, String fmt, Object... obj) {
        Log.e(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), fmt), obj));
    }

    /**
     * 调用栈信息
     * 只打印调用log的上一个栈的信息
     */
    private static String stackInfo(StackTraceElement[] traces) {
        String str = "";
        if (traces.length > 1 && traces[1] != null) {
            String fileName = traces[1].getFileName();
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                fileName = fileName.substring(0, index);
            }
            str = String.format("%s.%d.%s()", fileName, traces[1].getLineNumber(), traces[1].getMethodName());
        }
        return fixStringLength(str, STACK_STR_LENGTH);
    }

    /**
     * 线程信息
     */
    private static String threadName() {
        return fixStringLength(Thread.currentThread().getName()
                + " " + time(), THREAD_STR_LENGTH);
    }

    private static String time() {
        return sFormatter.format(new Date());
    }

    /**
     * 输出固定长度的字符串
     * <p/>
     * 不够被空格, 过长做trim()
     */
    private static String fixStringLength(String s, int targetLen) {
        if (s != null && targetLen > 0) {
            int len = s.length();
            if (len > targetLen) {
                return s.substring(0, targetLen);
            }

            StringBuilder sb = new StringBuilder(s);
            while (len < targetLen) {
                sb.append(" ");
                len++;
            }
            return sb.toString();
        }
        return "";
    }

    public static void showStackTrace(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            logDebug(tag, Log.getStackTraceString(new Throwable()));
        }
    }

    /**
     * 打印调用堆栈
     * <p/>
     * *
     */
    public static void printStackTrace() {
        printStackTrace(null);
    }

    public static void printStackTrace(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                sb.append(stackElements[i]);
                if (i != stackElements.length - 1) {
                    sb.append("\n\t");
                }
            }
            if (TextUtils.isEmpty(tag)) {
                System.out.println(sb);
            } else {
                logDebug(tag, sb.toString());
            }
        }
    }

    /**
     * Print View Structure(PVS): print hierarchy structure of view
     * <p/>
     * from parent and not show class's full name
     *
     * @param v view
     */
    public static void pvs(View v) {
        pvs(v, true, false);
    }

    /**
     * print hierarchy structure of view
     *
     * @param v            view
     * @param fromRoot     print view structure from root
     * @param showFullName show full name of class
     */
    public static void pvs(View v, boolean fromRoot, boolean showFullName) {
        if (v == null) {
            return;
        }

        ViewParent p;
        if (!(v instanceof ViewParent)) {
            p = v.getParent();
        } else {
            p = (ViewParent) v;
        }

        // get root
        if (fromRoot) {
            while (p.getParent() != null) {
                // can not get child from ViewRootImpl -> break here.
                if (p.getParent().getClass().getName().equals("android.view.ViewRootImpl")) {
                    break;
                }
                p = p.getParent();
            }
        }

        logDebug(TAG_STRUCTURE, "%s", showFullName ? p.getClass().getName() : p.getClass().getSimpleName());

        doPvs(p, "", showFullName, 1);
    }

    /**
     * print structure
     *
     * @param vg           view
     * @param pre          pre string from parent
     * @param showFullName show full class name
     * @param level        level of node
     */
    /* preview:
         node: (level, index_of_child, ClassName of View)
         DecorView
          └────(1, 0, ActionBarOverlayLayout)
                ├────(2, 0, FrameLayout)
                │     └────(3, 0, CanvasLayerView)
                ├────(2, 1, ActionBarContainer)
                │     ├────(3, 0, ActionBarView)               // mark1: not the last node of parent, add v-line to child's pre
                │     │     └────(4, 0, LinearLayout)          // mark2: the last node of parent, do not add v-line to child's pre
                │     │           ├────(5, 0, HomeView)
                │     │           │     ├────(6, 0, ImageView)
                │     │           │     └────(6, 1, ImageView)
                │     │           └────(5, 1, LinearLayout)
                │     │                 ├────(6, 0, TextView)
                │     │                 └────(6, 1, TextView)
                │     └────(3, 1, ActionBarContextView)
                └────(2, 2, ActionBarContainer)
     */
    private static void doPvs(ViewParent vg, String pre, boolean showFullName, int level) {
        if (!(vg instanceof ViewGroup)) {
            return;
        }

        ViewGroup group = (ViewGroup) vg;
        int count = group.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View v = group.getChildAt(i);
                String tmpStr = pre;

                // special symbol for the last node
                if (i == count - 1) {
                    tmpStr += "└───";
                } else {
                    tmpStr += "├───";
                }

                // print current node
                logDebug(TAG_STRUCTURE, "%s(%d, %d, %s)", tmpStr, level, i,
                        showFullName ? v.getClass().getName() : v.getClass().getSimpleName());

                String childPre = pre;
                // if current node is the last node, do not draw v-line
                // see mark2
                if (i == count - 1) {
                    childPre += "     ";
                } else {
                    childPre += "│    "; // see mark1
                }

                if (v instanceof ViewGroup) {
                    doPvs((ViewGroup) v, childPre, showFullName, level + 1);
                }
            }
        }
    }
}

