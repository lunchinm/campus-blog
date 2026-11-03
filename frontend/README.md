# 校园博客平台前端

基于 Vue 3 + Vite 的博客前端应用，与 Spring Boot 后端配合运行。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5 | 前端框架（Composition API + `<script setup>`） |
| Vite | 8 | 构建工具 |
| Element Plus | 2.13 | UI 组件库（自动导入） |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.15 | HTTP 请求 |
| Vditor | 3.11 | Markdown 编辑器与渲染 |
| Highlight.js | 11.11 | 代码高亮 |
| Cropper.js | 1.6 | 头像裁剪 |

## 快速启动

```bash
npm install
npm run dev
```

前端运行在 http://localhost:8088，自动代理 `/api` 和 `/uploads` 到后端 8080。

## 构建

```bash
npm run build    # 输出到 dist/
npm run preview  # 预览生产构建
```

## 项目结构

```
src/
  api/           # Axios API 封装
  components/    # 公共组件（Markdown 编辑器/渲染、标签输入、点击特效、关注按钮）
  composables/   # 组合式函数（主题切换）
  router/        # 路由配置
  utils/         # 工具函数（认证、请求、格式化）
  views/         # 页面组件
  App.vue        # 根布局
  main.js        # 入口
  style.css      # 全局样式与按钮主题
```

## 主要功能

- 用户注册 / 登录 / JWT 鉴权
- 文章列表（搜索、标签筛选、关注 Tab）
- 文章详情（Markdown 渲染、评论树、点赞、收藏、关注作者）
- 文章编辑（Vditor Markdown 编辑器、自动保存草稿、标签输入）
- 草稿箱管理
- 我的内容（文章 / 评论 / 点赞 / 收藏，批量操作）
- 用户个人主页（统计、关注/粉丝、关注按钮）
- 消息通知中心（未读红点、标记已读）
- 用户设置（资料修改、密码修改、头像上传与裁剪）

## 代理配置

开发服务器代理（`vite.config.js`）：
- `/api` -> `http://localhost:8080`（去掉 `/api` 前缀）
- `/uploads` -> `http://localhost:8080`
