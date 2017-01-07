package com.github.creative.mars;

import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.mars.BaseEvent;
import com.tencent.mars.Mars;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.sdt.SdtLogic;
import com.tencent.mars.stn.StnLogic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MarsCoreModule extends ReactContextBaseJavaModule {

    MarsCoreStub stub = null;



    private static final String TASK_JSON_WRAPPER = "JSON";
    private static final String TASK_PB_WRAPPER = "PB";


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
//        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
//        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);

        return super.getConstants();
    }

    @ReactMethod
    public void init(ReadableMap profile)
    {
        if (stub != null) {
            return;
        }

        stub = new MarsCoreStub(getReactApplicationContext());

        AppLogic.setCallBack(stub);
        StnLogic.setCallBack(stub);
        SdtLogic.setCallBack(stub);

        Mars.init(getReactApplicationContext(), new Handler(Looper.getMainLooper()));

        ReadableArray longLinkPorts = profile.getArray("longLinkPorts");
        int [] ports = new int[longLinkPorts.size()];
        for (int i = 0; i < longLinkPorts.size(); i ++) {
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
    public void send() {

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
