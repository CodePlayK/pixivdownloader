package com.pixivdownloader.core.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author huyi.zhang
 */
@RegisterMapper
public interface BaseMapper<T> extends Mapper<T>, IdsMapper<T>, IdListMapper<T, Long> {
}
