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

package com.qihoo360.replugin.loader.b;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.qihoo360.replugin.MethodInvoker;
import com.qihoo360.replugin.RePluginEnv;
import com.qihoo360.replugin.RePluginFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author RePlugin Team
 */

/**
 * Helper to register for and send broadcasts of Intents to local objects
 * within your process.  This is has a number of advantages over sending
 * global broadcasts with {@link android.content.Context#sendBroadcast}:
 * <ul>
 * <li> You know that the data you are broadcasting won't leave your app, so
 * don't need to worry about leaking private data.
 * <li> It is not possible for other applications to send these broadcasts to
 * your app, so you don't need to worry about having security holes they can
 * exploit.
 * <li> It is more efficient than sending a global broadcast through the
 * system.
 * </ul>
 */
public class PluginLocalBroadcastManager {
    private static class ReceiverRecord {
        final IntentFilter filter;
        final BroadcastReceiver receiver;
        boolean broadcasting;

        ReceiverRecord(IntentFilter _filter, BroadcastReceiver _receiver) {
            filter = _filter;
            receiver = _receiver;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(128);
            builder.append("Receiver{");
            builder.append(receiver);
            builder.append(" filter=");
            builder.append(filter);
            builder.append("}");
            return builder.toString();
        }
    }

    private static class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent _intent, ArrayList<ReceiverRecord> _receivers) {
            intent = _intent;
            receivers = _receivers;
        }
    }

    private static final String TAG = "PluginLocalBroadcastManager";
    private static final boolean DEBUG = false;

    private final Context mAppContext;

    private final HashMap<BroadcastReceiver, ArrayList<IntentFilter>> mReceivers
            = new HashMap<BroadcastReceiver, ArrayList<IntentFilter>>();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions
            = new HashMap<String, ArrayList<ReceiverRecord>>();

    private final ArrayList<BroadcastRecord> mPendingBroadcasts
            = new ArrayList<BroadcastRecord>();

    static final int MSG_EXEC_PENDING_BROADCASTS = 1;

    private final Handler mHandler;

    private static final Object mLock = new Object();
    private static PluginLocalBroadcastManager mInstance;

    private static Object sOrigInstance;

    public static Object getInstance(Context context) {
        synchronized (mLock) {
            if (RePluginFramework.mHostInitialized) {
                try {
                    sOrigInstance = ProxyLocalBroadcastManagerVar.getInstance.call(null, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mInstance == null) {
                Context appContext = RePluginEnv.getHostContext();
                if (appContext == null) {
                    appContext = context.getApplicationContext();
                }
                mInstance = new PluginLocalBroadcastManager(appContext);
            }
            return mInstance;
        }
    }

    private PluginLocalBroadcastManager(Context context) {
        mAppContext = context;
        mHandler = new Handler(context.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_EXEC_PENDING_BROADCASTS:
                        executePendingBroadcasts();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    /**
     * Register a receive for any local broadcasts that match the given IntentFilter.
     *
     * @param receiver The BroadcastReceiver to handle the broadcast.
     * @param filter Selects the Intent broadcasts to be received.
     *
     * @see #unregisterReceiver
     */
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (RePluginFramework.mHostInitialized) {
            try {
                ProxyLocalBroadcastManagerVar.registerReceiver.call(sOrigInstance, receiver, filter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        synchronized (mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            ArrayList<IntentFilter> filters = mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList<IntentFilter>(1);
                mReceivers.put(receiver, filters);
            }
            filters.add(filter);
            for (int i=0; i<filter.countActions(); i++) {
                String action = filter.getAction(i);
                ArrayList<ReceiverRecord> entries = mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList<ReceiverRecord>(1);
                    mActions.put(action, entries);
                }
                entries.add(entry);
            }
        }
    }

    /**
     * Unregister a previously registered BroadcastReceiver.  <em>All</em>
     * filters that have been registered for this BroadcastReceiver will be
     * removed.
     *
     * @param receiver The BroadcastReceiver to unregister.
     *
     * @see #registerReceiver
     */
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (RePluginFramework.mHostInitialized) {
            try {
                ProxyLocalBroadcastManagerVar.unregisterReceiver.call(sOrigInstance, receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        synchronized (mReceivers) {
            ArrayList<IntentFilter> filters = mReceivers.remove(receiver);
            if (filters == null) {
                return;
            }
            for (int i=0; i<filters.size(); i++) {
                IntentFilter filter = filters.get(i);
                for (int j=0; j<filter.countActions(); j++) {
                    String action = filter.getAction(j);
                    ArrayList<ReceiverRecord> receivers = mActions.get(action);
                    if (receivers != null) {
                        for (int k=0; k<receivers.size(); k++) {
                            if (receivers.get(k).receiver == receiver) {
                                receivers.remove(k);
                                k--;
                            }
                        }
                        if (receivers.size() <= 0) {
                            mActions.remove(action);
                        }
                    }
                }
            }
        }
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *     Intent will receive the broadcast.
     *
     * @see #registerReceiver
     */
    public boolean sendBroadcast(Intent intent) {
        if (RePluginFramework.mHostInitialized) {
            try {
                return (Boolean) ProxyLocalBroadcastManagerVar.sendBroadcast.call(sOrigInstance, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        synchronized (mReceivers) {
            final String action = intent.getAction();
            final String type = intent.resolveTypeIfNeeded(
                    mAppContext.getContentResolver());
            final Uri data = intent.getData();
            final String scheme = intent.getScheme();
            final Set<String> categories = intent.getCategories();

            final boolean debug = DEBUG ||
                    ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
            if (debug) {
                Log.v(
                        TAG, "Resolving type " + type + " scheme " + scheme
                                + " of intent " + intent);
            }

            ArrayList<ReceiverRecord> entries = mActions.get(intent.getAction());
            if (entries != null) {
                if (debug) {
                    Log.v(TAG, "Action list: " + entries);
                }

                ArrayList<ReceiverRecord> receivers = null;
                for (int i=0; i<entries.size(); i++) {
                    ReceiverRecord receiver = entries.get(i);
                    if (debug) {
                        Log.v(TAG, "Matching against filter " + receiver.filter);
                    }

                    if (receiver.broadcasting) {
                        if (debug) {
                            Log.v(TAG, "  Filter's target already added");
                        }
                        continue;
                    }

                    int match = receiver.filter.match(action, type, scheme, data,
                            categories, "PluginLocalBroadcastManager");
                    if (match >= 0) {
                        if (debug) {
                            Log.v(TAG, "  Filter matched!  match=0x" +
                                    Integer.toHexString(match));
                        }
                        if (receivers == null) {
                            receivers = new ArrayList<ReceiverRecord>();
                        }
                        receivers.add(receiver);
                        receiver.broadcasting = true;
                    } else {
                        if (debug) {
                            String reason;
                            switch (match) {
                                case IntentFilter.NO_MATCH_ACTION: reason = "action"; break;
                                case IntentFilter.NO_MATCH_CATEGORY: reason = "category"; break;
                                case IntentFilter.NO_MATCH_DATA: reason = "data"; break;
                                case IntentFilter.NO_MATCH_TYPE: reason = "type"; break;
                                default: reason = "unknown reason"; break;
                            }
                            Log.v(TAG, "  Filter did not match: " + reason);
                        }
                    }
                }

                if (receivers != null) {
                    for (int i=0; i<receivers.size(); i++) {
                        receivers.get(i).broadcasting = false;
                    }
                    mPendingBroadcasts.add(new BroadcastRecord(intent, receivers));
                    if (!mHandler.hasMessages(MSG_EXEC_PENDING_BROADCASTS)) {
                        mHandler.sendEmptyMessage(MSG_EXEC_PENDING_BROADCASTS);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Like {@link #sendBroadcast(Intent)}, but if there are any receivers for
     * the Intent this function will block and immediately dispatch them before
     * returning.
     */
    public void sendBroadcastSync(Intent intent) {
        if (RePluginFramework.mHostInitialized) {
            try {
                ProxyLocalBroadcastManagerVar.sendBroadcastSync.call(sOrigInstance, intent);
            } catch (Exception e) {

            }
            return;
        }
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    private void executePendingBroadcasts() {
        while (true) {
            BroadcastRecord[] brs = null;
            synchronized (mReceivers) {
                final int N = mPendingBroadcasts.size();
                if (N <= 0) {
                    return;
                }
                brs = new BroadcastRecord[N];
                mPendingBroadcasts.toArray(brs);
                mPendingBroadcasts.clear();
            }
            for (BroadcastRecord br : brs) {
                for (int j=0; j<br.receivers.size(); j++) {
                    br.receivers.get(j).receiver.onReceive(mAppContext, br.intent);
                }
            }
        }
    }

    public static void registerReceiver(Object instance, BroadcastReceiver receiver, IntentFilter filter) {
        ((PluginLocalBroadcastManager) instance).registerReceiver(receiver, filter);
    }

    public static void unregisterReceiver(Object instance, BroadcastReceiver receiver) {
        ((PluginLocalBroadcastManager) instance).unregisterReceiver(receiver);
    }

    public static boolean sendBroadcast(Object instance, Intent intent) {
        return ((PluginLocalBroadcastManager) instance).sendBroadcast(intent);
    }

    public static void sendBroadcastSync(Object instance, Intent intent) {
        ((PluginLocalBroadcastManager) instance).sendBroadcastSync(intent);
    }

    public static class ProxyLocalBroadcastManagerVar {

        static MethodInvoker getInstance;

        static MethodInvoker registerReceiver;

        static MethodInvoker unregisterReceiver;

        static MethodInvoker sendBroadcast;

        static MethodInvoker sendBroadcastSync;

        public static void initLocked(final ClassLoader classLoader) {
            // 填充LocalBroadcastManager各方法
            final String localBroadcastManager = "android.support.v4.content.LocalBroadcastManager";
            getInstance = new MethodInvoker(classLoader, localBroadcastManager, "getInstance", new Class<?>[]{Context.class});
            registerReceiver = new MethodInvoker(classLoader, localBroadcastManager, "registerReceiver", new Class<?>[]{BroadcastReceiver.class, IntentFilter.class});
            unregisterReceiver = new MethodInvoker(classLoader, localBroadcastManager, "unregisterReceiver", new Class<?>[]{BroadcastReceiver.class});
            sendBroadcast = new MethodInvoker(classLoader, localBroadcastManager, "sendBroadcast", new Class<?>[]{Intent.class});
            sendBroadcastSync = new MethodInvoker(classLoader, localBroadcastManager, "sendBroadcastSync", new Class<?>[]{Intent.class});
        }
    }
}
