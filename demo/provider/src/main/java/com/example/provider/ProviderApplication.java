package com.example.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(value = {"classpath:dubbo-provider.xml"})
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
        try {
            //阻塞作用，否则会由于不是web项目，执行main方法后立即停止服务。
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
