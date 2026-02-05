package com.fitness.controller;

import com.fitness.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {
    
    // 文件上传根目录
    private static final String UPLOAD_DIR = "uploads/";
    
    /**
     * 上传单个文件
     */
    @PostMapping("/single")
    public Result<String> uploadSingle(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        try {
            String fileUrl = saveFile(file, "common");
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传多个文件
     */
    @PostMapping("/multiple")
    public Result<List<String>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("文件不能为空");
        }
        
        List<String> fileUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = saveFile(file, "common");
                    fileUrls.add(fileUrl);
                }
            }
            return Result.success(fileUrls);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传教练认证证书
     */
    @PostMapping("/coach/certificate")
    public Result<String> uploadCertificate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        try {
            String fileUrl = saveFile(file, "coach/certificates");
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("证书上传失败", e);
            return Result.error("证书上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传打卡图片
     */
    @PostMapping("/checkin/image")
    public Result<String> uploadCheckinImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        try {
            String fileUrl = saveFile(file, "checkin/images");
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("打卡图片上传失败", e);
            return Result.error("打卡图片上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存文件到本地
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("文件名为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 生成唯一文件名：日期_UUID.扩展名
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFilename = dateStr + "_" + uuid + extension;
        
        // 构建完整的保存路径（使用绝对路径）
        String relativePath = subDir + "/" + dateStr + "/" + newFilename;
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 创建子目录
        File subDirectory = new File(uploadDir, subDir + "/" + dateStr);
        if (!subDirectory.exists()) {
            subDirectory.mkdirs();
        }
        
        // 保存文件 - 使用绝对路径
        File destFile = new File(uploadDir, relativePath);
        Files.write(destFile.toPath(), file.getBytes());
        
        log.info("文件上传成功：{}", destFile.getAbsolutePath());
        
        // 返回相对路径（用于存储到数据库和访问）
        return "/uploads/" + relativePath;
    }
}
