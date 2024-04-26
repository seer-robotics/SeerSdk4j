package com.seer.rbk;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
 class RbkDecoder extends ByteToMessageDecoder {
    private volatile boolean started = false;

    private volatile int flowNo = 0;

    private volatile int apiNo = 0;

    private volatile int bodySize = -1;
    //自定义解码
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> out) throws Exception {
        if(!started){
            while (buf.readableBytes() > 0){//
                if(buf.readByte() == BasicConstant.START_MARK){//如果不相等死循环，无法退出
                    started = true;
                    break;
                }
            }
        }
        if (!started) return;
        if(bodySize < 0 && buf.readableBytes() >= 15){
            buf.readByte();             // 协议版本
            flowNo = buf.readShort();   // 序号
            bodySize = buf.readInt();   //数据区长度
            apiNo = buf.readShort();    // 编号
            buf.readerIndex(buf.readerIndex() + 6);
        }
        if(bodySize < 0) return;
        if(buf.readableBytes() < bodySize) return;
        String bodyStr = bodySize == 0 ? "" : buf.readCharSequence(bodySize, StandardCharsets.UTF_8).toString();
        out.add(RbkFrame.builder().flowNo(flowNo).apiNo(apiNo).bodyStr(bodyStr).build());
        started = false;
        flowNo = -1;
        bodySize = -1;
    }
}
