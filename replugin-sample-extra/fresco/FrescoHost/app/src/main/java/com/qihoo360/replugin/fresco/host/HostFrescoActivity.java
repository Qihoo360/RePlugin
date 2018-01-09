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

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 宿主中使用fresco
 *
 * @author RePlugin Team
 */
public class HostFrescoActivity extends AppCompatActivity {

    private static final String IMAGE_URL = "https://img1.doubanio.com/view/photo/large/public/p2504463708.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_fresco);

        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.image);
        draweeView.setImageURI(Uri.parse(IMAGE_URL));
    }
}