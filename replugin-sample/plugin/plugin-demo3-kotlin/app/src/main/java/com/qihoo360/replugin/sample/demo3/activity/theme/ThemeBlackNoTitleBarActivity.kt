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

package com.qihoo360.replugin.sample.demo3.activity.theme

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * @author RePlugin Team
 */
class ThemeBlackNoTitleBarActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.setPadding(30, 30, 30, 30)
        val lp = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        root.layoutParams = lp

        setContentView(root)

        // textView
        val textView = TextView(this)
        textView.gravity = Gravity.CENTER
        textView.textSize = 30f
        textView.text = "Theme3: BlackNoTitleBar"
        val lp2 = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        textView.layoutParams = lp2
        root.addView(textView)
    }

}
