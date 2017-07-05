package com.qihoo360.replugin.sample.demo1.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.sample.demo1.R;

/**
 * 描述类作用
 * <p>
 * 作者 coder
 * 创建时间 2017/7/5
 */

public class DemoFragment extends Fragment {

    public DemoFragment() {
    }

    private static final String TAG = "DemoFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        /**
         * 需要注意不能使用inflater及container因为他们的Context是宿主的
         */
        return LayoutInflater.from(RePlugin.getPluginContext()).inflate(R.layout.main_fragment, container, false);
    }
}
