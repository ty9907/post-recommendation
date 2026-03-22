# 内容处理与推荐能力库

## 项目简介

本项目是一个基于 Java 21 的内容处理能力库，围绕“标签提取、帖子推荐、重复内容检测”三条主线提供可复用的 SDK 与基础组件。

当前仓库已经不再只是单一的标签提取示例，而是一个可直接嵌入业务系统的内容处理中台原型，包含：

- 标签提取模块：从文章内容中提取关键词标签，支持 `Simple`、`IKAnalyzer`、`HanLP`
- 帖子推荐 SDK：基于用户标签、帖子标签、浏览历史进行多维度加权推荐
- 重复内容检测模块：支持实时/批量查重、缓存、白名单、人工审核、分层过滤和性能监控

## 核心能力

### 1. 标签提取

- 支持多种分词与标签提取实现
- 支持停用词过滤、词性过滤、频率过滤
- 提供标签质量评估工具

主要入口：

- `com.example.demo.tag.service.TagService`

### 2. 帖子推荐 SDK

- 支持标签匹配维度
- 支持浏览历史维度
- 支持维度权重动态调整
- 支持批量推荐、分页和性能测试

主要入口：

- `com.example.demo.recommendation.PostRecommendationSDK`

推荐文档：

- `docs/recommendation/README.md`
- `docs/recommendation/api-reference.md`
- `docs/recommendation/data-models.md`
- `docs/recommendation/error-codes.md`
- `docs/recommendation/examples.md`

### 3. 重复内容检测

- 支持 `TFIDF`、`COSINE`、`EDIT_DISTANCE`、`SIMHASH`、`HYBRID`、`WORD2VEC`
- 支持实时检测器 `RealTimeDetector`
- 支持批量检测器 `BatchDetector`
- 支持缓存、白名单、人工审核
- 支持富文本预处理、SimHash 倒排索引、标签倒排索引、候选集缓存
- 支持风险分级、异步精检、性能指标采集、索引同步

主要入口：

- `com.example.demo.duplicate.service.DuplicateCheckService`

相关文档：

- `docs/duplicate-article-check-design.md`

## 新增的性能优化能力

重复检测模块已经补齐了之前规划但未落地的性能优化链路：

- 富文本预处理接口与实现
- SimHash 分段倒排索引
- 标签倒排索引
- 候选集管理器与候选缓存
- 风险等级评估
- 异步精检任务队列
- 性能监控与性能报告
- 索引同步服务

对应新增包：

- `com.example.demo.duplicate.preprocess`
- `com.example.demo.duplicate.index`
- `com.example.demo.duplicate.candidate`
- `com.example.demo.duplicate.async`
- `com.example.demo.duplicate.monitor`
- `com.example.demo.duplicate.sync`
- `com.example.demo.duplicate.risk`

## 项目结构

```text
post-recommendation/
├── src/
│   ├── main/java/com/example/demo/
│   │   ├── tag/              # 标签提取模块
│   │   ├── recommendation/   # 推荐 SDK 模块
│   │   └── duplicate/        # 重复内容检测模块
│   └── test/                 # 单元测试、集成测试、性能测试
├── docs/                     # 设计和使用文档
├── pom.xml
└── README.md
```

## 构建与测试

环境要求：

- JDK 21+
- Maven 3.8.1+

常用命令：

```bash
mvn compile
mvn test
mvn package
```

本次整理后，完整测试套件已通过：

- 363 个测试全部通过

## 接入说明

### 作为 SDK/组件接入

当前仓库的定位是“可嵌入业务系统的能力库”，而不是完整的 Web 应用。  
如果你要接入真实业务系统，推荐通过以下方式使用：

- 标签提取：直接调用 `TagService`
- 推荐：初始化 `PostRecommendationSDK`
- 查重：通过 `DuplicateCheckService` 或直接使用检测器
- 文章数据接入：实现 `com.example.demo.duplicate.service.ArticleRepository`

### 已实现但仍需业务方决定的部分

- 是否在发布流程中直接阻断高风险内容
- 是否将中风险内容接入人工审核系统
- 索引持久化路径与生命周期策略
- Web API / Spring Boot 外层封装

## 重要说明

- 顶层 README 已与当前代码结构同步，不再把项目描述为“仅标签提取系统”
- 推荐 SDK 与重复检测模块都已具备独立使用能力
- 重复检测的性能优化主线已完成实现，并补充了索引、候选集、异步、风险分级和监控相关测试

## 许可证

本项目采用 MIT 许可证。
