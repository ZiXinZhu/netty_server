package com.zzx.netty_server.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzx.netty_server.core.EchoServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Service
@Slf4j
@RequestMapping("/netty")
public class EchoController {

    Map<String,String> mapdata=new ConcurrentHashMap<>();


    @Autowired
    EchoServerHandler handler;




    /**
     * 农信
     * @param signKey
     * @param type 0微信 1支付宝
     * @param money
     * @param order
     * @return
     * @throws InterruptedException
     */
    @GetMapping(value = "/nongxinpay")
    public String nongxin(String type,String money,String order,String signKey) throws InterruptedException {
        System.out.println("下单数据："+signKey+type+money+order);
        String rule="{\"type\":\""+type+"\",\"money\":\""+money+"\",\"order\":\""+order+"\"}";
        try {
            sentmessage(signKey,rule);
            System.out.println("已成功发送："+rule);
        }catch (Exception e){
            log.info("Exception:"+e.getMessage());
            return "{order filed},{data:"+rule+"}";
        }

        for (int i=0;i<20;i++){
            log.info(mapdata.toString());
            Thread.sleep(300);
            String url=mapdata.get(order);
            log.info(url);
            if(null!=url){
                if(url.contains("payurl")){
                    mapdata.remove(order);
                    return url;
                }
            }

        }
        return "fail";
    }

    /**
     * 农信
     * @param string
     */
    @PostMapping(value = "/nongxin")
    public String nongxin(@RequestBody String string){
        System.out.println(string);
        JSONObject jsonObject= JSON.parseObject(string);
        System.out.println("收到数据："+jsonObject);
        mapdata.put(jsonObject.get("account").toString(),string);
        if(!string.equals("")){
            return "success";
        }
        return "fail";
    }


    /**
     * 测试发送接口
     * @return string
     */
    @GetMapping("/testSend")
    public String testSend(String key){
        String rule="{\"code\":\"200\",\"sign\":\"147258369\"}";
        return sentmessage(key,rule);
    }

    /**
     * 测试接收接口
     * @param string
     */
    @PostMapping(value = "/testReceive")
    public String  testReceive(@RequestBody String string){
        System.out.println("=>=>=>=>=>=>测试app收到数据："+string);
        return "success";
    }



    /**
     * 出站
     * @param signKey
     * @param rule
     * @return
     */
    private String sentmessage(String signKey ,String rule){
        ByteBuf byteBuffer= Unpooled.copiedBuffer(rule, Charset.forName("UTF-8"));
        String host=EchoServerHandler.mapkey.get(signKey);
        EchoServerHandler.map.get(host).writeAndFlush(byteBuffer.duplicate());

        return "发送成功！";
    }


}
