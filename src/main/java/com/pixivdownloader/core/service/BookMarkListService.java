package com.pixivdownloader.core.service;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.BookMarkListTask;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.DownloadTask;
import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;


/**
 * 书签列表服务
 *
 * @author hakace
 * @date 2022/10/27
 */
@Component
public class BookMarkListService extends PicService {
    private final Logger LOGGER = LogManager.getLogger();
    private final static int THREAD_SIZE = 4;
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 多线程获取所有收藏
     *
     * @return 收藏list
     */
    public List<Bookmark> getBookmarkList() throws InterruptedException {
        final String BOOKMARK_LIST_URL = EntityPreset.HttpEnum.BOOKMARK_LIST_URL_BEGIN.URL + cookieUtils.getUSERID()
                + EntityPreset.HttpEnum.BOOKMARK_LIST_URL_END.URL;
        int lastPage = 1;
        String body = "";
        try {
            ResponseEntity<String> responseEntity = requestUtils.requestPreset(BOOKMARK_LIST_URL + "1", HttpMethod.GET);
            body = responseEntity.getBody();
        } catch (RestClientException e) {
            LOGGER.error("[{}]解析响应失败,请检查谷歌浏览器Pixiv登录是否过期!:{}", StringUtils.substringBefore(e.getMessage(), ": [{"), e.getMessage());
        }
        String bookmarkBody = requestUtils.getBookmarkBody(body);
        List<Bookmark> bookmarkList = null;
        try {
            bookmarkList = JSON.parseArray(bookmarkBody, Bookmark.class);
        } catch (Exception e) {
            LOGGER.error("解析响应失败,请检查谷歌浏览器Pixiv登录是否过期!:{}", e.getMessage());
        }
        lastPage = requestUtils.getBetween(body, "\"lastPage\":", ",\"ads\"");
        assert bookmarkList != null;
        bookmarkList.clear();
        Map<Integer, Integer> map = requestUtils.divideNumByPartNum(1, lastPage, THREAD_SIZE);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(6, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5));
        List<Future> futures = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            BookMarkListTask getBookMarkListTask = applicationContext.getBean(BookMarkListTask.class);
            getBookMarkListTask.setBg(entry.getKey());
            getBookMarkListTask.setEnd(entry.getValue());
            getBookMarkListTask.setUrl(BOOKMARK_LIST_URL);
            futures.add(executor.submit(getBookMarkListTask));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("线程获取收藏报错!:{}", e.getMessage());
            e.printStackTrace();
        }
        for (Future future : futures) {
            try {
                bookmarkList.addAll((Collection<? extends Bookmark>) future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("线程获取收藏报错!:{}", e.getMessage());
                e.printStackTrace();
            }
        }
        executor.shutdown();
        String pathName;
        List<Bookmark> list1 = new ArrayList<>();
        for (Bookmark bookmark : bookmarkList) {
            if ("".equals(bookmark.getUrlS()) || null == bookmark.getUrlS()) {
                LOGGER.warn("图片地址为空!跳过!:{}", bookmark.getTitle());
                continue;
            }
            if ("R-18G".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18G_PATH();
            } else if ("R-18".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18_PATH();
            } else {
                pathName = filePathProperties.getNONEH_PATH();
            }
            File file = new File(pathName);
            String[] list = file.list();
            int c = 0;
            assert list != null;
            for (String s : list) {
                for (int i = 0; i < bookmark.getPageCount(); i++) {
                    if (s.contains(bookmark.getBookmarkId()) && s.contains("_p" + i)) {
                        c++;
                        break;
                    }
                }
            }
            if (c < bookmark.getPageCount()) {
                list1.add(bookmark);
                LOGGER.info("图片加入下载队列!{}", bookmark.getTitle());
            }

        }
        LOGGER.info("本次获取到的收藏共{}条,目标收藏条数:{}", bookmarkList.size(), list1.size());
        return list1;
    }

    /**
     * 根据分片后的收藏异步下载
     *
     * @param bookmarkList 收藏
     * @param executor     executor
     */
    public void asyncDownloadBookmark(List<Bookmark> bookmarkList, ThreadPoolExecutor
            executor) {
        Collections.shuffle(bookmarkList);
        Set<List<Bookmark>> set = requestUtils.divideListByPartNum(bookmarkList, 5);
        for (List<Bookmark> bookmarks : set) {
            DownloadTask downloadTask = applicationContext.getBean(DownloadTask.class);
            downloadTask.setBookmarkList(bookmarks);
            executor.execute(downloadTask);
        }
    }
}

