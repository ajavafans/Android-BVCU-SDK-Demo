package com.smarteye.test;

public class Test {
    static{
        System.loadLibrary("testlib");
    }
    public static native int test(int method_id,int arg);
}
