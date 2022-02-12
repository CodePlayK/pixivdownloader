package com.pixivdownloader.core.entity;


import com.pixivdownloader.core.service.PicService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@Data
public class DownloadTask implements Runnable {
    @Autowired
    private PicService picService;

    private List<Bookmark> bookmarkList;

    public DownloadTask(List<Bookmark> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    @Override
    public void run() {
        picService.getPicByPage(bookmarkList);
    }


}
