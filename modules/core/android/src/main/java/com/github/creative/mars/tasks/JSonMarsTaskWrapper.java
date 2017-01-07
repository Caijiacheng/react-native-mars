package com.github.creative.mars.tasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by caijiacheng on 06/01/2017.
 */
//TODO:
public class JSonMarsTaskWrapper extends MarsTaskWrapper {


    JsonObject request;
    JsonObject response;

    public JSonMarsTaskWrapper(JsonObject request, JsonObject response) {
        this.request = request;
        this.response = response;
    }



    @Override
    public Object getResponse() {
        return response;
    }

    @Override
    public Object getRequest() {
        return request;
    }

    @Override
    public void onTaskSend() {

    }

    @Override
    public void onTaskEnd() {

    }

    @Override
    public void onTaskCancel() {

    }

    @Override
    public void onTaskResponse() {

    }

    @Override
    public byte[] marshal() {
        try {
            final byte[] flatArray = request.toString().getBytes("utf-8");
            return flatArray;
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unmarshal(byte[] bytes) {

        try {
            response = new JsonParser().parse(new String(bytes, "utf-8")).getAsJsonObject();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
