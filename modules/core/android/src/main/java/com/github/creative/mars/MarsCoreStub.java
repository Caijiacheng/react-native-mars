package com.github.creative.mars;

import android.content.Context;
import android.os.RemoteException;

import com.github.creative.mars.tasks.MarsTaskProperty;
import com.github.creative.mars.tasks.MarsTaskWrapper;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.sdt.SdtLogic;
import com.tencent.mars.stn.StnLogic;
import com.tencent.mars.xlog.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by caijiacheng on 06/01/2017.
 */

public class MarsCoreStub implements StnLogic.ICallBack, AppLogic.ICallBack, SdtLogic.ICallBack {


    private static final String TAG = MarsCoreStub.class.getName();

    Context context = null;

    MarsCoreStub(Context ctx) {
        this.context = ctx;
    }


    public static final String DEVICE_NAME = android.os.Build.MANUFACTURER + "-" + android.os.Build.MODEL;
    public static final String DEVICE_TYPE = "android-" + android.os.Build.VERSION.SDK_INT;
    private AppLogic.AccountInfo accountInfo = new AppLogic.AccountInfo();
    private AppLogic.DeviceInfo deviceInfo = new AppLogic.DeviceInfo(DEVICE_NAME, DEVICE_TYPE);
    private int clientVersion = 200;
    private String appFilePath = null;

    private Map<Integer, MarsTaskWrapper> mapID2Task = new ConcurrentHashMap<>();
    private Map<MarsTaskWrapper, Integer> mapTask2ID = new ConcurrentHashMap<>();


    public void setAppLogicAccountInfo(AppLogic.AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public void setAppLogicDeviceInfo(AppLogic.DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setClientVersion(int clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setAppFilePath(String appFilePath) {
        this.appFilePath = appFilePath;
    }


    public void send(MarsTaskWrapper taskWrapper) throws Exception {
        final StnLogic.Task task = new StnLogic.Task(StnLogic.Task.EShort, 0, "", null);

        // Set host & cgi path
        final String host = taskWrapper.getProperties().getString(MarsTaskProperty.OPTIONS_HOST);
        final String cgiPath = taskWrapper.getProperties().getString(MarsTaskProperty.OPTIONS_CGI_PATH);
        task.shortLinkHostList = new ArrayList<>();
        task.shortLinkHostList.add(host);
        task.cgi = cgiPath;

        final boolean shortSupport = taskWrapper.getProperties().getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_SHORT_SUPPORT, true);
        final boolean longSupport = taskWrapper.getProperties().getBoolean(MarsTaskProperty.OPTIONS_CHANNEL_LONG_SUPPORT, false);
        if (shortSupport && longSupport) {
            task.channelSelect = StnLogic.Task.EBoth;

        } else if (shortSupport) {
            task.channelSelect = StnLogic.Task.EShort;

        } else if (longSupport) {
            task.channelSelect = StnLogic.Task.ELong;

        } else {
            Log.e(TAG, "invalid channel strategy");
            throw new Exception("Invalid Channel Strategy");
        }

        // Set cmdID if necessary
        int cmdID = taskWrapper.getProperties().getInt(MarsTaskProperty.OPTIONS_CMD_ID, -1);
        if (cmdID != -1) {
            task.cmdID = cmdID;
        }

        mapID2Task.put(task.taskID, taskWrapper);
        mapTask2ID.put(taskWrapper, task.taskID);

        // Send
        Log.i(TAG, "now start task with id %d", task.taskID);
        StnLogic.startTask(task);
        if (StnLogic.hasTask(task.taskID)) {
            Log.i(TAG, "stn task started with id %d", task.taskID);

        } else {
            Log.e(TAG, "stn task start failed with id %d", task.taskID);
            throw new Exception("stn task start failed with id " + task.taskID);
        }

    }


    public void cancel(MarsTaskWrapper taskWrapper) throws RemoteException {
        if (taskWrapper == null) {
            Log.e(TAG, "cannot cancel null wrapper");
            return;
        }

        final Integer taskID = mapTask2ID.remove(taskWrapper);
        if (taskID == null) {
            Log.w(TAG, "cancel null taskID wrapper");

        } else {
            Log.d(TAG, "cancel wrapper with taskID=%d using stn stop", taskID);
            StnLogic.stopTask(taskID);
            taskWrapper.onTaskCancel();
            mapID2Task.remove(taskID); // TODO: check return
        }
    }

    @Override
    public String getAppFilePath() {

        if (appFilePath != null) {
            return appFilePath;
        }
        try {
            File file = context.getFilesDir();
            if (!file.exists()) {
                file.createNewFile();
            }
            return file.toString();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    @Override
    public AppLogic.AccountInfo getAccountInfo() {
        return accountInfo;
    }

    @Override
    public int getClientVersion() {
        return clientVersion;
    }

    @Override
    public AppLogic.DeviceInfo getDeviceType() {
        return deviceInfo;
    }

    @Override
    public void reportSignalDetectResults(String resultsJson) {

    }

    @Override
    public boolean makesureAuthed() {
        return false;
    }

    @Override
    public String[] onNewDns(String host) {
        return null;
    }

    @Override
    public void onPush(int cmdid, byte[] data) {

    }

    @Override
    public boolean req2Buf(int taskID, Object userContext, ByteArrayOutputStream reqBuffer, int[] errCode, int channelSelect) {

        final MarsTaskWrapper taskWrapper = mapID2Task.get(taskID);
        if (taskWrapper == null) {
            Log.e(TAG, "invalid req2Buf for task, taskID=%d", taskID);
            return false;
        }

        try {
            reqBuffer.write(taskWrapper.marshal());
            taskWrapper.onTaskSend();
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "task wrapper req2buf failed for short, check your encode process");
        }

        return false;
    }

    @Override
    public int buf2Resp(int taskID, Object userContext, byte[] respBuffer, int[] errCode, int channelSelect) {

        final MarsTaskWrapper taskWrapper = mapID2Task.get(taskID);
        if (taskWrapper == null) {
            Log.e(TAG, "buf2Resp: wrapper not found for stn task, taskID=%", taskID);
            return StnLogic.RESP_FAIL_HANDLE_TASK_END;
        }

        try {
            taskWrapper.unmarshal(respBuffer);
            taskWrapper.onTaskResponse();
            return StnLogic.RESP_FAIL_HANDLE_NORMAL;

        }catch (Throwable e) {
            Log.e(TAG, "remote wrapper disconnected, clean this context, taskID=%d", taskID);
            MarsTaskWrapper taskToRemove = mapID2Task.remove(taskID);
            if (taskToRemove != null) {
                mapTask2ID.remove(taskToRemove);
            }

        }
        return StnLogic.RESP_FAIL_HANDLE_TASK_END;

    }

    @Override
    public int onTaskEnd(int taskID, Object userContext, int errType, int errCode) {
        final MarsTaskWrapper wrapper = mapID2Task.remove(taskID);
        if (wrapper == null) {
            Log.w(TAG, "stn task onTaskEnd callback may fail, null wrapper, taskID=%d", taskID);
            return 0; //
        }

        try {
            wrapper.onTaskEnd();
        } finally {
            mapTask2ID.remove(wrapper); // onTaskEnd will be called only once for each task
        }

        return 0;
    }

    @Override
    public void reportFlow(int wifiRecv, int wifiSend, int mobileRecv, int mobileSend) {

    }

    @Override
    public void reportConnectInfo(int status, int longlinkstatus) {

    }

    @Override
    public int getLongLinkIdentifyCheckBuffer(ByteArrayOutputStream identifyReqBuf, ByteArrayOutputStream hashCodeBuffer, int[] reqRespCmdID) {
        return 0;
    }

    @Override
    public boolean onLongLinkIdentifyResp(byte[] buffer, byte[] hashCodeBuffer) {
        return false;
    }

    @Override
    public void requestDoSync() {

    }

    @Override
    public String[] requestNetCheckShortLinkHosts() {
        return new String[0];
    }

    @Override
    public boolean isLogoned() {
        return false;
    }

    @Override
    public void reportTaskProfile(String taskString) {

    }
}
