package com.zzx.netty_server;

import com.zzx.netty_server.core.EchoServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Configuration
public class NettyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyServerApplication.class, args);
        try {
            new EchoServer(8457).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

}
