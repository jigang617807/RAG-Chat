# RAG-Chat
1. 项目功能梳理
整个项目主要包含四个核心模块：
# 用户认证模块 (AuthController, UserService)
注册：支持用户名、手机号注册，包含简单的密码复杂度校验（英文+数字+特殊字符）。
登录：基于 Session 的用户名/密码登录。
找回密码：模拟发送短信验证码（控制台打印），验证后重置密码。
权限控制：通过 Session 判断用户登录状态。
# 用户个人中心 (UserController)
资料修改：修改性别、年龄、电话。
头像上传：支持图片上传，保存到本地文件系统并更新数据库路径。
# 文档知识库管理 (DocumentController, DocumentService)
文档上传：接收 PDF 文件，使用 PDFBox 提取文本。
文本切分：基于双换行符（\n\n）进行简单的文本分块（Chunking）。
向量化：调用智谱 AI 的 Embedding 接口将文本块转为向量并存入数据库。
文档删除：事务性删除，级联删除文档记录、向量块、关联的对话记录以及本地文件。
# AI 对话模块 (ChatController, RagService)
对话管理：基于文档 ID 创建或复用对话。
RAG 检索：将用户问题向量化，在内存中计算余弦相似度，检索 Top 5 相关切片。
流式问答：利用 Server-Sent Events (SSE) 调用智谱 GLM 模型进行流式回复，并将问答记录存入 MySQL。
上述AI对话调用的是API不是本地部署的。在Java设置中添加环境变量：ZHIPU_API_KEY：你的key
application.yaml里面设置你的数据库
