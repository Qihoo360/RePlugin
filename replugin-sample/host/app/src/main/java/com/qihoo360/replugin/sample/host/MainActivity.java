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
import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author RePlugin Team
 */
public class MainActivity extends Activity {

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

        findViewById(R.id.btn_start_demo3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 若没有安装，则直接提示“错误”
                // TODO 将来把回调串联上
                if (RePlugin.isPluginInstalled("demo3")) {
                    RePlugin.startActivity(MainActivity.this, RePlugin.createIntent("demo3", "com.qihoo360.replugin.sample.demo3.MainActivity"));
                } else {
                    Toast.makeText(MainActivity.this, "You must install demo3 first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_start_demo4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 示例：直接通过宿主打开WebView插件中的Activity
                // FIXME: 后续可以将webview MainActivity URL 改为动态传入
                // 若没有安装，则直接提示“错误”
                if (RePlugin.isPluginInstalled("webview")) {
                    RePlugin.startActivity(MainActivity.this, RePlugin.createIntent("webview", "com.qihoo360.replugin.sample.webview.MainActivity"));
                } else {
                    Toast.makeText(MainActivity.this, "You must install webview first!", Toast.LENGTH_SHORT).show();
                }
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
                        simulateInstallExternalPlugin();
                        pd.dismiss();
                    }
                }, 1000);
            }
        });

        // 刻意使用Thread的ClassLoader来测试效果
        testThreadClassLoader();
    }

    private void testThreadClassLoader() {
        // 在2.1.7及以前版本，如果直接调用此方法，则拿到的ClassLoader可能是PathClassLoader或者为空。有极个别Java库会用到此方法
        // 这里务必确保：cl == getClassLoader()，才符合预期
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != getClassLoader()) {
            throw new RuntimeException("Thread.current.classLoader != getClassLoader(). cl=" + cl + "; getC=" + getClassLoader());
        }
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
     * 模拟安装或升级（覆盖安装）外置插件
     * 注意：为方便演示，外置插件临时放置到Host的assets/external目录下，具体说明见README</p>
     */
    private void simulateInstallExternalPlugin() {
        String demo3Apk= "demo3.apk";
        String demo3apkPath = "external" + File.separator + demo3Apk;

        // 文件是否已经存在？直接删除重来
        String pluginFilePath = getFilesDir().getAbsolutePath() + File.separator + demo3Apk;
        File pluginFile = new File(pluginFilePath);
        if (pluginFile.exists()) {
            FileUtils.deleteQuietly(pluginFile);
        }

        // 开始复制
        copyAssetsFileToAppFiles(demo3apkPath, demo3Apk);
        PluginInfo info = null;
        if (pluginFile.exists()) {
            info = RePlugin.install(pluginFilePath);
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
