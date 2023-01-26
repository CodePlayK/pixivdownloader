package com.pixivdownloader.core;

import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import com.pixivdownloader.core.service.BookMarkListService;
import com.pixivdownloader.core.service.NovelService;
import com.pixivdownloader.core.service.RankingService;
import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.FilesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class PixivDownloaderRunner implements CommandLineRunner {
    private final Logger LOGGER = LogManager.getLogger();
    @Autowired
    protected FilesUtils filesUtils;
    @Autowired
    private BookMarkListService bookMarkDownloadService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private CookieUtils cookieUtils;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("初始化......");
        filesUtils.getDir();
        cookieUtils.getCookies();

        LOGGER.warn("P站小说收藏夹与每日排行榜下载启动!!");
        novelService.process();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(6, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10));
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

        executor.shutdown();
        LOGGER.info("所有线程启动完毕!");

    }


}
