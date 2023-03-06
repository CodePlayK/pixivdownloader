package com.pixivdownloader.core.properties;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 文件路径属性
 *
 * @author hakace
 * @date 2022/10/31
 */
@Component
@Data
public class FilePathProperties {

    private static final FilePathProperties FILE_PATH_PROPERTIES = new FilePathProperties();

    String R18_PATH;
    String R18G_PATH;
    String R18_GIF_PATH;
    String R18G_GIF_PATH;
    String R18_COMIC_PATH;
    String R18G_COMIC_PATH;
    String R18G_NOVEL_RANKING;
    String R18_NOVEL_RANKING;
    String NOVEL_PATH;
    String NONEH_COMIC_PATH;
    String NONEH_PATH;
    String RANKING;
    String R34_PATH;


    private FilePathProperties() {

    }

    public static FilePathProperties getInstance() {
        return FILE_PATH_PROPERTIES;
    }

}
