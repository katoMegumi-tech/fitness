package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 处理绑定申请请求DTO
 */
@Data
public class HandleBindRequest {
    
    /**
     * 申请状态：APPROVED/REJECTED
     */
    @NotBlank(message = "处理结果不能为空")
    private String applyStatus;
    
    /**
     * 拒绝理由（拒绝时填写）
     */
    private String rejectReason;
}
