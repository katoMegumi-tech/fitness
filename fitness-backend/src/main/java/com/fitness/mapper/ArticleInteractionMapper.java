package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.ArticleInteraction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章互动Mapper
 */
@Mapper
public interface ArticleInteractionMapper extends BaseMapper<ArticleInteraction> {
}
