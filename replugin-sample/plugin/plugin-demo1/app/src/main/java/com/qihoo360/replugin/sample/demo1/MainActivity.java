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
import android.content.ContentValues;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author RePlugin Team
 */
public class MainActivity extends Activity {

    private static List<TestItem> mItems = new ArrayList<>();

    static {
        mItems.add(new TestItem("Theme BlackNoTitleBar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeBlackNoTitleBarActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Theme BlackNoTitleBarFullscreen", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeBlackNoTitleBarFullscreenActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Theme Dialog", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ThemeDialogActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("SingleTop", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SingleTopActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));

        mItems.add(new TestItem("Start activity by action", new View.OnClickListener() {
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
        mItems.add(new TestItem("Start activity by action(other plugin)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.qihoo360.replugin.sample.demo2.action.theme_fullscreen_2");
                RePlugin.startActivity(v.getContext(), intent, "demo2", null);
            }
        }));
        mItems.add(new TestItem("Send broadcast to self", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.qihoo360.repluginapp.replugin.receiver.ACTION1");
                intent.putExtra("name", "jerry");
                v.getContext().sendBroadcast(intent);
            }
        }));
        mItems.add(new TestItem("Start Service (self)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PluginDemoService1.class);
                intent.setAction("action1");
                v.getContext().startService(intent);
            }
        }));
        mItems.add(new TestItem("Start Task Affinity Demo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TAActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Start SingleInstance Demo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TIActivity1.class);
                v.getContext().startActivity(intent);
            }
        }));
        mItems.add(new TestItem("Test Intent Filter", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.demo1");
                intent.addCategory("category_demo");
                //Factory.startActivity(context, intent, "", "", IPluginManager.PROCESS_AUTO);
                v.getContext().startActivity(intent);
            }
        }));

        mItems.add(new TestItem("当前进程-Provider", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("content://com.qihoo360.replugin.sample.demo1.provider2/" + "test");

                ContentValues cv = new ContentValues();
                cv.put("name", "erhu");
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
        mItems.add(new TestItem("跳转其他插件Activity", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RePlugin.startActivity(v.getContext(), new Intent(), "demo2", "com.qihoo360.replugin.sample.demo2.databinding.DataBindingActivity");
            }
        }));
        mItems.add(new TestItem("与demo2通信", new View.OnClickListener() {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(new TestAdapter());
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
