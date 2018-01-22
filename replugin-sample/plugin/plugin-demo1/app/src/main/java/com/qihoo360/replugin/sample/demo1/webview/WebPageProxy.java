package com.qihoo360.replugin.sample.demo1.webview;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import java.lang.reflect.Method;

/**
 * @author RePlugin Team
 */
public class WebPageProxy extends ViewProxy<View> implements IWebPage {

    private static final String PLUGIN_NAME = "webview";

    private static final String CLASS_NAME = "com.qihoo360.replugin.sample.webview.views.SimpleWebPage";

    private static Method sGetWebViewMethod;

    private static final ViewProxy.Creator CREATOR = new ViewProxy.Creator(PLUGIN_NAME, CLASS_NAME);

    protected WebPageProxy(View view) {
        super(view);
    }

    public static WebPageProxy create(Context c) {
        boolean b = CREATOR.init();
        if (!b) {
            return null;
        }
        View v = CREATOR.newViewInstance(c);
        return new WebPageProxy(v);
    }

    static WebPageProxy createByObject(View wv) {
        boolean b = CREATOR.init();
        if (!b) {
            return null;
        }
        return new WebPageProxy(wv);
    }

    @Override
    public WebView getWebView() {
        if (sGetWebViewMethod == null) {
            sGetWebViewMethod = CREATOR.fetchMethodByName("getWebView");
        }
        if (sGetWebViewMethod == null) {
            return null;
        }
        Object obj = invoke(sGetWebViewMethod);
        if (obj instanceof WebView) {
            return (WebView) obj;
        }
        return null;
    }
}
