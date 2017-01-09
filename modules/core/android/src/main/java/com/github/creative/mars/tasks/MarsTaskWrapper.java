package com.github.creative.mars.tasks;

import android.os.Bundle;

import static com.github.creative.mars.tasks.MarsTaskProperty.OPTIONS_CGI_PATH;
import static com.github.creative.mars.tasks.MarsTaskProperty.OPTIONS_CHANNEL_LONG_SUPPORT;
import static com.github.creative.mars.tasks.MarsTaskProperty.OPTIONS_CHANNEL_SHORT_SUPPORT;
import static com.github.creative.mars.tasks.MarsTaskProperty.OPTIONS_CMD_ID;
import static com.github.creative.mars.tasks.MarsTaskProperty.OPTIONS_HOST;

/**
 * Created by caijiacheng on 06/01/2017.
 */

abstract  public class MarsTaskWrapper implements IMarshal{


    private Bundle properties = new Bundle();


    static final String TAG = MarsTaskWrapper.class.getName();


    public MarsTaskWrapper() {
    }


    public MarsTaskWrapper setHttpRequest(String host, String path) {
        properties.putString(OPTIONS_HOST, ("".equals(host) ? null : host));
        properties.putString(OPTIONS_CGI_PATH, path);
        return this;

    }

    public MarsTaskWrapper setLongChannelSupport(boolean support) {
        properties.putBoolean(OPTIONS_CHANNEL_LONG_SUPPORT, support);
        return this;
    }

    public MarsTaskWrapper setShortChannelSupport(boolean support) {
        properties.putBoolean(OPTIONS_CHANNEL_SHORT_SUPPORT, support);
        return this;
    }

    public MarsTaskWrapper setCmdID(int cmdID) {
        properties.putInt(OPTIONS_CMD_ID, cmdID);
        return this;
    }


    public Bundle getProperties() {
        return properties;
    }


    public abstract Object getResponse();
    public abstract Object getRequest();

    public abstract void onTaskSend();
    public abstract void onTaskEnd();
    public abstract void onTaskCancel();
    public abstract void onTaskResponse();
    public abstract void onTaskError(Throwable e);
//    public abstract void onTaskError(err)



}
