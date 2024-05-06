package com.seer.sdk.rbk;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class RbkPortClient {
    private final String host;
    private final Integer port;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean resetting = true;
    private Integer flowNoCounter = 0;
    private final Map<Integer, String> responseMap = new ConcurrentHashMap<>();
    private volatile NettyClient nettyClient = null;

    RbkPortClient(String host, Integer port){
        this.port = port;
        this.host = host;
    }

    RbkResult request(Integer apiNo, String reqStr, Long timeout) {
        RbkResult rbkResult = doRequest(apiNo, reqStr, timeout);
        if (rbkResult.getKind() != RbkResultKind.Ok) {
            log.info("请求失败，销毁 RBK 端口客户端，错误类型=$kind, apiNo=$apiNo");
            reset();
        }
        return rbkResult;
    }

    private RbkResult doRequest(int apiNo, String reqStr, Long timeout){
        timeout = timeout == null ? 10 * 1000 : timeout;
        lock.lock();
        try{
            NettyClient c;
            RbkResult.RbkResultBuilder rbkResultBuilder = RbkResult.builder()
                    .ip(host).apiNo(apiNo)
                    .reqStr(reqStr).resStr("");
            try {
                c = getNettyClient();
            } catch (Exception e) {
                log.error("connect error ip {} port {} {}", host, port, e.getMessage());
                return e instanceof InterruptedException ? rbkResultBuilder.kind(RbkResultKind.Interrupted).build()
                        : rbkResultBuilder.kind(RbkResultKind.ConnectFail).errMsg(e.getMessage()).build();
            }
            Integer flowNo = nextFlowNo(); // 流水号
            CompositeByteBuf reqBuf = RbkEncoder.buildReqBytes(apiNo, reqStr, flowNo);
            try {
                c.write(reqBuf);
            } catch (InterruptedException e) {
                log.error("doRequest write error ip {} port {} {}", host, port, e.getMessage());
                return rbkResultBuilder.kind(RbkResultKind.WriteError).errMsg(e.getMessage()).build();
            }

            while (!resetting) {
                try {
                    if(!condition.await(timeout, TimeUnit.MILLISECONDS)){
                        //等待超时
                        return rbkResultBuilder.kind(RbkResultKind.Timeout).errMsg("Timeout").build();
                    }
                } catch (InterruptedException e) {
                    // 等待结果被中断
                    log.error("Waiting for results to be interrupted ip {} port {} {}", host, port, e.getMessage());
                    return rbkResultBuilder.kind(RbkResultKind.Interrupted).errMsg("Timeout").build();
                }
                if(responseMap.containsKey(flowNo)){
                    String resStr = responseMap.remove(flowNo);
                    return rbkResultBuilder.kind(RbkResultKind.Ok).resStr(resStr).build();
                }
            }
            //已重置
            return rbkResultBuilder.kind(RbkResultKind.Disposed).build();
        }finally {
            lock.unlock();
        }

    }

    private NettyClient getNettyClient() throws Exception {
        NettyClient c = nettyClient;
        if (c == null) {
            c = createNettyClient();
            resetting = false;
            nettyClient = c;
        }
        return c;
    }

    private NettyClient createNettyClient() throws Exception {
        RbkInboundHandler handler = new RbkInboundHandler(this);
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        b.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RbkDecoder());
                        pipeline.addLast(handler);
                    }
                });
        try {
            ChannelFuture cf = b.connect().sync();
            return NettyClient.builder().eventLoopGroup(eventExecutors)
                    .channelFuture(cf).build();
        } catch (InterruptedException e) {
            log.error("createNettyClient error ip {} port {} {}", host, port, e.getMessage());
            eventExecutors.shutdownGracefully().sync();
            throw e;
        }
    }

     void onMessage(ChannelHandlerContext ctx, RbkFrame msg)  {
        responseMap.put(msg.getFlowNo(), msg.getBodyStr());
         lock.lock();
         try {
             condition.signalAll();
         }finally {
             lock.unlock();
         }

     }

     void onError(Throwable e) {
        log.error("netty error ", e.getMessage());
        reset();
    }

    private Integer nextFlowNo(){
        Integer no = (flowNoCounter + 1) % 512;
        flowNoCounter = no;
        return no;
    }

    private synchronized void reset() {
        responseMap.clear();
        if (resetting || nettyClient == null) {
            return;
        }
        // logger.debug("dispose rbk client start")
        resetting = true;
        NettyClient c = nettyClient;
        try {
            c.close();
        } catch (Exception e) {
            // ignore
        }
        nettyClient = null;
    }

    void dispose() {
        reset();
    }
}
