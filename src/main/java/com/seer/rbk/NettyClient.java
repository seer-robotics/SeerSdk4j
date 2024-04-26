package com.seer.rbk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import lombok.Builder;
import lombok.Data;
import java.util.concurrent.TimeUnit;

@Data
@Builder
 class NettyClient {
    private EventLoopGroup eventLoopGroup;
    private ChannelFuture channelFuture;

    public void write(ByteBuf bf) throws InterruptedException {
        ByteBufUtil.prettyHexDump(bf);
        channelFuture.channel().writeAndFlush(bf).await(5000, TimeUnit.MILLISECONDS);
    }
    public void close() {
        try {
            channelFuture.channel().close();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // ignore
        } finally {
            try {
                eventLoopGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
