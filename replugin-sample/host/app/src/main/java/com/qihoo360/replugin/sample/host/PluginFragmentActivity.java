package com.qihoo360.replugin.sample.host;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.qihoo360.replugin.RePlugin;

/**
 * 打开插件中的Fragment
 * <p>
 * 作者 coder
 * 创建时间 2017/7/6
 */

public class PluginFragmentActivity extends FragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册相关Fragment的类
        //注册一个全局Hook用于拦截系统对XX类的寻找定向到Demo1中的XX类主要是用于在xml中可以直接使用插件中的类
        RePlugin.registerHookingClass("com.qihoo360.replugin.sample.demo1.fragment.DemoFragment", RePlugin.createComponentName("demo1", "com.qihoo360.replugin.sample.demo1.fragment.DemoFragment"), null);
        setContentView(R.layout.activity_plugin_fragment);


        //代码使用插件Fragment
        ClassLoader d1ClassLoader = RePlugin.fetchClassLoader("demo1");//获取插件的ClassLoader
        try {
            Fragment fragment = d1ClassLoader.loadClass("com.qihoo360.replugin.sample.demo1.fragment.DemoCodeFragment").asSubclass(Fragment.class).newInstance();//使用插件的Classloader获取指定Fragment实例
            getSupportFragmentManager().beginTransaction().add(R.id.container2, fragment).commit();//添加Fragment到UI
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
