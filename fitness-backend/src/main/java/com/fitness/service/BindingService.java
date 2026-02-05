package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.request.BindApplyRequest;
import com.fitness.dto.request.HandleBindRequest;
import com.fitness.dto.response.BindApplyResponse;
import com.fitness.dto.response.BindRecordResponse;
import com.fitness.entity.BindApply;
import com.fitness.entity.BindRecord;
import com.fitness.entity.CoachProfile;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.BindApplyMapper;
import com.fitness.mapper.BindRecordMapper;
import com.fitness.mapper.CoachProfileMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 绑定服务类
 */
@Service
@RequiredArgsConstructor
public class BindingService {
    
    private final BindApplyMapper bindApplyMapper;
    private final BindRecordMapper bindRecordMapper;
    private final UserMapper userMapper;
    private final CoachProfileMapper coachProfileMapper;
    
    /**
     * 用户发起绑定申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyBinding(BindApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证教练是否存在且已认证
        User coach = userMapper.selectById(request.getCoachUserId());
        if (coach == null || !"COACH".equals(coach.getRole())) {
            throw new BusinessException("教练不存在");
        }
        
        CoachProfile profile = coachProfileMapper.selectById(request.getCoachUserId());
        if (profile == null || !"APPROVED".equals(profile.getCertificationStatus())) {
            throw new BusinessException("该教练尚未通过认证");
        }
        
        // 检查是否已有绑定关系
        LambdaQueryWrapper<BindRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(BindRecord::getUserId, userId)
                     .isNull(BindRecord::getUnbindTime);
        Long bindCount = bindRecordMapper.selectCount(recordWrapper);
        if (bindCount > 0) {
            throw new BusinessException("您已绑定教练，请先解绑后再申请");
        }
        
        // 检查是否已有待处理的申请
        LambdaQueryWrapper<BindApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(BindApply::getUserId, userId)
                    .eq(BindApply::getApplyStatus, "PENDING");
        Long applyCount = bindApplyMapper.selectCount(applyWrapper);
        if (applyCount > 0) {
            throw new BusinessException("您已有待处理的绑定申请");
        }
        
        // 创建申请记录
        BindApply apply = new BindApply();
        apply.setUserId(userId);
        apply.setCoachUserId(request.getCoachUserId());
        apply.setApplyReason(request.getApplyReason());
        apply.setApplyStatus("PENDING");
        bindApplyMapper.insert(apply);
    }
    
    /**
     * 教练处理绑定申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleBinding(Long applyId, HandleBindRequest request) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 查询申请记录
        BindApply apply = bindApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        
        if (!apply.getCoachUserId().equals(coachId)) {
            throw new BusinessException("无权处理此申请");
        }
        
        if (!"PENDING".equals(apply.getApplyStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 更新申请状态
        apply.setApplyStatus(request.getApplyStatus());
        apply.setRejectReason(request.getRejectReason());
        apply.setHandleTime(LocalDateTime.now());
        bindApplyMapper.updateById(apply);
        
        // 如果通过，创建绑定关系
        if ("APPROVED".equals(request.getApplyStatus())) {
            // 再次检查用户是否已有绑定
            LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BindRecord::getUserId, apply.getUserId())
                   .isNull(BindRecord::getUnbindTime);
            Long count = bindRecordMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("该用户已绑定其他教练");
            }
            
            BindRecord record = new BindRecord();
            record.setUserId(apply.getUserId());
            record.setCoachUserId(coachId);
            bindRecordMapper.insert(record);
            
            // 更新教练的学员数
            CoachProfile profile = coachProfileMapper.selectById(coachId);
            if (profile != null) {
                profile.setTotalStudents(profile.getTotalStudents() + 1);
                coachProfileMapper.updateById(profile);
            }
        }
    }
    
    /**
     * 解绑
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbind(String unbindReason) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询当前绑定关系
        LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindRecord::getUserId, userId)
               .isNull(BindRecord::getUnbindTime);
        BindRecord record = bindRecordMapper.selectOne(wrapper);
        
        if (record == null) {
            throw new BusinessException("您尚未绑定教练");
        }
        
        // 更新解绑信息
        record.setUnbindTime(LocalDateTime.now());
        record.setUnbindReason(unbindReason);
        bindRecordMapper.updateById(record);
        
        // 更新教练的学员数
        CoachProfile profile = coachProfileMapper.selectById(record.getCoachUserId());
        if (profile != null && profile.getTotalStudents() > 0) {
            profile.setTotalStudents(profile.getTotalStudents() - 1);
            coachProfileMapper.updateById(profile);
        }
    }
    
    /**
     * 查询用户的绑定申请列表
     */
    public Page<BindApplyResponse> getUserApplies(int pageNum, int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<BindApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BindApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindApply::getUserId, userId)
               .orderByDesc(BindApply::getApplyTime);
        
        Page<BindApply> applyPage = bindApplyMapper.selectPage(page, wrapper);
        
        Page<BindApplyResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(applyPage.getTotal());
        
        List<BindApplyResponse> records = applyPage.getRecords().stream()
            .map(this::convertToApplyResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询教练收到的绑定申请列表
     */
    public Page<BindApplyResponse> getCoachApplies(int pageNum, int pageSize, String status) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        Page<BindApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BindApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindApply::getCoachUserId, coachId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BindApply::getApplyStatus, status);
        }
        
        wrapper.orderByDesc(BindApply::getApplyTime);
        
        Page<BindApply> applyPage = bindApplyMapper.selectPage(page, wrapper);
        
        Page<BindApplyResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(applyPage.getTotal());
        
        List<BindApplyResponse> records = applyPage.getRecords().stream()
            .map(this::convertToApplyResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询用户当前的绑定关系
     */
    public BindRecordResponse getCurrentBinding() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindRecord::getUserId, userId)
               .isNull(BindRecord::getUnbindTime);
        BindRecord record = bindRecordMapper.selectOne(wrapper);
        
        if (record == null) {
            return null;
        }
        
        return convertToRecordResponse(record);
    }
    
    /**
     * 查询教练的学员列表
     */
    public Page<BindRecordResponse> getCoachStudents(int pageNum, int pageSize) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        Page<BindRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindRecord::getCoachUserId, coachId)
               .isNull(BindRecord::getUnbindTime)
               .orderByDesc(BindRecord::getBindTime);
        
        Page<BindRecord> recordPage = bindRecordMapper.selectPage(page, wrapper);
        
        Page<BindRecordResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(recordPage.getTotal());
        
        List<BindRecordResponse> records = recordPage.getRecords().stream()
            .map(this::convertToRecordResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 转换申请记录为响应DTO
     */
    private BindApplyResponse convertToApplyResponse(BindApply apply) {
        BindApplyResponse response = new BindApplyResponse();
        BeanUtils.copyProperties(apply, response);
        
        // 查询用户信息
        User user = userMapper.selectById(apply.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
        }
        
        // 查询教练信息
        User coach = userMapper.selectById(apply.getCoachUserId());
        if (coach != null) {
            response.setCoachName(coach.getName());
        }
        
        return response;
    }
    
    /**
     * 转换绑定记录为响应DTO
     */
    private BindRecordResponse convertToRecordResponse(BindRecord record) {
        BindRecordResponse response = new BindRecordResponse();
        BeanUtils.copyProperties(record, response);
        
        // 查询用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
            response.setUserPhone(user.getPhone());
            response.setUserEmail(user.getEmail());
        }
        
        // 查询教练信息
        User coach = userMapper.selectById(record.getCoachUserId());
        if (coach != null) {
            response.setCoachName(coach.getName());
            response.setCoachPhone(coach.getPhone());
            response.setCoachEmail(coach.getEmail());
        }
        
        return response;
    }
}
