package com.pixivdownloader.core.service;

import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.utils.FilesUtils;
import com.pixivdownloader.core.utils.GifUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import com.pixivdownloader.core.utils.UnZipUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.pixivdownloader.core.constance.EntityPreset.FILE_TYPE.GIF;
import static com.pixivdownloader.core.constance.EntityPreset.FILE_TYPE.ZIP;
import static com.pixivdownloader.core.constance.EntityPreset.HttpEnum.PICURL;

@Component
public class PicService {
    private final Logger LOGGER = LogManager.getLogger();
    @Autowired
    protected RequestUtils requestUtils;
    @Autowired
    protected FilesUtils filesUtils;
    @Autowired
    protected FilePathProperties filePathProperties;
    @Autowired
    private GifUtils gifUtils;
    @Autowired
    private UnZipUtils unZipUtils;
    protected static final int COMIC_SIZE = 35;


    public void getPicByPage(List<Bookmark> bookmarkList) {
        if (bookmarkList.size() == 0) {
            return;
        }
        LOGGER.warn("当前线程首个收藏：{}", bookmarkList.get(0).getTitle());
        boolean skipFlag = false;
        int successCount = 0;
        int skipCount = 0;
        int totalCount = 0;
        String pathName = filePathProperties.getR18_PATH();
        for (int i1 = 0; i1 < bookmarkList.size(); i1++) {
            Bookmark bookmark = bookmarkList.get(i1);
            if ("R-18G".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18G_PATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getR18G_COMIC_PATH();
                }
            } else if ("R-18".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18_PATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getR18_COMIC_PATH();
                }
            } else {
                pathName = filePathProperties.getNONEH_PATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getNONEH_COMIC_PATH();
                }
            }
            LOGGER.info("开始下载第[{}/{}]条:{}", i1, bookmarkList.size(), bookmark.getTitle());
            if ("2".equals(bookmark.getType())) {
                totalCount++;
                try {
                    if (getGif(bookmark) > 0) {
                        skipCount++;
                    } else {
                        successCount++;
                    }
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            for (int i = 0; i < bookmark.getPageCount(); i++) {
                totalCount++;
                ResponseEntity<byte[]> responseEntity = null;
                StringBuilder temptags = new StringBuilder();
                bookmark.getTags().forEach(a -> temptags.append("_").append(a));
                String fileName = getFileName(bookmark, i, temptags);
                File f = null;
                boolean flag = false;
                for (EntityPreset.FILE_TYPE fileType : EntityPreset.FILE_TYPE.values()) {
                    String url = PICURL.URL + bookmark.getUrlS() + "_p" + i + fileType.FILE_TYPE;
                    fileName = filesUtils.cutFileName(fileName, bookmark, i, bookmark.getFilType());
                    f = new File(pathName + fileName);
                    if (f.exists()) {
                        LOGGER.info("已存在:{},跳过……", fileName);
                        skipCount++;
                        break;
                    } else {
                        try {
                            responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                            flag = true;
                            break;
                        } catch (RestClientException e) {
                            LOGGER.info("【{}】文件类型错误！修改重试……", fileType.FILE_TYPE);
                        }
                    }
                }
                if (!flag) {
                    continue;
                }
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (Exception e) {
                        LOGGER.error("文件写入失败:{}", fileName);
                        LOGGER.error(e.getMessage());
                    }
                }
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(Objects.requireNonNull(responseEntity.getBody()), 0, responseEntity.getBody().length);
                    out.flush();
                } catch (Exception e) {
                    LOGGER.error("文件写入失败:{}", fileName);
                    LOGGER.error(e.getMessage());
                }
                LOGGER.info("单张下载成功!:{}", fileName);
                successCount++;
            }
            LOGGER.info("收藏下载成功!:{}", bookmark.getTitle());
        }
        java.text.NumberFormat numberformat = java.text.NumberFormat.getInstance();
        numberformat.setMaximumFractionDigits(2);
        LocalDateTime dt = LocalDateTime.now();
        if (totalCount - skipCount == 0) {
            LOGGER.warn("P站涩图下载结束!!!" + dt.getYear() + "年" + dt.getMonth() + "月" + dt.getDayOfMonth() + " " + dt.getHour() + "点" + dt.getMinute()
                    + "分:本次下载完毕，收藏共:" + totalCount + "条，跳过:" + skipCount + "条，应下载:" +
                    (totalCount - skipCount) + "条，下载成功:" + successCount + "条.");
        } else {
            LOGGER.warn("P站涩图下载结束!!!" + dt.getYear() + "年" + dt.getMonth() + "月" + dt.getDayOfMonth() + " " + dt.getHour() + "点" + dt.getMinute()
                    + "分:本次下载完毕，收藏共:" + totalCount + "条，跳过:" + skipCount + "条，应下载:" +
                    (totalCount - skipCount) + "条，下载成功:" + successCount + "条，成功率:" + new BigDecimal(successCount * 100).
                    divide(new BigDecimal(totalCount - skipCount), 2, RoundingMode.HALF_UP) + "%");
        }
    }

    /**
     * 获取GIF
     *
     * @param bookmark
     * @return
     */
    public int getGif(Bookmark bookmark) {
        boolean skipFlag = false;
        String pathName = filePathProperties.getR18_GIF_PATH();

        String url = EntityPreset.HttpEnum.GIFURL.URL + bookmark.getUrlS() + EntityPreset.HttpEnum.URLZIP.URL;
        StringBuilder temptags = new StringBuilder();
        for (String tag : bookmark.getTags()) {
            temptags.append("_").append(tag);
        }
        if ("R-18G".equals(bookmark.getTags().get(0))) {
            pathName = filePathProperties.getR18G_GIF_PATH();
        }
        String fileName = bookmark.getBookmarkId() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + GIF.FILE_TYPE;
        fileName = filesUtils.cutFileName(fileName, bookmark, 0, GIF.FILE_TYPE);
        File f = new File(pathName + fileName);
        File f1 = new File(pathName);
        for (String s : Objects.requireNonNull(f1.list())) {
            if (s.contains(bookmark.getId())) {
                skipFlag = true;
                break;
            }
        }
        if (skipFlag) {
            LOGGER.info("已存在:{},跳过……", fileName);
            return 1;
        } else {
            if (!f.exists()) {
                fileName = StringUtils.substringBefore(fileName, GIF.FILE_TYPE) + ZIP.FILE_TYPE;
                f = new File(pathName + fileName);
                ResponseEntity<byte[]> bytes = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(Objects.requireNonNull(bytes.getBody()), 0, bytes.getBody().length);
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    unZipUtils.zipUncompress(pathName + fileName);
                } catch (Exception e) {
                    LOGGER.error("{}解压失败!", pathName + fileName);
                    LOGGER.error(e.getMessage());
                }
                String fileName1 = StringUtils.substringBefore(fileName, ZIP.FILE_TYPE);
                String[] fileList = filesUtils.findFileList(new File(pathName + "\\" + fileName1 + "\\"));
                if (null == fileList || fileList.length == 0) {
                    LOGGER.error(pathName + "\\" + fileName1 + "\\");
                }
                gifUtils.jpgToGif(fileList, pathName + fileName1 + GIF.FILE_TYPE);
                filesUtils.deleteFolder(pathName + "\\" + fileName1 + "\\");
                filesUtils.deleteFolder(pathName + "\\" + fileName);
            } else {
                LOGGER.info("已存在:{},跳过……", pathName);
                return 1;
            }
            LOGGER.info("GIF下载成功!:{}", fileName);
            return 0;
        }
    }

    protected String getFileName(Bookmark bookmark, int i, StringBuilder temptags) {
        return bookmark.getBookmarkId() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_p" + i + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + bookmark.getFilType();
    }

    protected String getRaningFileName(RankingPic bookmark, int i, StringBuilder temptags) {
        return bookmark.getDate() + "_" + bookmark.getRank() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_p" + i + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + bookmark.getFilType();
    }

    /**
     * 获取GIF
     *
     * @param bookmark
     * @return
     */
    public int getRankingGif(RankingPic bookmark, String path) {
        boolean skipFlag = false;
        String url = EntityPreset.HttpEnum.GIFURL.URL + bookmark.getUrlS() + EntityPreset.HttpEnum.URLZIP.URL;
        StringBuilder temptags = new StringBuilder();
        for (String tag : bookmark.getTags()) {
            temptags.append("_").append(tag);
        }
        String fileName = bookmark.getDate() + "_" + bookmark.getRank() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + GIF.FILE_TYPE;
        fileName = filesUtils.cutRankingFileName(fileName, bookmark, 0, GIF.FILE_TYPE);
        File f = new File(path + fileName);
        File f1 = new File(path);
        for (String s : Objects.requireNonNull(f1.list())) {
            if (s.contains(bookmark.getId())) {
                skipFlag = true;
                break;
            }
        }
        if (skipFlag) {
            LOGGER.info("已存在:{},跳过……", fileName);
            return 1;
        } else {
            if (!f.exists()) {
                fileName = StringUtils.substringBefore(fileName, GIF.FILE_TYPE) + ZIP.FILE_TYPE;
                f = new File(path + fileName);
                ResponseEntity<byte[]> bytes = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(Objects.requireNonNull(bytes.getBody()), 0, bytes.getBody().length);
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    unZipUtils.zipUncompress(path + fileName);
                } catch (Exception e) {
                    LOGGER.error("{}解压失败!", path + fileName);
                    LOGGER.error(e.getMessage());
                }
                String fileName1 = StringUtils.substringBefore(fileName, ZIP.FILE_TYPE);
                String[] fileList = filesUtils.findFileList(new File(path + "\\" + fileName1 + "\\"));
                if (null == fileList || fileList.length == 0) {
                    LOGGER.error(path + "\\" + fileName1 + "\\");
                }
                gifUtils.jpgToGif(fileList, path + fileName1 + GIF.FILE_TYPE);
                filesUtils.deleteFolder(path + "\\" + fileName1 + "\\");
                filesUtils.deleteFolder(path + "\\" + fileName);
            } else {
                LOGGER.info("已存在:{},跳过……", path);
                return 1;
            }
            LOGGER.info("GIF下载成功!:{}", fileName);
            return 0;
        }
    }

}
