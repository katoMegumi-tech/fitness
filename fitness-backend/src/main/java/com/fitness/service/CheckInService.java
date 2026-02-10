package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.BindRecord;
import com.fitness.entity.CheckInRecord;
import com.fitness.entity.UserBodyHistory;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.BindRecordMapper;
import com.fitness.mapper.CheckInRecordMapper;
import com.fitness.mapper.UserBodyHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡服务类
 */
@Service
@RequiredArgsConstructor
public class CheckInService {
    
    private final CheckInRecordMapper checkInRecordMapper;
    private final UserBodyHistoryMapper userBodyHistoryMapper;
    private final BindRecordMapper bindRecordMapper;
    private final NotificationService notificationService;
    private final com.fitness.mapper.UserMapper userMapper;
    
    /**
     * 提交打卡
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitCheckIn(CheckInRecord checkInRecord) {
        Long userId = StpUtil.getLoginIdAsLong();
        checkInRecord.setUserId(userId);
        
        // 验证完成度范围（0-100）
        if (checkInRecord.getExerciseCompletion() < 0 || checkInRecord.getExerciseCompletion() > 100) {
            throw new BusinessException("运动完成度必须在0-100之间");
        }
        
        // 设置默认值
        if (checkInRecord.getCheckType() == null) {
            checkInRecord.setCheckType("NORMAL");
        }
        
        // 如果是完成目标打卡，必须填写身体数据
        if ("TARGET".equals(checkInRecord.getCheckType())) {
            if (checkInRecord.getWeight() == null || 
                checkInRecord.getBodyFatRate() == null || 
                checkInRecord.getMuscleMass() == null) {
                throw new BusinessException("完成目标打卡必须填写体重、体脂率和肌肉量");
            }
        }
        
        if (checkInRecord.getCheckTime() == null) {
            checkInRecord.setCheckTime(LocalDateTime.now());
        }
        
        // 生成打卡编号：CHK + 时间戳 + 用户ID后4位
        String checkNo = "CHK" + System.currentTimeMillis() + String.format("%04d", userId % 10000);
        checkInRecord.setCheckNo(checkNo);
        
        // 如果没有指定计划ID，使用默认值0（表示无计划打卡）
        if (checkInRecord.getPlanId() == null) {
            checkInRecord.setPlanId(0L);
        }
        
        // 设置打卡状态
        // 完成目标打卡需要审核，状态为 PENDING
        // 普通打卡不需要审核，状态为 COMPLETED
        if ("TARGET".equals(checkInRecord.getCheckType())) {
            checkInRecord.setCheckStatus("PENDING");
        } else {
            checkInRecord.setCheckStatus("COMPLETED");
        }
        
        // 保存打卡记录
        checkInRecordMapper.insert(checkInRecord);
        
        // 如果是完成目标打卡，发送通知给教练
        if ("TARGET".equals(checkInRecord.getCheckType())) {
            // 查询用户的教练
            LambdaQueryWrapper<BindRecord> bindWrapper = new LambdaQueryWrapper<>();
            bindWrapper.eq(BindRecord::getUserId, userId)
                       .isNull(BindRecord::getUnbindTime);
            BindRecord bindRecord = bindRecordMapper.selectOne(bindWrapper);
            
            if (bindRecord != null) {
                notificationService.sendNotification(
                    bindRecord.getCoachUserId(),
                    userId,
                    "CHECK_IN",
                    "学员完成目标打卡",
                    "您的学员提交了完成目标打卡，请及时审核判断是否达标",
                    checkInRecord.getCheckId()
                );
            }
        }
        
        // 如果记录了体重和体脂，自动更新身体数据历史
        if (checkInRecord.getWeight() != null || checkInRecord.getBodyFatRate() != null) {
            UserBodyHistory history = new UserBodyHistory();
            history.setUserId(userId);
            history.setRecordDate(checkInRecord.getCheckTime().toLocalDate());
            history.setWeight(checkInRecord.getWeight());
            history.setBodyFatRate(checkInRecord.getBodyFatRate());
            if (checkInRecord.getMuscleMass() != null) {
                history.setMuscleMass(checkInRecord.getMuscleMass());
            }
            history.setRecordSource("CHECK_IN");
            userBodyHistoryMapper.insert(history);
        }
    }
    
    /**
     * 查询打卡历史（分页）
     */
    public IPage<CheckInRecord> getCheckInHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<CheckInRecord> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
               .orderByDesc(CheckInRecord::getCheckTime);
        
        return checkInRecordMapper.selectPage(page, wrapper);
    }
    
    /**
     * 教练点评打卡记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCoachComment(Long checkInId, String comment, Integer isQualified) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 查询打卡记录
        CheckInRecord checkInRecord = checkInRecordMapper.selectById(checkInId);
        if (checkInRecord == null) {
            throw new BusinessException("打卡记录不存在");
        }
        
        // 验证教练是否有权限点评（必须是该用户的教练）
        LambdaQueryWrapper<BindRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BindRecord::getUserId, checkInRecord.getUserId())
               .eq(BindRecord::getCoachUserId, coachId)
               .isNull(BindRecord::getUnbindTime);
        
        Long count = bindRecordMapper.selectCount(wrapper);
        if (count == 0) {
            throw new BusinessException("您不是该用户的教练，无权点评");
        }
        
        // 更新点评
        checkInRecord.setCoachComment(comment);
        checkInRecord.setCoachCommentTime(LocalDateTime.now());
        
        // 如果是完成目标打卡，需要设置是否达标
        if ("TARGET".equals(checkInRecord.getCheckType())) {
            if (isQualified == null) {
                throw new BusinessException("完成目标打卡必须判断是否达标");
            }
            checkInRecord.setIsQualified(isQualified);
            
            // 审核后状态变为已完结
            checkInRecord.setCheckStatus("COMPLETED");
            
            // 如果达标，自动解绑
            if (isQualified == 1) {
                LambdaQueryWrapper<BindRecord> bindWrapper = new LambdaQueryWrapper<>();
                bindWrapper.eq(BindRecord::getUserId, checkInRecord.getUserId())
                           .eq(BindRecord::getCoachUserId, coachId)
                           .isNull(BindRecord::getUnbindTime);
                BindRecord bindRecord = bindRecordMapper.selectOne(bindWrapper);
                if (bindRecord != null) {
                    bindRecord.setUnbindTime(LocalDateTime.now());
                    bindRecord.setUnbindReason("完成目标，自动解绑");
                    bindRecordMapper.updateById(bindRecord);
                }
                
                // 达标通知
                notificationService.sendNotification(
                    checkInRecord.getUserId(),
                    coachId,
                    "CHECK_IN",
                    "恭喜！您已达成健身目标",
                    "教练已确认您达成健身目标，训练计划完成。您可以重新选择教练开始新的训练！",
                    checkInRecord.getCheckId()
                );
            } else {
                // 未达标通知
                notificationService.sendNotification(
                    checkInRecord.getUserId(),
                    coachId,
                    "CHECK_IN",
                    "打卡审核结果",
                    "教练已审核您的打卡，暂未达标。请继续努力，坚持锻炼！",
                    checkInRecord.getCheckId()
                );
            }
        }
        
        checkInRecordMapper.updateById(checkInRecord);
    }
    
    /**
     * 查询学员的打卡记录（教练端）
     */
    public IPage<CheckInRecord> getStudentCheckIns(Long studentId, Integer pageNum, Integer pageSize) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 验证绑定关系
        LambdaQueryWrapper<BindRecord> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(BindRecord::getUserId, studentId)
                   .eq(BindRecord::getCoachUserId, coachId)
                   .isNull(BindRecord::getUnbindTime);
        
        Long count = bindRecordMapper.selectCount(bindWrapper);
        if (count == 0) {
            throw new BusinessException("该用户不是您的学员");
        }
        
        // 查询打卡记录
        return getCheckInHistory(studentId, pageNum, pageSize);
    }
    
    /**
     * 生成身体数据趋势图表数据
     */
    public Map<String, Object> getBodyDataTrend(Long userId, Integer days) {
        // 查询最近N天的打卡记录
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
               .ge(CheckInRecord::getCheckTime, startTime)
               .orderByAsc(CheckInRecord::getCheckTime);
        
        List<CheckInRecord> records = checkInRecordMapper.selectList(wrapper);
        
        // 构建图表数据
        Map<String, Object> result = new HashMap<>();
        result.put("dates", records.stream().map(r -> r.getCheckTime().toLocalDate().toString()).toArray());
        result.put("weights", records.stream().map(CheckInRecord::getWeight).toArray());
        result.put("bodyFats", records.stream().map(CheckInRecord::getBodyFatRate).toArray());
        result.put("exerciseCompletions", records.stream().map(CheckInRecord::getExerciseCompletion).toArray());
        result.put("dietCompletions", records.stream().map(r -> {
            // 将饮食完成度字符串转换为数字
            String diet = r.getDietCompletion();
            if ("COMPLETE".equals(diet)) return 100;
            if ("PARTIAL".equals(diet)) return 50;
            return 0;
        }).toArray());
        
        return result;
    }
    
    /**
     * 获取打卡统计数据
     */
    public Map<String, Object> getCheckInStats(Long userId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
               .ge(CheckInRecord::getCheckTime, startTime);
        
        List<CheckInRecord> records = checkInRecordMapper.selectList(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", days);
        stats.put("checkInDays", records.size());
        stats.put("checkInRate", records.isEmpty() ? 0 : (records.size() * 100.0 / days));
        
        // 计算平均完成度
        if (!records.isEmpty()) {
            double avgExercise = records.stream()
                    .mapToInt(CheckInRecord::getExerciseCompletion)
                    .average()
                    .orElse(0);
            double avgDiet = records.stream()
                    .mapToInt(r -> {
                        String diet = r.getDietCompletion();
                        if ("COMPLETE".equals(diet)) return 100;
                        if ("PARTIAL".equals(diet)) return 50;
                        return 0;
                    })
                    .average()
                    .orElse(0);
            
            stats.put("avgExerciseCompletion", avgExercise);
            stats.put("avgDietCompletion", avgDiet);
        } else {
            stats.put("avgExerciseCompletion", 0);
            stats.put("avgDietCompletion", 0);
        }
        
        return stats;
    }
    
    /**
     * 查询待审核的打卡列表（教练端）
     */
    public IPage<Map<String, Object>> getPendingCheckIns(Integer pageNum, Integer pageSize, String checkType) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        // 查询教练的所有学员
        LambdaQueryWrapper<BindRecord> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(BindRecord::getCoachUserId, coachId)
                   .isNull(BindRecord::getUnbindTime);
        List<BindRecord> bindRecords = bindRecordMapper.selectList(bindWrapper);
        
        if (bindRecords.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        List<Long> studentIds = bindRecords.stream()
                .map(BindRecord::getUserId)
                .collect(java.util.stream.Collectors.toList());
        
        // 查询学员的打卡记录
        Page<CheckInRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CheckInRecord::getUserId, studentIds);
        
        // 如果指定了打卡类型，则筛选
        if (checkType != null && !checkType.isEmpty()) {
            wrapper.eq(CheckInRecord::getCheckType, checkType);
            // 只显示待审核的完成目标打卡（状态为PENDING）
            if ("TARGET".equals(checkType)) {
                wrapper.eq(CheckInRecord::getCheckStatus, "PENDING");
            }
        }
        
        wrapper.orderByDesc(CheckInRecord::getCheckTime);
        
        IPage<CheckInRecord> checkInPage = checkInRecordMapper.selectPage(page, wrapper);
        
        // 转换为包含用户信息的Map
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(checkInPage.getTotal());
        
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (CheckInRecord checkIn : checkInPage.getRecords()) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("checkInId", checkIn.getCheckId());  // 前端使用 checkInId
            map.put("checkId", checkIn.getCheckId());    // 保持一致性
            map.put("checkNo", checkIn.getCheckNo());
            map.put("userId", checkIn.getUserId());
            map.put("checkType", checkIn.getCheckType());
            map.put("checkStatus", checkIn.getCheckStatus());
            map.put("checkTime", checkIn.getCheckTime());
            map.put("exerciseCompletion", checkIn.getExerciseCompletion());
            map.put("dietCompletion", checkIn.getDietCompletion());
            map.put("weight", checkIn.getWeight());
            map.put("bodyFatRate", checkIn.getBodyFatRate());
            map.put("muscleMass", checkIn.getMuscleMass());
            map.put("userRemark", checkIn.getUserRemark());
            map.put("images", checkIn.getImages());
            map.put("coachComment", checkIn.getCoachComment());
            map.put("isQualified", checkIn.getIsQualified());
            
            // 查询用户姓名
            com.fitness.entity.User user = userMapper.selectById(checkIn.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
            }
            
            records.add(map);
        }
        
        resultPage.setRecords(records);
        return resultPage;
    }
}
