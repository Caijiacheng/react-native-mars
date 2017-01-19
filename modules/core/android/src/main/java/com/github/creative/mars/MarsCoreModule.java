package com.github.creative.mars;

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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.github.creative.mars.BaseConstants.CGIHISTORY_CMDID;
import static com.github.creative.mars.BaseConstants.CONNSTATUS_CMDID;
import static com.github.creative.mars.BaseConstants.FLOW_CMDID;
import static com.github.creative.mars.BaseConstants.PUSHMSG_CMDID;
import static com.github.creative.mars.BaseConstants.SDTRESULT_CMDID;

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
    public void handleRecvMessage(int cmdid, byte[] data) {
        WritableMap json = Arguments.createMap();
        json.putInt("cmdid", cmdid);
        json.putArray("buffer", toReadableArray(data));
        mEventEmitter.emit(Events.EVENT_ON_PUSH.toString(), json);
    }

    @Override
    public void handleRecvStatistic(int cmdid, String info) {
        if (mEnableStat) {
            WritableMap json = Arguments.createMap();
            json.putInt("cmdid", cmdid);
            json.putString("stat", info);
            mEventEmitter.emit(Events.EVENT_ON_STAT.toString(), json);
        }
    }


    public enum Events {
        EVENT_ON_PUSH("onMarsPush"),
        EVENT_ON_STAT("onMarsStat");

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
        constants.put("PUSHMSG_CMDID", PUSHMSG_CMDID);
        constants.put("FLOW_CMDID", FLOW_CMDID);
        constants.put("CGIHISTORY_CMDID", CGIHISTORY_CMDID);
        constants.put("CONNSTATUS_CMDID", CONNSTATUS_CMDID);
        constants.put("SDTRESULT_CMDID", SDTRESULT_CMDID);
        return constants;
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
        stub.setOnPushListener(this);

        if (BuildConfig.DEBUG)
        {
            Log.e(TAG, "MarsCoreModule init ok. longHost => " + profile.getString("longLinkHost"));
            Log.e(TAG, "shortLinkPort " + profile.getInt("shortLinkPort") + " longLinkPort: " + ports[0]);
        }

    }


    @ReactMethod
    public void notifyNetworkChange() {
        if (stub != null) {
            BaseEvent.onNetworkChange();
        }
    }

    private boolean mEnableStat = false;
    @ReactMethod
    public void setStatisticEnable(boolean enable) {
        mEnableStat = enable;
    }



    static public byte[] toByteArray(ReadableArray data) {

        byte[] bytes = new byte[data.size()];

        for (int i = 0 ; i < data.size(); i ++ ) {
            bytes[i] = (byte) data.getInt(i);
        }
        return bytes;
    }

    static public WritableArray toReadableArray(byte[] bytes) {
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
            public void onTaskEnd(int errType, int errCode) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, String.format("onEnd :  properties => %s. errType => %d, errCode => %d",
                        properties.toString(), errType, errCode));
                if (errType != 0 || errCode != 0 )
                {
                    promise.reject("-1", new Exception("Task Error: errType: " + errType + " errCode: " + errCode));
                }
            }

            @Override
            public void onTaskCancel() {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, String.format("onTaskCancel : data => %s, properties => %s",
                        MemoryDump.dumpHex(toByteArray(data)),
                        properties.toString()));
                promise.reject("-1", new Exception("taskCancel"));
            }

            @Override
            public void onTaskResponse() {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, String.format("onTaskResponse : data => %s, properties => %s, response => %s", MemoryDump.dumpHex(toByteArray(data)), properties.toString(),
                            MemoryDump.dumpHex(response)));
                byte[] response = (byte[]) getResponse();
                promise.resolve(toReadableArray(response));
            }
        };


        taskWrapper.setHttpRequest(properties.getString(MarsTaskProperty.OPTIONS_HOST),
                properties.getString(MarsTaskProperty.OPTIONS_CGI_PATH));
        taskWrapper.setLongChannelSupport(
                properties.getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_LONG_SUPPORT));
        taskWrapper.setShortChannelSupport(
                properties.getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_SHORT_SUPPORT));
        taskWrapper.setCmdID(
                properties.getInt(MarsTaskProperty.OPTIONS_CMD_ID));

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
