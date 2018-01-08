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

package com.qihoo360.replugin.sample.demo1.webview;

import android.content.Context;
import android.view.View;

import com.qihoo360.replugin.RePlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.qihoo360.replugin.sample.demo1.BuildConfig.DEBUG;

/**
 * @author RePlugin Team
 */
public abstract class ViewProxy<V extends View> {

    public static class Creator {

        private final String mPlgName;

        private final String mComponentName;

        private Context mPlgContext;

        private Class<?> mTargetClass;

        private Constructor<?> mTargetConstructor;

        public Creator(String pn, String cn) {
            mPlgName = pn;
            mComponentName = cn;
        }

        public boolean init() {
            if (mPlgContext == null) {
                mPlgContext = RePlugin.fetchContext(mPlgName);
                if (mPlgContext == null) {
                    return false;
                }
            }
            if (mTargetClass == null) {
                mTargetClass = fetchClassByName(mPlgContext.getClassLoader(), mComponentName);
                if (mTargetClass == null) {
                    return false;
                }
            }
            if (mTargetConstructor == null) {
                mTargetConstructor = fetchConstructorByName(Context.class);
                if (mTargetConstructor == null) {
                    return false;
                }
            }
            return true;
        }

        public Method fetchMethodByName(String name, Class<?>... pt) {
            Method m;
            try {
                m = mTargetClass.getDeclaredMethod(name, pt);
            } catch (Throwable e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                return null;
            }
            return m;
        }

        public View newViewInstance(Context c) {
            if (mTargetConstructor == null) {
                return null;
            }
            Object o;
            try {
                o = mTargetConstructor.newInstance(c);
            } catch (Throwable e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                return null;
            }
            if (!(o instanceof View)) {
                return null;
            }
            return (View) o;
        }

        private Constructor<?> fetchConstructorByName(Class<?>... pt) {
            try {
                return mTargetClass.getConstructor(pt);
            } catch (NoSuchMethodException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        private Class<?> fetchClassByName(ClassLoader cl, String cn) {
            Class<?> clz;
            try {
                clz = cl.loadClass(cn);
            } catch (Throwable e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                return null;
            }
            return clz;
        }
    }

    protected V mView;

    protected ViewProxy(V view) {
        mView = view;
    }

    public V getView() {
        return mView;
    }

    protected Object invoke(Method m, Object... args) {
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(mView, args);
        } catch (Throwable e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
