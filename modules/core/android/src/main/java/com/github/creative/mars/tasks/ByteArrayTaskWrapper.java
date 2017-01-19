package com.github.creative.mars.tasks;

/**
 * Created by caijiacheng on 06/01/2017.
 */

abstract  public class ByteArrayTaskWrapper extends MarsTaskWrapper{


    protected  byte[] request;
    protected  byte[] response;


    public ByteArrayTaskWrapper(byte[] request) {
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
        return request;
    }

    @Override
    public void unmarshal(byte[] bytes) {
        response = bytes;
    }
}
