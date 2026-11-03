# 校园博客平台

基于 Vue 3 + Spring Boot 的前后端分离博客系统，支持文章管理、用户系统、社交互动、消息通知、Markdown 编辑等核心功能。

---

## 技术栈

### 前端（frontend/）

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5 | 前端框架（Composition API） |
| Vite | 8 | 构建工具 |
| Element Plus | 2.13 | UI 组件库 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.15 | HTTP 请求 |
| Vditor | 3.11 | Markdown 编辑器与渲染 |
| Highlight.js | 11.11 | 代码高亮 |
| Cropper.js | 1.6 | 头像裁剪 |

### 后端（backend/）

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.14 | 后端框架 |
| Java | 17 | 运行环境 |
| MySQL | 8.x | 数据库 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Druid | 1.2.16 | 数据库连接池 |
| JJWT | 0.12.6 | JWT 认证 |
| BCrypt | - | 密码加密 |

---

## 快速启动

### 1. 数据库

确保本地 MySQL 运行，创建数据库 `test`：

```sql
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4;
```

数据库账号：`root` / `iaml2c1h2`（见 `demo/src/main/resources/application.yml`）

### 2. 后端

```bash
cd demo
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 3. 前端

```bash
cd Vue3demo
npm install
npm run dev
```

前端运行在 http://localhost:8088，自动代理 `/api` 和 `/uploads` 到后端 8080。

---

## 功能一览

| 功能 | 说明 |
|------|------|
| 用户注册 / 登录 | JWT 鉴权，BCrypt 密码加密 |
| 文章列表 | 展示全部文章，支持标题搜索、标签筛选、关注用户文章 Tab |
| 文章详情 | Markdown 渲染，评论树（支持多级回复），点赞，收藏，关注作者 |
| 文章编辑 | Vditor Markdown 编辑器，支持图片上传插入，标签输入，自动保存草稿 |
| 草稿箱 | 自动保存与手动保存草稿，继续编辑，删除草稿 |
| 我的文章 | 当前用户的文章、评论、点赞、收藏管理，支持批量操作 |
| 收藏夹 | 收藏 / 取消收藏文章 |
| 点赞 | 对文章点赞，对评论点赞 |
| 关注系统 | 关注 / 取消关注用户，查看粉丝与关注列表 |
| 用户个人主页 | 展示用户基本信息、统计数据（文章数、获赞数、粉丝数、关注数） |
| 消息通知 | 点赞、收藏、评论、回复、关注触发通知，未读红点，标记已读 |
| 头像设置 | 预设头像 + 本地上传（Canvas 裁剪） |

---

## 项目结构

```
IdeaProjects/
+-- README.md                    # 本文件
+-- docs/                        # 开发文档与规划
|   +-- P2-API-Design.md         # P2 接口设计文档
|   +-- P2-database-schema.sql   # P2 数据库变更 SQL
|   +-- P2-中期规划-用户主页关注消息夜间模式.md  # P2 实施计划
|   +-- blog-enhancement-plan.md # 博客增强功能实施计划
|   +-- button-style-unification-plan.md # 按钮样式统一计划
|   +-- avatar-debug-log.md      # 头像功能调试记录
|   +-- code-optimization.md     # 代码优化修复记录
+-- demo/                        # 后端（Spring Boot）
|   +-- src/main/java/com/example/demo/
|       +-- config/              # 跨域、JWT 拦截、MyBatis 配置
|       +-- controller/          # API 控制器
|       +-- entity/              # 数据实体
|       +-- mapper/              # MyBatis-Plus Mapper
|       +-- service/             # 业务逻辑层
|       +-- utils/               # JWT 工具类
|   +-- src/main/resources/
|       +-- application.yml      # 应用配置
|   +-- uploads/avatars/         # 头像上传目录
+-- frontend/                    # 前端（Vue 3）
    +-- src/
        +-- api/                 # Axios API 封装
        +-- components/          # 公共组件
        +-- composables/         # 组合式函数
        +-- router/              # 路由配置
        +-- utils/               # 工具函数
        +-- views/               # 页面组件
```

---

## API 接口一览

### 用户

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /user/register | 用户注册 |
| POST | /user/login | 用户登录 |
| GET  | /user/info | 获取当前用户信息 |
| PUT  | /user/update | 更新用户信息（含头像、bio） |
| POST | /user/avatar/upload | 上传头像文件 |
| GET  | /user/avatars/presets | 获取预设头像列表 |
| GET  | /user/{id}/profile | 获取用户个人主页资料 |

### 文章

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /article/list | 文章列表 |
| GET  | /article/search | 标题搜索 |
| GET  | /article/my | 当前用户文章 |
| GET  | /article/following | 关注用户的文章 |
| GET  | /article/{id} | 文章详情 |
| POST | /article | 新建文章 |
| PUT  | /article | 更新文章 |
| DELETE | /article/{id} | 删除文章 |
| DELETE | /article/batch | 批量删除文章 |

### 评论

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /article/{id}/comments | 文章评论列表 |
| POST | /article/{id}/comments | 发表评论（支持回复） |
| DELETE | /article/comments/{id} | 删除评论 |
| DELETE | /article/comments/batch | 批量删除评论 |
| GET  | /article/comments/my | 我的评论 |

### 点赞

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /article/{id}/like | 文章点赞 / 取消点赞 |
| GET  | /article/{id}/like | 文章点赞状态 |
| GET  | /article/likes/my | 我的点赞 |
| POST | /comment/{id}/like | 评论点赞 / 取消点赞 |
| GET  | /comment/{id}/like | 评论点赞状态 |

### 收藏

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /article/{id}/favorite | 收藏 / 取消收藏 |
| GET  | /article/{id}/favorite | 收藏状态 |
| GET  | /article/favorites/my | 我的收藏 |
| DELETE | /article/favorites/batch | 批量取消收藏 |

### 关注

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /follow/{userId} | 关注用户 |
| DELETE | /follow/{userId} | 取消关注 |
| GET  | /follow/followers/{userId} | 粉丝列表 |
| GET  | /follow/following/{userId} | 关注列表 |

### 通知

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /notification/list | 通知列表 |
| GET  | /notification/unread-count | 未读通知数 |
| PUT  | /notification/read/{id} | 标记单条已读 |
| PUT  | /notification/read-all | 标记全部已读 |

### 草稿

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /drafts | 草稿列表 |
| GET  | /drafts/{id} | 草稿详情 |
| POST | /drafts | 保存草稿 |
| PUT  | /drafts/{id} | 更新草稿 |
| DELETE | /drafts/{id} | 删除草稿 |

### 标签

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /tags | 标签列表 |

### 上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /upload | 通用图片上传（供 Vditor 使用） |

---

## 常见问题

**Q：头像上传后不显示？**
A：检查 Vite 代理配置，确保 `/uploads` 已被代理到后端 8080。详见 `docs/avatar-debug-log.md`。

**Q：切换文章页面数据不刷新？**
A：文章详情页已添加 watch 监听路由参数变化。详见 `docs/code-optimization.md`。

**Q：如何执行数据库变更？**
A：见 `docs/P2-database-schema.sql` 和相关规划文档中的 SQL 语句。


---

## 目录结构

```
campus-blog/
  frontend/   Vue 3 + Vite 前端（12 个页面：登录注册/文章列表与详情/Markdown 编辑/草稿/收藏/消息/通知/个人主页/设置）
  backend/    Spring Boot 3 后端（16 个 Controller / 14 个实体：文章、评论、点赞、收藏、标签、关注、私信、通知、草稿）
  docs/       设计与调试记录
```

## 配置说明

后端连接信息放在 `backend/src/main/resources/application.yml`，**该文件不入库**（含数据库口令）。
首次运行请复制 `application.example.yml` 为 `application.yml` 并改成自己的 MySQL 账号口令，同时把 `file.upload-dir` 指向本机目录。
