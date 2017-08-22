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
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.common.utils.TimeUtils;
import com.qihoo360.replugin.sample.demo1.activity.single_instance.TIActivity1;
import com.qihoo360.replugin.sample.demo1.activity.single_top.SingleTopActivity1;
import com.qihoo360.replugin.sample.demo1.activity.standard.StandardActivity;
import com.qihoo360.replugin.sample.demo1.activity.task_affinity.TAActivity1;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeBlackNoTitleBarActivity;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeBlackNoTitleBarFullscreenActivity;
import com.qihoo360.replugin.sample.demo1.activity.theme.ThemeDialogActivity;
import com.qihoo360.replugin.sample.demo1.service.PluginDemoService1;
import com.qihoo360.replugin.sample.demo2.IDemo2;
import com.qihoo360.replugin.sample.library.LibMainActivity;

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
        mItems.add(new TestItem("Activity: Standard", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), StandardActivity.class);
                v.getContext().startActivity(intent);
            }
        }));
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
        mItems.add(new TestItem("Activity: AppCompat (to Demo2)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RePlugin.startActivity(v.getContext(), new Intent(), "demo2", "com.qihoo360.replugin.sample.demo2.activity.appcompat.AppCompatActivityDemo");
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
        mItems.add(new TestItem("Activity: Open in Application", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context app = v.getContext().getApplicationContext();
                Intent intent = new Intent(app, ThemeDialogActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                app.startActivity(intent);
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
        mItems.add(new TestItem("Activity: DataBinding (to Demo2)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RePlugin.startActivity(v.getContext(), new Intent(), "demo2", "com.qihoo360.replugin.sample.demo2.databinding.DataBindingActivity");
            }
        }));
        mItems.add(new TestItem("Activity: startForResult (to Demo2)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("demo2", "com.qihoo360.replugin.sample.demo2.activity.for_result.ForResultActivity"));
                MainActivity.this.startActivityForResult(intent, REQUEST_CODE_DEMO2);
                // 也可以这么用
                // RePlugin.startActivityForResult(MainActivity.this, intent, REQUEST_CODE_DEMO2);
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
        mItems.add(new TestItem("Activity: By Action (to Demo2)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.qihoo360.replugin.sample.demo2.action.theme_fullscreen_2");
                RePlugin.startActivity(v.getContext(), intent, "demo2", null);
            }
        }));
        mItems.add(new TestItem("Activity: start Host activity(Main)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(RePlugin.getHostContext().getPackageName(), "com.qihoo360.replugin.sample.host.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
            }
        }));

        // =========
        // Other Components
        // =========
        mItems.add(new TestItem("Broadcast: Send (to All)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.qihoo360.repluginapp.replugin.receiver.ACTION1");
                intent.putExtra("name", "jerry");
                v.getContext().sendBroadcast(intent);
            }
        }));
        mItems.add(new TestItem("Service: Start (at :p0 process)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PluginDemoService1.class);
                intent.setAction("action1");
                v.getContext().startService(intent);
            }
        }));
        mItems.add(new TestItem("Service: Implicit Start (at :UI process)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setPackage("com.qihoo360.replugin.sample.demo1");
                intent.setAction("com.qihoo360.replugin.sample.demo1.action.XXXX");
                v.getContext().startService(intent);
            }
        }));
        mItems.add(new TestItem("Provider: Query (at UI process)", new View.OnClickListener() {
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
        mItems.add(new TestItem("Use Host Method: Direct use (Ex. TimeUtils)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此为RePlugin的另一种做法，可直接调用宿主的Utils
                // 虽然不是很推荐（版本控制问题，见FAQ），但毕竟需求较大，且我们是“相对安全的共享代码”方案，故可以使用
                final String curTime = TimeUtils.getNowString();
                if (!TextUtils.isEmpty(curTime)) {
                    Toast.makeText(v.getContext(), "current time: " + TimeUtils.getNowString(), Toast.LENGTH_SHORT).show();
                    // 打印其ClassLoader
                    Log.d("MainActivity", "Use Host Method: cl=" + TimeUtils.class.getClassLoader());
                } else {
                    Toast.makeText(v.getContext(), "Failed to obtain current time(from host)", Toast.LENGTH_SHORT).show();
                }
            }
        }));
        mItems.add(new TestItem("Use Demo2 Method: Reflection (Recommend)", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 这是RePlugin的推荐玩法：反射调用Demo2，这样"天然的"做好了"版本控制"
                // 避免出现我们当年2013年的各种问题
                ClassLoader cl = RePlugin.fetchClassLoader("demo2");
                if (cl == null) {
                    Toast.makeText(v.getContext(), "Not install Demo2", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Class clz = cl.loadClass("com.qihoo360.replugin.sample.demo2.MainApp");
                    Method m = clz.getDeclaredMethod("helloFromDemo1", Context.class, String.class);
                    m.invoke(null, v.getContext(), "Demo1");
                } catch (Exception e) {
                    // 有可能Demo2根本没有这个类，也有可能没有相应方法（通常出现在"插件版本升级"的情况）
                    Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }));
        mItems.add(new TestItem("Resources: Use demo2's layout", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout contentView = RePlugin.fetchViewByLayoutName("demo2", "from_demo1", null);
                if (contentView == null) {
                    Toast.makeText(v.getContext(), "from_demo1 Not Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                Dialog d = new Dialog(v.getContext());
                d.setContentView(contentView);
                d.show();
            }
        }));
        mItems.add(new TestItem("Binder: Fast-Fetch (to Demo2)", new View.OnClickListener() {
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

        // =========
        // aar
        // =========
        mItems.add(new TestItem("AAR Activity: Standard", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LibMainActivity.class);
                v.getContext().startActivity(intent);
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
