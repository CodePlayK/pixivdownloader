package com.pixivdownloader.core.entity;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.utils.RequestUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 书签列表任务
 *
 * @author hakace
 * @date 2022/10/28
 */
@Component
@Scope("prototype")
@Data
public class BookMarkListTask implements Callable {
    private Logger LOGGER = LogManager.getLogger();
    private int bg;
    private int end;
    private String url;
    private String phpsessid;
    @Autowired
    private RequestUtils requestUtils;

    @Override
    public List<Bookmark> call() {
        String BOOKMARK_LIST_URL = url;
        List<Bookmark> bookmarkList = new ArrayList<>();
        LOGGER.info("开始获取第{}到{}页收藏……", bg, end);
        for (int i = bg; i <= end; i++) {
            LOGGER.info("开始获取第{}页收藏……", i);
            String url = BOOKMARK_LIST_URL + i;
            ResponseEntity<String> responseEntity = requestUtils.requestPreset(url, HttpMethod.GET);
            Objects.requireNonNull(bookmarkList).addAll(Objects.requireNonNull(JSON.parseArray(StringUtils.substringBetween(responseEntity.getBody(), "\"bookmarks\":", "],\"total\"") + "]", Bookmark.class)));
        }
        return bookmarkList;
    }


}
