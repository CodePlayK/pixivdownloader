package com.pixivdownloader.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "except", ignoreUnknownFields = false)
@PropertySource("classpath:config/except.properties")
@Component
public class ExceptionProperties {
    private String autherId;
    private String picId;
    private String tags;
    private String title;

    public ExceptionProperties() {
    }


    public ExceptionProperties(String autherId, String picId, String tags, String title) {
        this.autherId = autherId;
        this.picId = picId;
        this.tags = tags;
        this.title = title;
    }

    public List<String> getTitle() {
        String[] split = title.split(",");
        return Arrays.asList(split);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAutherId() {
        String[] split = autherId.split(",");
        return Arrays.asList(split);
    }

    public void setAutherId(String autherId) {
        this.autherId = autherId;
    }

    public List<String> getPicId() {
        String[] split = picId.split(",");
        return Arrays.asList(split);
    }

    public void setPicId(String picId) {
        this.picId = picId;
    }

    public List<String> getTags() {
        String[] split = tags.split(",");
        return Arrays.asList(split);
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
