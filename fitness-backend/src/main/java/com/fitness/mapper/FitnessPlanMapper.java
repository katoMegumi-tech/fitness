package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.FitnessPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健身计划Mapper接口
 */
@Mapper
public interface FitnessPlanMapper extends BaseMapper<FitnessPlan> {
}
