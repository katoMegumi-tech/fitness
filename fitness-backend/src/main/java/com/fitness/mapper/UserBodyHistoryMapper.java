package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.UserBodyHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户身体数据历史记录Mapper接口
 */
@Mapper
public interface UserBodyHistoryMapper extends BaseMapper<UserBodyHistory> {
}
