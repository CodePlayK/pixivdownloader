package com.pixivdownloader.core.entity.novel;

import lombok.Data;

import java.util.List;

/**
 * @author Hakace
 * @create 2022/10/25 19:06
 */
@Data
public class Novel {
    private Integer id;
    private String title;
    private String comment;
    private String novelType;
    private String userId;
    private Integer wordCount;
    private String userName;
    private String url;
    private String bookmarkCount;
    private List<String> tags;
    private Serie series;
    private String text;


}
