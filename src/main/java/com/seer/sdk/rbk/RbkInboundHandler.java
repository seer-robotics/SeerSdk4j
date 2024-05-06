package com.seer.sdk.rbk;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

class RbkInboundHandler extends SimpleChannelInboundHandler<RbkFrame> {
    private RbkPortClient client;

    public RbkInboundHandler(RbkPortClient client) {
        this.client = client;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RbkFrame msg) throws Exception {
        try {
            client.onMessage(ctx, msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            ctx.close();
        } catch (Exception e) {
            //
        }
        client.onError(cause);
    }

}
