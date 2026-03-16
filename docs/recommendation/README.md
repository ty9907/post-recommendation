# 帖子推荐SDK接口使用说明

## SDK概述

帖子推荐SDK（PostRecommendationSDK）是一个基于多维度加权组合的推荐系统，旨在为用户提供个性化的帖子推荐服务。该SDK采用单例模式设计，支持灵活的维度扩展和动态权重调整。

### 主要特性

- **多维度推荐**：支持标签匹配、浏览历史等多个推荐维度
- **动态权重调整**：运行时可动态调整各维度的权重配置
- **批量处理**：支持批量推荐请求，提高处理效率
- **可扩展架构**：支持自定义推荐维度的扩展
- **线程安全**：采用并发安全设计，支持多线程环境使用

### 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                  PostRecommendationSDK                   │
│                      (单例入口)                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐    ┌─────────────────────────┐    │
│  │   SDKConfig     │    │  RecommendationService  │    │
│  │   (配置管理)     │    │     (推荐服务接口)       │    │
│  └─────────────────┘    └─────────────────────────┘    │
│                                   │                      │
│                                   ▼                      │
│                        ┌─────────────────────┐          │
│                        │   DimensionManager   │          │
│                        │    (维度管理器)       │          │
│                        └─────────────────────┘          │
│                                   │                      │
│              ┌────────────────────┼────────────────────┐ │
│              ▼                    ▼                    ▼ │
│   ┌──────────────────┐ ┌──────────────────┐    ...     │
│   │ TagMatching      │ │ BrowseHistory    │            │
│   │ Dimension        │ │ Dimension        │            │
│   │ (标签匹配维度)    │ │ (浏览历史维度)    │            │
│   └──────────────────┘ └──────────────────┘            │
└─────────────────────────────────────────────────────────┘
```

## 快速开始

### 1. 初始化SDK

在使用SDK之前，必须先进行初始化：

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;

// 方式一：使用默认配置
SDKConfig config = SDKConfig.defaultConfig();
PostRecommendationSDK.getInstance().initialize(config);

// 方式二：使用Builder构建配置
SDKConfig config = SDKConfig.builder()
    .addDimensionWeight("TAG_MATCHING", 0.6)
    .addDimensionWeight("BROWSE_HISTORY", 0.4)
    .enableCache(true)
    .defaultLimit(20)
    .threadPoolSize(4)
    .build();

PostRecommendationSDK.getInstance().initialize(config);

// 方式三：使用SDKConfigBuilder（推荐）
SDKConfig config = PostRecommendationSDK.configBuilder()
    .addDimensionWeight("TAG_MATCHING", 0.6)
    .addDimensionWeight("BROWSE_HISTORY", 0.4)
    .defaultLimit(10)
    .build();

PostRecommendationSDK.getInstance().initialize(config);
```

### 2. 构建推荐请求

```java
import com.example.demo.recommendation.model.*;

// 创建用户标签
List<UserTag> userTags = Arrays.asList(
    new UserTag("Java", 0.8, "技术"),
    new UserTag("Spring", 0.6, "框架"),
    new UserTag("微服务", 0.5, "架构")
);

// 创建帖子标签
List<PostTag> postTags = Arrays.asList(
    new PostTag("Java", 0.9, 1001L),
    new PostTag("并发编程", 0.7, 1001L),
    new PostTag("Spring Boot", 0.8, 1002L),
    new PostTag("微服务", 0.6, 1002L)
);

// 创建推荐配置
RecommendationConfig config = new RecommendationConfig();
config.setLimit(10);

// 构建请求
RecommendationRequest request = new RecommendationRequest();
request.setUserTags(userTags);
request.setPostTags(postTags);
request.setConfig(config);
```

### 3. 获取推荐结果

```java
// 执行推荐
List<RecommendationResult> results = PostRecommendationSDK.getInstance().recommend(request);

// 处理结果
for (RecommendationResult result : results) {
    System.out.println("帖子ID: " + result.getPostId());
    System.out.println("总分: " + result.getTotalScore());
    System.out.println("各维度分数: " + result.getDimensionScores());
}
```

### 4. 关闭SDK

在应用关闭时，建议调用shutdown方法释放资源：

```java
PostRecommendationSDK.getInstance().shutdown();
```

## 核心概念

### 推荐维度

推荐维度是推荐系统的核心计算单元，每个维度负责从不同角度计算推荐分数。

#### 内置维度

| 维度名称 | 说明 | 计算逻辑 |
|---------|------|---------|
| TAG_MATCHING | 标签匹配维度 | 基于用户标签与帖子标签的匹配程度计算分数 |
| BROWSE_HISTORY | 浏览历史维度 | 基于用户浏览历史与候选帖子的相似度计算分数 |

#### 维度权重

维度权重决定了各维度在最终推荐分数中的占比。SDK会在初始化时自动归一化权重，使所有维度权重之和为1。

```java
// 权重归一化示例
// 设置 TAG_MATCHING=0.6, BROWSE_HISTORY=0.4
// 归一化后：TAG_MATCHING=0.6, BROWSE_HISTORY=0.4 (总和=1.0)
```

### 推荐流程

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  构建请求     │ ──▶ │  验证请求     │ ──▶ │  提取候选帖子 │
└──────────────┘     └──────────────┘     └──────────────┘
                                                 │
                                                 ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  返回结果     │ ◀── │  排序&限制    │ ◀── │  计算各维度分数│
└──────────────┘     └──────────────┘     └──────────────┘
```

### 数据模型关系

```
RecommendationRequest
├── userTags: List<UserTag>        // 用户标签偏好
├── postTags: List<PostTag>        // 候选帖子标签
├── browseHistory: List<BrowseHistory>  // 用户浏览历史
└── config: RecommendationConfig   // 推荐配置

RecommendationResult
├── postId: Long                   // 推荐帖子ID
├── totalScore: double             // 加权总分
└── dimensionScores: Map<String, Double>  // 各维度分数
```

## 使用示例

### 基本使用

```java
// 初始化
SDKConfig config = PostRecommendationSDK.configBuilder()
    .defaultLimit(10)
    .build();
PostRecommendationSDK.getInstance().initialize(config);

// 构建请求并获取推荐
RecommendationRequest request = new RecommendationRequest();
request.setUserTags(Arrays.asList(new UserTag("Java", 0.8, "技术")));
request.setPostTags(Arrays.asList(new PostTag("Java", 0.9, 1001L)));

List<RecommendationResult> results = PostRecommendationSDK.getInstance().recommend(request);
```

### 批量推荐

```java
Map<String, RecommendationRequest> requests = new HashMap<>();

// 添加多个请求
requests.put("user_001", request1);
requests.put("user_002", request2);

// 批量执行
Map<String, List<RecommendationResult>> results = 
    PostRecommendationSDK.getInstance().batchRecommend(requests);

// 获取特定用户的结果
List<RecommendationResult> user1Results = results.get("user_001");
```

### 动态调整权重

```java
// 运行时动态调整维度权重
Map<String, Double> newWeights = new HashMap<>();
newWeights.put("TAG_MATCHING", 0.7);
newWeights.put("BROWSE_HISTORY", 0.3);

PostRecommendationSDK.getInstance().updateDimensionWeights(newWeights);
```

### 结合浏览历史推荐

```java
// 创建浏览历史
List<BrowseHistory> browseHistory = Arrays.asList(
    new BrowseHistory(1001L, System.currentTimeMillis() - 3600000, 
        Arrays.asList(new PostTag("Java", 0.9, 1001L))),
    new BrowseHistory(1002L, System.currentTimeMillis() - 7200000,
        Arrays.asList(new PostTag("Spring", 0.8, 1002L)))
);

RecommendationRequest request = new RecommendationRequest();
request.setBrowseHistory(browseHistory);
request.setPostTags(candidatePostTags);

List<RecommendationResult> results = PostRecommendationSDK.getInstance().recommend(request);
```

## 最佳实践

### 1. 初始化时机

- 在应用启动时完成SDK初始化
- 避免在推荐请求处理过程中进行初始化
- 初始化只需执行一次，重复调用会被忽略

### 2. 资源管理

- 应用关闭时务必调用`shutdown()`方法
- shutdown后需要重新初始化才能使用

### 3. 权重配置

- 根据业务场景调整各维度权重
- 权重值建议在0.0-1.0范围内
- SDK会自动归一化权重

### 4. 性能优化

- 使用批量推荐接口处理大量请求
- 合理设置线程池大小
- 根据实际需求设置返回结果数量限制

## 相关文档

- [API参考文档](./api-reference.md)
- [数据模型文档](./data-models.md)
- [错误码定义](./error-codes.md)
- [使用示例代码](./examples.md)
