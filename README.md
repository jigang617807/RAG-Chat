# 📄 智能文档问答系统 (RAG Document Q&A)

基于 Spring Boot  和 智谱AI (ZhipuAI/GLM) 构建的 RAG（检索增强生成）项目。该系统允许用户上传 PDF 文档，通过向量检索技术与文档内容进行自然语言对话。

## ✨ 主要功能

* **用户系统**
    * 注册、登录、注销（BCrypt 密码加密）。
    * 个人资料管理（修改信息、上传头像）。
    * 密码重置（模拟短信验证码流程）。
* **文档管理**
    * PDF 文件上传与解析（使用 Apache PDFBox）。
    * 智能文本切分（Text Splitter，支持重叠分块）。
    * 文档列表查看与删除（级联删除关联的向量数据和对话记录）。
* **RAG 核心 (Retrieval-Augmented Generation)**
    * **Embedding**: 使用智谱 `embedding-3` 模型生成文本向量。
    * **向量存储**: 这里的 Demo 使用 MySQL `JSON` 字段存储向量（注：生产环境建议使用专用向量数据库）。
    * **向量检索**: 基于余弦相似度（Cosine Similarity）的 Java 内存检索算法。
* **AI 对话**
    * 基于 `glm-4.5-flash` 或 `glm-4-plus` 模型。
    * **流式响应 (SSE)**: 实现打字机效果的实时回答。
    * 多轮对话上下文记忆。
    * 聊天记录持久化存储。

## 🛠 技术栈

* **后端**: Java 17+, Spring Boot 3.2.0
* **数据库**: MySQL 8.0 (使用 Spring Data JPA)
* **AI SDK**: Zhipu AI SDK (`zai-sdk`), DashScope SDK
* **工具库**: Apache PDFBox (PDF解析), Lombok
* **前端**: Thymeleaf 模板引擎, HTML5, CSS3 (原生 JS, 无需 Vue/React)

## 🚀 快速开始

### 1. 环境准备
* JDK 17 或更高版本
* Maven 3.6+
* MySQL 数据库
* **智谱 AI API Key** (需要自行去 [智谱开放平台](https://open.bigmodel.cn/) 申请)

### 2. 数据库配置
在 MySQL 中创建一个空的数据库，名称为 `simple_demo`。
```sql
CREATE DATABASE simple_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
