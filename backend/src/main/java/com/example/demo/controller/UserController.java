package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ArticleLike;
import com.example.demo.entity.User;
import com.example.demo.entity.UserFollow;
import com.example.demo.mapper.ArticleLikeMapper;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.UserFollowMapper;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp");

    private static final List<String> PREDEFINED_AVATARS = Arrays.asList(
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar1&backgroundColor=b6e3f4",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar2&backgroundColor=c0aede",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar3&backgroundColor=d1d4f9",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar4&backgroundColor=ffd5dc",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar5&backgroundColor=ffdfbf",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar6&backgroundColor=ffd5dc",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar7&backgroundColor=b6e3f4",
            "https://api.dicebear.com/9.x/avataaars/svg?seed=avatar8&backgroundColor=c0aede"
    );

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        User existing = userService.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (existing != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userService.save(user);
        result.put("success", true);
        result.put("message", "注册成功");
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !encoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        result.put("user", userInfo);
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> info(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        User user = userService.getById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        result.put("success", true);
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        User user = userService.getById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        String nickname = body.get("nickname");
        String username = body.get("username");
        String password = body.get("password");
        String avatar = body.get("avatar");
        String bio = body.get("bio");

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (username != null && !username.equals(user.getUsername())) {
            User existing = userService.getOne(new QueryWrapper<User>().eq("username", username));
            if (existing != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            user.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(encoder.encode(password));
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        userService.updateById(user);
        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @PostMapping("/avatar/upload")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择文件");
            return result;
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    result.put("success", false);
                    result.put("message", "无法创建上传目录: " + dir.getAbsolutePath());
                    return result;
                }
            }
            String originalName = file.getOriginalFilename();
            String ext = ".png";
            if (originalName != null && originalName.contains(".")) {
                String rawExt = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(rawExt)) {
                    ext = rawExt;
                }
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + "_" + userId + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);

            String avatarUrl = "/uploads/avatars/" + filename;
            result.put("success", true);
            result.put("message", "上传成功");
            result.put("url", avatarUrl);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/avatars/presets")
    public Map<String, Object> presetAvatars() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("avatars", PREDEFINED_AVATARS);
        return result;
    }

    @GetMapping("/{id}/profile")
    public Map<String, Object> getUserProfile(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getById(id);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("createTime", user.getCreateTime());

        long articleCount = articleMapper.selectCount(
                new QueryWrapper<Article>().eq("user_id", id));
        long likeCount = articleLikeMapper.selectCount(
                new QueryWrapper<ArticleLike>().eq("user_id", id));
        long followerCount = userFollowMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("followed_id", id));
        long followingCount = userFollowMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("follower_id", id));

        profile.put("articleCount", articleCount);
        profile.put("likeCount", likeCount);
        profile.put("followerCount", followerCount);
        profile.put("followingCount", followingCount);

        Integer currentUserId = (Integer) request.getAttribute("userId");
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(id)) {
            isFollowing = userFollowMapper.selectCount(
                    new QueryWrapper<UserFollow>()
                            .eq("follower_id", currentUserId)
                            .eq("followed_id", id)) > 0;
        }
        profile.put("isFollowing", isFollowing);
        profile.put("isSelf", currentUserId != null && currentUserId.equals(id));

        result.put("success", true);
        result.put("profile", profile);
        return result;
    }
}
