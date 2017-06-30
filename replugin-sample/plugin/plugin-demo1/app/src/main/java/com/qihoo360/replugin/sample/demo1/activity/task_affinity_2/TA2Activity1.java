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

package com.qihoo360.replugin.sample.demo1.activity.task_affinity_2;

import android.content.Intent;
import android.view.View;

import com.qihoo360.replugin.sample.demo1.BaseActivity;

/**
 * @author RePlugin Team
 */
public class TA2Activity1 extends BaseActivity {

    @Override
    public void jump(View v) {
        startActivity(new Intent(this, TA2Activity2.class));
    }

    @Override
    public String title() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String btnTxt() {
        return TA2Activity2.class.getSimpleName();
    }
}
