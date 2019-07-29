package com.zzx.netty_server;

import com.zzx.netty_server.core.EchoServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyServerApplication.class, args);
        try {
            new EchoServer(8457).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
