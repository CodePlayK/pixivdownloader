package com.pixivdownloader.core.mapper.bookmark;

import com.pixivdownloader.core.entity.AuthorPo;
import com.pixivdownloader.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AuthorMapper extends BaseMapper<AuthorPo> {

}
