package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.Result;
import com.fitness.entity.CheckInRecord;
import com.fitness.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 打卡控制器
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {
    
    private final CheckInService checkInService;
    
    /**
     * 提交打卡
     */
    @PostMapping("/submit")
    @SaCheckRole("USER")
    public Result<Void> submitCheckIn(@RequestBody CheckInRecord checkInRecord) {
        checkInService.submitCheckIn(checkInRecord);
        return Result.success();
    }
    
    /**
     * 查询我的打卡历史
     */
    @GetMapping("/my-history")
    @SaCheckRole("USER")
    public Result<IPage<CheckInRecord>> getMyCheckInHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<CheckInRecord> page = checkInService.getCheckInHistory(userId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询学员的打卡记录（教练端）
     */
    @GetMapping("/student/{studentId}")
    @SaCheckRole("COACH")
    public Result<IPage<CheckInRecord>> getStudentCheckIns(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CheckInRecord> page = checkInService.getStudentCheckIns(studentId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 教练点评打卡记录
     */
    @PostMapping("/comment/{checkInId}")
    @SaCheckRole("COACH")
    public Result<Void> addCoachComment(
            @PathVariable Long checkInId,
            @RequestBody Map<String, Object> request) {
        String comment = (String) request.get("comment");
        Integer isQualified = request.get("isQualified") != null ? 
            Integer.parseInt(request.get("isQualified").toString()) : null;
        checkInService.addCoachComment(checkInId, comment, isQualified);
        return Result.success();
    }
    
    /**
     * 获取身体数据趋势
     */
    @GetMapping("/trend")
    @SaCheckRole("USER")
    public Result<Map<String, Object>> getBodyDataTrend(
            @RequestParam(defaultValue = "30") Integer days) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> trend = checkInService.getBodyDataTrend(userId, days);
        return Result.success(trend);
    }
    
    /**
     * 获取打卡统计数据
     */
    @GetMapping("/stats")
    @SaCheckRole("USER")
    public Result<Map<String, Object>> getCheckInStats(
            @RequestParam(defaultValue = "30") Integer days) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> stats = checkInService.getCheckInStats(userId, days);
        return Result.success(stats);
    }
    
    /**
     * 查询待审核的打卡列表（教练端）
     */
    @GetMapping("/pending")
    @SaCheckRole("COACH")
    public Result<IPage<Map<String, Object>>> getPendingCheckIns(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String checkType) {
        IPage<Map<String, Object>> page = checkInService.getPendingCheckIns(pageNum, pageSize, checkType);
        return Result.success(page);
    }
    
    /**
     * 获取学员打卡统计（教练端）
     */
    @GetMapping("/coach/stats")
    @SaCheckRole("COACH")
    public Result<IPage<Map<String, Object>>> getStudentCheckInStats(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long coachId = StpUtil.getLoginIdAsLong();
        IPage<Map<String, Object>> page = checkInService.getStudentCheckInStats(coachId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取学员打卡记录列表（教练端）
     */
    @GetMapping("/coach/records")
    @SaCheckRole("COACH")
    public Result<IPage<Map<String, Object>>> getStudentCheckInRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        IPage<Map<String, Object>> page = checkInService.getStudentCheckInRecords(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
