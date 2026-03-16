# API参考文档

本文档详细介绍了帖子推荐SDK的所有公共API接口。

## 目录

- [PostRecommendationSDK 类](#postrecommendationsdk-类)
- [RecommendationService 接口](#recommendationservice-接口)
- [RecommendationDimension 接口](#recommendationdimension-接口)
- [DimensionManager 类](#dimensionmanager-类)
- [SDKConfig 类](#sdkconfig-类)
- [SDKConfigBuilder 类](#sdkconfigbuilder-类)

---

## PostRecommendationSDK 类

帖子推荐SDK的入口类，提供统一的推荐服务访问接口。采用单例模式实现，确保全局只有一个SDK实例。

**包路径**: `com.example.demo.recommendation.PostRecommendationSDK`

### 类方法

#### getInstance()

获取SDK单例实例。

```java
public static PostRecommendationSDK getInstance()
```

**返回值**

| 类型 | 说明 |
|------|------|
| PostRecommendationSDK | SDK单例实例 |

**示例**

```java
PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();
```

---

#### configBuilder()

创建配置构建器实例。

```java
public static SDKConfigBuilder configBuilder()
```

**返回值**

| 类型 | 说明 |
|------|------|
| SDKConfigBuilder | 配置构建器实例 |

**示例**

```java
SDKConfig config = PostRecommendationSDK.configBuilder()
    .addDimensionWeight("TAG_MATCHING", 0.6)
    .defaultLimit(10)
    .build();
```

---

### 实例方法

#### initialize(SDKConfig config)

初始化SDK。必须在使用其他方法之前调用此方法。

```java
public synchronized void initialize(SDKConfig config)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| config | SDKConfig | 是 | SDK配置对象 |

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | 当配置为空时抛出 |

**示例**

```java
SDKConfig config = SDKConfig.defaultConfig();
PostRecommendationSDK.getInstance().initialize(config);
```

**注意事项**

- 初始化只需执行一次，重复调用会被忽略
- 必须在使用其他SDK方法之前完成初始化

---

#### recommend(RecommendationRequest request)

执行单次推荐，根据推荐请求计算推荐结果。

```java
public List<RecommendationResult> recommend(RecommendationRequest request)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| request | RecommendationRequest | 是 | 推荐请求对象 |

**返回值**

| 类型 | 说明 |
|------|------|
| List\<RecommendationResult\> | 推荐结果列表，按总分降序排列 |

**异常**

| 异常类型 | 错误码 | 说明 |
|----------|--------|------|
| SDKNotInitializedException | SDK_001 | SDK未初始化时抛出 |
| InvalidRequestException | SDK_002 | 请求无效时抛出 |

**示例**

```java
RecommendationRequest request = new RecommendationRequest();
request.setUserTags(userTags);
request.setPostTags(postTags);

List<RecommendationResult> results = sdk.recommend(request);
```

---

#### batchRecommend(Map\<String, RecommendationRequest\> requests)

批量执行推荐，并行处理多个推荐请求。

```java
public Map<String, List<RecommendationResult>> batchRecommend(Map<String, RecommendationRequest> requests)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requests | Map\<String, RecommendationRequest\> | 是 | 请求映射，键为请求ID，值为推荐请求 |

**返回值**

| 类型 | 说明 |
|------|------|
| Map\<String, List\<RecommendationResult\>\> | 结果映射，键为请求ID，值为推荐结果列表 |

**异常**

| 异常类型 | 错误码 | 说明 |
|----------|--------|------|
| SDKNotInitializedException | SDK_001 | SDK未初始化时抛出 |
| InvalidRequestException | SDK_002 | 请求无效时抛出 |

**示例**

```java
Map<String, RecommendationRequest> requests = new HashMap<>();
requests.put("user_001", request1);
requests.put("user_002", request2);

Map<String, List<RecommendationResult>> results = sdk.batchRecommend(requests);
List<RecommendationResult> user1Results = results.get("user_001");
```

---

#### updateDimensionWeights(Map\<String, Double\> weights)

动态更新维度权重。

```java
public void updateDimensionWeights(Map<String, Double> weights)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| weights | Map\<String, Double\> | 是 | 维度权重映射，键为维度名称，值为权重值 |

**异常**

| 异常类型 | 错误码 | 说明 |
|----------|--------|------|
| SDKNotInitializedException | SDK_001 | SDK未初始化时抛出 |

**示例**

```java
Map<String, Double> weights = new HashMap<>();
weights.put("TAG_MATCHING", 0.7);
weights.put("BROWSE_HISTORY", 0.3);

sdk.updateDimensionWeights(weights);
```

---

#### getDimensionManager()

获取维度管理器实例。

```java
public DimensionManager getDimensionManager()
```

**返回值**

| 类型 | 说明 |
|------|------|
| DimensionManager | 维度管理器实例 |

**异常**

| 异常类型 | 错误码 | 说明 |
|----------|--------|------|
| SDKNotInitializedException | SDK_001 | SDK未初始化时抛出 |

**示例**

```java
DimensionManager manager = sdk.getDimensionManager();
```

---

#### isInitialized()

检查SDK是否已初始化。

```java
public boolean isInitialized()
```

**返回值**

| 类型 | 说明 |
|------|------|
| boolean | 是否已初始化 |

**示例**

```java
if (sdk.isInitialized()) {
    // 执行推荐操作
}
```

---

#### getConfig()

获取当前SDK配置。

```java
public SDKConfig getConfig()
```

**返回值**

| 类型 | 说明 |
|------|------|
| SDKConfig | SDK配置，未初始化时返回null |

**示例**

```java
SDKConfig config = sdk.getConfig();
if (config != null) {
    int limit = config.getDefaultLimit();
}
```

---

#### shutdown()

关闭SDK，释放资源。关闭后需要重新初始化才能使用。

```java
public synchronized void shutdown()
```

**示例**

```java
sdk.shutdown();
```

**注意事项**

- 应用关闭时务必调用此方法释放资源
- shutdown后SDK实例仍然存在，但需要重新初始化

---

## RecommendationService 接口

推荐服务接口，定义推荐系统的核心功能规范。

**包路径**: `com.example.demo.recommendation.service.RecommendationService`

### 方法列表

#### recommend(RecommendationRequest request)

单次推荐，根据推荐请求计算推荐结果。

```java
List<RecommendationResult> recommend(RecommendationRequest request)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| request | RecommendationRequest | 是 | 推荐请求对象 |

**返回值**

| 类型 | 说明 |
|------|------|
| List\<RecommendationResult\> | 推荐结果列表，按总分降序排列 |

---

#### batchRecommend(Map\<String, RecommendationRequest\> requests)

批量推荐，并行处理多个推荐请求。

```java
Map<String, List<RecommendationResult>> batchRecommend(Map<String, RecommendationRequest> requests)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requests | Map\<String, RecommendationRequest\> | 是 | 请求映射 |

**返回值**

| 类型 | 说明 |
|------|------|
| Map\<String, List\<RecommendationResult\>\> | 结果映射 |

---

#### updateDimensionWeights(Map\<String, Double\> weights)

动态更新维度权重。

```java
void updateDimensionWeights(Map<String, Double> weights)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| weights | Map\<String, Double\> | 是 | 维度权重映射 |

---

## RecommendationDimension 接口

推荐维度接口，定义推荐系统中各个维度的计算规范。实现此接口可以创建自定义推荐维度。

**包路径**: `com.example.demo.recommendation.dimension.RecommendationDimension`

### 方法列表

#### getName()

获取维度名称。

```java
String getName()
```

**返回值**

| 类型 | 说明 |
|------|------|
| String | 维度名称，必须唯一 |

---

#### calculate(DimensionContext context)

计算推荐分数。

```java
DimensionResult calculate(DimensionContext context)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| context | DimensionContext | 是 | 维度上下文，包含计算所需的各种数据 |

**返回值**

| 类型 | 说明 |
|------|------|
| DimensionResult | 维度计算结果 |

---

#### initialize(RecommendationConfig config)

初始化维度。

```java
void initialize(RecommendationConfig config)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| config | RecommendationConfig | 是 | 推荐配置 |

---

## DimensionManager 类

维度管理器，用于管理推荐系统中的各个维度。

**包路径**: `com.example.demo.recommendation.dimension.DimensionManager`

### 构造器

```java
public DimensionManager()
```

---

### 方法列表

#### registerDimension(String name, RecommendationDimension dimension, double weight)

注册维度。

```java
public void registerDimension(String name, RecommendationDimension dimension, double weight)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |
| dimension | RecommendationDimension | 是 | 维度实例 |
| weight | double | 是 | 维度权重 |

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | 参数无效时抛出 |

---

#### removeDimension(String name)

移除维度。

```java
public RecommendationDimension removeDimension(String name)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |

**返回值**

| 类型 | 说明 |
|------|------|
| RecommendationDimension | 被移除的维度实例，不存在则返回null |

---

#### getDimension(String name)

获取维度实例。

```java
public RecommendationDimension getDimension(String name)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |

**返回值**

| 类型 | 说明 |
|------|------|
| RecommendationDimension | 维度实例，不存在则返回null |

---

#### getDimensionNames()

获取所有维度名称。

```java
public List<String> getDimensionNames()
```

**返回值**

| 类型 | 说明 |
|------|------|
| List\<String\> | 维度名称列表 |

---

#### getDimensions()

获取所有维度实例。

```java
public List<RecommendationDimension> getDimensions()
```

**返回值**

| 类型 | 说明 |
|------|------|
| List\<RecommendationDimension\> | 维度实例列表 |

---

#### setDimensionWeight(String name, double weight)

设置维度权重。

```java
public void setDimensionWeight(String name, double weight)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |
| weight | double | 是 | 权重值 |

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | 权重为负数或维度不存在时抛出 |

---

#### getDimensionWeight(String name)

获取维度权重。

```java
public double getDimensionWeight(String name)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |

**返回值**

| 类型 | 说明 |
|------|------|
| double | 权重值，维度不存在返回0.0 |

---

#### setDimensionEnabled(String name, boolean enabled)

设置维度启用状态。

```java
public void setDimensionEnabled(String name, boolean enabled)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |
| enabled | boolean | 是 | 是否启用 |

---

#### isDimensionEnabled(String name)

判断维度是否启用。

```java
public boolean isDimensionEnabled(String name)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |

**返回值**

| 类型 | 说明 |
|------|------|
| boolean | 是否启用 |

---

#### normalizeWeights()

归一化权重，使所有启用的维度权重之和为1。

```java
public void normalizeWeights()
```

---

#### containsDimension(String name)

判断维度是否存在。

```java
public boolean containsDimension(String name)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |

**返回值**

| 类型 | 说明 |
|------|------|
| boolean | 是否存在 |

---

#### getDimensionCount()

获取维度数量。

```java
public int getDimensionCount()
```

**返回值**

| 类型 | 说明 |
|------|------|
| int | 维度数量 |

---

#### clear()

清空所有维度。

```java
public void clear()
```

---

## SDKConfig 类

SDK配置类，用于存储SDK的各项配置参数。使用Builder模式构建。

**包路径**: `com.example.demo.recommendation.config.SDKConfig`

### 静态方法

#### defaultConfig()

创建默认配置。

```java
public static SDKConfig defaultConfig()
```

**返回值**

| 类型 | 说明 |
|------|------|
| SDKConfig | 默认配置实例 |

**默认值**

| 配置项 | 默认值 |
|--------|--------|
| enableCache | true |
| defaultLimit | 10 |
| threadPoolSize | CPU核心数 |
| dimensionWeights | 空Map |

---

#### builder()

创建配置构建器。

```java
public static Builder builder()
```

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

### 实例方法

#### getDimensionWeights()

获取维度权重配置。

```java
public Map<String, Double> getDimensionWeights()
```

**返回值**

| 类型 | 说明 |
|------|------|
| Map\<String, Double\> | 维度权重映射，不可修改 |

---

#### isEnableCache()

是否启用缓存。

```java
public boolean isEnableCache()
```

**返回值**

| 类型 | 说明 |
|------|------|
| boolean | 是否启用缓存 |

---

#### getDefaultLimit()

获取默认返回结果数量限制。

```java
public int getDefaultLimit()
```

**返回值**

| 类型 | 说明 |
|------|------|
| int | 默认限制数量 |

---

#### getThreadPoolSize()

获取线程池大小。

```java
public int getThreadPoolSize()
```

**返回值**

| 类型 | 说明 |
|------|------|
| int | 线程池大小 |

---

### Builder 内部类

#### addDimensionWeight(String name, double weight)

添加维度权重。

```java
public Builder addDimensionWeight(String name, double weight)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |
| weight | double | 是 | 权重值（≥0） |

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

#### dimensionWeights(Map\<String, Double\> weights)

批量设置维度权重。

```java
public Builder dimensionWeights(Map<String, Double> weights)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| weights | Map\<String, Double\> | 是 | 维度权重映射 |

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

#### enableCache(boolean enable)

设置是否启用缓存。

```java
public Builder enableCache(boolean enable)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| enable | boolean | 是 | 是否启用 |

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

#### defaultLimit(int limit)

设置默认返回结果数量限制。

```java
public Builder defaultLimit(int limit)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | int | 是 | 限制数量（≥0） |

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

#### threadPoolSize(int size)

设置线程池大小。

```java
public Builder threadPoolSize(int size)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| size | int | 是 | 线程池大小（>0） |

**返回值**

| 类型 | 说明 |
|------|------|
| Builder | 构建器实例 |

---

#### build()

构建SDKConfig实例。

```java
public SDKConfig build()
```

**返回值**

| 类型 | 说明 |
|------|------|
| SDKConfig | SDKConfig实例 |

---

## SDKConfigBuilder 类

SDK配置构建器，提供流式API构建SDKConfig实例。

**包路径**: `com.example.demo.recommendation.config.SDKConfigBuilder`

### 构造器

```java
public SDKConfigBuilder()
```

---

### 方法列表

#### addDimensionWeight(String name, double weight)

添加维度权重。

```java
public SDKConfigBuilder addDimensionWeight(String name, double weight)
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 维度名称 |
| weight | double | 是 | 权重值（≥0） |

**返回值**

| 类型 | 说明 |
|------|------|
| SDKConfigBuilder | 构建器实例 |

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | 参数无效时抛出 |

---

#### dimensionWeights(Map\<String, Double\> weights)

批量设置维度权重。

```java
public SDKConfigBuilder dimensionWeights(Map<String, Double> weights)
```

---

#### enableCache(boolean enable)

设置是否启用缓存。

```java
public SDKConfigBuilder enableCache(boolean enable)
```

---

#### defaultLimit(int limit)

设置默认返回结果数量限制。

```java
public SDKConfigBuilder defaultLimit(int limit)
```

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | limit小于0时抛出 |

---

#### threadPoolSize(int size)

设置线程池大小。

```java
public SDKConfigBuilder threadPoolSize(int size)
```

**异常**

| 异常类型 | 说明 |
|----------|------|
| IllegalArgumentException | size小于等于0时抛出 |

---

#### build()

构建SDKConfig实例。

```java
public SDKConfig build()
```

---

#### reset()

重置构建器状态。

```java
public SDKConfigBuilder reset()
```

---

## 相关文档

- [接口使用说明](./README.md)
- [数据模型文档](./data-models.md)
- [错误码定义](./error-codes.md)
- [使用示例代码](./examples.md)
