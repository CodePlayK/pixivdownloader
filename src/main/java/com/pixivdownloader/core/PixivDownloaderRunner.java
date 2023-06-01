package com.pixivdownloader.core;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.AuthorDetails;
import com.pixivdownloader.core.entity.AuthorPo;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import com.pixivdownloader.core.mapper.bookmark.AuthorMapper;
import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.service.BookMarkListService;
import com.pixivdownloader.core.service.NovelService;
import com.pixivdownloader.core.service.R34BookmarkService;
import com.pixivdownloader.core.service.RankingService;
import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.FilesUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class PixivDownloaderRunner implements CommandLineRunner {
    private final Logger LOGGER = LogManager.getLogger();
    @Autowired
    private FilePathProperties filePathProperties;
    @Autowired
    protected FilesUtils filesUtils;
    @Autowired
    protected RequestUtils requestUtils;
    @Autowired
    private BookMarkListService bookMarkDownloadService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private R34BookmarkService r34BookmarkService;
    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private AuthorMapper authorMapper;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("初始化......");
        filesUtils.getDir();
        cookieUtils.getCookies();
        preDownload();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(6, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10));

        LOGGER.warn("P站小说收藏夹与每日排行榜下载启动!!");
        novelService.process();

        LOGGER.warn("P站涩图收藏夹下载启动!!");
        List<Bookmark> bookmarkList = bookMarkDownloadService.getBookmarkList();
        bookMarkDownloadService.asyncDownloadBookmark(bookmarkList, executor);

        LOGGER.info("排行榜下载更新开始！");
        ArrayList<RankingPic> rankingList = new ArrayList<>();
        for (EntityPreset.RATING_TYPE value : EntityPreset.RATING_TYPE.values()) {
            rankingList.addAll(rankingService.getRankingListByTypeDay(value.RANKING_TYPE));
        }
        List<RankingPic> picList = rankingService.getPicsInfoByIds(rankingList);
        rankingService.asyncDownloadRanking(picList, executor);

        LOGGER.info("R34收藏下载开始！");
        r34BookmarkService.process();

        executor.shutdown();
        LOGGER.info("所有线程启动完毕!");

    }

    /**
     * 下载前任务
     */
    private void preDownload() {
        //getAllBookmarkedUser();
        //addAllBookmarkedUser();
    }

    /***
     * 根据userid获取所有收藏
     * @param userid
     * @return
     */
    private Set<String> getAllBookmarkedUser(String userid) {
        int i = 0;
        Set<String> set = new HashSet<>();
        while (true) {
            i++;
            ResponseEntity<String> response = requestUtils.requestPreset("https://www.pixiv.net/touch/ajax/user/related?id=" + userid + "&type=following&p=" + i, HttpMethod.GET);
            String sting = requestUtils.getStingBy3PinIndex(response.getBody(), "related", 1, ":", 1, ",\"tags", 1);
            List<AuthorDetails> authorDetails = JSON.parseArray(sting, AuthorDetails.class);
            for (AuthorDetails authorDetail : authorDetails) {
                set.add(authorDetail.getUserId());
            }
            if (authorDetails.size() == 0) {
                break;
            }
        }
        return set;
    }

    /***
     * 将老账号收藏转移到新账号
     */
    private void addAllBookmarkedUser() {
        Set<String> set = getAllBookmarkedUser(cookieUtils.getUSERID());
        int j = 0;
        List<AuthorPo> authors = authorMapper.selectAll();
        for (AuthorPo author : authors) {
            if (set.contains(author.getUserId())) {
                LOGGER.info("[{}]{}已经收藏好，跳过！", author.getUserId(), author.getUserName());
                continue;
            }
            j++;
            LOGGER.info("开始添加第{}-[{}]{}到关注者~", j, author.getUserId(), author.getUserName());
            requestUtils.addBookmarkUser(author.getUserId());

            try {
                Thread.sleep((long) (Math.random() * 5000 + 3000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
