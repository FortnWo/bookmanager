# 智慧图书管理系统（**AI-Enhanced Smart Book Management System**）

基于 Spring Boot + MyBatis-Plus + Spring Security 的图书管理系统，并集成百度文心大模型（ERNIE）实现 AI 智能助手与 RAG（检索增强生成）问答功能。系统分为管理员端和读者端，支持图书管理、读者管理、借阅记录、公告发布、智能问答等完整业务流程。

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [数据库设计](#数据库设计)
- [AI 智能助手说明](#ai-智能助手说明)
- [权限与角色](#权限与角色)
- [访问页面](#访问页面)
- [常见问题](#常见问题)

## 项目简介

本项目是一个面向高校图书馆场景的综合管理系统，作为课程的实践成果。系统以 Spring Boot 为核心框架，结合 MyBatis-Plus 进行数据持久化操作，使用 Spring Security 实现基于角色的访问控制（RBAC），并通过 Thymeleaf 模板引擎渲染页面。

系统的亮点在于集成了百度千帆平台的文心大模型，实现了具备上下文记忆、人格切换、RAG 知识库检索的 AI 智能助手，可为读者提供图书推荐、借阅咨询等服务。

## 技术栈

| 类别     | 技术 / 工具                                        |
| -------- | -------------------------------------------------- |
| 后端框架 | Spring Boot 2.4.1                                  |
| 安全框架 | Spring Security                                    |
| ORM 框架 | MyBatis-Plus 3.5.5                                 |
| 数据库   | MySQL                                              |
| 模板引擎 | Thymeleaf（集成 thymeleaf-extras-springsecurity5） |
| 前端 UI  | Bootstrap                                          |
| AI 能力  | 百度千帆 ERNIE（ernie-speed-pro-128k）             |
| 构建工具 | Maven                                              |
| 开发语言 | Java 11                                            |
| 辅助工具 | Lombok、spring-boot-devtools                       |

## 功能特性

### 管理员端

- **图书管理**：图书的增、删、改、查，支持按书名 / 作者 / 出版社模糊搜索
- **读者管理**：读者信息的增、删、改、查
- **用户管理**：系统用户（账号）的添加，自动关联读者信息录入
- **借阅记录管理**：查看全部借阅记录，支持按读者卡号 / 图书号 / 书名检索，并办理还书
- **公告管理**：编辑并发布图书馆公告（首页展示最新公告）
- **文档管理**：录入馆内知识文档，作为 AI 助手 RAG 检索的知识库
- **AI 设置**：配置 AI 助手的模型参数（temperature、top_p、max_tokens 等）

### 读者端

- **图书浏览与搜索**：查看全部馆藏图书，按关键词检索
- **图书详情**：查看图书详细信息
- **借阅图书**：在线借阅图书（自动生成借阅记录）
- **借阅记录**：查看个人的借阅历史
- **个人信息**：查看与修改个人资料
- **修改密码**：修改登录密码

### AI 智能助手

- **多轮对话**：基于会话（Session）保持上下文记忆，历史消息持久化至数据库
- **RAG 检索增强**：根据用户提问检索馆内文档，将检索结果作为参考资料注入提示词
- **人格切换（Persona）**：内置「图书馆助手」「轻松闲聊机器人」等多种人格风格
- **参数调节**：支持运行时调节模型、temperature、top_p、max_tokens 等参数
- **会话管理**：支持清除会话历史、限制单会话最大消息数与 token 数

## 项目结构

```
bookmanager/
├── pom.xml                          # Maven 依赖与构建配置
├── library.sql                      # 数据库初始化脚本
├── src/main/
│   ├── java/com/fortn/bookmanager/
│   │   ├── BookmanagerApplication.java      # 启动类（@EnableCaching、@MapperScan）
│   │   ├── config/                          # 配置类
│   │   │   ├── SecurityConfig.java          #   Spring Security 安全配置
│   │   │   ├── SecurityUserDetailsService.java #   用户认证服务
│   │   │   ├── CustomPasswordEncoder.java   #   自定义密码编码器
│   │   │   ├── WebConfig.java               #   Web MVC 配置（拦截器注册）
│   │   │   ├── UserSessionInterceptor.java  #   会话拦截器（注入用户信息）
│   │   │   ├── HttpClientConfig.java        #   RestTemplate 配置
│   │   │   └── GlobalExceptionHandler.java  #   全局异常处理
│   │   ├── controller/                      # 控制器层
│   │   │   ├── AuthController.java          #   登录/首页
│   │   │   ├── UserController.java          #   修改密码
│   │   │   ├── AiController.java            #   AI 对话 REST 接口
│   │   │   ├── admin/                       #   管理员控制器
│   │   │   │   ├── AdminBookController.java
│   │   │   │   ├── AdminReaderController.java
│   │   │   │   ├── AdminRecordController.java
│   │   │   │   ├── AdminUserController.java
│   │   │   │   ├── AdminDocumentController.java
│   │   │   │   ├── AdminAiController.java
│   │   │   │   └── AnnouncementController.java
│   │   │   └── user/                        #   读者控制器
│   │   │       ├── UserBookController.java
│   │   │       ├── UserRecordController.java
│   │   │       └── UserReaderController.java
│   │   ├── mapper/                          # MyBatis-Plus Mapper 接口
│   │   ├── model/                           # AI 相关模型
│   │   │   ├── ChatMessage.java            #   对话消息
│   │   │   ├── Document.java                #   知识文档
│   │   │   ├── Persona.java                 #   人格定义
│   │   │   └── SessionSettings.java         #   会话设置
│   │   ├── pojo/                            # 实体类
│   │   │   ├── Book.java
│   │   │   ├── Reader.java
│   │   │   ├── Record.java
│   │   │   ├── User.java
│   │   │   └── Announcement.java
│   │   └── service/                         # 业务层
│   │       ├── BookService.java
│   │       ├── ReaderService.java
│   │       ├── RecordService.java
│   │       ├── UserService.java
│   │       ├── AnnouncementService.java
│   │       ├── BaiduAiService.java          #   百度文心大模型调用
│   │       ├── ChatSessionService.java       #   对话会话管理
│   │       ├── PersonaService.java           #   人格管理
│   │       ├── RetrieverService.java         #   RAG 检索服务
│   │       └── SessionSettingsService.java    #   会话参数管理
│   └── resources/
│       ├── application.yml                  # 主配置文件
│       ├── application.properties           # MyBatis-Plus 配置
│       ├── static/                          # 静态资源（Bootstrap、jQuery）
│       └── templates/                       # Thymeleaf 模板
│           ├── index.html                   #   首页
│           ├── login.html                   #   登录页
│           ├── password.html                #   修改密码页
│           ├── announcement_edit.html       #   公告编辑页
│           ├── admin/                       #   管理员页面
│           └── user/                        #   读者页面
```

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- 百度千帆平台 API Key（用于 AI 助手功能）

### 安装与运行

1. **克隆项目**

   ```bash
   git clone <仓库地址>
   cd bookmanager
   ```
2. **初始化数据库**

   在 MySQL 中执行根目录下的 `library.sql` 脚本：

   ```bash
   mysql -u root -p < library.sql
   ```

   该脚本会自动创建 `library` 数据库并初始化表结构与示例数据（含图书分类、示例图书、公告等）。
3. **修改配置**

   编辑 `src/main/resources/application.yml`，将数据库账号密码改为本地配置：

   ```yaml
   spring:
     datasource:
       username: root
       password: 你的数据库密码
       url: jdbc:mysql://localhost:3306/library?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=GMT%2B8
   ```

   如需启用 AI 助手功能，填入百度千帆平台的 API Key：

   ```yaml
   baidu:
     ai:
       apiKey: "你的百度千帆 API Key"
       # 根据需求填入合适的完整模型名称，此处以ernie-speed-pro-128k为例
       model: "ernie-speed-pro-128k"
   ```
4. **编译与运行**

   ```bash
   # 编译
   mvn clean package -DskipTests

   # 运行
   mvn spring-boot:run
   ```

   或直接在 IDE 中运行 `BookmanagerApplication` 主类。
5. **访问系统**

   启动成功后，浏览器访问：[http://localhost:8080](http://localhost:8080)

## 配置说明

### application.yml 核心配置

```yaml
server:
  port: 8080                          # 服务端口

spring:
  datasource:                         # 数据库连接
    username: root                    # 数据库用户名，测试外不建议使用root
    password: 你的密码
    url: jdbc:mysql://localhost:3306/library?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=GMT%2B8
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:                         # MyBatis-Plus 配置
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.fortn.bookmanager.pojo
  configuration:
    map-underscore-to-camel-case: true

baidu:                                # 百度文心大模型配置
  ai:
    apiKey: "你的百度千帆 API Key"
    model: "ernie-speed-pro-128k"
    temperature: 0.8
    top_p: 0.9
    max-tokens: 2048
  ernie:
  									  # 千帆大模型官网获取最新调用地址
    url: "https://qianfan.baidubce.com/v2/chat/completions"

chat:                                 # 对话会话限制
  session:
    max-messages: 50
    max-tokens: 4000
```

### application.properties

```properties
mybatis-plus.mapper-locations=classpath*:mapper/*.xml
mybatis-plus.type-aliases-package=com..bookmanager.pojo
```

## 数据库设计

系统使用 MySQL 数据库 `library`，主要表结构如下：

| 表名             | 说明       | 主要字段                                                                                              |
| ---------------- | ---------- | ----------------------------------------------------------------------------------------------------- |
| `class_info`   | 图书分类表 | class_id（分类号）、class_name（分类名）                                                              |
| `book_info`    | 图书信息表 | book_id（图书号）、name、author、publish、ISBN、introduction、price、pubdate、class_id、state（状态） |
| `reader_info`  | 读者信息表 | reader_id（卡号）、name、sex、birth、address、telcode                                                 |
| `user`         | 系统用户表 | username（账号）、password、role（权限：ADMIN/READER）                                                |
| `lend_list`    | 借阅记录表 | sernum（流水号）、book_id、reader_id、lend_date、back_date、book_name                                 |
| `announcement` | 公告表     | id、content、created_at                                                                               |

> 示例数据：图书分类（马克思主义、哲学、文学、历史地理等 22 类）以及若干示例图书（含《大雪中的山庄》《人类简史》《明朝那些事儿》等）。

## AI 智能助手说明

系统通过 `AiController` 提供 `/api/ai/chat` REST 接口，集成百度千帆文心大模型，核心机制如下：

1. **会话管理**：每个浏览器会话分配唯一 `sessionId`，对话消息持久化到 `chat_message` 表，支持多轮上下文。
2. **RAG 检索增强**：`RetrieverService` 根据用户提问从 `document` 表检索 Top-3 相关文档，拼接为参考资料注入提示词，使回答更贴合馆藏实际。
3. **人格系统（Persona）**：`PersonaService` 内置多种人格（如图书馆助手、轻松闲聊机器人），可切换不同风格的对话。
4. **参数调节**：管理员可通过 `/admin/ai/settings` 页面配置模型参数；读者可在请求体中覆盖 `model`、`temperature`、`top_p`、`max_tokens`。
5. **容错处理**：`BaiduAiService` 对 HTTP 状态码、响应内容类型进行校验，AI 服务异常时会抛出友好错误信息。

## 权限与角色

系统采用 Spring Security 实现基于角色的访问控制：

| 角色   | 角色标识        | 可访问路径                                       | 说明                                            |
| ------ | --------------- | ------------------------------------------------ | ----------------------------------------------- |
| 管理员 | `ROLE_ADMIN`  | `/admin/**`、`/api/admin/**`                 | 管理图书、读者、记录、用户、公告、文档、AI 设置 |
| 读者   | `ROLE_READER` | `/user/**`                                     | 浏览图书、借阅、查看个人记录与信息              |
| 未登录 | —              | `/toLoginPage`、`/img/**`、`/bootstrap/**` | 仅可访问登录页与静态资源                        |

- 登录入口：`/toLoginPage`，登录处理 URL：`/login`，成功后跳转 `/index`
- 登出 URL：`/logout`，登出后返回登录页
- `UserSessionInterceptor` 拦截所有请求，将登录用户名、姓名注入 Session 供页面使用
- 密码使用 `CustomPasswordEncoder`（明文比对），生产环境建议替换为 `BCryptPasswordEncoder`

## 访问页面

### 公共页面

| 路径              | 模板              | 说明                 |
| ----------------- | ----------------- | -------------------- |
| `/`、`/index` | `index.html`    | 首页（展示最新公告） |
| `/toLoginPage`  | `login.html`    | 登录页               |
| `/toChPwdPage`  | `password.html` | 修改密码页           |

### 管理员页面（`/admin/**`）

| 路径                            | 模板                        | 说明         |
| ------------------------------- | --------------------------- | ------------ |
| `/admin/book/getAll`          | `admin/books.html`        | 图书列表     |
| `/admin/book/info/{id}`       | `admin/book_info.html`    | 图书详情     |
| `/admin/book/toAddPage`       | `admin/book_add.html`     | 添加图书     |
| `/admin/book/toEditPage/{id}` | `admin/book_edit.html`    | 编辑图书     |
| `/admin/reader/getAll`        | `admin/readers.html`      | 读者列表     |
| `/admin/record/getAll`        | `admin/records.html`      | 借阅记录列表 |
| `/admin/user/toAddPage`       | `admin/user_add.html`     | 添加用户     |
| `/admin/document/toAddPage`   | `admin/document_add.html` | 添加知识文档 |
| `/admin/ai/settings`          | `admin/ai_settings.html`  | AI 设置      |
| `/admin/announcement/edit`    | `announcement_edit.html`  | 公告编辑     |

### 读者页面（`/user/**`）

| 路径                          | 模板                             | 说明         |
| ----------------------------- | -------------------------------- | ------------ |
| `/user/book/getAll`         | `user/books.html`              | 图书列表     |
| `/user/book/info/{id}`      | `user/book_info.html`          | 图书详情     |
| `/user/book/search`         | `user/book_search_result.html` | 图书搜索结果 |
| `/user/record/getOwnRecord` | `user/ownRecord.html`          | 个人借阅记录 |
| `/user/toInfoPage`          | `user/reader_info.html`        | 个人信息     |

## 常见问题

### 1. 启动报错：数据库连接失败

请检查 `application.yml` 中的数据库 `username`、`password`、`url` 是否正确，并确保 MySQL 服务已启动、`library` 数据库已通过 `library.sql` 脚本初始化。

### 2. AI 助手无响应或报错

- 确认 `application.yml` 中的 `baidu.ai.apiKey` 已正确配置百度千帆平台 API Key
- 检查网络是否能访问 `https://qianfan.baidubce.com`
- 查看后端日志中 `[BaiduAiService]` 相关错误信息

### 3. 登录失败提示「用户名或密码错误」

- 确认数据库 `user` 表中存在对应账号
- 默认密码为明文存储，如需修改密码可登录后通过 `/toChPwdPage` 修改

### 4. 页面样式丢失

确保静态资源路径 `/bootstrap/**`、`/img/**` 未被 Spring Security 拦截（配置中已放行）。

---

> 本项目为教学实训项目，部分安全配置（如明文密码）仅供学习参考，生产环境部署需加强安全措施。本仓库只提供必要的代码及相关配置。
