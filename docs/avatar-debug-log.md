# 头像上传功能调试记录

> 博客系统头像模块开发中的问题排查与修复经过。

---

## 问题现象

上传本地图片 → 裁剪确认 → 提示"上传成功" → 头像不变化。

## 排查过程

### 第 1 轮：数据库未写入

- `confirmCrop()` 中调用了 `uploadAvatar()` 上传文件，但遗漏了 `updateUser()` 写入数据库的步骤
- 前端 localStorage 更新了、导航栏也即时显示了，但重新登录后数据库旧值覆盖了 localStorage
- **修复**：补上 `await updateUser({ avatar: finalUrl })`

### 第 2 轮：路径空格导致静态资源映射失效

- 路径 `C:\Users\MIC lunchinm\...` 含有空格，Spring 的 `addResourceHandlers` 无法正确处理
- **尝试**：从 WebConfig 移除 ResourceHandler，改用 FileController 端点独立处理
- **结果**：仍无效，因为请求根本没到达后端 8080

### 第 3 轮（关键突破）：Content-Type 为 text/html

- Network 面板显示头像请求端口是 **8088**（Vite 开发服务器），不是 **8080**（Spring Boot 后端）
- Content-Type 是 `text/html` 而非 `image/png`——Vite 找不到该文件，返回了 SPA 的 index.html
- **根因**：`vite.config.js` 的 proxy 只配置了 `/api`，没有配置 `/uploads`
- **修复**：在 proxy 中增加 `/uploads` 规则

```js
// vite.config.js
proxy: {
    '/api': { target: 'http://localhost:8080', changeOrigin: true, rewrite: (p) => p.replace(/^\/api/, '') },
    '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
}
```

---

## 经验总结

### 分层排查法

| 层 | 验证方法 | 本次发现 |
|---|---------|---------|
| 数据库 | SQL SELECT 查看字段值 | 值未写入 |
| 文件系统 | 查看文件是否存在 | 文件存在有效 |
| 后端直连 | 浏览器直接访问后端端口 | 能显示图片 |
| 前端→后端链路 | F12 Network 看 Status / ContentType | **关键线索**：text/html |
| 前端状态 | Console 查看 localStorage | 值正确 |

### Vue + Spring Boot 分离架构常见坑

1. **Vite 代理只配了 `/api`**——非 API 的后端资源（文件上传、静态资源）也需要额外配置代理
2. **Windows 路径空格**——Spring 的 ResourceHandler 用 `file:` 协议解析带空格路径会失败，改用 Controller 端点更可靠
3. **浏览器缓存**——同一 URL 的图片更新后浏览器可能用缓存，加 `?v=时间戳` 解决
4. **el-avatar 缓存**——即使 src 变化，组件可能不重渲染，需要绑定 `:key`
