package com.zzx.netty_server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzx.netty_server.core.EchoServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


@RestController
@Service
@Slf4j
@Data
@ToString
@ConfigurationProperties(prefix = "netty")
@RequestMapping("/netty")
public class EchoController {

    Queue<String> queue = new ConcurrentLinkedDeque<>();
    Map<String, String> mapdata = new ConcurrentHashMap<>();
    private String host;


    @Autowired
    private RestTemplate template;


    /**
     * 固码
     *
     * @param signKey
     * @return
     */
    @GetMapping(value = "/fixdeCode")
    public String send(String signKey, String bili) {
        log.info("固码下单数据signKey:{},bili:{}", signKey, bili);
        String rule = "{\"100\":\"8\",\"200\":\"5\",\"500\":\"4\",\"1000\":\"3\"}";
        if ((bili != null) && (!bili.equals(""))) {
            rule = bili;
        }
        return sentmessage(signKey, rule);
    }


    /**
     * 固码
     *
     * @param string
     */
    @PostMapping(value = "/inserts")
    public void inserts(@RequestBody String string) {
        System.out.println("固码收到支付宝数据：" + string);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject params = JSON.parseObject(string);
        HttpEntity<String> requestEntity = new HttpEntity<>(params.toJSONString(), headers);
        ResponseEntity<String> response = null;
        log.info("开始请求！");
        try {
            String url = "http://" + host + ":8983/iromMan/inserts";
            log.info("url:" + url);
            response = template.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("请求通道结果：" + response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 农信
     *
     * @param signKey
     * @param type    0微信 1支付宝
     * @param money
     * @param order
     * @return
     * @throws InterruptedException
     */
    @GetMapping(value = "/nongxinpay")
    public String nongxin(String type, String money, String order, String signKey) throws InterruptedException {
        System.out.println("下单数据：" + signKey + type + money + order);
        String rule = "{\"type\":\"" + type + "\",\"money\":\"" + money + "\",\"order\":\"" + order + "\"}";
        try {
            sentmessage(signKey, rule);
            System.out.println("已成功发送：" + rule);
        } catch (Exception e) {
            log.info("Exception:" + e.getMessage());
            return "{order filed},{data:" + rule + "}";
        }
        for (int i = 0; i < 20; i++) {
            Thread.sleep(300);
            String url = mapdata.get(order);
            log.info("得到支付链接：" + url);
            if (null != url) {
                if (url.contains("payurl")) {
                    mapdata.remove(order);
                    return url;
                }
            }

        }
        return "fail";
    }


    /**
     * 农信
     *
     * @param string
     */
    @PostMapping(value = "/nongxin")
    public String nongxin(@RequestBody String string) {
        System.out.println(string);
        JSONObject jsonObject = JSON.parseObject(string);
        System.out.println("收到数据：" + jsonObject);
        mapdata.put(jsonObject.get("account").toString(), string);
        if (!string.equals("")) {
            return "success";
        }
        return "fail";
    }


    /**
     * 测试发送接口
     *
     * @return string
     * boy:
     * http://47.106.182.147:8234/netty/testSend?key=555555
     * kub:
     * http://120.79.239.138:8234/netty/testSend?key=123457
     */
    @GetMapping("/testSend")
    public String testSend(String key) throws InterruptedException {
        String rule = "{\"code\":\"200\",\"sign\":\"147258369\"}";
        sentmessage(key, rule);
        String msg = queue.poll();
        for(int i=0; i<10; i++){
            Thread.sleep(500);
            if(msg!=null){
                break;
            }
        }
        return msg;
    }


    /**
     * 测试接收接口
     *
     * @param string
     */
    @PostMapping(value = "/testReceive")
    public String testReceive(@RequestBody String string) {
        System.out.println("=>=>=>=>=>=>测试app收到数据：" + string);
        queue.add(string);
        return "success";
    }


    /**
     * 出站
     *
     * @param signKey
     * @param rule
     * @return
     */
    private String sentmessage(String signKey, String rule) {
        ByteBuf byteBuffer = Unpooled.copiedBuffer(rule, Charset.forName("UTF-8"));
        String host = EchoServerHandler.mapkey.get(signKey);
        EchoServerHandler.map.get(host).writeAndFlush(byteBuffer.duplicate());
        return "发送成功！";
    }

}
