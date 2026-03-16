# 数据模型文档

本文档详细介绍了帖子推荐SDK中使用的所有数据模型。

## 目录

- [RecommendationRequest - 推荐请求模型](#recommendationrequest---推荐请求模型)
- [RecommendationResult - 推荐结果模型](#recommendationresult---推荐结果模型)
- [BrowseHistory - 浏览历史模型](#browsehistory---浏览历史模型)
- [UserTag - 用户标签模型](#usertag---用户标签模型)
- [PostTag - 帖子标签模型](#posttag---帖子标签模型)
- [RecommendationConfig - 推荐配置模型](#recommendationconfig---推荐配置模型)
- [DimensionContext - 维度上下文模型](#dimensioncontext---维度上下文模型)
- [DimensionResult - 维度结果模型](#dimensionresult---维度结果模型)

---

## RecommendationRequest - 推荐请求模型

推荐请求数据模型类，用于封装推荐请求的所有参数。

**包路径**: `com.example.demo.recommendation.model.RecommendationRequest`

### 类定义

```java
public class RecommendationRequest {
    private List<UserTag> userTags;                 // 用户标签列表
    private List<PostTag> postTags;                 // 帖子标签列表
    private List<BrowseHistory> browseHistory;      // 浏览历史列表
    private RecommendationConfig config;            // 配置参数
}
```

### 字段说明

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userTags | List\<UserTag\> | 否* | 用户标签列表，表示用户的兴趣偏好 |
| postTags | List\<PostTag\> | 否* | 帖子标签列表，包含候选帖子的标签信息 |
| browseHistory | List\<BrowseHistory\> | 否* | 用户浏览历史列表 |
| config | RecommendationConfig | 否 | 推荐配置参数 |

> *注：userTags、postTags、browseHistory 三者至少需要提供一个

### 构造器

#### 默认构造器

```java
public RecommendationRequest()
```

#### 带参数构造器

```java
public RecommendationRequest(List<UserTag> userTags, 
                             List<PostTag> postTags,
                             List<BrowseHistory> browseHistory, 
                             RecommendationConfig config)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getUserTags() | List\<UserTag\> | 获取用户标签列表 |
| setUserTags(List\<UserTag\>) | void | 设置用户标签列表 |
| getPostTags() | List\<PostTag\> | 获取帖子标签列表 |
| setPostTags(List\<PostTag\>) | void | 设置帖子标签列表 |
| getBrowseHistory() | List\<BrowseHistory\> | 获取浏览历史列表 |
| setBrowseHistory(List\<BrowseHistory\>) | void | 设置浏览历史列表 |
| getConfig() | RecommendationConfig | 获取配置参数 |
| setConfig(RecommendationConfig) | void | 设置配置参数 |

### 使用示例

```java
RecommendationRequest request = new RecommendationRequest();
request.setUserTags(Arrays.asList(
    new UserTag("Java", 0.8, "技术"),
    new UserTag("Spring", 0.6, "框架")
));
request.setPostTags(Arrays.asList(
    new PostTag("Java", 0.9, 1001L),
    new PostTag("Spring Boot", 0.8, 1002L)
));

RecommendationConfig config = new RecommendationConfig();
config.setLimit(10);
request.setConfig(config);
```

---

## RecommendationResult - 推荐结果模型

推荐结果数据模型类，用于存储推荐计算的结果。

**包路径**: `com.example.demo.recommendation.model.RecommendationResult`

### 类定义

```java
public class RecommendationResult {
    private Long postId;                        // 帖子ID
    private double totalScore;                  // 总分
    private Map<String, Double> dimensionScores; // 各维度分数详情
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| postId | Long | 推荐的帖子ID |
| totalScore | double | 加权总分，所有维度分数的加权和 |
| dimensionScores | Map\<String, Double\> | 各维度分数详情，键为维度名称，值为该维度的分数 |

### 构造器

#### 默认构造器

```java
public RecommendationResult()
```

#### 带参数构造器

```java
public RecommendationResult(Long postId, double totalScore, Map<String, Double> dimensionScores)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getPostId() | Long | 获取帖子ID |
| setPostId(Long) | void | 设置帖子ID |
| getTotalScore() | double | 获取总分 |
| setTotalScore(double) | void | 设置总分 |
| getDimensionScores() | Map\<String, Double\> | 获取各维度分数详情 |
| setDimensionScores(Map\<String, Double\>) | void | 设置各维度分数详情 |

### 使用示例

```java
List<RecommendationResult> results = sdk.recommend(request);

for (RecommendationResult result : results) {
    System.out.println("帖子ID: " + result.getPostId());
    System.out.println("总分: " + result.getTotalScore());
    
    Map<String, Double> scores = result.getDimensionScores();
    System.out.println("标签匹配分数: " + scores.get("TAG_MATCHING"));
    System.out.println("浏览历史分数: " + scores.get("BROWSE_HISTORY"));
}
```

---

## BrowseHistory - 浏览历史模型

浏览历史数据模型类，用于存储用户的帖子浏览历史记录。

**包路径**: `com.example.demo.recommendation.model.BrowseHistory`

### 类定义

```java
public class BrowseHistory {
    private Long postId;                // 帖子ID
    private Long browseTime;            // 浏览时间（时间戳）
    private List<PostTag> tags;         // 帖子标签列表
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| postId | Long | 用户浏览过的帖子ID |
| browseTime | Long | 浏览时间，Unix时间戳（毫秒） |
| tags | List\<PostTag\> | 该帖子包含的标签列表 |

### 构造器

#### 默认构造器

```java
public BrowseHistory()
```

#### 带参数构造器

```java
public BrowseHistory(Long postId, Long browseTime, List<PostTag> tags)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getPostId() | Long | 获取帖子ID |
| setPostId(Long) | void | 设置帖子ID |
| getBrowseTime() | Long | 获取浏览时间 |
| setBrowseTime(Long) | void | 设置浏览时间 |
| getTags() | List\<PostTag\> | 获取帖子标签列表 |
| setTags(List\<PostTag\>) | void | 设置帖子标签列表 |

### 使用示例

```java
List<BrowseHistory> history = Arrays.asList(
    new BrowseHistory(
        1001L, 
        System.currentTimeMillis() - 3600000,  // 1小时前
        Arrays.asList(new PostTag("Java", 0.9, 1001L))
    ),
    new BrowseHistory(
        1002L, 
        System.currentTimeMillis() - 86400000,  // 1天前
        Arrays.asList(new PostTag("Spring", 0.8, 1002L))
    )
);

request.setBrowseHistory(history);
```

---

## UserTag - 用户标签模型

用户标签数据模型类，用于存储用户的标签偏好信息。

**包路径**: `com.example.demo.recommendation.model.UserTag`

### 类定义

```java
public class UserTag {
    private String name;        // 标签名称
    private double weight;      // 标签权重，表示用户对该标签的偏好程度
    private String type;        // 标签类型
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| name | String | 标签名称，如"Java"、"Spring"等 |
| weight | double | 标签权重（0.0-1.0），表示用户对该标签的偏好程度 |
| type | String | 标签类型，如"技术"、"框架"、"架构"等 |

### 构造器

#### 默认构造器

```java
public UserTag()
```

#### 带参数构造器

```java
public UserTag(String name, double weight, String type)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getName() | String | 获取标签名称 |
| setName(String) | void | 设置标签名称 |
| getWeight() | double | 获取标签权重 |
| setWeight(double) | void | 设置标签权重 |
| getType() | String | 获取标签类型 |
| setType(String) | void | 设置标签类型 |

### 使用示例

```java
List<UserTag> userTags = Arrays.asList(
    new UserTag("Java", 0.9, "技术"),
    new UserTag("Spring", 0.7, "框架"),
    new UserTag("微服务", 0.5, "架构"),
    new UserTag("MySQL", 0.6, "数据库")
);

request.setUserTags(userTags);
```

---

## PostTag - 帖子标签模型

帖子标签数据模型类，用于存储帖子的标签信息。

**包路径**: `com.example.demo.recommendation.model.PostTag`

### 类定义

```java
public class PostTag {
    private String name;        // 标签名称
    private double weight;      // 标签权重，表示标签在帖子中的重要程度
    private Long postId;        // 帖子ID
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| name | String | 标签名称 |
| weight | double | 标签权重（0.0-1.0），表示该标签在帖子中的重要程度 |
| postId | Long | 所属帖子ID |

### 构造器

#### 默认构造器

```java
public PostTag()
```

#### 带参数构造器

```java
public PostTag(String name, double weight, Long postId)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getName() | String | 获取标签名称 |
| setName(String) | void | 设置标签名称 |
| getWeight() | double | 获取标签权重 |
| setWeight(double) | void | 设置标签权重 |
| getPostId() | Long | 获取帖子ID |
| setPostId(Long) | void | 设置帖子ID |

### 使用示例

```java
List<PostTag> postTags = Arrays.asList(
    new PostTag("Java", 0.9, 1001L),        // 帖子1001的Java标签
    new PostTag("并发编程", 0.7, 1001L),     // 帖子1001的并发编程标签
    new PostTag("Spring Boot", 0.8, 1002L), // 帖子1002的Spring Boot标签
    new PostTag("微服务", 0.6, 1002L)        // 帖子1002的微服务标签
);

request.setPostTags(postTags);
```

---

## RecommendationConfig - 推荐配置模型

推荐配置数据模型类，用于配置推荐系统的参数。

**包路径**: `com.example.demo.recommendation.model.RecommendationConfig`

### 类定义

```java
public class RecommendationConfig {
    private Map<String, Double> dimensionWeights;   // 维度权重配置
    private boolean enableCache;                    // 是否启用缓存
    private int limit;                              // 返回结果数量限制
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| dimensionWeights | Map\<String, Double\> | 维度权重配置，键为维度名称，值为权重值 |
| enableCache | boolean | 是否启用缓存 |
| limit | int | 返回结果数量限制，0表示不限制 |

### 构造器

#### 默认构造器

```java
public RecommendationConfig()
```

#### 带参数构造器

```java
public RecommendationConfig(Map<String, Double> dimensionWeights, 
                            boolean enableCache, 
                            int limit)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getDimensionWeights() | Map\<String, Double\> | 获取维度权重配置 |
| setDimensionWeights(Map\<String, Double\>) | void | 设置维度权重配置 |
| isEnableCache() | boolean | 是否启用缓存 |
| setEnableCache(boolean) | void | 设置是否启用缓存 |
| getLimit() | int | 获取返回结果数量限制 |
| setLimit(int) | void | 设置返回结果数量限制 |

### 使用示例

```java
RecommendationConfig config = new RecommendationConfig();

Map<String, Double> weights = new HashMap<>();
weights.put("TAG_MATCHING", 0.6);
weights.put("BROWSE_HISTORY", 0.4);
config.setDimensionWeights(weights);

config.setEnableCache(true);
config.setLimit(20);

request.setConfig(config);
```

---

## DimensionContext - 维度上下文模型

维度上下文数据模型类，用于存储维度计算所需的上下文信息。

**包路径**: `com.example.demo.recommendation.dimension.DimensionContext`

### 类定义

```java
public class DimensionContext {
    private List<UserTag> userTags;             // 用户标签列表
    private List<PostTag> postTags;             // 帖子标签列表
    private List<BrowseHistory> browseHistory;  // 浏览历史列表
    private Long candidatePostId;               // 候选帖子ID
    private List<PostTag> candidatePostTags;    // 候选帖子标签
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userTags | List\<UserTag\> | 用户标签列表 |
| postTags | List\<PostTag\> | 所有帖子标签列表 |
| browseHistory | List\<BrowseHistory\> | 用户浏览历史列表 |
| candidatePostId | Long | 当前计算的候选帖子ID |
| candidatePostTags | List\<PostTag\> | 候选帖子的标签列表 |

### 构造器

#### 默认构造器

```java
public DimensionContext()
```

#### 带参数构造器

```java
public DimensionContext(List<UserTag> userTags, 
                        List<PostTag> postTags,
                        List<BrowseHistory> browseHistory, 
                        Long candidatePostId,
                        List<PostTag> candidatePostTags)
```

### Builder模式

```java
DimensionContext context = DimensionContext.builder()
    .userTags(userTags)
    .postTags(postTags)
    .browseHistory(browseHistory)
    .candidatePostId(postId)
    .candidatePostTags(candidateTags)
    .build();
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getUserTags() | List\<UserTag\> | 获取用户标签列表 |
| setUserTags(List\<UserTag\>) | void | 设置用户标签列表 |
| getPostTags() | List\<PostTag\> | 获取帖子标签列表 |
| setPostTags(List\<PostTag\>) | void | 设置帖子标签列表 |
| getBrowseHistory() | List\<BrowseHistory\> | 获取浏览历史列表 |
| setBrowseHistory(List\<BrowseHistory\>) | void | 设置浏览历史列表 |
| getCandidatePostId() | Long | 获取候选帖子ID |
| setCandidatePostId(Long) | void | 设置候选帖子ID |
| getCandidatePostTags() | List\<PostTag\> | 获取候选帖子标签 |
| setCandidatePostTags(List\<PostTag\>) | void | 设置候选帖子标签 |
| builder() | Builder | 创建Builder实例（静态方法） |

---

## DimensionResult - 维度结果模型

维度结果数据模型类，用于存储维度计算的结果。

**包路径**: `com.example.demo.recommendation.dimension.DimensionResult`

### 类定义

```java
public class DimensionResult {
    private String dimensionName;       // 维度名称
    private double score;               // 分数
    private Map<String, Object> details; // 详情
}
```

### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| dimensionName | String | 维度名称 |
| score | double | 该维度计算得出的分数 |
| details | Map\<String, Object\> | 计算详情，可存储中间结果或调试信息 |

### 构造器

#### 默认构造器

```java
public DimensionResult()
```

#### 带参数构造器

```java
public DimensionResult(String dimensionName, double score)

public DimensionResult(String dimensionName, double score, Map<String, Object> details)
```

### 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getDimensionName() | String | 获取维度名称 |
| setDimensionName(String) | void | 设置维度名称 |
| getScore() | double | 获取分数 |
| setScore(double) | void | 设置分数 |
| getDetails() | Map\<String, Object\> | 获取详情 |
| setDetails(Map\<String, Object\>) | void | 设置详情 |
| addDetail(String, Object) | void | 添加详情项 |

### 使用示例

```java
DimensionResult result = new DimensionResult("TAG_MATCHING", 0.85);
result.addDetail("matchedTags", Arrays.asList("Java", "Spring"));
result.addDetail("matchCount", 2);
```

---

## 数据模型关系图

```
┌─────────────────────────────────────────────────────────────┐
│                    RecommendationRequest                     │
├─────────────────────────────────────────────────────────────┤
│  userTags: List<UserTag>                                     │
│  postTags: List<PostTag>                                     │
│  browseHistory: List<BrowseHistory>                          │
│  config: RecommendationConfig                                │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│   UserTag    │    │   PostTag    │    │  BrowseHistory   │
├──────────────┤    ├──────────────┤    ├──────────────────┤
│ name         │    │ name         │    │ postId           │
│ weight       │    │ weight       │    │ browseTime       │
│ type         │    │ postId       │    │ tags: List<PostTag>│
└──────────────┘    └──────────────┘    └──────────────────┘
                                               │
                                               ▼
                                       ┌──────────────┐
                                       │   PostTag    │
                                       └──────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    RecommendationResult                      │
├─────────────────────────────────────────────────────────────┤
│  postId: Long                                                │
│  totalScore: double                                          │
│  dimensionScores: Map<String, Double>                        │
└─────────────────────────────────────────────────────────────┘
```

## 相关文档

- [接口使用说明](./README.md)
- [API参考文档](./api-reference.md)
- [错误码定义](./error-codes.md)
- [使用示例代码](./examples.md)
