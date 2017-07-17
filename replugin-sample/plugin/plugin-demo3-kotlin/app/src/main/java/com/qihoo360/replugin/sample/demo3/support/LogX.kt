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

package com.qihoo360.replugin.sample.demo3.support

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author RePlugin Team
 */
object LogX {

    private val THREAD_STR_LENGTH = 20
    private val STACK_STR_LENGTH = 40

    private val LOG_FORMATTER = "❖ %s %s ❖   %s"
    private val TAG_STRUCTURE = "v_structure"
    private val sFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun logDebug(tag: String?, msg: String) {
        Log.d(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), msg))
    }

    fun logDebug(tag: String?, fmt: String, vararg obj: Any) {
        Log.d(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), fmt), *obj))
    }

    fun logWarn(tag: String, msg: String) {
        Log.w(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), msg))
    }

    fun logWarn(tag: String, fmt: String, vararg obj: Any) {
        Log.w(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), fmt), *obj))
    }

    fun logInfo(tag: String, msg: String) {
        Log.i(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), msg))
    }

    fun logInfo(tag: String, fmt: String, vararg obj: Any) {
        Log.i(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), fmt), *obj))
    }

    fun logError(tag: String, msg: String) {
        Log.e(tag, String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), msg))
    }

    fun logError(tag: String, fmt: String, vararg obj: Any) {
        Log.e(tag, String.format(
                String.format(LOG_FORMATTER, threadName(), stackInfo(Throwable().stackTrace), fmt), *obj))
    }

    /**
     * 调用栈信息
     * 只打印调用log的上一个栈的信息
     */
    private fun stackInfo(traces: Array<StackTraceElement>): String {
        var str = ""
        if (traces.size > 1 && traces[1] != null) {
            var fileName = traces[1].fileName
            val index = fileName.lastIndexOf(".")
            if (index > 0) {
                fileName = fileName.substring(0, index)
            }
            str = String.format("%s.%d.%s()", fileName, traces[1].lineNumber, traces[1].methodName)
        }
        return fixStringLength(str, STACK_STR_LENGTH)
    }

    /**
     * 线程信息
     */
    private fun threadName(): String {
        return fixStringLength(Thread.currentThread().name
                + " " + time(), THREAD_STR_LENGTH)
    }

    private fun time(): String {
        return sFormatter.format(Date())
    }

    /**
     * 输出固定长度的字符串
     *
     *
     * 不够被空格, 过长做trim()
     */
    private fun fixStringLength(s: String?, targetLen: Int): String {
        if (s != null && targetLen > 0) {
            var len = s.length
            if (len > targetLen) {
                return s.substring(0, targetLen)
            }

            val sb = StringBuilder(s)
            while (len < targetLen) {
                sb.append(" ")
                len++
            }
            return sb.toString()
        }
        return ""
    }

    fun showStackTrace(tag: String) {
        if (!TextUtils.isEmpty(tag)) {
            logDebug(tag, Log.getStackTraceString(Throwable()))
        }
    }

    @JvmOverloads fun printStackTrace(tag: String? = null) {
        if (TextUtils.isEmpty(tag)) {
            return
        }

        val sb = StringBuilder()
        val stackElements = Throwable().stackTrace
        if (stackElements != null) {
            for (i in stackElements.indices) {
                sb.append(stackElements[i])
                if (i != stackElements.size - 1) {
                    sb.append("\n\t")
                }
            }
            if (TextUtils.isEmpty(tag)) {
                println(sb)
            } else {
                logDebug(tag, sb.toString())
            }
        }
    }

    /**
     * print hierarchy structure of view

     * @param v            view
     * *
     * @param fromRoot     print view structure from root
     * *
     * @param showFullName show full name of class
     */
    @JvmOverloads fun pvs(v: View?, fromRoot: Boolean = true, showFullName: Boolean = false) {
        if (v == null) {
            return
        }

        var p: ViewParent
        if (v !is ViewParent) {
            p = v.parent
        } else {
            p = v
        }

        // get root
        if (fromRoot) {
            while (p.parent != null) {
                // can not get child from ViewRootImpl -> break here.
                if (p.parent.javaClass.getName() == "android.view.ViewRootImpl") {
                    break
                }
                p = p.parent
            }
        }

        logDebug(TAG_STRUCTURE, "%s", if (showFullName) p.javaClass.getName() else p.javaClass.getSimpleName())

        doPvs(p, "", showFullName, 1)
    }

    /**
     * print structure

     * @param vg           view
     * *
     * @param pre          pre string from parent
     * *
     * @param showFullName show full class name
     * *
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
    private fun doPvs(vg: ViewParent, pre: String, showFullName: Boolean, level: Int) {
        if (vg !is ViewGroup) {
            return
        }

        val group = vg
        val count = group.childCount
        if (count > 0) {
            for (i in 0..count - 1) {
                val v = group.getChildAt(i)
                var tmpStr = pre

                // special symbol for the last node
                if (i == count - 1) {
                    tmpStr += "└───"
                } else {
                    tmpStr += "├───"
                }

                // print current node
                logDebug(TAG_STRUCTURE, "%s(%d, %d, %s)", tmpStr, level, i,
                        if (showFullName) v.javaClass.getName() else v.javaClass.getSimpleName())

                var childPre = pre
                // if current node is the last node, do not draw v-line
                // see mark2
                if (i == count - 1) {
                    childPre += "     "
                } else {
                    childPre += "│    " // see mark1
                }

                if (v is ViewGroup) {
                    doPvs(v, childPre, showFullName, level + 1)
                }
            }
        }
    }
}
/**
 * 打印调用堆栈
 *
 *
 * *
 */
/**
 * Print View Structure(PVS): print hierarchy structure of view
 *
 *
 * from parent and not show class's full name

 * @param v view
 */

