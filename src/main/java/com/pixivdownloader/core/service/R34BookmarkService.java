package com.pixivdownloader.core.service;

import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.utils.FilesUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class R34BookmarkService {
    private static final String BOOKMARK_OPEN = "https://wimg.rule34.xxx/thumbnails//";
    private static final String BOOKMARK_URL = "https://rule34.xxx/index.php?page=favorites&s=view&id=2376350&pid=";
    private static final String DOWNLOAD_URL = "https://ahrimp4.rule34.xxx//images/";
    private static final String DETAIL_URL = "https://rule34.xxx/index.php?page=post&s=view&id=";
    private final Logger LOGGER = LogManager.getLogger(R34BookmarkService.class);
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private FilesUtils filesUtils;

    @Autowired
    private FilePathProperties filePathProperties;

    public void process() {
        int total = 0;
        int skip = 0;
        int success = 0;
        int pid = 0;
        ResponseEntity<String> bookmarkResponse = requestUtils.requestPreset(BOOKMARK_URL + pid, HttpMethod.GET);
        Map<String, String> existBookmark = existBookmark();
        boolean flag = false;
        while (bookmarkResponse.getStatusCode() == HttpStatus.OK) {
            for (int i = 1; i <= 50; i++) {
                System.out.println(filesUtils.getBar(i, 50, "R34下载-" + Thread.currentThread().getName()));
                String imgSrc = requestUtils
                        .getStingBy3PinIndex(bookmarkResponse.getBody(), "<![CDATA[", i, BOOKMARK_OPEN, 1, "\"", 1);
                if (imgSrc.isEmpty()) {
                    flag = true;
                    break;
                }
                total++;
                String favoriteId = StringUtils.substringBefore(imgSrc, "/");
                String urlId = StringUtils.substringAfter(imgSrc, "thumbnail_");
                String imgId = StringUtils.substringAfter(imgSrc, "?");
                if (existBookmark.containsKey(imgId)) {
                    //LOGGER.warn("该收藏已经下载成功，跳过！【{}】", existBookmark.get(imgId));
                    skip++;
                    continue;
                }
                urlId = StringUtils.substringBefore(urlId, ".");
                String url = DOWNLOAD_URL + favoriteId + "/" + urlId + ".mp4";
                String url_img = DETAIL_URL + imgId;
                ResponseEntity<String> detailResponse = requestUtils.requestPreset(url_img, HttpMethod.GET);
                String copyright = filesUtils.passFaileName(requestUtils.getStingBy3PinIndex(detailResponse.getBody(), "Copyright", 1, "\">", 3, "<", 1));
                String Character = filesUtils.passFaileName(requestUtils.getStingBy3PinIndex(detailResponse.getBody(), "Character", 1, "\">", 3, "<", 1));
                String Artist = filesUtils.passFaileName(requestUtils.getStingBy3PinIndex(detailResponse.getBody(), "h6>Artist", 1, "\">", 3, "<", 1));
                File file = new File(filePathProperties.getR34_PATH() + copyright + "_" + Character + "_" + Artist + "_" + imgId + ".mp4");
                //LOGGER.info("开始下载【{}】", file.getPath());
                ResponseEntity<byte[]> responseEntity = requestUtils.requestStream34Preset(url, HttpMethod.GET);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(Objects.requireNonNull(responseEntity.getBody()), 0, responseEntity.getBody().length);
                    out.flush();
                    success++;
                } catch (Exception e) {
                    LOGGER.error("R34写入失败![{}]", file.getPath(), e);
                }
            }
            pid += 50;
            if (flag) {
                break;
            }
            bookmarkResponse = requestUtils.requestPreset(BOOKMARK_URL + pid, HttpMethod.GET);
        }
        result(success, skip, total);
    }


    private Map<String, String> existBookmark() {
        Map<String, String> map = new HashMap<>();
        File file = new File(filePathProperties.getR34_PATH());
        String[] list = Objects.requireNonNull(file.list());
        for (String s : list) {
            String s1 = StringUtils.substringAfterLast(s, "_");
            s1 = StringUtils.substringBefore(s1, ".");
            map.put(s1, s);
        }
        return map;
    }

    private void result(int successCount, int skipCount, int totalCount) {
        LocalDateTime time = LocalDateTime.now();
        DecimalFormat df = new DecimalFormat("0.00");
        String sucRate = "100";
        if (totalCount - skipCount > 0) {
            sucRate = df.format((float) (successCount) / (totalCount - skipCount) * 100);
        }
        LOGGER.warn(
                "R34下载结束!!!{},共收藏:{}张,跳过:{}张,需下载{}张,下载成功:{}成功率:{}%",
                time.toString(), totalCount, skipCount, totalCount - skipCount, successCount, sucRate
        );
    }
}
