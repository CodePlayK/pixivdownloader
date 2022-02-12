package com.pixivdownloader.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "path", ignoreUnknownFields = false)
@PropertySource("classpath:config/path.properties")
@Component
@Data
public class PathProperties {
    private String configFilePath;
}
