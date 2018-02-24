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

package com.qihoo360.replugin.sample.demo1.activity.notify_test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.qihoo360.replugin.sample.demo1.R;

import static com.qihoo360.replugin.sample.demo1.support.NotifyUtils.NOTIFY_KEY;
import static com.qihoo360.replugin.sample.demo1.support.NotifyUtils.TAG;


/**
 * @author RePlugin Team
 */
public class NotifyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_notify);
        try {
            ((TextView)findViewById(R.id.btn_show_notify)).setText(getIntent().getStringExtra(NOTIFY_KEY));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            finish();
        }
    }
}
