# 按钮样式统一实施计划

> **目标：** 将本次新增的页面/组件中未使用统一按钮样式的 `el-button` 全部替换为项目既有的三种按钮样式（`donate-btn`、`neu-btn`、`donate-btn-red`）。

---

## 当前状态分析

### 项目既有按钮样式（定义于 `src/style.css`）

| 样式类 | 视觉特征 | 使用场景 |
|--------|---------|----------|
| `.donate-btn` | 蓝紫渐变、外发光、内阴影凸起 | 主操作：发布、保存、登录、注册、发送 |
| `.neu-btn` | 灰色拟态（neumorphism）、内凹阴影 | 次要/取消操作：取消、返回、退出管理 |
| `.donate-btn-red` | 红粉渐变、外发光、内阴影凸起 | 危险操作：删除 |

### 本次新增但**未使用统一样式**的按钮

经排查，以下文件中的 `el-button` 使用了 Element Plus 默认样式（`type="primary"`、`type="danger"` plain 等），需要替换：

#### 1. `src/views/Drafts.vue`（草稿箱页面）
- **第 63 行**：`<el-button type="primary" @click="router.push('/article/edit')">写文章</el-button>`
  - 应改为：`class="donate-btn"`
- **第 72 行**：`<el-button type="primary" @click="router.push('/article/edit')">去写文章</el-button>`
  - 应改为：`class="donate-btn"`
- **第 92 行**：`<el-button type="primary" size="small" @click="handleEdit(draft)">继续编辑</el-button>`
  - 应改为：`class="donate-btn" size="small"`
- **第 93 行**：`<el-button type="danger" size="small" plain @click="handleDelete(draft)">删除</el-button>`
  - 应改为：`class="donate-btn-red" size="small"`

#### 2. `src/components/TagInput.vue`（标签输入组件）
- **第 72 行**：`<button class="tag-remove" type="button" @click.stop="removeTag(index)">`
  - 这是一个纯 HTML `<button>`，不是 `el-button`
  - 当前样式已足够简洁，**建议保持不变**（chip 内的删除按钮不需要大按钮样式）

---

## 实施步骤

### Task 1: 修改 `Drafts.vue` 中的按钮样式

**文件**：`src/views/Drafts.vue`

**修改内容**：
1. 顶部"写文章"按钮：`type="primary"` → `class="donate-btn"`
2. 空状态"去写文章"按钮：`type="primary"` → `class="donate-btn"`
3. 卡片"继续编辑"按钮：`type="primary"` → `class="donate-btn"`
4. 卡片"删除"按钮：`type="danger" plain` → `class="donate-btn-red"`

**验证**：打开草稿箱页面，确认所有按钮视觉风格与其他页面一致。

---

## 不在本次处理范围的按钮（确认保留现状）

| 位置 | 按钮 | 原因 |
|------|------|------|
| `ArticleDetail.vue` 评论点赞 | `.comment-like-btn` | 自定义小图标按钮，非 `el-button`，已有独立样式 |
| `TagInput.vue` 标签删除 | `.tag-remove` | 纯 HTML `<button>`，chip 内部小按钮，不需要大按钮样式 |
| `ArticleDetail.vue` 回复/删除 | `el-button text` | 文本按钮，已有 `reply-btn` 等样式 |

---

## 计划总结

仅需修改 **1 个文件**（`Drafts.vue`）中的 **4 个按钮**，将 Element Plus 默认样式替换为项目统一的 `.donate-btn` 或 `.donate-btn-red` 类。
