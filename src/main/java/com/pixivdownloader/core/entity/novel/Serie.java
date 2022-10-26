package com.pixivdownloader.core.entity.novel;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Hakace
 * @create 2022/10/25 19:48
 */
@Data
@Mapper
public class Serie {
    private Integer seriesId;
    private String seriesTitle;
    private String firstNovelId;
    private String contentOrder;
    private NextNovel nextNovel;
    private PrevNovel prevNovel;
}
