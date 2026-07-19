package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.dto.request.UserBodyDataRequest;
import com.fitness.dto.response.UserBodyDataResponse;
import com.fitness.entity.User;
import com.fitness.entity.UserBodyBaseData;
import com.fitness.entity.UserBodyHistory;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.UserBodyBaseDataMapper;
import com.fitness.mapper.UserBodyHistoryMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户信息服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserBodyBaseDataMapper bodyBaseDataMapper;
    private final UserBodyHistoryMapper bodyHistoryMapper;
    private final UserMapper userMapper;
    
    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, Map<String, Object> updates) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 只允许更新特定字段
        if (updates.containsKey("avatar")) {
            user.setAvatar((String) updates.get("avatar"));
        }
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        
        userMapper.updateById(user);
    }
    
    /**
     * 保存或更新用户身体数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBodyData(UserBodyDataRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询是否已有基础数据
        LambdaQueryWrapper<UserBodyBaseData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBodyBaseData::getUserId, userId);
        UserBodyBaseData baseData = bodyBaseDataMapper.selectOne(wrapper);
        
        if (baseData == null) {
            // 新增
            baseData = new UserBodyBaseData();
            baseData.setUserId(userId);
            baseData.setHeight(request.getHeight());
            baseData.setTargetWeight(request.getTargetWeight());
            baseData.setTargetBodyFatRate(request.getTargetBodyFatRate());
            baseData.setFoodPreference(request.getFoodPreference());
            baseData.setAllergens(request.getAllergens());
            baseData.setFitnessGoal(request.getFitnessGoal());
            baseData.setExerciseFrequency(request.getExerciseFrequency());
            baseData.setExerciseDuration(request.getExerciseDuration());
            baseData.setHealthConditions(request.getHealthConditions());
            bodyBaseDataMapper.insert(baseData);
        } else {
            // 更新
            baseData.setHeight(request.getHeight());
            baseData.setTargetWeight(request.getTargetWeight());
            baseData.setTargetBodyFatRate(request.getTargetBodyFatRate());
            baseData.setFoodPreference(request.getFoodPreference());
            baseData.setAllergens(request.getAllergens());
            baseData.setFitnessGoal(request.getFitnessGoal());
            baseData.setExerciseFrequency(request.getExerciseFrequency());
            baseData.setExerciseDuration(request.getExerciseDuration());
            baseData.setHealthConditions(request.getHealthConditions());
            bodyBaseDataMapper.updateById(baseData);
        }
        
        // 创建历史记录（如果提供了当前体重等数据）
        if (request.getCurrentWeight() != null) {
            // 计算BMI
            BigDecimal bmi = calculateBMI(request.getCurrentWeight(), request.getHeight());
            
            UserBodyHistory history = new UserBodyHistory();
            history.setUserId(userId);
            history.setWeight(request.getCurrentWeight());
            history.setBodyFatRate(request.getCurrentBodyFatRate());
            history.setMuscleMass(request.getMuscleMass());
            history.setBmi(bmi);
            history.setWaistline(request.getWaistline());
            history.setRecordDate(LocalDate.now());
            history.setRecordSource("MANUAL");
            bodyHistoryMapper.insert(history);
        }
    }
    
    /**
     * 查询用户身体基础数据
     */
    public UserBodyDataResponse getBodyData() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getBodyDataByUserId(userId);
    }
    
    /**
     * 根据用户ID查询身体基础数据
     */
    public UserBodyDataResponse getBodyDataByUserId(Long userId) {
        LambdaQueryWrapper<UserBodyBaseData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBodyBaseData::getUserId, userId);
        UserBodyBaseData baseData = bodyBaseDataMapper.selectOne(wrapper);
        
        if (baseData == null) {
            return null;
        }
        
        UserBodyDataResponse response = new UserBodyDataResponse();
        BeanUtils.copyProperties(baseData, response);
        return response;
    }
    
    /**
     * 查询用户身体数据历史记录
     */
    public List<UserBodyHistory> getBodyHistory(Integer days) {
        Long userId = StpUtil.getLoginIdAsLong();
        return getBodyHistoryByUserId(userId, days);
    }
    
    /**
     * 根据用户ID查询身体数据历史记录
     */
    public List<UserBodyHistory> getBodyHistoryByUserId(Long userId, Integer days) {
        LambdaQueryWrapper<UserBodyHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBodyHistory::getUserId, userId);
        
        if (days != null && days > 0) {
            LocalDate startDate = LocalDate.now().minusDays(days);
            wrapper.ge(UserBodyHistory::getRecordDate, startDate);
        }
        
        wrapper.orderByDesc(UserBodyHistory::getRecordDate);
        return bodyHistoryMapper.selectList(wrapper);
    }
    
    /**
     * 计算BMI指数
     * BMI = 体重(kg) / 身高(m)²
     */
    private BigDecimal calculateBMI(BigDecimal weight, BigDecimal height) {
        if (weight == null || height == null || height.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal heightSquared = height.multiply(height);
        return weight.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }
}
