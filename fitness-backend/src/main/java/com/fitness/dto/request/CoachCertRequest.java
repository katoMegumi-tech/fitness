package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 教练认证申请请求DTO
 */
@Data
public class CoachCertRequest {
    
    /**
     * 证书图片路径列表
     */
    @NotNull(message = "证书图片不能为空")
    private List<CertImage> certImages;
    
    /**
     * 个人简介
     */
    @NotBlank(message = "个人简介不能为空")
    private String introduction;
    
    /**
     * 擅长领域
     */
    @NotBlank(message = "擅长领域不能为空")
    private String specialties;
    
    /**
     * 从业年限
     */
    @NotNull(message = "从业年限不能为空")
    private Integer yearsOfExperience;
    
    /**
     * 证书图片信息
     */
    @Data
    public static class CertImage {
        /**
         * 证书名称
         */
        private String name;
        
        /**
         * 证书图片路径
         */
        private String path;
    }
}
