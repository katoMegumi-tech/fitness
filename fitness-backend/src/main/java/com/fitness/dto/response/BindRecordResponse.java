package com.fitness.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 绑定关系响应DTO
 */
@Data
public class BindRecordResponse {
    
    /**
     * 绑定ID
     */
    private Long bindId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户手机号
     */
    private String userPhone;
    
    /**
     * 用户邮箱
     */
    private String userEmail;
    
    /**
     * 教练ID
     */
    private Long coachUserId;
    
    /**
     * 教练姓名
     */
    private String coachName;
    
    /**
     * 教练手机号
     */
    private String coachPhone;
    
    /**
     * 教练邮箱
     */
    private String coachEmail;
    
    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;
    
    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;
    
    /**
     * 解绑原因
     */
    private String unbindReason;
}
