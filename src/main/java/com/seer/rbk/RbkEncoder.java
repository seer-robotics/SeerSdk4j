package com.seer.rbk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;

import java.nio.charset.StandardCharsets;

class RbkEncoder {
    public static CompositeByteBuf buildReqBytes(Integer apiNo, String bodyStr, Integer flowNo) {
        byte[] bodyBytes = bodyStr.getBytes(StandardCharsets.UTF_8);
        ByteBuf headBuf = Unpooled.buffer(BasicConstant.HEAD_SIZE);
        ByteBuf bodyBuf = Unpooled.copiedBuffer(bodyBytes);
        headBuf.writeByte(BasicConstant.START_MARK);
        headBuf.writeByte(BasicConstant.PROTO_VERSION);
        headBuf.writeShort(flowNo);                         //序号
        headBuf.writeInt(bodyBytes.length);
        headBuf.writeShort(apiNo);
        headBuf.writeBytes(BasicConstant.RBKHEADERRESERVED);//保留区
        CompositeByteBuf reqBuf = Unpooled.compositeBuffer();
        reqBuf.addComponent(true, headBuf);
        reqBuf.addComponent(true, bodyBuf);
        return reqBuf;
    }
}
