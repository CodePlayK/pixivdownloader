package com.pixivdownloader.core.service;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.ranking.RankingDownloadTask;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import com.pixivdownloader.core.properties.ExceptionProperties;
import com.pixivdownloader.core.properties.FilePathProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static com.pixivdownloader.core.constance.EntityPreset.HttpEnum.*;
import static com.pixivdownloader.core.constance.EntityPreset.RATING.R18;
import static com.pixivdownloader.core.constance.EntityPreset.RATING.R18G;

@Component
public class RankingService extends PicService {
    private final Logger LOGGER = LogManager.getLogger();
    private String RANKINGPATH;
    @Autowired
    private FilePathProperties filePathProperties;
    @Autowired
    private ExceptionProperties exceptionProperties;
    @Autowired
    private ApplicationContext applicationContext;

    /***
     * 获取排行榜
     * @param type 排行榜类型
     * @return 排行榜列表
     */
    public List<RankingPic> getRankingListByTypeDay(String type) {
        List<RankingPic> ranking = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        List<String> dateList = new ArrayList();
        for (int i = 0; i < 7; i++) {
            today = today.minusDays(1);
            String date = today.format(fmt);
            dateList.add(date);
            //date=2022-02-03
            for (int i1 = 1; i1 <= 2; i1++) {
                String url = RANKINGURL.URL + type + "&date=" + date + "&page=" + i1;
                String body = requestUtils.getBodyByUrl(url);
                String s1 = requestUtils.getRankingBody(body);
                List<RankingPic> ranking1 = JSON.parseArray(s1, RankingPic.class);
                for (RankingPic rankingPic : ranking1) {
                    rankingPic.setDate(date);
                    rankingPic.setRATING_TYPE(type);
                }
                LOGGER.info("{}-{}排行榜共拉取到{}条!", date, type, ranking1.size());
                ranking.addAll(ranking1);
            }
        }
        return ranking;
    }


    /**
     * 获取图片信息
     *
     * @param ids 排行榜列表
     * @return 排行榜信息列表
     */
    public List<RankingPic> getPicsInfoByIds(List<RankingPic> ids) {
        RANKINGPATH = filePathProperties.getRANKING();
        List<RankingPic> bookmarkListAll = new ArrayList<>();
        StringBuilder builder = new StringBuilder(MULTIPICDTLURL.URL);
        int t = 0;
        String url = "";
        for (RankingPic id : ids) {
            builder.append("illust_ids[]=").append(id.getIllustId()).append("&");
            t++;
            if (t == 19) {
                url = builder.toString();
                String bodyByUrl = requestUtils.getBodyByUrl(url);
                String multiPicBody = requestUtils.getMultiPicBody(bodyByUrl);
                List<RankingPic> bookmarkList = JSON.parseArray(multiPicBody, RankingPic.class);
                builder = new StringBuilder(MULTIPICDTLURL.URL);
                t = 0;
                bookmarkListAll.addAll(bookmarkList);
            }

        }
        url = builder.toString();
        String bodyByUrl = requestUtils.getBodyByUrl(url);
        String multiPicBody = requestUtils.getMultiPicBody(bodyByUrl);
        List<RankingPic> bookmarkList = JSON.parseArray(multiPicBody, RankingPic.class);
        bookmarkListAll.addAll(bookmarkList);

        List<String> autherId = exceptionProperties.getAutherId();
        List<String> picId = exceptionProperties.getPicId();
        List<String> tags = exceptionProperties.getTags();
        List<String> titles = exceptionProperties.getTitle();
        List<RankingPic> list = new ArrayList<>();
        boolean flag = false;
        for (RankingPic rankingPic : bookmarkListAll) {
            flag = false;

            if (autherId.contains(rankingPic.getAuthorDetails().getUserId()) ||
                    picId.contains(rankingPic.getId())) {
                LOGGER.warn("图片作者在排除列表中,跳过!:{}", rankingPic.getTitle());
                continue;
            }
            for (String tag : rankingPic.getTags()) {
                for (String s : tags) {
                    if (tag.contains(s)) {
                        flag = true;
                        LOGGER.warn("图片标签在排除列表中,跳过!:{}", rankingPic.getTitle());
                        break;
                    }

                }
                if (flag) {
                    break;
                }
            }
            for (String title : titles) {
                if (rankingPic.getTitle().contains(title)) {
                    LOGGER.warn("图片标题在排除列表中,跳过!:{}", rankingPic.getTitle());
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }
            for (RankingPic pic : ids) {
                if (pic.getIllustId().equals(rankingPic.getId())) {
                    rankingPic.setRATING_TYPE(pic.getRATING_TYPE());
                    rankingPic.setDate(pic.getDate());
                    rankingPic.setIllustId(pic.getIllustId());
                    rankingPic.setRank(pic.getRank());
                    break;
                }
            }
            list.add(rankingPic);
        }
        List<RankingPic> list2 = new ArrayList<>();

        for (RankingPic rankingPic : list) {
            flag = false;
            StringBuilder pathBuilder = new StringBuilder(RANKINGPATH);
            if (EntityPreset.RATING_TYPE.DAILY_R18.RANKING_TYPE.equals(rankingPic.getRATING_TYPE())) {
                pathBuilder.append(R18).append("\\").append(rankingPic.getDate()).append("\\");
            } else if (EntityPreset.RATING_TYPE.R18G.RANKING_TYPE.equals(rankingPic.getRATING_TYPE())) {
                pathBuilder.append(R18G).append("\\").append(rankingPic.getDate()).append("\\");
            }
            String path = pathBuilder.toString();
            File f1 = new File(path);
            String[] list1 = f1.list();
            if (null == list1) {
                list2.add(rankingPic);
                continue;
            }
            int c = 0;
            for (String s : Objects.requireNonNull(list1)) {
                for (int i = 0; i < rankingPic.getPageCount(); i++) {
                    if (s.contains(rankingPic.getId()) && s.contains("_p" + i)) {
                        c++;
                    }
                }
            }
            if (c < rankingPic.getPageCount()) {
                list2.add(rankingPic);
                LOGGER.info("图片加入下载队列!{}", rankingPic.getTitle());
            }
        }

        LOGGER.warn("共获取到{}张图片,跳过{}条,实际需要下载{}条!", ids.size(), ids.size() - list2.size(), list2.size());
        return list2;
    }

    /**
     * 获取排行榜图片
     *
     * @param bookmarkList 排行榜列表
     * @param cookies      cookies
     */
    public void getRankingPicByPage(List<RankingPic> bookmarkList, HashMap<String, String> cookies) {
        LOGGER.warn("当前线程首个收藏：{}", bookmarkList.get(0).getTitle());
        int successCount = 0;
        int skipCount = 0;
        int totalCount = 0;
        for (int i1 = 0; i1 < bookmarkList.size(); i1++) {
            StringBuilder pathBuilder = new StringBuilder(RANKINGPATH);
            RankingPic bookmark = bookmarkList.get(i1);
            if (EntityPreset.RATING_TYPE.DAILY_R18.RANKING_TYPE.equals(bookmark.getRATING_TYPE())) {
                pathBuilder.append(R18).append("\\").append(bookmark.getDate()).append("\\");
            } else if (EntityPreset.RATING_TYPE.R18G.RANKING_TYPE.equals(bookmark.getRATING_TYPE())) {
                pathBuilder.append(R18G).append("\\").append(bookmark.getDate()).append("\\");
            }
            String path = pathBuilder.toString();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
                LOGGER.info("创建[{}]目录成功!", path);
            }
            LOGGER.info("开始下载第[{}/{}]条:{}", i1, bookmarkList.size(), bookmark.getTitle());
            if ("2".equals(bookmark.getType())) {
                totalCount++;
                try {
                    if (getRankingGif(bookmark, path) > 0) {
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
                String url = PICURL.URL + bookmark.getUrlS() + "_p" + i + bookmark.getFilType();
                ResponseEntity<byte[]> responseEntity = null;
                StringBuilder temptags = new StringBuilder();
                for (String tag : bookmark.getTags()) {
                    temptags.append("_").append(tag);
                }
                String fileName = getRaningFileName(bookmark, i, temptags);
                fileName = filesUtils.cutRankingFileName(fileName, bookmark, i, bookmark.getFilType());
                File f = new File(path + fileName);
                if (f.exists()) {
                    LOGGER.info("已存在:{},跳过……", fileName);
                    skipCount++;
                    continue;
                } else {
                    try {
                        responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                    } catch (RestClientException e) {
                        LOGGER.info("文件类型错误！修改重试……");
                        if (EntityPreset.FILE_TYPE.JPG.FILE_TYPE.equals(bookmark.getFilType())) {
                            bookmark.setFilType(EntityPreset.FILE_TYPE.PNG.FILE_TYPE);
                            fileName = getRaningFileName(bookmark, i, temptags);
                            fileName = filesUtils.cutRankingFileName(fileName, bookmark, i, bookmark.getFilType());
                            url = PICURL.URL + bookmark.getUrlS() + "_p" + i + EntityPreset.FILE_TYPE.PNG.FILE_TYPE;
                            f = new File(path + fileName);
                        }
                        try {
                            responseEntity = requestUtils.requestStreamPreset(url, HttpMethod.GET);
                        } catch (RestClientException restClientException) {
                            LOGGER.info("文件类型错误！修改重试……");
                            bookmark.setFilType(EntityPreset.FILE_TYPE.GIF.FILE_TYPE);
                            fileName = getRaningFileName(bookmark, i, temptags);
                            fileName = filesUtils.cutRankingFileName(fileName, bookmark, i, bookmark.getFilType());
                            url = PICURL.URL + bookmark.getUrlS() + "_p" + i + EntityPreset.FILE_TYPE.GIF.FILE_TYPE;
                            f = new File(path + fileName);
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
        LOGGER.warn("P站涩图7日排行榜下载结束!!!" + dt.getYear() + "年" + dt.getMonth() + "月" + dt.getDayOfMonth() + " " + dt.getHour() + "点" + dt.getMinute()
                + "分:本次下载完毕，收藏共:" + totalCount + "条，跳过:" + skipCount + "条，应下载:" +
                (totalCount - skipCount) + "条，下载成功:" + successCount + "条，成功率:" + new BigDecimal(successCount * 100).divide(new BigDecimal(totalCount - skipCount + 1), 2, RoundingMode.HALF_UP) + "%");
    }


    public void asyncDownloadRanking(List<RankingPic> bookmarkList, ThreadPoolExecutor
            executor) {
        Set<List<RankingPic>> set = requestUtils.divideRankingListByPartNum(bookmarkList, 5);
        for (List<RankingPic> bookmarks : set) {
            RankingDownloadTask downloadTask = applicationContext.getBean(RankingDownloadTask.class);
            //DownloadTask downloadTask = new DownloadTask(bookmarks);
            downloadTask.setBookmarkList(bookmarks);
            executor.execute(downloadTask);
        }
    }
}
