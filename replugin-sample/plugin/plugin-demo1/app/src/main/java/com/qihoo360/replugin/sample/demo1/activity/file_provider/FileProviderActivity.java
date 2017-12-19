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

package com.qihoo360.replugin.sample.demo1.activity.file_provider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.qihoo360.replugin.sample.demo1.R;
import com.qihoo360.replugin.sample.demo1.provider.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Created by cundong on 2017/12/15.
 * <p>
 * 插件中使用 FileProvider 示例
 */
public class FileProviderActivity extends Activity implements View.OnClickListener {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1023;
    private static final int REQUEST_TAKE_PHOTO = 1024;

    // Host Provider Authorities
    private static final String PROVIDER_AUTHORITIES = "com.qihoo360.replugin.sample.host.FILE_PROVIDER";

    private Button mButton;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_4);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    takePhoto();
                }
            } else {
                takePhoto();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "sd Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri uri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, PROVIDER_AUTHORITIES, getPhotoFile());
        } else {
            uri = Uri.fromFile(getPhotoFile());
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private File getPhotoFile() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File photoFile = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
            try {
                photoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return photoFile;
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {

                    File photoFile = getPhotoFile();
                    if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
                        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(photoFile.getPath(), 300, 300);
                        if (null != bitmap) {
                            Toast.makeText(this, "Get Photo Success.", Toast.LENGTH_SHORT).show();
                            mImageView.setImageBitmap(bitmap);
                        } else {
                            Toast.makeText(this, "Get Photo Fail.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Get Photo Fail.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}