🤖 RAG-Chat: 基于智谱 AI 的智能文档问答系统
RAG-Chat 是一个轻量级的检索增强生成（Retrieval-Augmented Generation）对话系统。它允许用户上传 PDF 文档构建个人知识库，并利用智谱 AI 的大模型能力进行基于文档上下文的流式问答。

✨ 核心功能 (Key Features)
1. 🔐 用户认证与安全 (Authentication)
注册机制：支持用户名/手机号注册，强制密码复杂度校验（英文+数字+特殊字符）。

登录会话：基于 Session 的状态管理。

密码找回：模拟短信验证码流程（控制台输出），验证身份后重置密码。

权限拦截：全站基于 Session 的登录状态校验。

2. 👤 个人中心 (User Center)
资料管理：支持修改年龄、性别、电话等基础信息。

头像管理：支持本地文件系统存储头像，并同步更新数据库。

3. 📚 RAG 知识库管理 (Knowledge Base)
文档解析：集成 PDFBox 解析上传的 PDF 文件。

智能切分 (Smart Chunking)：

采用 带重叠的滑动窗口算法 (Fixed Size + Overlap)。

策略：默认块大小 1000 字符，重叠 200 字符，有效避免上下文语义断裂。

向量化存储：调用智谱 AI Embedding 接口生成向量，并持久化存储。

级联删除：支持事务性删除，一键清理文档记录、向量数据、关联对话及本地文件。

4. 💬 AI 智能对话 (AI Chat)
上下文检索：基于内存计算 余弦相似度，实时检索 Top 5 最相关的文档切片。

流式响应 (Streaming)：基于 Server-Sent Events (SSE) 技术，实时流式输出智谱 GLM 模型的回复，降低用户等待焦虑。

历史持久化：完整的问答历史记录存入 MySQL，支持基于文档 ID 复用对话上下文。

🛠 技术栈 (Tech Stack)
后端框架: Java, Spring Boot

数据库: MySQL

AI 模型服务: 智谱 AI (GLM-4 / Embedding-2)

文档处理: Apache PDFBox

通信协议: HTTP, Server-Sent Events (SSE)

🚀 快速开始 (Getting Started)
1. 环境准备
JDK 17+

MySQL 8.0+

Maven 3.6+

2. 数据库配置
在 src/main/resources/application.yaml 中配置您的本地数据库连接：

YAML

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rag_chat?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update # 开发环境自动建表
3. AI 模型配置 (环境变量)
本项目依赖智谱 AI 的 API 服务，不要将 Key 硬编码在代码中。请在 IDEA 或系统的环境变量中添加：

变量名: ZHIPU_API_KEY

变量值: 您的智谱AI_API_KEY (例如: 123456.abcdefg)

4. 启动项目
运行主启动类，项目默认端口为 8080。
