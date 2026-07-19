package com.fitness.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教练资料响应DTO
 */
@Data
public class CoachProfileResponse {
    
    /**
     * 教练用户ID
     */
    private Long userId;
    
    /**
     * 教练姓名
     */
    private String name;
    
    /**
     * 教练账号
     */
    private String account;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 证书图片列表
     */
    private List<CertImage> certImages;
    
    /**
     * 个人简介
     */
    private String introduction;
    
    /**
     * 擅长领域
     */
    private String specialties;
    
    /**
     * 从业年限
     */
    private Integer yearsOfExperience;
    
    /**
     * 认证状态：PENDING/APPROVED/REJECTED
     */
    private String certificationStatus;
    private String auditOpinion;
    private LocalDateTime auditTime;
    
    /**
     * 教练评分
     */
    private BigDecimal rating;
    
    /**
     * 累计服务学员数
     */
    private Integer totalStudents;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 证书图片信息
     */
    @Data
    public static class CertImage {
        private String name;
        private String path;
    }
}
