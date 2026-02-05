package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.dto.request.AuditRequest;
import com.fitness.entity.CertAuditRecord;
import com.fitness.entity.CoachProfile;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CertAuditRecordMapper;
import com.fitness.mapper.CoachProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审核服务类
 */
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final CertAuditRecordMapper certAuditRecordMapper;
    private final CoachProfileMapper coachProfileMapper;
    
    /**
     * 审核教练资质
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditCoachCertification(Long coachUserId, AuditRequest request) {
        Long adminId = StpUtil.getLoginIdAsLong();
        
        // 验证审核状态
        if (!"APPROVED".equals(request.getAuditStatus()) && 
            !"REJECTED".equals(request.getAuditStatus())) {
            throw new BusinessException("审核状态无效");
        }
        
        // 查询待审核的记录
        LambdaQueryWrapper<CertAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertAuditRecord::getCoachUserId, coachUserId)
               .eq(CertAuditRecord::getAuditStatus, "PENDING")
               .orderByDesc(CertAuditRecord::getApplyTime)
               .last("LIMIT 1");
        
        CertAuditRecord record = certAuditRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到待审核的认证申请");
        }
        
        // 更新审核记录
        record.setAuditStatus(request.getAuditStatus());
        record.setAuditOpinion(request.getAuditOpinion());
        record.setAdminId(adminId);
        record.setAuditTime(LocalDateTime.now());
        certAuditRecordMapper.updateById(record);
        
        // 更新教练资料的认证状态
        CoachProfile profile = coachProfileMapper.selectById(coachUserId);
        if (profile != null) {
            profile.setCertificationStatus(request.getAuditStatus());
            coachProfileMapper.updateById(profile);
        }
    }
    
    /**
     * 撤销教练审核（将状态改回待审核）
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeCoachAudit(Long coachUserId) {
        Long adminId = StpUtil.getLoginIdAsLong();
        
        // 查询最近的审核记录
        LambdaQueryWrapper<CertAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertAuditRecord::getCoachUserId, coachUserId)
               .in(CertAuditRecord::getAuditStatus, "APPROVED", "REJECTED")
               .orderByDesc(CertAuditRecord::getAuditTime)
               .last("LIMIT 1");
        
        CertAuditRecord record = certAuditRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到可撤销的审核记录");
        }
        
        // 更新审核记录为待审核
        record.setAuditStatus("PENDING");
        record.setAuditOpinion("审核已撤销");
        record.setAdminId(adminId);
        record.setAuditTime(LocalDateTime.now());
        certAuditRecordMapper.updateById(record);
        
        // 更新教练资料的认证状态
        CoachProfile profile = coachProfileMapper.selectById(coachUserId);
        if (profile != null) {
            profile.setCertificationStatus("PENDING");
            coachProfileMapper.updateById(profile);
        }
    }
    
    /**
     * 查询教练的审核历史
     */
    public java.util.List<CertAuditRecord> getAuditHistory(Long coachUserId) {
        LambdaQueryWrapper<CertAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertAuditRecord::getCoachUserId, coachUserId)
               .orderByDesc(CertAuditRecord::getApplyTime);
        return certAuditRecordMapper.selectList(wrapper);
    }
}
