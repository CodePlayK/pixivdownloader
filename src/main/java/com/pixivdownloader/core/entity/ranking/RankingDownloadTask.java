package com.pixivdownloader.core.entity.ranking;


import com.pixivdownloader.core.service.RankingService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Scope("prototype")
@Data
public class RankingDownloadTask implements Runnable {
    @Autowired
    private RankingService rankingService;

    private List<RankingPic> bookmarkList;
    private HashMap<String, String> cookies;

    public RankingDownloadTask(List<RankingPic> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    @Override
    public void run() {
        rankingService.getRankingPicByPage(bookmarkList, cookies);
    }


}
