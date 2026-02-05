package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.BindRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 绑定关系Mapper接口
 */
@Mapper
public interface BindRecordMapper extends BaseMapper<BindRecord> {
}
