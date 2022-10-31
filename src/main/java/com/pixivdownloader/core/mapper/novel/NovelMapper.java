package com.pixivdownloader.core.mapper.novel;

import com.pixivdownloader.core.entity.novel.po.NovelPo;
import com.pixivdownloader.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hakace
 * @create 2022/10/25 21:05
 */
@Mapper
@Component
public interface NovelMapper extends BaseMapper<NovelPo> {
    @Select("select novel_id from novel where 1=1 and file_type ='RANKING'")
    List<NovelPo> queryAllRankingNovelId();

    @Select("select novel_id from novel where 1=1")
    List<NovelPo> queryAllNovelId();

    @Select("select novel_id from novel where 1=1 and file_type ='FAVORITE'")
    List<NovelPo> queryAllFavoriteNovelId();
}
