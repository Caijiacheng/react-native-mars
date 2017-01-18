package com.github.creative.mars;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.github.creative.mars.tasks.ByteArrayTaskWrapper;
import com.github.creative.mars.tasks.MarsTaskProperty;
import com.tencent.mars.BaseEvent;
import com.tencent.mars.Mars;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.sdt.SdtLogic;
import com.tencent.mars.stn.StnLogic;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MarsCoreModule extends ReactContextBaseJavaModule implements MarsCoreStub.onPushListener {

    MarsCoreStub stub = null;


    final static String TAG = MarsCoreModule.class.getName();
    private DeviceEventManagerModule.RCTDeviceEventEmitter mEventEmitter;

    public MarsCoreModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @Override
    public void initialize() {

        mEventEmitter = getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

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
        getReactApplicationContext().addLifecycleEventListener(listener);

    }

    @Override
    public void onRecvPush(int cmdid, byte[] data) {
        WritableMap json = Arguments.createMap();
        json.putInt("cmdid", cmdid);
        json.putString("data", new String(data, Charset.forName("UTF-8")));
        mEventEmitter.emit(Events.EVENT_ON_PUSH.toString(), json);
    }


    public enum Events {
        EVENT_ON_PUSH("onMarsPush");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
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

//        System.loadLibrary("stlport_shared");

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
        stub.setOnPushListener(this);

        Log.e(TAG, "MarsCoreModule init ok. longHost => " + profile.getString("longLinkHost"));
        Log.e(TAG, "shortLinkPort " + profile.getInt("shortLinkPort") + " longLinkPort: " + ports[0]);
    }


    @ReactMethod
    public void notifyNetworkChange() {
        if (stub != null) {
            BaseEvent.onNetworkChange();
        }
    }


    static public byte[] toByteArray(ReadableArray data) {

        byte[] bytes = new byte[data.size()];

        for (int i = 0 ; i < data.size(); i ++ ) {
            bytes[i] = (byte) data.getInt(i);
        }
        return bytes;
    }

    static public ReadableArray toReadableArray(byte[] bytes) {
        WritableArray array = Arguments.createArray();
        for (int i = 0; i < bytes.length; i++) {
            array.pushInt(bytes[i]);
        }
        return array;
    }


    @ReactMethod
    public void send(final ReadableArray data, final ReadableMap properties, final Promise promise) {



        ByteArrayTaskWrapper taskWrapper = new ByteArrayTaskWrapper(toByteArray(data)) {
            @Override
            public void onTaskSend() {
                Log.e(TAG, String.format("onSend : data => %s, properties => %s",
                        MemoryDump.dumpHex(toByteArray(data)), properties.toString()));
            }

            @Override
            public void onTaskEnd(int errType, int errCode) {
                Log.e(TAG, String.format("onEnd : data => %s, properties => %s. errType => %d, errCode => %d",
                        data, properties.toString(), errType, errCode));
                if (errType != 0 || errCode != 0 )
                {
                    promise.reject("-1", new Exception("Task Error: errType: " + errType + " errCode: " + errCode));
                }
            }

            @Override
            public void onTaskCancel() {
                Log.e(TAG, String.format("onTaskCancel : data => %s, properties => %s",
                        MemoryDump.dumpHex(toByteArray(data)),
                        properties.toString()));
                promise.reject("-1", new Exception("taskCancel"));
            }

            @Override
            public void onTaskResponse() {

                byte[] response = (byte[]) getResponse();
                Log.e(TAG, String.format("onTaskResponse : data => %s, properties => %s, response => %s", MemoryDump.dumpHex(toByteArray(data)), properties.toString(),
                        MemoryDump.dumpHex(response)));
                promise.resolve(toReadableArray(response));
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
        return "MarsCoreModule";
    }


}
