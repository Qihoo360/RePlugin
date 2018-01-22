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

package com.qihoo360.replugin.fresco.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qihoo360.replugin.RePlugin;

/**
 * @author RePlugin Team
 */
public class MainActivity extends AppCompatActivity {

    private Button mButton1;
    private Button mButton2;

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RePlugin.startActivity(MainActivity.this, new Intent(), "plugin1", "com.qihoo360.replugin.fresco.plugin.MainActivity");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);

        mButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HostFrescoActivity.class));
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        long begin = System.currentTimeMillis();
                        RePlugin.preload("plugin1");
                        Log.d("FrescoHost", "preload use:" + (System.currentTimeMillis() - begin));

                        mHandler.sendEmptyMessage(-1);
                    }
                }.start();
            }
        });
    }
}