package com.fitness.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充配置
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        
        // 尝试填充所有可能的创建时间字段
        tryFill(metaObject, "createdAt", now);
        tryFill(metaObject, "createTime", now);
        tryFill(metaObject, "applyTime", now);
        tryFill(metaObject, "bindTime", now);
        
        // 尝试填充所有可能的更新时间字段
        tryFill(metaObject, "updatedAt", now);
        tryFill(metaObject, "updateTime", now);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        
        // 尝试填充所有可能的更新时间字段
        tryFill(metaObject, "updatedAt", now);
        tryFill(metaObject, "updateTime", now);
    }
    
    /**
     * 尝试填充字段：如果字段存在且值为null，则填充
     */
    private void tryFill(MetaObject metaObject, String fieldName, Object fieldVal) {
        if (metaObject.hasSetter(fieldName) && metaObject.getValue(fieldName) == null) {
            this.setFieldValByName(fieldName, fieldVal, metaObject);
        }
    }
}
