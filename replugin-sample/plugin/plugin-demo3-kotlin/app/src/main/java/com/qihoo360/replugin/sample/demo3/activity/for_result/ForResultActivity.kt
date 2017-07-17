package com.qihoo360.replugin.sample.demo3.activity.for_result

import android.content.Intent
import android.os.Bundle

import com.qihoo360.replugin.loader.a.PluginActivity

/**
 * @author RePlugin Team
 */
class ForResultActivity : PluginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent()
        intent.putExtra("data", "data from demo1 plugin, resultCode is 0x012")
        setResult(0x012, intent)

        finish()
    }
}
