package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.request.CoachCertRequest;
import com.fitness.dto.response.CoachProfileResponse;
import com.fitness.entity.BindRecord;
import com.fitness.entity.CertAuditRecord;
import com.fitness.entity.CoachProfile;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.BindRecordMapper;
import com.fitness.mapper.CertAuditRecordMapper;
import com.fitness.mapper.CoachProfileMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachService {
    
    private final CoachProfileMapper coachProfileMapper;
    private final CertAuditRecordMapper certAuditRecordMapper;
    private final UserMapper userMapper;
    private final BindRecordMapper bindRecordMapper;
    
    /**
     * 提交教练认证申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitCertification(CoachCertRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查用户角色是否为COACH
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"COACH".equals(user.getRole())) {
            throw new BusinessException("只有教练角色才能申请认证");
        }
        
        // 检查是否已有待审核的申请
        LambdaQueryWrapper<CertAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertAuditRecord::getCoachUserId, userId)
               .eq(CertAuditRecord::getAuditStatus, "PENDING");
        Long count = certAuditRecordMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("您已有待审核的认证申请，请勿重复提交");
        }
        
        // 创建或更新教练资料
        CoachProfile profile = coachProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new CoachProfile();
            profile.setUserId(userId);
            profile.setCertificationStatus("PENDING");
            profile.setRating(java.math.BigDecimal.valueOf(5.00));
            profile.setTotalStudents(0);
        }
        
        // 将证书列表转换为JSON字符串
        profile.setCertsPath(JSONUtil.toJsonStr(request.getCertImages()));
        profile.setIntroduction(request.getIntroduction());
        profile.setSpecialties(request.getSpecialties());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setCertificationStatus("PENDING");
        
        if (profile.getCreatedAt() == null) {
            coachProfileMapper.insert(profile);
        } else {
            coachProfileMapper.updateById(profile);
        }
        
        // 创建审核记录
        CertAuditRecord record = new CertAuditRecord();
        record.setCoachUserId(userId);
        record.setAuditStatus("PENDING");
        certAuditRecordMapper.insert(record);
    }
    
    /**
     * 查询教练认证状态
     */
    public CoachProfileResponse getCertificationStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getCoachProfile(userId);
    }
    
    /**
     * 根据ID查询教练资料
     */
    public CoachProfileResponse getCoachProfile(Long coachId) {
        User user = userMapper.selectById(coachId);
        if (user == null) {
            throw new BusinessException("教练不存在");
        }
        
        CoachProfile profile = coachProfileMapper.selectById(coachId);
        
        CoachProfileResponse response = new CoachProfileResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setAccount(user.getAccount());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        
        if (profile != null) {
            // 解析证书JSON
            String certsPath = profile.getCertsPath();
            if (certsPath != null && !certsPath.trim().isEmpty()) {
                try {
                    List<CoachProfileResponse.CertImage> certImages = 
                        JSONUtil.toList(certsPath, CoachProfileResponse.CertImage.class);
                    response.setCertImages(certImages);
                } catch (Exception e) {
                    log.warn("解析证书路径失败: {}", certsPath, e);
                    response.setCertImages(new ArrayList<>());
                }
            } else {
                response.setCertImages(new ArrayList<>());
            }
            response.setIntroduction(profile.getIntroduction());
            response.setSpecialties(profile.getSpecialties());
            response.setYearsOfExperience(profile.getYearsOfExperience());
            response.setCertificationStatus(profile.getCertificationStatus());
            response.setRating(profile.getRating());
            response.setTotalStudents(profile.getTotalStudents());
            response.setCreatedAt(profile.getCreatedAt());
        } else {
            response.setCertificationStatus("NOT_APPLIED");
        }
        
        return response;
    }
    
    /**
     * 查询待审核的教练列表（管理员）
     */
    public Page<CoachProfileResponse> getPendingCoaches(int pageNum, int pageSize, String certificationStatus) {
        // 查询教练资料
        Page<CoachProfile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CoachProfile> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了状态，则按状态筛选；否则查询所有
        if (certificationStatus != null && !certificationStatus.isEmpty()) {
            wrapper.eq(CoachProfile::getCertificationStatus, certificationStatus);
        }
        
        wrapper.orderByAsc(CoachProfile::getCreatedAt);
        
        Page<CoachProfile> profilePage = coachProfileMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO
        Page<CoachProfileResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(profilePage.getTotal());
        
        List<CoachProfileResponse> records = profilePage.getRecords().stream()
            .map(profile -> getCoachProfile(profile.getUserId()))
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询已认证的教练列表
     */
    public Page<CoachProfileResponse> getApprovedCoaches(int pageNum, int pageSize) {
        Page<CoachProfile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CoachProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachProfile::getCertificationStatus, "APPROVED")
               .orderByDesc(CoachProfile::getRating)
               .orderByDesc(CoachProfile::getTotalStudents);
        
        Page<CoachProfile> profilePage = coachProfileMapper.selectPage(page, wrapper);
        
        Page<CoachProfileResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(profilePage.getTotal());
        
        List<CoachProfileResponse> records = profilePage.getRecords().stream()
            .map(profile -> getCoachProfile(profile.getUserId()))
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询可选择的教练列表（排除当前用户已绑定的教练）
     */
    public Page<CoachProfileResponse> getAvailableCoaches(int pageNum, int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户当前绑定的教练ID
        LambdaQueryWrapper<BindRecord> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(BindRecord::getUserId, userId)
                   .isNull(BindRecord::getUnbindTime);
        BindRecord currentBind = bindRecordMapper.selectOne(bindWrapper);
        
        Page<CoachProfile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CoachProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachProfile::getCertificationStatus, "APPROVED");
        
        // 如果已有绑定，排除该教练
        if (currentBind != null) {
            wrapper.ne(CoachProfile::getUserId, currentBind.getCoachUserId());
        }
        
        wrapper.orderByDesc(CoachProfile::getRating)
               .orderByDesc(CoachProfile::getTotalStudents);
        
        Page<CoachProfile> profilePage = coachProfileMapper.selectPage(page, wrapper);
        
        Page<CoachProfileResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(profilePage.getTotal());
        
        List<CoachProfileResponse> records = profilePage.getRecords().stream()
            .map(profile -> getCoachProfile(profile.getUserId()))
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
}
