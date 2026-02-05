package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.CertAuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教练资质审核记录Mapper接口
 */
@Mapper
public interface CertAuditRecordMapper extends BaseMapper<CertAuditRecord> {
}
