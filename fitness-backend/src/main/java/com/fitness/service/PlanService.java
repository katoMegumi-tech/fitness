package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.request.AuditRequest;
import com.fitness.dto.request.CreatePlanRequest;
import com.fitness.dto.response.FitnessPlanResponse;
import com.fitness.entity.BindRecord;
import com.fitness.entity.FitnessPlan;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.BindRecordMapper;
import com.fitness.mapper.FitnessPlanMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 健身计划服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
    
    private final FitnessPlanMapper planMapper;
    private final BindRecordMapper bindRecordMapper;
    private final UserMapper userMapper;
    
    // 敏感词列表（用于安全检查）
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
        "药物", "激素", "类固醇", "兴奋剂", "违禁", "非法", "危险", "极端",
        "禁食", "绝食", "过度", "暴力", "伤害"
    );
    
    /**
     * 检查计划内容是否安全
     */
    private boolean isSafePlan(CreatePlanRequest request) {
        String content = (request.getPlanName() + " " + 
                         request.getFitnessGoal() + " " + 
                         request.getExercisePlan() + " " + 
                         request.getDietPlan()).toLowerCase();
        
        // 检查是否包含敏感词
        for (String word : SENSITIVE_WORDS) {
            if (content.contains(word)) {
                log.warn("计划包含敏感词: {}", word);
                return false;
            }
        }
        
        // 检查计划周期是否合理（1-365天）
        if (request.getPlanCycle() == null || 
            request.getPlanCycle() < 1 || 
            request.getPlanCycle() > 365) {
            log.warn("计划周期不合理: {}", request.getPlanCycle());
            return false;
        }
        
        // 检查难度等级是否合理
        if (request.getPlanDifficulty() == null ||
            !Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(request.getPlanDifficulty())) {
            log.warn("计划难度不合理: {}", request.getPlanDifficulty());
            return false;
        }
        
        return true;
    }
    
    /**
     * 教练创建健身计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPlan(CreatePlanRequest request) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 验证教练和用户的绑定关系
        LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindRecord::getUserId, request.getUserId())
               .eq(BindRecord::getCoachUserId, coachId)
               .isNull(BindRecord::getUnbindTime);
        BindRecord bindRecord = bindRecordMapper.selectOne(wrapper);
        
        if (bindRecord == null) {
            throw new BusinessException("您与该用户没有绑定关系");
        }
        
        // 创建计划
        FitnessPlan plan = new FitnessPlan();
        plan.setPlanNo("PLAN-" + IdUtil.getSnowflakeNextIdStr());
        plan.setUserId(request.getUserId());
        plan.setCoachUserId(coachId);
        plan.setPlanName(request.getPlanName());
        plan.setFitnessGoal(request.getFitnessGoal());
        plan.setExercisePlan(request.getExercisePlan());
        plan.setDietPlan(request.getDietPlan());
        plan.setPlanDifficulty(request.getPlanDifficulty());
        plan.setPlanCycle(request.getPlanCycle());
        plan.setPlanStartTime(request.getPlanStartTime());
        plan.setPlanEndTime(request.getPlanEndTime());
        plan.setPlanStatus("DRAFT");  // 草稿状态
        plan.setVersion(1);
        plan.setAuditStatus("PENDING");  // 待审核状态
        
        planMapper.insert(plan);
        
        log.info("教练创建计划，等待管理员审核: {}", plan.getPlanNo());
    }
    
    /**
     * 教练调整健身计划（创建新版本）
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustPlan(Long planId, CreatePlanRequest request) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 查询原计划
        FitnessPlan oldPlan = planMapper.selectById(planId);
        if (oldPlan == null) {
            throw new BusinessException("计划不存在");
        }
        
        if (!oldPlan.getCoachUserId().equals(coachId)) {
            throw new BusinessException("无权调整此计划");
        }
        
        // 创建新版本计划
        FitnessPlan newPlan = new FitnessPlan();
        newPlan.setPlanNo("PLAN-" + IdUtil.getSnowflakeNextIdStr());
        newPlan.setUserId(oldPlan.getUserId());
        newPlan.setCoachUserId(coachId);
        newPlan.setPlanName(request.getPlanName());
        newPlan.setFitnessGoal(request.getFitnessGoal());
        newPlan.setExercisePlan(request.getExercisePlan());
        newPlan.setDietPlan(request.getDietPlan());
        newPlan.setPlanDifficulty(request.getPlanDifficulty());
        newPlan.setPlanCycle(request.getPlanCycle());
        newPlan.setPlanStartTime(request.getPlanStartTime());
        newPlan.setPlanEndTime(request.getPlanEndTime());
        newPlan.setPlanStatus("DRAFT");  // 草稿状态
        newPlan.setVersion(oldPlan.getVersion() + 1);
        newPlan.setParentPlanId(planId);
        newPlan.setAdjustmentReason(request.getAdjustmentReason());
        newPlan.setAuditStatus("PENDING");  // 待审核状态
        
        planMapper.insert(newPlan);
        
        log.info("教练调整计划，等待管理员审核: {}", newPlan.getPlanNo());
    }
    
    /**
     * 管理员审核健身计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPlan(Long planId, AuditRequest request) {
        Long adminId = StpUtil.getLoginIdAsLong();
        
        FitnessPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        
        if (!"PENDING".equals(plan.getAuditStatus())) {
            throw new BusinessException("该计划已被审核");
        }
        
        // 更新审核信息
        plan.setAuditStatus(request.getAuditStatus());
        plan.setAuditRemark(request.getAuditOpinion());
        plan.setAuditAdminId(adminId);
        plan.setAuditTime(LocalDateTime.now());
        
        // 如果审核通过，设置为待用户确认状态
        if ("APPROVED".equals(request.getAuditStatus())) {
            plan.setPlanStatus("PENDING");  // 等待用户确认
            plan.setUserConfirmStatus("PENDING");
        } else {
            // 审核拒绝，设置为草稿状态
            plan.setPlanStatus("DRAFT");
        }
        
        planMapper.updateById(plan);
    }
    
    /**
     * 用户确认健身计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPlan(Long planId, String confirmStatus, String rejectReason) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        FitnessPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权确认此计划");
        }
        
        if (!"APPROVED".equals(plan.getAuditStatus())) {
            throw new BusinessException("该计划尚未通过管理员审核");
        }
        
        if (!"PENDING".equals(plan.getUserConfirmStatus())) {
            throw new BusinessException("该计划已被确认");
        }
        
        // 更新用户确认信息
        plan.setUserConfirmStatus(confirmStatus);
        plan.setUserConfirmTime(LocalDateTime.now());
        
        if ("APPROVED".equals(confirmStatus)) {
            // 用户同意，激活计划
            plan.setPlanStatus("ACTIVE");
        } else {
            // 用户拒绝，返回给教练重新制定
            plan.setPlanStatus("DRAFT");
            plan.setUserRejectReason(rejectReason);
        }
        
        planMapper.updateById(plan);
    }
    
    /**
     * 查询用户的健身计划列表
     */
    public Page<FitnessPlanResponse> getUserPlans(int pageNum, int pageSize, String planStatus) {
        Long userId = StpUtil.getLoginIdAsLong();
        return getPlansByUserId(userId, pageNum, pageSize, planStatus);
    }
    
    /**
     * 检查用户是否有激活的计划
     */
    public boolean hasActivePlan(Long userId) {
        LambdaQueryWrapper<FitnessPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessPlan::getUserId, userId)
               .eq(FitnessPlan::getPlanStatus, "ACTIVE")
               .eq(FitnessPlan::getAuditStatus, "APPROVED")
               .eq(FitnessPlan::getUserConfirmStatus, "APPROVED");
        
        Long count = planMapper.selectCount(wrapper);
        return count > 0;
    }
    
    /**
     * 根据用户ID查询健身计划列表
     */
    public Page<FitnessPlanResponse> getPlansByUserId(Long userId, int pageNum, int pageSize, String planStatus) {
        Page<FitnessPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessPlan::getUserId, userId);
        
        // 如果指定了计划状态，则按状态筛选
        if (planStatus != null && !planStatus.isEmpty()) {
            wrapper.eq(FitnessPlan::getPlanStatus, planStatus);
        }
        
        wrapper.orderByDesc(FitnessPlan::getCreatedAt);
        
        Page<FitnessPlan> planPage = planMapper.selectPage(page, wrapper);
        
        Page<FitnessPlanResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(planPage.getTotal());
        
        List<FitnessPlanResponse> records = planPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    // 兼容旧方法
    public Page<FitnessPlanResponse> getPlansByUserId(Long userId, int pageNum, int pageSize) {
        return getPlansByUserId(userId, pageNum, pageSize, null);
    }
    
    /**
     * 查询教练创建的计划列表
     */
    public Page<FitnessPlanResponse> getCoachPlans(int pageNum, int pageSize) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        Page<FitnessPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessPlan::getCoachUserId, coachId)
               .orderByDesc(FitnessPlan::getCreatedAt);
        
        Page<FitnessPlan> planPage = planMapper.selectPage(page, wrapper);
        
        Page<FitnessPlanResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(planPage.getTotal());
        
        List<FitnessPlanResponse> records = planPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询待审核的计划列表（管理员）
     */
    public Page<FitnessPlanResponse> getPendingPlans(int pageNum, int pageSize, String auditStatus) {
        Page<FitnessPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessPlan> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了审核状态，则按状态筛选；否则查询所有
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(FitnessPlan::getAuditStatus, auditStatus);
        }
        
        wrapper.orderByDesc(FitnessPlan::getCreatedAt);
        
        Page<FitnessPlan> planPage = planMapper.selectPage(page, wrapper);
        
        Page<FitnessPlanResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(planPage.getTotal());
        
        List<FitnessPlanResponse> records = planPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 根据ID查询计划详情
     */
    public FitnessPlanResponse getPlanById(Long planId) {
        FitnessPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        return convertToResponse(plan);
    }
    
    /**
     * 转换为响应DTO
     */
    private FitnessPlanResponse convertToResponse(FitnessPlan plan) {
        FitnessPlanResponse response = new FitnessPlanResponse();
        BeanUtils.copyProperties(plan, response);
        
        // 查询用户信息
        User user = userMapper.selectById(plan.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
        }
        
        // 查询教练信息
        User coach = userMapper.selectById(plan.getCoachUserId());
        if (coach != null) {
            response.setCoachName(coach.getName());
        }
        
        return response;
    }
}
