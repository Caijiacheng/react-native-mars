package com.github.creative.mars.tasks;

import java.nio.charset.Charset;

/**
 * Created by caijiacheng on 06/01/2017.
 */

abstract  public class TextMarsTaskWrapper extends MarsTaskWrapper{


    protected  String request;
    protected  String response;


    public TextMarsTaskWrapper(String request) {
        this.request = request;
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
    public byte[] marshal() {
        return request.toString().getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public void unmarshal(byte[] bytes) {
        response = new String(bytes, Charset.forName("UTF-8"));
    }
}
