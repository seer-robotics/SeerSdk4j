package com.seer.sdk.rbk;

public class BasicConstant {
    public static final byte START_MARK = 0x5A;     // 同步头
    public static final int HEAD_SIZE = 16;
    public static final byte PROTO_VERSION = 0x01;  //  版本号
    public static byte[] RBKHEADERRESERVED = {0, 0, 0, 0, 0, 0};//保留区
}
