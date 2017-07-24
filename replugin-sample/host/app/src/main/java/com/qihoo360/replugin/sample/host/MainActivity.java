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

package com.qihoo360.replugin.sample.host;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author RePlugin Team
 */
public class MainActivity extends Activity {

    // 外置插件相关
    private final static String PLUGIN_NAME = "demo3";
    private final static String PLUGIN_APK = PLUGIN_NAME + ".apk";
    private final static String PLUGIN_PATH = "external" + File.separator + PLUGIN_APK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start_demo1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 刻意以“包名”来打开
                RePlugin.startActivity(MainActivity.this, RePlugin.createIntent("com.qihoo360.replugin.sample.demo1", "com.qihoo360.replugin.sample.demo1.MainActivity"));
            }
        });

        findViewById(R.id.btn_start_plugin_for_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 刻意以“Alias（别名）”来打开
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("demo1", "com.qihoo360.replugin.sample.demo1.activity.for_result.ForResultActivity"));
                RePlugin.startActivityForResult(MainActivity.this, intent, REQUEST_CODE_DEMO1, null);
            }
        });

        findViewById(R.id.btn_load_fragment_from_demo1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PluginFragmentActivity.class));
            }
        });

        findViewById(R.id.btn_install_apk_from_assets).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final ProgressDialog pd = ProgressDialog.show(MainActivity.this, "Installing...", "Please wait...", true, true);
                // FIXME: 仅用于安装流程演示 2017/7/24
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        simulateInstallExternalPlugin(PLUGIN_NAME);
                        pd.dismiss();
                    }
                }, 1000);
            }
        });
    }

    private static final int REQUEST_CODE_DEMO1 = 0x011;
    private static final int RESULT_CODE_DEMO1 = 0x012;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DEMO1 && resultCode == RESULT_CODE_DEMO1) {
            Toast.makeText(this, data.getStringExtra("data"), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 模拟安装外置插件
     * 注意：为方便演示，外置插件临时放置到Host的assets/external目录下，具体说明见README</p>
     *
     * @param pluginName 待安装的插件名
     */
    private void simulateInstallExternalPlugin(String pluginName) {
        PluginInfo info = RePlugin.getPluginInfo(pluginName);
        if (info == null) {
            String pluginFilePath = getFilesDir().getAbsolutePath() + File.separator + PLUGIN_APK;
            File pluginFile = new File(pluginFilePath);
            if (!pluginFile.exists()) {
                copyAssetsFileToAppFiles(PLUGIN_PATH, PLUGIN_APK);
                if (pluginFile.exists()) {
                    info = RePlugin.install(pluginFilePath);
                }
            }
        }

        if (info != null) {
            RePlugin.startActivity(MainActivity.this, RePlugin.createIntent(info.getName(), "com.qihoo360.replugin.sample.demo3.MainActivity"));
        } else {
            Toast.makeText(MainActivity.this, "install external plugin failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从assets目录中复制某文件内容
     *  @param  assetFileName assets目录下的Apk源文件路径
     *  @param  newFileName 复制到/data/data/package_name/files/目录下文件名
     */
    private void copyAssetsFileToAppFiles(String assetFileName, String newFileName) {
        InputStream is = null;
        FileOutputStream fos = null;
        int buffsize = 1024;

        try {
            is = this.getAssets().open(assetFileName);
            fos = this.openFileOutput(newFileName, Context.MODE_PRIVATE);
            int byteCount = 0;
            byte[] buffer = new byte[buffsize];
            while((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
