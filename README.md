# **📄 智能文档问答系统 (RAG Document Q\&A)**

基于 Spring Boot 3 和 智谱AI (ZhipuAI/GLM) 构建的 RAG（检索增强生成）演示项目。该系统允许用户上传 PDF 文档，通过向量检索技术与文档内容进行自然语言对话。

## **✨ 主要功能**

* **用户系统**  
  * 注册、登录、注销（BCrypt 密码加密）。  
  * 个人资料管理（修改信息、上传头像）。  
  * 密码重置（模拟短信验证码流程）。  
* **文档管理**  
  * PDF 文件上传与解析（使用 Apache PDFBox）。  
  * 智能文本切分（Text Splitter，支持重叠分块）。  
  * 文档列表查看与删除（级联删除关联的向量数据和对话记录）。  
* **RAG 核心 (Retrieval-Augmented Generation)**  
  * **Embedding**: 使用智谱 embedding-3 模型生成文本向量。  
  * **向量存储**: 这里的 Demo 使用 MySQL JSON 字段存储向量（注：生产环境建议使用专用向量数据库）。  
  * **向量检索**: 基于余弦相似度（Cosine Similarity）的 Java 内存检索算法。  
* **AI 对话**  
  * 基于 glm-4.5-flash 或 glm-4-plus 模型。  
  * **流式响应 (SSE)**: 实现打字机效果的实时回答。  
  * 多轮对话上下文记忆。  
  * 聊天记录持久化存储。

## **🛠 技术栈**

* **后端**: Java 17+, Spring Boot 3.2.0  
* **数据库**: MySQL 8.0 (使用 Spring Data JPA)  
* **AI SDK**: Zhipu AI SDK (zai-sdk), DashScope SDK  
* **工具库**: Apache PDFBox (PDF解析), Lombok  
* **前端**: Thymeleaf 模板引擎, HTML5, CSS3 (原生 JS, 无需 Vue/React)

## **🚀 快速开始**

### **1\. 环境准备**

* JDK 17 或更高版本  
* Maven 3.6+  
* MySQL 数据库  
* **智谱 AI API Key** (需要自行去 [智谱开放平台](https://open.bigmodel.cn/) 申请)

### **2\. 数据库配置**

在 MySQL 中创建一个空的数据库，名称为 simple\_demo。

```sql
CREATE DATABASE simple_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

*项目启动时，JPA 的 ddl-auto: update 配置会自动创建所需的数据表 (user, document, document\_chunk, conversation, chat\_message)。*

### **3\. 项目配置**

修改 src/main/resources/application.yml 文件：

**配置数据库连接：**

YAML

spring:  
  datasource:  
    url: jdbc:mysql://localhost:3306/simple\_demo?useSSL=false\&serverTimezone=UTC  
    username:       \# 你的数据库用户名  
    password:     \# 你的数据库密码

配置 API Key：  
设置环境变量：
```YAML
\# 方式一：直接修改 yml (不推荐提交到 Git)  
ZHIPU\_API\_KEY: "你的key.xxxxxxxx"

\# 方式二：设置系统环境变量 (推荐)  
\# Windows: set ZHIPU\_API\_KEY=你的key  
\# Linux/Mac: export ZHIPU\_API\_KEY=你的key

### **4\. 启动项目**

在项目根目录下运行：

Bash

mvn spring-boot:run

或者在 IDEA 中直接运行主启动类。

### **5\. 访问系统**

打开浏览器访问：

* 入口地址: http://localhost:8080/auth/login  
* 测试账号: 先点击“立即注册”创建一个新账号。

## **📂 项目结构说明**

Plaintext

src/main/java/com/example/demo  
├── config/             \# WebMvc 配置 (资源映射, 拦截器等)  
├── controller/         \# 控制层 (Auth, Chat, Document, User)  
├── entity/             \# 数据库实体 (JPA Entity)  
├── repository/         \# 数据访问层 (DAO)  
├── service/            \# 业务逻辑层 (RAG 核心逻辑, 用户服务)  
│   ├── RagService.java \# 核心：Embedding 生成与向量检索  
│   └── ...  
└── utils/  
    └── TextSplitter.java \# 文本切分工具类

## **⚙️ 关键配置参数**

在 application.yml 中可以调整以下参数：

* zhipu.embedding-model: 嵌入模型，默认 embedding-3。  
* zhipu.chat-model: 聊天模型，默认 glm-4.5-flash (免费且速度快)。  
* spring.servlet.multipart.max-file-size: 单个文件上传大小限制，默认 50MB。  
* upload.dir: 文件存储根目录，默认为当前项目目录 .。

## **⚠️ 注意事项**

1. **向量检索性能**: 本项目为了演示简便，将 Embedding 向量以 JSON 格式存在 MySQL 中，并在内存中进行余弦相似度计算。**这仅适用于小型演示项目**。如果文档量巨大，请接入 Milvus、PgVector 或 Elasticsearch。  
2. **API 费用**: 使用 Embedding 和 Chat 模型会消耗 Token，请关注智谱 AI 控制台的额度使用情况。  
3. **文件路径**: 上传的文件默认保存在项目根目录下的 uploads/ 文件夹中。

## **📸 效果预览**

1. **上传文档**: 支持拖拽上传 PDF，后台自动进行文本清洗和切片。  
2. **对话界面**: 仿 Apple 风格的极简 UI，支持流式输出，体验流畅。  
3. **文档列表**: 查看已上传文档，支持一键删除（同时清理所有相关数据）。

---

**Enjoy Coding\!** 🎉
