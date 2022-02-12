package com.pixivdownloader;

import com.pixivdownloader.core.service.PicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class PixivdownloaderApplicationTests {

    @Autowired
    private PicService picService;

    @Test
    void contextLoads() throws IOException {
    }
    @Test
    void test1() throws IOException {
    }

}
