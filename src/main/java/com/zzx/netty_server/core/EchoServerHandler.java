package com.zzx.netty_server.core;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzx.netty_server.controller.EchoController;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    //以客户端ip+端口为键，对应的通道为值，存在map中。便于主动向客户端发信息
    public static Map<String, Channel> map=new HashMap<>();
    public static Map<String, String> mapkey=new HashMap<>();

    @Autowired
    EchoController echoController;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当有客户端连上时调用该方法
        String client= String.valueOf(ctx.channel().remoteAddress());
        Channel channel=ctx.channel();
        map.put(client,channel);
        System.out.println("client:"+client);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //当服务器端收到客户端消息时调用该方法
        ByteBuf in=(ByteBuf)msg;
        String message=in.toString(Charset.forName("UTF-8"));
        System.out.println("服务器收到消息："+message);
        if(message.contains("signKey")){
            JSONObject jsonObject= JSON.parseObject(message);
            String client= String.valueOf(ctx.channel().remoteAddress());
            String key= String.valueOf(jsonObject.get("signKey"));
            System.out.println("signKey"+key);
            mapkey.put(key,client);
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //当客户端发生异常时调用该方法
        map.remove(ctx.channel().remoteAddress());
        System.out.println("host:"+ctx.channel().remoteAddress()+"链接断开");
    }

}
