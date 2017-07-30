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

package com.qihoo360.replugin.sample.demo1.testcase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.qihoo360.replugin.sample.demo1.R;
import com.qihoo360.replugin.sample.demo1.TestItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osan on 2017/7/30.
 */
public class TestCaseActivity extends Activity {

    private List<TestItem> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_testcase);

        initData();

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(new TestAdapter());
    }

    private void initData() {
        // =========
        // TestMultiDex
        // =========
        mItems.add(new TestItem("Test access MultiDex method", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ret = TestMultiDex.accessMultiDexMethod();
                Toast.makeText(TestCaseActivity.this, ret, Toast.LENGTH_SHORT).show();
            }
        }));


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
                convertView = new Button(TestCaseActivity.this);
            }

            TestItem item = getItem(position);
            ((Button) convertView).setText(item.title);
            convertView.setOnClickListener(item.mClickListener);
            return convertView;
        }
    }
}
