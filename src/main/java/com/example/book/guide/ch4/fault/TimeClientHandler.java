package com.example.book.guide.ch4.fault;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alan Yin
 * @date 2020/7/16
 */
@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {

    private int counter;

    private byte[] req;

    /**
     * Creates a client-side handler.
     */
    public TimeClientHandler() {
        req = ("QUERY TIME ORDER" + System.getProperty("line.separator"))
                .getBytes();
    }

    // 变化点
    // 33-40,客户端与服务端链路建立成功后，循环发送 100 条消息，每发送一条就刷新一次，保证每条消息都写入 Channel中
    // 按照设计，服务端应该接收到 100 条请求消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf message = null;
        for (int i = 0; i < 100; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        // 变化点
        // 客户端每接收服务端一次应答，就打印一次计数，按照设计应该打印 100 次
        System.out.println("Now is : " + body + " ; the counter is : " + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 释放资源
        log.warn(("Unexpected exception from downstream : " + cause.getMessage()));
        ctx.close();
    }
}
