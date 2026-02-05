package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.CoachProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教练扩展信息Mapper接口
 */
@Mapper
public interface CoachProfileMapper extends BaseMapper<CoachProfile> {
}
