# 点赞、评论、收藏按钮 UI 设计规范

## 设计规范说明

本文档提取自 ArticleDetail.vue，包含点赞、评论、收藏三个按钮的完整 UI 设计规范和交互效果，可直接被 AI 理解和复用。

---

## 1. 评论按钮

### 1.1 图标 (SVG)

```svg
<svg
  stroke-linejoin="round"
  stroke-linecap="round"
  stroke="currentColor"
  stroke-width="2"
  viewBox="0 0 24 24"
  height="44"
  width="44"
  xmlns="http://www.w3.org/2000/svg"
  class="comment-icon"
  fill="none"
>
  <path fill="none" d="M0 0h24v24H0z" stroke="none"></path>
  <path d="M8 9h8"></path>
  <path d="M8 13h6"></path>
  <path d="M18 4a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-5l-5 3v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12z"></path>
</svg>
```

### 1.2 默认样式 (CSS)

```css
.comment-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.comment-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}

.comment-icon {
  width: 32px;
  height: 32px;
  stroke: #666;
  transition: transform 0.2s ease-in-out, stroke 0.2s ease-in-out;
}
```

### 1.3 Hover 效果

```css
.comment-btn:hover .comment-icon {
  transform: scale(1.25);
  stroke: #3b82f6;
}
```

**效果说明**: 鼠标悬停时图标放大 1.25 倍，颜色从灰色 (#666) 变为蓝色 (#3b82f6)，过渡时间 0.2 秒。

### 1.4 模板结构

```html
<div class="comment-btn-wrapper group">
  <button class="btn comment-btn" type="button" @click="handleClick">
    <!-- SVG 图标 -->
  </button>
  <span class="tooltip">评论</span>
  <span class="count">{{ count }}</span>
</div>
```

---

## 2. 点赞按钮

### 2.1 图标 (SVG)

```svg
<svg
  class="icon"
  xmlns="http://www.w3.org/2000/svg"
  width="32"
  height="32"
  viewBox="0 0 17.503 15.625"
>
  <path
    id="Fill"
    d="M8.752,15.625h0L1.383,8.162a4.824,4.824,0,0,1,0-6.762,4.679,4.679,0,0,1,6.674,0l.694.7.694-.7a4.678,4.678,0,0,1,6.675,0,4.825,4.825,0,0,1,0,6.762L8.752,15.624ZM4.72,1.25A3.442,3.442,0,0,0,2.277,2.275a3.562,3.562,0,0,0,0,5l6.475,6.556,6.475-6.556a3.563,3.563,0,0,0,0-5A3.443,3.443,0,0,0,12.786,1.25h-.01a3.415,3.415,0,0,0-2.443,1.038L8.752,3.9,7.164,2.275A3.442,3.442,0,0,0,4.72,1.25Z"
  ></path>
</svg>
```

### 2.2 默认样式 (CSS)

```css
.like-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.like-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}

.icon {
  width: 32px;
  height: 32px;
  transition: transform 0.2s linear;
}

.icon path {
  fill: #ccc;
  transition: fill 0.2s linear;
}
```

### 2.3 Hover 效果

```css
.like-btn:hover > .icon {
  transform: scale(1.2);
}

.like-btn:hover > .icon path {
  fill: rgb(177, 139, 189);
}
```

**效果说明**: 鼠标悬停时图标放大 1.2 倍，填充色从浅灰色 (#ccc) 变为紫色 (rgb(177, 139, 189))，过渡时间 0.2 秒。

### 2.4 Active 状态

```css
.like-btn.active .icon path {
  fill: #e74c3c;
}
```

**效果说明**: 已点赞状态时填充色变为红色 (#e74c3c)。

### 2.5 模板结构

```html
<div class="like-btn-wrapper group">
  <button class="btn like-btn" :class="{ active: liked }" type="button" @click="handleClick">
    <!-- SVG 图标 -->
  </button>
  <span class="tooltip">点赞</span>
  <span class="count">{{ count }}</span>
</div>
```

---

## 3. 收藏按钮

### 3.1 图标 (SVG)

```svg
<svg
  class="favorite-icon"
  height="44"
  viewBox="0 0 24 24"
  width="44"
  xmlns="http://www.w3.org/2000/svg"
>
  <path d="M9.362,9.158c0,0-3.16,0.35-5.268,0.584c-0.19,0.023-0.358,0.15-0.421,0.343s0,0.394,0.14,0.521c1.566,1.429,3.919,3.569,3.919,3.569c-0.002,0-0.646,3.113-1.074,5.19c-0.036,0.188,0.032,0.387,0.196,0.506c0.163,0.119,0.373,0.121,0.538,0.028c1.844-1.048,4.606-2.624,4.606-2.624s2.763,1.576,4.604,2.625c0.168,0.092,0.378,0.09,0.541-0.029c0.164-0.119,0.232-0.318,0.195-0.505c-0.428-2.078-1.071-5.191-1.071-5.191s2.353-2.14,3.919-3.566c0.14-0.131,0.202-0.332,0.14-0.524s-0.23-0.319-0.42-0.341c-2.108-0.236-5.269-0.586-5.269-0.586s-1.31-2.898-2.183-4.83c-0.082-0.173-0.254-0.294-0.456-0.294s-0.375,0.122-0.453,0.294C10.671,6.26,9.362,9.158,9.362,9.158z"></path>
</svg>
```

### 3.2 默认样式 (CSS)

```css
.favorite-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.favorite-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}

.favorite-btn .favorite-icon {
  width: 44px;
  height: 44px;
  transition: transform 0.3s ease, fill 0.3s ease;
  fill: #666;
}
```

### 3.3 Hover 效果

```css
.favorite-btn:hover .favorite-icon {
  transform: scale(1.1);
}
```

**效果说明**: 鼠标悬停时图标放大 1.1 倍，过渡时间 0.3 秒。

### 3.4 Active 状态

```css
.favorite-btn.active .favorite-icon {
  fill: #f59e0b;
}
```

**效果说明**: 已收藏状态时填充色变为橙色 (#f59e0b)。

### 3.5 模板结构

```html
<div class="favorite-btn-wrapper group">
  <button class="btn favorite-btn" :class="{ active: favorited }" type="button" @click="handleClick">
    <!-- SVG 图标 -->
  </button>
  <span class="tooltip">收藏</span>
  <span class="count">{{ count }}</span>
</div>
```

---

## 4. 通用样式

### 4.1 Tooltip 提示框

```css
.tooltip {
  position: absolute;
  top: -56px;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  transform-origin: left;
  z-index: 20;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background-color: white;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease-in-out, opacity 0.3s ease-in-out, visibility 0.3s ease-in-out;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
}

.group:hover .tooltip {
  transform: translateX(-50%) scaleX(1);
  opacity: 1;
  visibility: visible;
}
```

**效果说明**: 鼠标悬停时显示白色背景的提示框，带灰色边框和阴影，通过缩放动画从左侧展开。

### 4.2 计数显示

```css
.count {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
```

### 4.3 按钮组容器

```css
.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
  align-items: flex-start;
}
```

---

## 5. 设计参数汇总

### 按钮尺寸

| 按钮类型 | 按钮尺寸 | 图标尺寸 | 圆角 |
|---------|---------|---------|------|
| 评论 | 50x50px | 32x32px | 10px |
| 点赞 | 50x50px | 32x32px | 10px |
| 收藏 | 50x50px | 44x44px | 10px |

### 颜色配置

| 状态 | 评论按钮 | 点赞按钮 | 收藏按钮 |
|------|---------|---------|---------|
| 默认 | stroke #666 | fill #ccc | fill #666 |
| Hover | stroke #3b82f6 (蓝) | fill rgb(177,139,189) (紫) | 无变化 |
| Active | - | fill #e74c3c (红) | fill #f59e0b (橙) |

### 动画配置

| 按钮类型 | Hover 缩放 | 过渡时间 |
|---------|-----------|---------|
| 评论 | 1.25x | 0.2s |
| 点赞 | 1.2x | 0.2s |
| 收藏 | 1.1x | 0.3s |

---

## 6. 复用指导

### 6.1 在 Vue 项目中复用

1. 将 SVG 图标代码复制到模板中
2. 将 CSS 样式复制到组件的 `<style>` 部分
3. 添加交互逻辑：
   - 评论：切换显示评论输入框
   - 点赞：调用 API 切换点赞状态，更新计数
   - 收藏：调用 API 切换收藏状态，更新计数

### 6.2 在其他框架中复用

1. 根据框架语法调整模板结构
2. 将 CSS 样式转换为对应框架的样式方案（如 styled-components、Tailwind CSS 等）
3. 实现对应的事件处理逻辑

### 6.3 注意事项

- 使用 CSS3 transition 实现平滑动画
- Tooltip 使用 group 类名配合 hover 选择器触发
- Active 状态通过动态添加 `.active` 类名控制
- 所有按钮均为透明背景、无边框设计
