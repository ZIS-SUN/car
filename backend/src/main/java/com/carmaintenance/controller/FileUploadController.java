package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Value("${file.upload.path:/uploads/}")
    private String uploadPath;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 上传服务项目图片
     */
    @PostMapping("/service-image")
    public Result<Map<String, String>> uploadServiceImage(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "services");
    }

    /**
     * 上传套餐图片
     */
    @PostMapping("/package-image")
    public Result<Map<String, String>> uploadPackageImage(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "packages");
    }

    /**
     * 上传门店图片
     */
    @PostMapping("/shop/images")
    public Result<Map<String, String>> uploadShopImages(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "shops");
    }

    /**
     * 通用图片上传方法
     */
    private Result<Map<String, String>> uploadImage(MultipartFile file, String category) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 创建上传目录
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fullPath = uploadPath + category + "/" + datePath;
            Path uploadDir = Paths.get(fullPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 构建访问URL
            String fileUrl = contextPath + "/uploads/" + category + "/" + datePath + "/" + newFilename;
            if (fileUrl.startsWith("//")) {
                fileUrl = fileUrl.substring(1);
            }

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            result.put("size", String.valueOf(file.getSize()));
            result.put("contentType", contentType);

            return Result.success("上传成功", result);

        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<String> deleteFile(@RequestParam String filePath) {
        try {
            // 安全检查，防止路径遍历攻击
            if (filePath.contains("..") || filePath.contains("//")) {
                return Result.error("非法文件路径");
            }

            // 构建完整文件路径
            String fullPath = uploadPath + filePath.replace(contextPath + "/uploads/", "");
            Path path = Paths.get(fullPath);

            // 检查文件是否存在
            if (!Files.exists(path)) {
                return Result.error("文件不存在");
            }

            // 删除文件
            Files.delete(path);
            return Result.success("文件删除成功");

        } catch (IOException e) {
            return Result.error("文件删除失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/file/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam String filePath) {
        try {
            // 安全检查
            if (filePath.contains("..") || filePath.contains("//")) {
                return Result.error("非法文件路径");
            }

            String fullPath = uploadPath + filePath.replace(contextPath + "/uploads/", "");
            Path path = Paths.get(fullPath);

            if (!Files.exists(path)) {
                return Result.error("文件不存在");
            }

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("filename", path.getFileName().toString());
            fileInfo.put("size", Files.size(path));
            fileInfo.put("contentType", Files.probeContentType(path));
            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toString());

            return Result.success(fileInfo);

        } catch (Exception e) {
            return Result.error("获取文件信息失败：" + e.getMessage());
        }
    }
}