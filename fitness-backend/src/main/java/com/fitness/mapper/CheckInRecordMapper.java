package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打卡记录Mapper接口
 */
@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {
}
