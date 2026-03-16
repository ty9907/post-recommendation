# 错误码定义文档

本文档详细介绍了帖子推荐SDK中定义的所有错误码及其处理方式。

## 目录

- [异常体系结构](#异常体系结构)
- [错误码列表](#错误码列表)
- [异常类详解](#异常类详解)
- [错误处理最佳实践](#错误处理最佳实践)

---

## 异常体系结构

SDK采用统一的异常体系，所有异常均继承自`RecommendationException`基类。

```
RecommendationException (基类)
├── SDKNotInitializedException    (SDK_001)
└── InvalidRequestException       (SDK_002)
```

### 异常类包路径

| 异常类 | 包路径 |
|--------|--------|
| RecommendationException | com.example.demo.recommendation.exception.RecommendationException |
| SDKNotInitializedException | com.example.demo.recommendation.exception.SDKNotInitializedException |
| InvalidRequestException | com.example.demo.recommendation.exception.InvalidRequestException |

---

## 错误码列表

### SDK_001: SDK未初始化

| 属性 | 值 |
|------|-----|
| 错误码 | SDK_001 |
| 异常类 | SDKNotInitializedException |
| 默认消息 | SDK未初始化，请先调用initialize方法进行初始化 |
| HTTP状态码建议 | 500 Internal Server Error |

**触发场景**

- 在调用`initialize()`方法之前调用其他SDK方法
- SDK已关闭（调用`shutdown()`后）再次使用

**解决方案**

```java
// 检查是否已初始化
if (!PostRecommendationSDK.getInstance().isInitialized()) {
    SDKConfig config = SDKConfig.defaultConfig();
    PostRecommendationSDK.getInstance().initialize(config);
}

// 或者直接捕获异常
try {
    List<RecommendationResult> results = sdk.recommend(request);
} catch (SDKNotInitializedException e) {
    // 重新初始化
    sdk.initialize(SDKConfig.defaultConfig());
    // 重试操作
    results = sdk.recommend(request);
}
```

---

### SDK_002: 无效请求

| 属性 | 值 |
|------|-----|
| 错误码 | SDK_002 |
| 异常类 | InvalidRequestException |
| 默认消息 | 无效的推荐请求 |
| HTTP状态码建议 | 400 Bad Request |

**触发场景**

- 请求对象为null
- 请求中用户标签、帖子标签、浏览历史均为空
- 批量请求中某个请求为null

**常见错误消息**

| 消息 | 说明 |
|------|------|
| 请求参数'request'不能为空 | 请求对象为null |
| 推荐请求必须包含用户标签、帖子标签或浏览历史中的至少一项 | 三项数据均为空 |
| 请求参数'requests[xxx]'不能为空 | 批量请求中ID为xxx的请求为null |
| 请求参数'fieldName'无效: reason | 特定字段无效 |

**解决方案**

```java
try {
    List<RecommendationResult> results = sdk.recommend(request);
} catch (InvalidRequestException e) {
    // 获取错误详情
    String errorCode = e.getErrorCode();
    String message = e.getMessage();
    
    // 记录日志
    logger.error("推荐请求无效: {} - {}", errorCode, message);
    
    // 返回错误响应
    return ErrorResponse.badRequest(errorCode, message);
}
```

---

## 异常类详解

### RecommendationException

推荐异常基类，所有推荐系统相关异常的父类。

**包路径**: `com.example.demo.recommendation.exception.RecommendationException`

#### 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| errorCode | String | 错误码 |

#### 构造器

```java
public RecommendationException()

public RecommendationException(String message)

public RecommendationException(String message, Throwable cause)

public RecommendationException(String errorCode, String message)

public RecommendationException(String errorCode, String message, Throwable cause)
```

#### 方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| getErrorCode() | String | 获取错误码 |
| setErrorCode(String) | void | 设置错误码 |

---

### SDKNotInitializedException

SDK未初始化异常，当SDK未初始化或已关闭时调用相关方法抛出此异常。

**包路径**: `com.example.demo.recommendation.exception.SDKNotInitializedException`

#### 常量

```java
private static final String DEFAULT_ERROR_CODE = "SDK_001";
private static final String DEFAULT_MESSAGE = "SDK未初始化，请先调用initialize方法进行初始化";
```

#### 构造器

```java
public SDKNotInitializedException()

public SDKNotInitializedException(String message)

public SDKNotInitializedException(String message, Throwable cause)
```

---

### InvalidRequestException

无效请求异常，当推荐请求参数无效时抛出此异常。

**包路径**: `com.example.demo.recommendation.exception.InvalidRequestException`

#### 常量

```java
private static final String DEFAULT_ERROR_CODE = "SDK_002";
```

#### 构造器

```java
public InvalidRequestException()

public InvalidRequestException(String message)

public InvalidRequestException(String message, Throwable cause)
```

#### 静态工厂方法

| 方法 | 说明 |
|------|------|
| forField(String fieldName, String reason) | 创建字段无效异常 |
| nullField(String fieldName) | 创建字段为空异常 |

**使用示例**

```java
// 抛出字段无效异常
throw InvalidRequestException.forField("userTags", "权重值超出范围");

// 抛出字段为空异常
throw InvalidRequestException.nullField("request");
```

---

## 错误处理最佳实践

### 1. 统一异常捕获

建议在应用层统一捕获SDK异常，并转换为适当的响应格式。

```java
@RestControllerAdvice
public class RecommendationExceptionHandler {

    @ExceptionHandler(SDKNotInitializedException.class)
    public ResponseEntity<ErrorResponse> handleSDKNotInitialized(SDKNotInitializedException e) {
        ErrorResponse error = new ErrorResponse(
            e.getErrorCode(),
            e.getMessage(),
            "SDK服务暂时不可用，请稍后重试"
        );
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e) {
        ErrorResponse error = new ErrorResponse(
            e.getErrorCode(),
            e.getMessage(),
            "请检查请求参数是否正确"
        );
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(RecommendationException.class)
    public ResponseEntity<ErrorResponse> handleRecommendationException(RecommendationException e) {
        ErrorResponse error = new ErrorResponse(
            e.getErrorCode(),
            e.getMessage(),
            "推荐服务处理异常"
        );
        return ResponseEntity.status(500).body(error);
    }
}
```

### 2. 初始化检查

在应用启动时完成SDK初始化，并提供健康检查接口。

```java
@Component
public class RecommendationSDKInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        SDKConfig config = SDKConfig.builder()
            .addDimensionWeight("TAG_MATCHING", 0.6)
            .addDimensionWeight("BROWSE_HISTORY", 0.4)
            .defaultLimit(20)
            .build();
        
        PostRecommendationSDK.getInstance().initialize(config);
        log.info("帖子推荐SDK初始化完成");
    }
}

@RestController
public class HealthController {

    @GetMapping("/health/recommendation")
    public Map<String, Object> checkRecommendationHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("initialized", PostRecommendationSDK.getInstance().isInitialized());
        return health;
    }
}
```

### 3. 请求参数验证

在调用SDK前进行参数预校验，提供更友好的错误提示。

```java
public class RecommendationRequestValidator {

    public void validate(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("推荐请求不能为空");
        }

        boolean hasData = false;
        
        if (request.getUserTags() != null && !request.getUserTags().isEmpty()) {
            hasData = true;
            validateUserTags(request.getUserTags());
        }
        
        if (request.getPostTags() != null && !request.getPostTags().isEmpty()) {
            hasData = true;
            validatePostTags(request.getPostTags());
        }
        
        if (request.getBrowseHistory() != null && !request.getBrowseHistory().isEmpty()) {
            hasData = true;
            validateBrowseHistory(request.getBrowseHistory());
        }
        
        if (!hasData) {
            throw new IllegalArgumentException("请至少提供用户标签、帖子标签或浏览历史中的一项");
        }
    }

    private void validateUserTags(List<UserTag> tags) {
        for (UserTag tag : tags) {
            if (tag.getName() == null || tag.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("用户标签名称不能为空");
            }
            if (tag.getWeight() < 0 || tag.getWeight() > 1) {
                throw new IllegalArgumentException("用户标签权重必须在0-1之间: " + tag.getName());
            }
        }
    }

    private void validatePostTags(List<PostTag> tags) {
        for (PostTag tag : tags) {
            if (tag.getName() == null || tag.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("帖子标签名称不能为空");
            }
            if (tag.getPostId() == null) {
                throw new IllegalArgumentException("帖子标签必须关联帖子ID: " + tag.getName());
            }
        }
    }

    private void validateBrowseHistory(List<BrowseHistory> history) {
        for (BrowseHistory item : history) {
            if (item.getPostId() == null) {
                throw new IllegalArgumentException("浏览历史必须包含帖子ID");
            }
        }
    }
}
```

### 4. 日志记录

记录详细的错误日志，便于问题排查。

```java
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    public List<RecommendationResult> recommend(RecommendationRequest request) {
        try {
            return PostRecommendationSDK.getInstance().recommend(request);
        } catch (SDKNotInitializedException e) {
            log.error("SDK未初始化，请检查应用启动日志");
            throw new ServiceException("推荐服务暂时不可用", e);
        } catch (InvalidRequestException e) {
            log.warn("无效的推荐请求: {} - {}", e.getErrorCode(), e.getMessage());
            throw new BusinessException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("推荐服务异常", e);
            throw new ServiceException("推荐服务处理失败", e);
        }
    }
}
```

### 5. 优雅关闭

在应用关闭时正确释放SDK资源。

```java
@Component
public class RecommendationSDKShutdown implements DisposableBean {

    @Override
    public void destroy() {
        if (PostRecommendationSDK.getInstance().isInitialized()) {
            PostRecommendationSDK.getInstance().shutdown();
            log.info("帖子推荐SDK已关闭");
        }
    }
}
```

### 6. 重试机制

对于可恢复的错误，实现重试机制。

```java
@Service
public class RecommendationServiceWithRetry {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 1000;

    public List<RecommendationResult> recommendWithRetry(RecommendationRequest request) {
        int attempt = 0;
        while (true) {
            try {
                return PostRecommendationSDK.getInstance().recommend(request);
            } catch (SDKNotInitializedException e) {
                attempt++;
                if (attempt >= MAX_RETRY) {
                    throw e;
                }
                // 尝试重新初始化
                try {
                    PostRecommendationSDK.getInstance().initialize(SDKConfig.defaultConfig());
                } catch (Exception initEx) {
                    log.warn("SDK重新初始化失败", initEx);
                }
                sleep(RETRY_DELAY_MS);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## 错误码速查表

| 错误码 | 异常类 | 说明 | HTTP状态码 |
|--------|--------|------|------------|
| SDK_001 | SDKNotInitializedException | SDK未初始化 | 500 |
| SDK_002 | InvalidRequestException | 无效请求 | 400 |

---

## 相关文档

- [接口使用说明](./README.md)
- [API参考文档](./api-reference.md)
- [数据模型文档](./data-models.md)
- [使用示例代码](./examples.md)
