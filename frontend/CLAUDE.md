# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

Vue 3 blog/article management frontend built with **Vue 3 + Vite 8 + Element Plus + Vue Router**. A single-page application (SPA) that communicates with a Spring Boot backend running on `localhost:8080`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Vue 3.5 (Composition API + `<script setup>`) |
| Build | Vite 8 |
| UI Library | Element Plus 2.13 (auto-imported via `unplugin-vue-components`) |
| Router | Vue Router 4.6 (Web History mode) |
| HTTP | Axios 1.15 |
| Styling | Scoped CSS + CSS custom properties (`src/style.css`) |
| Markdown Editor | Vditor 3.11 (with image upload support) |
| Image Crop | cropperjs 1.6 (manual Canvas-based crop in UserSettings) |

## Project Structure

```
src/
  main.js              -- App entry point
  App.vue              -- Root layout: animated bubble background, nav bar, <router-view>
  style.css            -- Global CSS variables, reset, and Element Plus button overrides
  router/index.js      -- Route definitions + auth guard
  utils/
    auth.js            -- Token/user CRUD in localStorage (key: blog_token / blog_user)
    request.js         -- Axios instance: baseURL=/api, timeout=10s, auto-inject Bearer token
    format.js          -- formatDate(isoString) helper
  api/
    user.js            -- register, login, getUserInfo, updateUser, uploadAvatar, getPresetAvatars, getUserProfile
    article.js         -- CRUD + getMyArticles, batchDeleteArticles, searchArticles, getFollowingArticles
    comment.js         -- CRUD + getMyComments, batchDeleteComments, toggleCommentLike, getCommentLikeStatus
    like.js            -- getLikeStatus, toggleLike, getMyLikes
    favorite.js        -- getFavoriteStatus, toggleFavorite, getMyFavorites, batchRemoveFavorites
    follow.js          -- followUser, unfollowUser, getFollowers, getFollowing
    notification.js    -- getNotifications, getUnreadCount, markAsRead, markAllAsRead
    draft.js           -- getDrafts, getDraft, saveDraft, updateDraft, deleteDraft
    tag.js             -- getTags
    goods.js           -- Unused legacy module
  views/
    ArticleList.vue    -- Article cards with tag filter, search, login gate for "写文章"
    ArticleDetail.vue  -- Full article view + comments (flat tree), likes, favorites, author follow
    ArticleEdit.vue    -- Create/edit article form with Vditor Markdown editor, auto-save draft
    MyArticles.vue     -- Tabbed view: articles / comments / likes / favorites with batch manage
    Drafts.vue         -- Draft list with continue edit / delete
    Login.vue          -- Username/password form
    Register.vue       -- Registration form (username, nickname, password)
    UserSettings.vue   -- Profile edit, password change, avatar picker (presets + crop upload)
    UserProfile.vue    -- Public user profile page with stats and follow button
    Favorites.vue      -- Favorites list
    Notifications.vue  -- Notification center with unread dot and mark-as-read
  components/
    HelloWorld.vue     -- Original Vite scaffold component (unused by router)
    MarkdownRender.vue -- Vditor-based Markdown preview with code highlight
    VditorEditor.vue   -- Vditor Markdown editor wrapper with image upload
    TagInput.vue       -- Tag chip input with predefined tag suggestions
    ClickSpark.vue     -- Canvas click spark animation effect
    StarFollowButton.vue -- Magnetic follow button with star burst animation
  composables/
    useTheme.js        -- Dark/light theme toggle with localStorage persistence
```

## Commands

```bash
npm run dev      # Start dev server on http://localhost:8088 (auto-opens browser)
npm run build    # Production build -> dist/
npm run preview  # Preview production build
```

## Key Architecture Patterns

### Auto-Imports (no `import` needed in components)
- `unplugin-auto-import`: Vue APIs (`ref`, `computed`, `watch`, `onMounted` etc.) and Element Plus message/message-box functions (`ElMessage`, `ElMessageBox`) are globally available — no need to import them in `.vue` files.
- `unplugin-vue-components`: Element Plus components (`ElButton`, `ElInput`, `ElDialog`, `ElUpload`, `ElAvatar`, `ElEmpty`, `ElTabs`, `ElTabPane`, `ElCheckbox`, `ElSelect`, `ElOption`, `ElForm`, `ElFormItem`, `ElBadge`, `ElSkeleton`) auto-resolved — no registration needed.

### API Proxy
Vite dev server proxies `/api` -> `http://localhost:8080` (strips `/api` prefix) and `/uploads` -> `http://localhost:8080`.

### Auth Flow
1. Login/Register -> backend returns `{ success, token, user }`
2. `setToken(token)` + `setUser(user)` persist to localStorage
3. Axios request interceptor auto-attaches `Authorization: Bearer <token>`
4. Router `beforeEach` guard redirects to `/login` for protected routes
5. `App.vue` syncs login state on every route change via `router.afterEach`

### Avatar Change Propagation
`UserSettings.vue` dispatches `window.dispatchEvent(new CustomEvent('avatar-changed'))` after avatar change. `App.vue` listens for this event to refresh the nav bar avatar (via `avatarVersion` counter as cache-bust key).

### Global Element Plus Button Classes
Three reusable button classes defined in `src/style.css`:
- `.neu-btn` — neumorphic style button
- `.donate-btn` — gradient blue CTA button
- `.donate-btn-red` — gradient red danger button

Usage: `<el-button class="donate-btn" ...>`

### API Response Convention
Backend returns `{ success: boolean, message?: string, ...data }`. Components check `res.success` and show `ElMessage.error(res.message)` on failure.

### Theme / Dark Mode
- `useTheme.js` composable manages dark/light mode via `document.documentElement.classList.toggle('dark')`
- Element Plus dark CSS vars imported in `main.js`
- `localStorage` key: `blog_theme`

### Draft Auto-Save
`ArticleEdit.vue` auto-saves drafts every 30s via `setInterval`. Draft ID is tracked in `draftId` ref. Manual "Save Draft" button also available.

## Notes

- `HelloWorld.vue` is the Vite scaffold template, not used by the current router.
- `src/api/goods.js` exists but appears unused.
- The project has no TypeScript — all files are plain JavaScript.
- No `.env` files or environment variable configuration is present.
- `style.css` contains the original Vite scaffold styles (`#app` width: 1126px, dark mode media queries) that are partially overridden by App.vue's scoped `#app` styles.
- Image upload in Vditor uses `/api/upload` endpoint (handled by backend `FileController`).
