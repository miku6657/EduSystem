# CAS 单点登录搭建方案

## 背景

EduAdmSys 目前是纯 **JWT + Spring Security** 无状态认证：`/api/auth/login` 校验 `sys_user` 表后签发 JWT，`JwtAuthenticationFilter` 校验 `Authorization: Bearer`。

本次目标：自建一个 **CAS 单点登录中心（Apereo CAS Server）**，并让 EduAdmSys 作为 **CAS Client** 接入，新增一条「跳转 CAS 登录 → 拿 ticket → 换用户身份 → 签发 JWT」的登录链路。

已定决策：
- CAS Server：官方 **Apereo CAS 7.3.x**（内置 Tomcat 的 executable war，无需单独 Tomcat）。
- 通信协议：**HTTP**（`http://localhost:8081/cas`），避开自签名证书。
- CAS 账号：**独立 MySQL 表 `cas_user`**，JDBC 认证，BCrypt 密码。
- 认证后：**保留 JWT**。CAS 只负责「登录那一下」，成功后 client 仍签自己的 JWT。

## 架构

```
浏览器 ──(1) 访问受保护接口───────────────────────────→ EduAdmSys (8080, CAS Client)
   ↑                                                      │
   │ (4) 302 跳回 service?ticket=ST-xxx                   │ (2) 未登录时 302 → CAS 登录页
   │                                                      ↓
   └──(3) 用户登录成功，CAS 生成 ticket(ST) ──── Apereo CAS Server (8081)
                          │
                          │ (5) client 拿 ticket 调 /serviceValidate，解析 XML 得 username
                          ↓
                 (6) client 查本地 sys_user → 签 JWT → 返回给前端
```

两套账号（独立）：CAS 用 `cas_user` 校验登录；EduAdmSys 用 `sys_user` 签发 JWT。**两表 username 需一致**（测试账号同名，如 `admin`）。

---

## 一、EduAdmSys Client 侧（已完成）

| 文件 | 说明 |
|------|------|
| `config/CasProperties.java` | 读 `cas.server-url-prefix`、`cas.client-service-url` |
| `service/CasService.java` | 调 `/serviceValidate` 校验 ticket，解析 XML 拿 username |
| `controller/CasAuthController.java` | `/api/auth/cas/login` 重定向登录；`/api/auth/cas/callback` 回调签发 JWT |
| `config/SecurityConfig.java` | 放行 `/api/auth/cas/login`、`/api/auth/cas/callback` |
| `resources/application.yml` | 新增 `cas:` 配置段 |

`application.yml` 新增：
```yaml
cas:
  server-url-prefix: http://localhost:8081/cas
  client-service-url: http://localhost:8080/api/auth/cas/callback
```

---

## 二、CAS Server 搭建

### 1. 生成工程
用官方生成器 https://start.apereo.org ，选 **7.3.x**，勾选依赖：`Core`(Tomcat webapp) + `JSON Service Registry` + `JDBC`，下载解压到仓库根的 `cas-server/` 目录（与 `edumanage/` 平级）。

> 网络不通时 clone 模板 `github.com/apereo/cas-overlay-template`，再改版本号。

### 2. `gradle.properties`
```
cas.version=7.3.8.2
```

### 3. `build.gradle` 的 dependencies 确认有
```groovy
implementation "org.apereo.cas:cas-server-webapp-tomcat:${project.'cas.version'}"
implementation "org.apereo.cas:cas-server-support-jdbc:${project.'cas.version'}"
implementation "org.apereo.cas:cas-server-support-json-service-registry:${project.'cas.version'}"
implementation "com.mysql:mysql-connector-j:8.0.33"
```

### 4. `etc/cas/config/cas.properties`（核心）
```properties
server.port=8081
server.ssl.enabled=false
server.servlet.context-path=/cas
cas.server.prefix=http://localhost:8081/cas
cas.tgc.secure=false

# 关掉内置默认账号 casuser
cas.authn.accept.enabled=false

# JDBC 认证，连现有 MySQL 的 cas_user 表
cas.authn.jdbc.query[0].url=jdbc:mysql://127.0.0.1:3306/eduSYSTEM?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
cas.authn.jdbc.query[0].user=root
cas.authn.jdbc.query[0].password=123456
cas.authn.jdbc.query[0].driver-class=com.mysql.cj.jdbc.Driver
cas.authn.jdbc.query[0].sql=SELECT password FROM cas_user WHERE username=?
cas.authn.jdbc.query[0].field-password=password
cas.authn.jdbc.query[0].password-encoder.type=BCRYPT
```

### 5. 登记 client：`etc/cas/services/edumanage-10000001.json`
```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^http://localhost:8080/api/auth/cas/callback",
  "name": "EduAdmSys",
  "id": 10000001
}
```
> 漏了这一步会报「service 未授权」。

### 6. 建表 + 插账号（BCrypt 密码）
```sql
CREATE TABLE cas_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL
);
```
密码存 BCrypt hash，用项目已有的 `BCryptPasswordEncoder` 生成（随便在测试类里跑）：
```java
System.out.println(new BCryptPasswordEncoder().encode("123456"));
```
把输出填进 INSERT；`sys_user` 里要有同名账号（如 `admin`），因为 callback 后 client 要用这个 username 查 `sys_user` 签 JWT。

> 想先跑通：临时把 `password-encoder.type` 改成 `NONE`、密码明文存，通了再换回 `BCRYPT`。

### 7. 启动
```bash
cd cas-server
./gradlew run          # 或 ./gradlew clean build 后 java -jar build/libs/cas.war
```
起好后访问 `http://localhost:8081/cas/login` 能出登录页。

---

## 三、联调验证

1. 起 CAS（8081）+ EduAdmSys（8080）。
2. 浏览器开 `http://localhost:8080/api/auth/cas/login` → 跳 CAS 登录页 → 用 `admin/123456` 登录 → 跳回 callback，拿到含 `token` 的 JSON。
3. 拿 token 调一个受保护接口，验证 JWT 链路正常；未登录直接访问仍 401。

---

## 四、常见坑

- **首次 `./gradlew` 拉依赖很大**：国内给 `build.gradle` 的 `repositories` 配阿里云 Maven 镜像。
- **HTTP 警告**：CAS 登录页提示 non-secure connection 属正常，不影响登录。
- **`serviceId` 不匹配**：确认 JSON 里正则与 `application.yml` 的 `cas.client-service-url` 完全一致。
- **属性名小版本差异**：CAS 不同小版本配置名偶有变化，以启动报错为准微调。
