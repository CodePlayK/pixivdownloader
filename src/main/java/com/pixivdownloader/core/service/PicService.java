package com.pixivdownloader.core.service;

import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.RankingPic;
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

import static com.pixivdownloader.core.constance.EntityPreset.FileType.GIF;
import static com.pixivdownloader.core.constance.EntityPreset.FileType.ZIP;
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

    /**
     * 收藏获取图片
     *
     * @param bookmarkList 收藏列表
     */
    public void getPicByPage(List<Bookmark> bookmarkList) {
        LOGGER.warn("当前线程首个收藏：{}", bookmarkList.get(0).getTitle());
        boolean skipFlag = false;
        int successCount = 0;
        int skipCount = 0;
        int totalCount = 0;
        String pathName = filePathProperties.getR18PATH();
        for (int i1 = 0; i1 < bookmarkList.size(); i1++) {
            Bookmark bookmark = bookmarkList.get(i1);
            if ("R-18G".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18GPATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getR18GCOMICPATH();
                }
            } else if ("R-18".equals(bookmark.getTags().get(0))) {
                pathName = filePathProperties.getR18PATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getR18COMICPATH();
                }
            } else {
                pathName = filePathProperties.getNONEHPATH();
                if (bookmark.getPageCount() > COMIC_SIZE) {
                    pathName = filePathProperties.getNONEHCOMICPATH();
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
                String url = PICURL.getUrl() + bookmark.getUrlS() + "_p" + i + bookmark.getFilType();
                ResponseEntity<byte[]> responseEntity = null;
                StringBuilder temptags = new StringBuilder();
                for (String tag : bookmark.getTags()) {
                    temptags.append("_").append(tag);
                }
                String fileName = getFileName(bookmark, i, temptags);
                fileName = filesUtils.cutFileName(fileName, bookmark, i, bookmark.getFilType());
                File f = new File(pathName + fileName);
                if (f.exists()) {
                    LOGGER.info("已存在:{},跳过……", fileName);
                    skipCount++;
                    continue;
                } else {
                    try {
                        responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                    } catch (RestClientException e) {
                        LOGGER.info("文件类型错误！修改重试……");
                        if (EntityPreset.FileType.JPG.getFileType().equals(bookmark.getFilType())) {
                            bookmark.setFilType(EntityPreset.FileType.PNG.getFileType());
                            fileName = getFileName(bookmark, i, temptags);
                            fileName = filesUtils.cutFileName(fileName, bookmark, i, bookmark.getFilType());
                            url = PICURL.getUrl() + bookmark.getUrlS() + "_p" + i + EntityPreset.FileType.PNG.getFileType();
                            f = new File(pathName + fileName);
                        }
                        try {
                            responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                        } catch (RestClientException restClientException) {
                            LOGGER.info("文件类型错误！修改重试……");
                            bookmark.setFilType(EntityPreset.FileType.GIF.getFileType());
                            fileName = getFileName(bookmark, i, temptags);
                            fileName = filesUtils.cutFileName(fileName, bookmark, i, bookmark.getFilType());
                            url = PICURL.getUrl() + bookmark.getUrlS() + "_p" + i + EntityPreset.FileType.GIF.getFileType();
                            f = new File(pathName + fileName);
                            try {
                                responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                            } catch (Exception exception) {
                                LOGGER.warn("下载失败:{}", fileName);
                                LOGGER.warn("失败链接:{}", bookmark.getUrlS());
                                LOGGER.warn("跳过……");
                                break;
                            }

                        }
                    }
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
        String pathName = filePathProperties.getR18GIFPATH();

        String url = EntityPreset.HttpEnum.GIFURL.getUrl() + bookmark.getUrlS() + EntityPreset.HttpEnum.URLZIP.getUrl();
        StringBuilder temptags = new StringBuilder();
        for (String tag : bookmark.getTags()) {
            temptags.append("_").append(tag);
        }
        if ("R-18G".equals(bookmark.getTags().get(0))) {
            pathName = filePathProperties.getR18GGIFPATH();
        }
        String fileName = bookmark.getBookmarkId() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + GIF.getFileType();
        fileName = filesUtils.cutFileName(fileName, bookmark, 0, GIF.getFileType());
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
                fileName = StringUtils.substringBefore(fileName, GIF.getFileType()) + ZIP.getFileType();
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
                String fileName1 = StringUtils.substringBefore(fileName, ZIP.getFileType());
                String[] fileList = filesUtils.findFileList(new File(pathName + "\\" + fileName1 + "\\"));
                if (null == fileList || fileList.length == 0) {
                    LOGGER.error(pathName + "\\" + fileName1 + "\\");
                }
                gifUtils.jpgToGif(fileList, pathName + fileName1 + GIF.getFileType());
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
        String pathName = path;
        String url = EntityPreset.HttpEnum.GIFURL.getUrl() + bookmark.getUrlS() + EntityPreset.HttpEnum.URLZIP.getUrl();
        StringBuilder temptags = new StringBuilder();
        for (String tag : bookmark.getTags()) {
            temptags.append("_").append(tag);
        }
        String fileName = bookmark.getDate() + "_" + bookmark.getRank() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + GIF.getFileType();
        fileName = filesUtils.cutRankingFileName(fileName, bookmark, 0, GIF.getFileType());
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
                fileName = StringUtils.substringBefore(fileName, GIF.getFileType()) + ZIP.getFileType();
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
                String fileName1 = StringUtils.substringBefore(fileName, ZIP.getFileType());
                String[] fileList = filesUtils.findFileList(new File(pathName + "\\" + fileName1 + "\\"));
                if (null == fileList || fileList.length == 0) {
                    LOGGER.error(pathName + "\\" + fileName1 + "\\");
                }
                gifUtils.jpgToGif(fileList, pathName + fileName1 + GIF.getFileType());
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

}
