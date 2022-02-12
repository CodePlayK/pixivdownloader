package com.pixivdownloader.core.properties;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class FilePathProperties {

    private static final FilePathProperties filePathProperties = new FilePathProperties();

    String R18PATH;
    String R18GPATH;
    String R18GIFPATH;
    String R18GGIFPATH;
    String NONEHPATH;
    String RANKING;


    private FilePathProperties() {

    }

    public static FilePathProperties getInstance() {

        return filePathProperties;
    }

}
