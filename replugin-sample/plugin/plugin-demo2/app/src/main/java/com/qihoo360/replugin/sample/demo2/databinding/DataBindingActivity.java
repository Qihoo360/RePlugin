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

package com.qihoo360.replugin.sample.demo2.databinding;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.qihoo360.replugin.sample.demo2.R;

/**
 * @author RePlugin Team
 */
public class DataBindingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabindingTestBinding binding = DataBindingUtil.setContentView(this, R.layout.databinding_test);
        Entry entry = new Entry();
        entry.setText("文本数据1");
        entry.setColor(0xff0000ff);
        //设置测试字符串
        binding.setStr("我是监听绑定的数据测试");
        binding.setEntry(entry);
    }
}
