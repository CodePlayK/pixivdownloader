package com.pixivdownloader.core.entity.novel;

import lombok.Data;

/**
 * @author Hakace
 * @create 2022/10/25 22:07
 */
@Data
public class NovelRanking {
    private Integer novelId;
    private Integer id;
    private Integer bookmarkId;
    private Integer rank;
}
