package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教练资质审核记录实体类
 */
@Data
@TableName("t_cert_audit_record")
public class CertAuditRecord {
    
    /**
     * 审核记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;
    
    /**
     * 申请教练ID
     */
    private Long coachUserId;
    
    /**
     * 审核状态：PENDING/APPROVED/REJECTED
     */
    private String auditStatus;
    
    /**
     * 审核意见
     */
    private String auditOpinion;
    
    /**
     * 审核管理员ID
     */
    private Long adminId;
    
    /**
     * 申请时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}
