package com.github.creative.mars;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.github.creative.mars.tasks.MarsTaskProperty;
import com.github.creative.mars.tasks.TextMarsTaskWrapper;
import com.tencent.mars.BaseEvent;
import com.tencent.mars.Mars;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.sdt.SdtLogic;
import com.tencent.mars.stn.StnLogic;
import com.tencent.mars.xlog.Log;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MarsCoreModule extends ReactContextBaseJavaModule {

    MarsCoreStub stub = null;


    final static String TAG = MarsCoreModule.class.getName();


    public MarsCoreModule(ReactApplicationContext reactContext) {
        super(reactContext);

        LifecycleEventListener listener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {

                setForeground(true);

            }

            @Override
            public void onHostPause() {
                setForeground(false);
            }

            @Override
            public void onHostDestroy() {

                destroy();

            }
        };
        reactContext.addLifecycleEventListener(listener);

    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {

        final Map<String, Object> constants = new HashMap<>();

        return super.getConstants();
    }

    @ReactMethod
    public void init(ReadableMap profile) {
        if (stub != null) {
            return;
        }

        stub = new MarsCoreStub(getReactApplicationContext());

        AppLogic.setCallBack(stub);
        StnLogic.setCallBack(stub);
        SdtLogic.setCallBack(stub);

        Mars.init(getReactApplicationContext(), new Handler(Looper.getMainLooper()));

        ReadableArray longLinkPorts = profile.getArray("longLinkPorts");
        int[] ports = new int[longLinkPorts.size()];
        for (int i = 0; i < longLinkPorts.size(); i++) {
            ports[i] = longLinkPorts.getInt(i);
        }
        StnLogic.setLonglinkSvrAddr(profile.getString("longLinkHost"), ports);
        StnLogic.setShortlinkSvrAddr(profile.getInt("shortLinkPort"));
        StnLogic.setClientVersion(profile.getInt("clientVersion"));
        Mars.onCreate(true);

        BaseEvent.onForeground(true);

        StnLogic.makesureLongLinkConnected();

    }


    @ReactMethod
    public void notifyNetworkChange() {
        if (stub != null) {
            BaseEvent.onNetworkChange();
        }
    }


    @ReactMethod
    public void send(final String data, final ReadableMap properties, final Promise promise) {
        TextMarsTaskWrapper taskWrapper = new TextMarsTaskWrapper(data) {
            @Override
            public void onTaskSend() {
                Log.e(TAG, String.format("onSend : data => %s, properties => %s", data, properties.toString()));
            }

            @Override
            public void onTaskEnd() {
                Log.e(TAG, String.format("onEnd : data => %s, properties => %s", data, properties.toString()));
            }

            @Override
            public void onTaskCancel() {
                Log.e(TAG, String.format("onTaskCancel : data => %s, properties => %s", data, properties.toString()));
                promise.reject("-1", new Exception("taskCancel"));
            }

            @Override
            public void onTaskResponse() {
                Log.e(TAG, String.format("onTaskCancel : data => %s, properties => %s, response => %s", data, properties.toString(), getResponse().toString()));
                promise.resolve(getResponse());
            }

            @Override
            public void onTaskError(Throwable e) {
                promise.reject("-1", e);
            }
        };

        Bundle taskProp = Arguments.toBundle(properties);
        taskWrapper.setHttpRequest(taskProp.getString(MarsTaskProperty.OPTIONS_HOST, ""),
                taskProp.getString(MarsTaskProperty.OPTIONS_CGI_PATH, "/"));
        taskWrapper.setLongChannelSupport(
                taskProp.getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_LONG_SUPPORT, false));
        taskWrapper.setShortChannelSupport(
                taskProp.getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_SHORT_SUPPORT, true));
        taskWrapper.setCmdID(
                taskProp.getInt(MarsTaskProperty.OPTIONS_CMD_ID, -1));

        stub.send(taskWrapper);

    }


    public void destroy() {
        if (stub != null) {
            Mars.onDestroy();
            stub = null;
        }
    }

    public void setForeground(boolean isForeground) {
        if (stub != null) {
            BaseEvent.onForeground(isForeground);
        }
    }


    @Override
    public String getName() {
        return "MarsCore";
    }


}
