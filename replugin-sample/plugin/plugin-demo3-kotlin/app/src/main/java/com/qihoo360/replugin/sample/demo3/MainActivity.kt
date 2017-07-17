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

package com.qihoo360.replugin.sample.demo3

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.sample.demo3.activity.single_instance.TIActivity1
import com.qihoo360.replugin.sample.demo3.activity.single_top.SingleTopActivity1
import com.qihoo360.replugin.sample.demo3.activity.task_affinity.TAActivity1
import com.qihoo360.replugin.sample.demo3.activity.theme.ThemeBlackNoTitleBarActivity
import com.qihoo360.replugin.sample.demo3.activity.theme.ThemeBlackNoTitleBarFullscreenActivity
import com.qihoo360.replugin.sample.demo3.activity.theme.ThemeDialogActivity

import java.util.ArrayList

/**
 * @author RePlugin Team
 */
class MainActivity : Activity() {

    private val mItems = ArrayList<TestItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        initData()

        val lv = findViewById(R.id.list_view) as ListView
        lv.adapter = TestAdapter()

        Toast.makeText(this, "Hello Kotlin ! Hello RePlugin !", Toast.LENGTH_SHORT).show()
    }

    private fun initData() {
        // TODO UI丑是丑了点儿，但能说明问题。以后会优化的

        // =========
        // Activity
        // =========
        mItems.add(TestItem("Kotlin Activity: Theme BlackNoTitleBar", View.OnClickListener { v ->
            val intent = Intent(v.context, ThemeBlackNoTitleBarActivity::class.java)
            v.context.startActivity(intent)
        }))

        mItems.add(TestItem("Kotlin Activity: By Action", View.OnClickListener { v ->
            val intent = Intent()
            intent.action = "com.qihoo360.replugin.sample.demo1.action.theme_fullscreen"
            /*
                    若 Intent 中声明 category, manifest 中未声明，则无法找到 Activity;
                    若 manifest 中声明 category, Intent 中未声明，则可以找到 Activity;
                */
            intent.addCategory("com.qihoo360.repluginapp.replugin.demo1.CATEGORY1")
            v.context.startActivity(intent)
        }))


        // =========
        // Other Components
        // =========
        mItems.add(TestItem("Kotlin Broadcast: Send (to All)", View.OnClickListener { v ->
            val intent = Intent()
            intent.action = "com.qihoo360.repluginapp.replugin.receiver.ACTION1"
            intent.putExtra("name", "jerry")
            v.context.sendBroadcast(intent)
        }))

        mItems.add(TestItem("Kotlin Provider: Query (at UI process)", View.OnClickListener { v ->
            val uri = Uri.parse("content://com.qihoo360.replugin.sample.demo1.provider2/" + "test")

            val cv = ContentValues()
            cv.put("name", "RePlugin Team")
            cv.put("address", "beijing")

            val urii = v.context.contentResolver.insert(uri, cv)
            Log.d("a4", "result=" + urii!!)
            if (urii != null) {
                Toast.makeText(v.context, urii.toString(), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(v.context, "null", Toast.LENGTH_SHORT).show()
            }
        }))

        // =========
        // Communication
        // =========
        mItems.add(TestItem("Kotlin ClassLoader: Reflection (to Demo2, Recommend)", View.OnClickListener { v ->
            // 这是RePlugin的推荐玩法：反射调用Demo2，这样"天然的"做好了"版本控制"
            // 避免出现我们当年2013年的各种问题
            val cl = RePlugin.fetchClassLoader("demo2")
            if (cl == null) {
                Toast.makeText(v.context, "Not install Demo2", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            try {
                val clz = cl.loadClass("com.qihoo360.replugin.sample.demo2.MainApp")
                val m = clz.getDeclaredMethod("helloFromDemo1", Context::class.java, String::class.java)
                m.invoke(null, v.context, "Demo1")
            } catch (e: Exception) {
                // 有可能Demo2根本没有这个类，也有可能没有相应方法（通常出现在"插件版本升级"的情况）
                Toast.makeText(v.context, "", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_CODE_DEMO2 && resultCode == RESULT_CODE_DEMO2) {
            Toast.makeText(this, data.getStringExtra("data"), Toast.LENGTH_SHORT).show()
        }
    }

    private inner class TestAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): TestItem {
            return mItems[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = Button(this@MainActivity)
            }

            val item = getItem(position)
            (convertView as Button).text = item.title
            convertView.setOnClickListener(item.mClickListener)
            return convertView
        }
    }

    companion object {

        private val REQUEST_CODE_DEMO2 = 0x021
        private val RESULT_CODE_DEMO2 = 0x022
    }
}
