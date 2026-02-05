package com.fitness.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 绑定申请响应DTO
 */
@Data
public class BindApplyResponse {
    
    /**
     * 申请ID
     */
    private Long applyId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 教练ID
     */
    private Long coachUserId;
    
    /**
     * 教练姓名
     */
    private String coachName;
    
    /**
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 申请状态
     */
    private String applyStatus;
    
    /**
     * 拒绝理由
     */
    private String rejectReason;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
}
