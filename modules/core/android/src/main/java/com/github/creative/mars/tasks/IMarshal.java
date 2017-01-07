package com.github.creative.mars.tasks;

/**
 * Created by caijiacheng on 06/01/2017.
 */

public  interface IMarshal {

    byte[] marshal();

    void unmarshal(byte[] bytes);



}
