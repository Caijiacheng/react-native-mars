package com.github.creative.mars;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.sdt.SdtLogic;
import com.tencent.mars.stn.StnLogic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MarsCoreModule extends ReactContextBaseJavaModule implements StnLogic.ICallBack, SdtLogic.ICallBack, AppLogic.ICallBack{
    public MarsCoreModule(ReactApplicationContext reactContext) {
        super(reactContext);

        final ReactApplicationContext ctx = reactContext;


        LifecycleEventListener listener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {

            }

            @Override
            public void onHostPause() {
            }

            @Override
            public void onHostDestroy() {
            }
        };

        reactContext.addLifecycleEventListener(listener);
    }

    @Override
    public String getName() {
        return "MarsCore";
    }


}
