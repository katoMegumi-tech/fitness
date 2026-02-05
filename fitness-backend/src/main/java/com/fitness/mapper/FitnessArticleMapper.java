package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.FitnessArticle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健身科普文章Mapper
 */
@Mapper
public interface FitnessArticleMapper extends BaseMapper<FitnessArticle> {
}
