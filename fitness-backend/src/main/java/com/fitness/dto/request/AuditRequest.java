package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审核请求DTO（通用）
 */
@Data
public class AuditRequest {
    
    /**
     * 审核状态：APPROVED/REJECTED
     */
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;
    
    /**
     * 审核意见
     */
    private String auditOpinion;
}
