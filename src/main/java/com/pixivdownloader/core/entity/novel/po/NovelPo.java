package com.pixivdownloader.core.entity.novel.po;

import com.alibaba.fastjson.JSONObject;
import com.pixivdownloader.core.entity.novel.Novel;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Hakace
 * @create 2022/10/25 20:02
 */
@Data
@Table(name = "Novel")
public class NovelPo {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer seq;
    private Integer novelId;
    private Integer favoriteId;
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;
    private String userId;
    private String novelType;
    private String fileType;
    private Integer wordCount;
    private String userName;
    private Date createTime;
    private String url;
    private Integer bookmarkCount;
    private String tags;
    private String series;
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    public NovelPo(Integer seq, Integer novelId, Integer favoriteId, String title, String comment, String userId, String novelType, String fileType, Integer wordCount, String userName, Date createTime, String url, Integer bookmarkCount, String tags, String series, String text) {
        this.seq = seq;
        this.novelId = novelId;
        this.favoriteId = favoriteId;
        this.title = title;
        this.comment = comment;
        this.userId = userId;
        this.novelType = novelType;
        this.fileType = fileType;
        this.wordCount = wordCount;
        this.userName = userName;
        this.createTime = createTime;
        this.url = url;
        this.bookmarkCount = bookmarkCount;
        this.tags = tags;
        this.series = series;
        this.text = text;
    }

    public NovelPo(Integer seq, Integer novelId, String title, String comment, String userId, String novelType, Integer wordCount, String userName, String url, Integer bookmarkCount, String tags, String series, String text) {
        this.seq = seq;
        this.novelId = novelId;
        this.title = title;
        this.comment = comment;
        this.userId = userId;
        this.novelType = novelType;
        this.wordCount = wordCount;
        this.userName = userName;
        this.url = url;
        this.bookmarkCount = bookmarkCount;
        this.tags = tags;
        this.series = series;
        this.text = text;
    }

    public NovelPo(Integer seq, Integer novelId, String title, String comment, String userId, Integer wordCount, String userName, String url, Integer bookmarkCount, String tags, String series, String text) {
        this.seq = seq;
        this.novelId = novelId;
        this.title = title;
        this.comment = comment;
        this.userId = userId;
        this.wordCount = wordCount;
        this.userName = userName;
        this.url = url;
        this.bookmarkCount = bookmarkCount;
        this.tags = tags;
        this.series = series;
        this.text = text;
    }

    public NovelPo(Novel novel) {
        this.novelType = novel.getNovelType();
        this.novelId = novel.getId();
        this.title = novel.getTitle();
        this.userId = novel.getUserId();
        this.wordCount = novel.getWordCount();
        this.url = novel.getUrl();
        this.userName = novel.getUserName();
        this.comment = novel.getComment();
        this.bookmarkCount = Integer.valueOf(novel.getBookmarkCount());
        this.text = novel.getText();
        this.tags = JSONObject.toJSONString(novel.getTags());
        this.series = JSONObject.toJSONString(novel.getSeries());
    }

    public NovelPo(Integer novelId) {
        this.novelId = novelId;
    }

    public NovelPo() {
    }

    public NovelPo(Integer seq, Integer novelId, String title, String comment, String userId, String novelType, String fileType, Integer wordCount, String userName, Date createTime, String url, Integer bookmarkCount, String tags, String series, String text) {
        this.seq = seq;
        this.novelId = novelId;
        this.title = title;
        this.comment = comment;
        this.userId = userId;
        this.novelType = novelType;
        this.fileType = fileType;
        this.wordCount = wordCount;
        this.userName = userName;
        this.createTime = createTime;
        this.url = url;
        this.bookmarkCount = bookmarkCount;
        this.tags = tags;
        this.series = series;
        this.text = text;
    }
}
