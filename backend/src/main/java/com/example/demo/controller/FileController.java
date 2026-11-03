package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileController {

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    @GetMapping("/uploads/avatars/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveAvatar(@PathVariable String filename) {
        try {
            File file = new File(uploadDir, filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            String contentType = "image/png";
            String name = filename.toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (name.endsWith(".gif")) contentType = "image/gif";
            else if (name.endsWith(".webp")) contentType = "image/webp";
            else if (name.endsWith(".bmp")) contentType = "image/bmp";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/uploads/articles/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveArticleImage(@PathVariable String filename) {
        try {
            String articleUploadDir = uploadDir.replace("/avatars", "/articles");
            File file = new File(articleUploadDir, filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            String contentType = "image/png";
            String name = filename.toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (name.endsWith(".gif")) contentType = "image/gif";
            else if (name.endsWith(".webp")) contentType = "image/webp";
            else if (name.endsWith(".bmp")) contentType = "image/bmp";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空");
            return result;
        }

        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 使用文章图片上传目录
            String articleUploadDir = uploadDir.replace("/avatars", "/articles");
            File dir = new File(articleUploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir, filename);
            file.transferTo(dest);

            result.put("success", true);
            result.put("url", "/uploads/articles/" + filename);
            result.put("name", originalName);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
    }
}
