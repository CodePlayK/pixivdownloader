package com.pixivdownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.pixivdownloader.core")
public class PixivdownloaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PixivdownloaderApplication.class, args);
    }

}
