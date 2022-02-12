package com.pixivdownloader.core.utils;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class GifUtils {

    public  synchronized void jpgToGif(String[] pic, String newPic) {
        try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);
            e.start(newPic);
            BufferedImage[] src = new BufferedImage[pic.length];
            for (int i = 0; i < src.length; i++) {
                e.setDelay(100); //设置播放的延迟时间
                src[i] = ImageIO.read(new File(pic[i])); // 读入需要播放的jpg文件
                e.addFrame(src[i]);  //添加到帧中
            }
            e.finish();
        } catch (Exception e) {
            System.out.println("jpgToGif Failed:");
            e.printStackTrace();
        }
    }

}
