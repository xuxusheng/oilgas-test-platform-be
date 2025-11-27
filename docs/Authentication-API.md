# 油气测试平台 - 认证使用指南

本文档为前端开发者提供认证功能的使用说明。

## Token 管理

### 1. 登陆并获取 Token

```javascript
// POST /api/auth/login
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'your_password'
  })
});

const data = await response.json();
// {
//   "accessToken": "eyJhbGc...",
//   "tokenType": "Bearer",
//   "expiresIn": 2592000,
//   "userId": 1,
//   "username": "admin",
//   "role": "ADMIN"
// }

// 保存 Token
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('tokenExpires', data.expiresAt);
```

### 2. 使用 Token 访问接口

```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('/api/auth/me', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
```

### 3. Token 过期检查

Token 有效期为 **30 天**（720 小时 = 2,592,000 秒）。

```javascript
// 检查 Token 是否过期
const expiresAt = localStorage.getItem('tokenExpires');
if (new Date(expiresAt) <= new Date()) {
  // Token 已过期，需要重新登录
  window.location.href = '/login';
}
```

### 4. 登出

```javascript
const accessToken = localStorage.getItem('accessToken');

await fetch('/api/auth/logout', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${accessToken}` }
});

// 清除本地存储
localStorage.removeItem('accessToken');
localStorage.removeItem('tokenExpires');
```

---

## 错误处理

### 401 Unauthorized

**未登录或 Token 过期**

```javascript
if (response.status === 401) {
  // 跳转到登录页
  window.location.href = '/login';
}
```

### 400 Bad Request

**用户名或密码错误**

返回示例：

```json
{
  "success": false,
  "message": "用户名或密码错误",
  "timestamp": "2024-11-27T10:30:00.000Z",
  "path": "/api/auth/login",
  "error": "bad_request"
}
```

---

## 常见问题

### Q: Token 过期后会自动刷新吗？

A: 不会。Token 过期后需要用户重新登录。

### Q: Token 存在哪里？

A: 建议使用 `localStorage` 或 `cookies` 保存：

```javascript
// localStorage
localStorage.setItem('accessToken', token);

// cookies
document.cookie = `accessToken=${token}; path=/; max-age=2592000`;
```

### Q: 如何防止 Token 泄露？

A: **永远不要**在 URL 中传递 Token：

```javascript
// ❌ 错误
fetch(`/api/users?token=${token}`);

// ✅ 正确
fetch('/api/users', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

---

## 配置信息

### Sa-Token 配置

- **Token 格式**: JWT
- **Token 有效期**: 30 天
- **JWT 密钥**: 生产环境请修改 `yimusi-oilgas-platform-change-me-for-production`

### 用户角色

| 角色 | 说明 |
|------|------|
| `ADMIN` | 管理员 |
| `USER` | 普通用户 |

**注意**: 所有 `/api/**` 接口都需要登录后才能访问，`/api/auth/login` 接口无需登录。