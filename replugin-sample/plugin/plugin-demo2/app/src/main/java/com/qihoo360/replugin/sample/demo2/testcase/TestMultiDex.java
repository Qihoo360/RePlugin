package com.qihoo360.replugin.sample.demo2.testcase;

/**
 * Created by osan on 2017/7/30.
 */

/**
 * multidex测试用例类
 * 用于测试RP框架对运行在android rom 5.0以下版本的插件（含多dex）的multidex支持情况
 */
public class TestMultiDex {

    private static final String TAG = "TestMultiDex";

    /**
     * test code for multidex
     * test access method of retrofit, cos it will be build to extra dex normally.
     * you can test other methods in extra dex, According to the actual situation
     */
    public static String accessMultiDexMethod(){


        String ret = "enable multiDexEnabled & uncomment accessMultiDexMethod";
        /**
         * test for extra dex
         * this case for : test whether class of extra dex can be accessed, after extra dex is loaded.
         */
        {
            /*retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl("http://www.mocky.io/v2/")
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            Log.d(TAG, "retrofit:" + retrofit.baseUrl());
            ret = "retrofit baseUrl: " + retrofit.baseUrl();*/
        }

        /**
         * test for main dex
         * this case for : test whether class of main dex can be still accessed, after extra dex is loaded.
         */
        {
            /*int autoHeight = com.google.android.gms.ads.AdSize.AUTO_HEIGHT;
            ret += "\nAdSize.AUTO_HEIGHT: " + autoHeight;*/
        }

        /**
         * test for extra dex
         * this case for : test whether class of extra dex can be still accessed again, after extra dex is loaded
         *                 and accessing class of main dex.
         */
        {
            /*retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl("http://www.mocky.io/v2/")
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            Log.d(TAG, "retrofit:" + retrofit.baseUrl());
            ret += "\nretrofit baseUrl again: " + retrofit.baseUrl();*/
        }

        return ret;
    }

}
