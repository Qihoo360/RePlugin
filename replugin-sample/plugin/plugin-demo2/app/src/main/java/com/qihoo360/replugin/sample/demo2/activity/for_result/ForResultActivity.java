package com.qihoo360.replugin.sample.demo2.activity.for_result;

import android.content.Intent;
import android.os.Bundle;

import com.qihoo360.replugin.loader.a.PluginActivity;

/**
 * @author RePlugin Team
 */
public class ForResultActivity extends PluginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.putExtra("data", "data from demo2 plugin, resultCode is 0x022");
        setResult(0x022, intent);

        finish();
    }
}
