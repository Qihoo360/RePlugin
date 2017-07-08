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

package com.qihoo360.replugin.sample.demo1;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.sample.demo1.activity.single_instance.TIActivity1;
import com.qihoo360.replugin.sample.demo1.activity.single_top.SingleTopActivity1;
import com.qihoo360.replugin.sample.demo1.activity.task_affinity.TAActivity1;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeBlackNoTitleBarActivity;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeBlackNoTitleBarFullscreenActivity;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeDialogActivity;
import com.qihoo360.replugin.sample.demo1.service.PluginDemoService1;
import com.qihoo360.replugin.sample.demo2.IDemo2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RePlugin Team
 */
public class MainActivity extends Activity {

    private List<TestItem> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initData();

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(new TestAdapter());
    }

    private void initData() {
        // TODO UI丑是丑了点儿，但能说明问题。以后会优化的

        // =========
        // Activity
        // =========
        mItems.add(new TestItem("Activity: Theme BlackNoTitleBar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeBlackNoTitleBarActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: Theme BlackNoTitleBarFullscreen", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeBlackNoTitleBarFullscreenActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: Theme Dialog", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeDialogActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: SingleTop", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SingleTopActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: SingleInstance", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TIActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: Task Affinity", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TAActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: By Intent Filter", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.demo1");
                intent.addCategory("category_demo");
                //Factory.startActivity(context, intent, "", "", IPluginManager.PROCESS_AUTO);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: DataBinding (Other Plugin)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RePlugin.startActivity(v.getContext(), new Intent(), "demo2", "com.qihoo360.replugin.sample.demo2.databinding.DataBindingActivity");
            }
        }));
        mItems.add(new TestItem("Activity: By Action", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.qihoo360.replugin.sample.demo1.action.theme_fullscreen");
                /*
                    若 Intent 中声明 category, manifest 中未声明，则无法找到 Activity;
                    若 manifest 中声明 category, Intent 中未声明，则可以找到 Activity;
                */
                intent.addCategory("com.qihoo360.repluginapp.replugin.demo1.CATEGORY1");
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Activity: By Action (Other Plugin)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.qihoo360.replugin.sample.demo2.action.theme_fullscreen_2");
                RePlugin.startActivity(v.getContext(), intent, "demo2", null);
            }
        }));


        // =========
        // Other Components
        // =========
        mItems.add(new TestItem("Broadcast: Send", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.qihoo360.repluginapp.replugin.receiver.ACTION1");
                intent.putExtra("name", "jerry");
                v.getContext().sendBroadcast(intent);
            }
        }));
        mItems.add(new TestItem("Service: Start", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PluginDemoService1.class);
                intent.setAction("action1");
                v.getContext().startService(intent);
            }
        }));

        mItems.add(new TestItem("Provider: Current process", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("content://com.qihoo360.replugin.sample.demo1.provider2/" + "test");

                ContentValues cv = new ContentValues();
                cv.put("name", "RePlugin Team");
                cv.put("address", "beijing");

                Uri urii = v.getContext().getContentResolver().insert(uri, cv);
                Log.d("a4", "result=" + urii);
                if (urii != null) {
                    Toast.makeText(v.getContext(), urii.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "null", Toast.LENGTH_SHORT).show();
                }
            }
        }));

        // =========
        // Communication
        // =========
        mItems.add(new TestItem("ClassLoader: Use demo2's class", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 近期会添加RePlugin.fetchClass方法，无需catch一堆
                ClassLoader cl = RePlugin.fetchClassLoader("demo2");
                if (cl == null) {
                    Toast.makeText(v.getContext(), "Not install Demo2", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Class clz = cl.loadClass("com.qihoo360.replugin.sample.demo2.MainApp");
                    Method m = clz.getDeclaredMethod("helloFromDemo1", Context.class, String.class);
                    m.invoke(null, v.getContext(), "Demo1");
                } catch (ClassNotFoundException e) {
                    // 有可能Demo2根本没有这个类，直接返回
                    Toast.makeText(v.getContext(), "MainApp not found", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // 有可能没有这个方法
                    Toast.makeText(v.getContext(), "helloFromDemo1() not found", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // 也有可能不允许你访问
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }));
        mItems.add(new TestItem("Fragment: Use demo2", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 志伟请搞定这里，谢谢！
            }
        }));
        mItems.add(new TestItem("Binder: Fast-Fetch", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IBinder b = RePlugin.fetchBinder("demo2", "demo2test");
                if (b == null) {
                    return;
                }
                IDemo2 demo2 = IDemo2.Stub.asInterface(b);
                try {
                    demo2.hello("helloooooooooooo");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }));
        mItems.add(new TestItem("startActivityForResult(data from demo2)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("demo2", "com.qihoo360.replugin.sample.demo2.activity.for_result.ForResultActivity"));
                MainActivity.this.startActivityForResult(intent, REQUEST_CODE_DEMO2);
            }
        }));
    }

    private static final int REQUEST_CODE_DEMO2 = 0x021;
    private static final int RESULT_CODE_DEMO2 = 0x022;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DEMO2 && resultCode == RESULT_CODE_DEMO2) {
            Toast.makeText(this, data.getStringExtra("data"), Toast.LENGTH_SHORT).show();
        }
    }

    private class TestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public TestItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new Button(MainActivity.this);
            }

            TestItem item = getItem(position);
            ((Button) convertView).setText(item.title);
            convertView.setOnClickListener(item.mClickListener);
            return convertView;
        }
    }
}
