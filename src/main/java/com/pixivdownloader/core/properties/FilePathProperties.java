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

    String R18PATH;
    String R18GPATH;
    String R18GIFPATH;
    String R18GGIFPATH;
    String R18COMICPATH;
    String R18GCOMICPATH;
    String R18GNOVELRANKING;
    String R18NOVELRANKING;
    String NOVELPATH;
    String NONEHCOMICPATH;
    String NONEHPATH;
    String RANKING;


    private FilePathProperties() {

    }

    public static FilePathProperties getInstance() {
        return FILE_PATH_PROPERTIES;
    }

}
