package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 绑定申请请求DTO
 */
@Data
public class BindApplyRequest {
    
    /**
     * 教练用户ID
     */
    @NotNull(message = "教练ID不能为空")
    private Long coachUserId;
    
    /**
     * 申请理由
     */
    private String applyReason;
}
