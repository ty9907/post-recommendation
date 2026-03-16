# 使用示例代码

本文档提供了帖子推荐SDK的各种使用场景示例代码。

## 目录

- [基本使用示例](#基本使用示例)
- [批量推荐示例](#批量推荐示例)
- [动态权重调整示例](#动态权重调整示例)
- [自定义维度扩展示例](#自定义维度扩展示例)
- [Spring Boot集成示例](#spring-boot集成示例)
- [完整业务场景示例](#完整业务场景示例)

---

## 基本使用示例

### 1. 最简单的使用方式

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;
import com.example.demo.recommendation.model.*;

public class BasicExample {

    public static void main(String[] args) {
        // 1. 初始化SDK（使用默认配置）
        SDKConfig config = SDKConfig.defaultConfig();
        PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();
        sdk.initialize(config);

        // 2. 准备用户标签
        List<UserTag> userTags = Arrays.asList(
            new UserTag("Java", 0.9, "技术"),
            new UserTag("Spring", 0.7, "框架")
        );

        // 3. 准备候选帖子标签
        List<PostTag> postTags = Arrays.asList(
            new PostTag("Java", 0.9, 1001L),
            new PostTag("并发编程", 0.7, 1001L),
            new PostTag("Spring Boot", 0.8, 1002L),
            new PostTag("微服务", 0.6, 1003L)
        );

        // 4. 构建请求
        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        // 5. 获取推荐结果
        List<RecommendationResult> results = sdk.recommend(request);

        // 6. 处理结果
        for (RecommendationResult result : results) {
            System.out.printf("帖子ID: %d, 总分: %.4f%n", 
                result.getPostId(), result.getTotalScore());
        }

        // 7. 关闭SDK
        sdk.shutdown();
    }
}
```

### 2. 使用Builder模式构建配置

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;

public class BuilderExample {

    public void initSDK() {
        SDKConfig config = PostRecommendationSDK.configBuilder()
            .addDimensionWeight("TAG_MATCHING", 0.6)
            .addDimensionWeight("BROWSE_HISTORY", 0.4)
            .enableCache(true)
            .defaultLimit(20)
            .threadPoolSize(8)
            .build();

        PostRecommendationSDK.getInstance().initialize(config);
    }
}
```

### 3. 结合浏览历史推荐

```java
import com.example.demo.recommendation.model.*;
import java.util.*;

public class BrowseHistoryExample {

    public List<RecommendationResult> recommendWithHistory() {
        // 用户浏览历史
        List<BrowseHistory> browseHistory = Arrays.asList(
            new BrowseHistory(
                1001L, 
                System.currentTimeMillis() - 3600000,  // 1小时前
                Arrays.asList(
                    new PostTag("Java", 0.9, 1001L),
                    new PostTag("并发编程", 0.8, 1001L)
                )
            ),
            new BrowseHistory(
                1002L, 
                System.currentTimeMillis() - 86400000,  // 1天前
                Arrays.asList(
                    new PostTag("Spring", 0.9, 1002L),
                    new PostTag("IoC", 0.7, 1002L)
                )
            )
        );

        // 候选帖子
        List<PostTag> candidatePosts = Arrays.asList(
            new PostTag("Java", 0.8, 2001L),
            new PostTag("JVM调优", 0.9, 2001L),
            new PostTag("Spring Boot", 0.9, 2002L),
            new PostTag("微服务", 0.7, 2003L)
        );

        RecommendationRequest request = new RecommendationRequest();
        request.setBrowseHistory(browseHistory);
        request.setPostTags(candidatePosts);

        RecommendationConfig config = new RecommendationConfig();
        config.setLimit(10);
        request.setConfig(config);

        return PostRecommendationSDK.getInstance().recommend(request);
    }
}
```

---

## 批量推荐示例

### 1. 基本批量推荐

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.model.*;
import java.util.*;

public class BatchRecommendExample {

    public void batchRecommend() {
        // 准备多个用户的请求
        Map<String, RecommendationRequest> requests = new HashMap<>();

        // 用户1的请求
        RecommendationRequest request1 = new RecommendationRequest();
        request1.setUserTags(Arrays.asList(
            new UserTag("Java", 0.9, "技术"),
            new UserTag("MySQL", 0.7, "数据库")
        ));
        request1.setPostTags(getCandidatePosts());
        requests.put("user_001", request1);

        // 用户2的请求
        RecommendationRequest request2 = new RecommendationRequest();
        request2.setUserTags(Arrays.asList(
            new UserTag("Python", 0.8, "技术"),
            new UserTag("机器学习", 0.9, "AI")
        ));
        request2.setPostTags(getCandidatePosts());
        requests.put("user_002", request2);

        // 用户3的请求
        RecommendationRequest request3 = new RecommendationRequest();
        request3.setUserTags(Arrays.asList(
            new UserTag("前端", 0.8, "技术"),
            new UserTag("Vue", 0.9, "框架")
        ));
        request3.setPostTags(getCandidatePosts());
        requests.put("user_003", request3);

        // 批量执行推荐
        Map<String, List<RecommendationResult>> results = 
            PostRecommendationSDK.getInstance().batchRecommend(requests);

        // 处理各用户的结果
        for (Map.Entry<String, List<RecommendationResult>> entry : results.entrySet()) {
            String userId = entry.getKey();
            List<RecommendationResult> userResults = entry.getValue();
            
            System.out.println("用户 " + userId + " 的推荐结果:");
            for (RecommendationResult result : userResults) {
                System.out.printf("  帖子ID: %d, 分数: %.4f%n", 
                    result.getPostId(), result.getTotalScore());
            }
        }
    }

    private List<PostTag> getCandidatePosts() {
        return Arrays.asList(
            new PostTag("Java", 0.9, 1001L),
            new PostTag("Python", 0.9, 1002L),
            new PostTag("Vue.js", 0.8, 1003L),
            new PostTag("MySQL优化", 0.7, 1004L)
        );
    }
}
```

### 2. 批量推荐服务封装

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchRecommendationService {

    public Map<Long, List<Long>> batchRecommendForUsers(
            Map<Long, List<String>> userTagMap,
            List<PostInfo> candidatePosts) {

        Map<String, RecommendationRequest> requests = new HashMap<>();

        for (Map.Entry<Long, List<String>> entry : userTagMap.entrySet()) {
            Long userId = entry.getKey();
            List<String> tags = entry.getValue();

            List<UserTag> userTags = tags.stream()
                .map(tag -> new UserTag(tag, 1.0, "auto"))
                .collect(Collectors.toList());

            List<PostTag> postTags = candidatePosts.stream()
                .flatMap(post -> post.getTags().stream()
                    .map(tag -> new PostTag(tag, 1.0, post.getId())))
                .collect(Collectors.toList());

            RecommendationRequest request = new RecommendationRequest();
            request.setUserTags(userTags);
            request.setPostTags(postTags);

            RecommendationConfig config = new RecommendationConfig();
            config.setLimit(10);
            request.setConfig(config);

            requests.put(String.valueOf(userId), request);
        }

        Map<String, List<RecommendationResult>> results = 
            PostRecommendationSDK.getInstance().batchRecommend(requests);

        Map<Long, List<Long>> userRecommendations = new HashMap<>();
        for (Map.Entry<String, List<RecommendationResult>> entry : results.entrySet()) {
            Long userId = Long.valueOf(entry.getKey());
            List<Long> postIds = entry.getValue().stream()
                .map(RecommendationResult::getPostId)
                .collect(Collectors.toList());
            userRecommendations.put(userId, postIds);
        }

        return userRecommendations;
    }
}

class PostInfo {
    private Long id;
    private List<String> tags;

    public Long getId() { return id; }
    public List<String> getTags() { return tags; }
}
```

---

## 动态权重调整示例

### 1. 运行时权重调整

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import java.util.*;

public class DynamicWeightExample {

    public void adjustWeights() {
        PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();

        // 场景1: 新用户更依赖标签匹配
        Map<String, Double> newUserWeights = new HashMap<>();
        newUserWeights.put("TAG_MATCHING", 0.8);
        newUserWeights.put("BROWSE_HISTORY", 0.2);
        sdk.updateDimensionWeights(newUserWeights);

        // 场景2: 老用户更依赖浏览历史
        Map<String, Double> oldUserWeights = new HashMap<>();
        oldUserWeights.put("TAG_MATCHING", 0.3);
        oldUserWeights.put("BROWSE_HISTORY", 0.7);
        sdk.updateDimensionWeights(oldUserWeights);

        // 场景3: 均衡权重
        Map<String, Double> balancedWeights = new HashMap<>();
        balancedWeights.put("TAG_MATCHING", 0.5);
        balancedWeights.put("BROWSE_HISTORY", 0.5);
        sdk.updateDimensionWeights(balancedWeights);
    }
}
```

### 2. 基于用户行为的权重自适应

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.model.*;
import java.util.*;

public class AdaptiveWeightService {

    private final PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();

    public List<RecommendationResult> recommendWithAdaptiveWeight(
            Long userId, 
            List<UserTag> userTags,
            List<PostTag> postTags,
            List<BrowseHistory> browseHistory) {

        Map<String, Double> weights = calculateAdaptiveWeights(browseHistory);
        sdk.updateDimensionWeights(weights);

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);
        request.setBrowseHistory(browseHistory);

        return sdk.recommend(request);
    }

    private Map<String, Double> calculateAdaptiveWeights(List<BrowseHistory> history) {
        Map<String, Double> weights = new HashMap<>();

        if (history == null || history.isEmpty()) {
            weights.put("TAG_MATCHING", 0.8);
            weights.put("BROWSE_HISTORY", 0.2);
        } else {
            long historyCount = history.size();
            long recentHistory = history.stream()
                .filter(h -> System.currentTimeMillis() - h.getBrowseTime() < 7 * 24 * 3600 * 1000L)
                .count();

            double historyWeight = Math.min(0.7, 0.3 + (recentHistory * 0.05));
            double tagWeight = 1.0 - historyWeight;

            weights.put("TAG_MATCHING", tagWeight);
            weights.put("BROWSE_HISTORY", historyWeight);
        }

        return weights;
    }
}
```

### 3. A/B测试权重配置

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ABTestWeightManager {

    private final Map<String, Map<String, Double>> experimentWeights = new ConcurrentHashMap<>();

    public ABTestWeightManager() {
        Map<String, Double> control = new HashMap<>();
        control.put("TAG_MATCHING", 0.5);
        control.put("BROWSE_HISTORY", 0.5);
        experimentWeights.put("control", control);

        Map<String, Double> variantA = new HashMap<>();
        variantA.put("TAG_MATCHING", 0.7);
        variantA.put("BROWSE_HISTORY", 0.3);
        experimentWeights.put("variant_a", variantA);

        Map<String, Double> variantB = new HashMap<>();
        variantB.put("TAG_MATCHING", 0.3);
        variantB.put("BROWSE_HISTORY", 0.7);
        experimentWeights.put("variant_b", variantB);
    }

    public void applyExperiment(String experimentId) {
        Map<String, Double> weights = experimentWeights.get(experimentId);
        if (weights != null) {
            PostRecommendationSDK.getInstance().updateDimensionWeights(weights);
        }
    }

    public String assignExperiment(Long userId) {
        int hash = Math.abs(userId.hashCode() % 100);
        if (hash < 50) {
            return "control";
        } else if (hash < 75) {
            return "variant_a";
        } else {
            return "variant_b";
        }
    }
}
```

---

## 自定义维度扩展示例

### 1. 实现自定义维度

```java
import com.example.demo.recommendation.dimension.*;
import com.example.demo.recommendation.model.*;
import java.util.*;

public class PopularityDimension implements RecommendationDimension {

    private static final String DIMENSION_NAME = "POPULARITY";

    private Map<Long, Integer> postViewCounts = new HashMap<>();

    @Override
    public String getName() {
        return DIMENSION_NAME;
    }

    @Override
    public DimensionResult calculate(DimensionContext context) {
        Long postId = context.getCandidatePostId();
        Integer viewCount = postViewCounts.getOrDefault(postId, 0);

        double maxViews = postViewCounts.values().stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(1);

        double score = viewCount / maxViews;

        DimensionResult result = new DimensionResult(DIMENSION_NAME, score);
        result.addDetail("viewCount", viewCount);
        result.addDetail("normalizedScore", score);

        return result;
    }

    @Override
    public void initialize(RecommendationConfig config) {
        // 可从配置或数据库加载热度数据
    }

    public void updateViewCount(Long postId, int count) {
        postViewCounts.put(postId, count);
    }
}
```

### 2. 注册自定义维度

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;
import com.example.demo.recommendation.dimension.DimensionManager;

public class CustomDimensionExample {

    public void registerCustomDimension() {
        SDKConfig config = SDKConfig.builder()
            .addDimensionWeight("TAG_MATCHING", 0.4)
            .addDimensionWeight("BROWSE_HISTORY", 0.4)
            .addDimensionWeight("POPULARITY", 0.2)
            .build();

        PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();
        sdk.initialize(config);

        DimensionManager manager = sdk.getDimensionManager();

        PopularityDimension popularityDimension = new PopularityDimension();
        popularityDimension.initialize(new RecommendationConfig());

        manager.registerDimension("POPULARITY", popularityDimension, 0.2);

        manager.normalizeWeights();
    }
}
```

### 3. 时间衰减维度

```java
import com.example.demo.recommendation.dimension.*;
import com.example.demo.recommendation.model.*;
import java.time.*;
import java.util.*;

public class TimeDecayDimension implements RecommendationDimension {

    private static final String DIMENSION_NAME = "TIME_DECAY";
    private static final double DECAY_RATE = 0.1;

    private Map<Long, Long> postPublishTimes = new HashMap<>();

    @Override
    public String getName() {
        return DIMENSION_NAME;
    }

    @Override
    public DimensionResult calculate(DimensionContext context) {
        Long postId = context.getCandidatePostId();
        Long publishTime = postPublishTimes.get(postId);

        if (publishTime == null) {
            return new DimensionResult(DIMENSION_NAME, 0.5);
        }

        long daysSincePublish = Duration.between(
            Instant.ofEpochMilli(publishTime),
            Instant.now()
        ).toDays();

        double score = Math.exp(-DECAY_RATE * daysSincePublish);

        DimensionResult result = new DimensionResult(DIMENSION_NAME, score);
        result.addDetail("daysSincePublish", daysSincePublish);
        result.addDetail("decayRate", DECAY_RATE);

        return result;
    }

    @Override
    public void initialize(RecommendationConfig config) {
        // 初始化配置
    }

    public void setPostPublishTime(Long postId, Long publishTime) {
        postPublishTimes.put(postId, publishTime);
    }
}
```

---

## Spring Boot集成示例

### 1. 自动配置类

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationAutoConfiguration {

    @Value("${recommendation.tag-matching-weight:0.5}")
    private double tagMatchingWeight;

    @Value("${recommendation.browse-history-weight:0.5}")
    private double browseHistoryWeight;

    @Value("${recommendation.default-limit:10}")
    private int defaultLimit;

    @Value("${recommendation.thread-pool-size:4}")
    private int threadPoolSize;

    @Value("${recommendation.enable-cache:true}")
    private boolean enableCache;

    @Bean
    public SDKConfig sdkConfig() {
        return SDKConfig.builder()
            .addDimensionWeight("TAG_MATCHING", tagMatchingWeight)
            .addDimensionWeight("BROWSE_HISTORY", browseHistoryWeight)
            .defaultLimit(defaultLimit)
            .threadPoolSize(threadPoolSize)
            .enableCache(enableCache)
            .build();
    }

    @Bean
    public PostRecommendationSDK postRecommendationSDK(SDKConfig config) {
        PostRecommendationSDK sdk = PostRecommendationSDK.getInstance();
        sdk.initialize(config);
        return sdk;
    }
}
```

### 2. 推荐服务类

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationFacade {

    private final PostRecommendationSDK sdk;

    public RecommendationFacade(PostRecommendationSDK sdk) {
        this.sdk = sdk;
    }

    public List<RecommendationDTO> recommend(RecommendationQuery query) {
        RecommendationRequest request = buildRequest(query);
        List<RecommendationResult> results = sdk.recommend(request);
        return convertToDTO(results);
    }

    private RecommendationRequest buildRequest(RecommendationQuery query) {
        List<UserTag> userTags = query.getUserTags().stream()
            .map(tag -> new UserTag(tag.getName(), tag.getWeight(), tag.getType()))
            .collect(Collectors.toList());

        List<PostTag> postTags = new ArrayList<>();
        for (PostInfo post : query.getCandidatePosts()) {
            for (String tag : post.getTags()) {
                postTags.add(new PostTag(tag, 1.0, post.getId()));
            }
        }

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        RecommendationConfig config = new RecommendationConfig();
        config.setLimit(query.getLimit() > 0 ? query.getLimit() : 10);
        request.setConfig(config);

        return request;
    }

    private List<RecommendationDTO> convertToDTO(List<RecommendationResult> results) {
        return results.stream()
            .map(r -> new RecommendationDTO(
                r.getPostId(),
                r.getTotalScore(),
                r.getDimensionScores()
            ))
            .collect(Collectors.toList());
    }
}
```

### 3. REST控制器

```java
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationFacade recommendationFacade;

    public RecommendationController(RecommendationFacade recommendationFacade) {
        this.recommendationFacade = recommendationFacade;
    }

    @PostMapping
    public ApiResponse<List<RecommendationDTO>> recommend(
            @RequestBody RecommendationQuery query) {
        List<RecommendationDTO> results = recommendationFacade.recommend(query);
        return ApiResponse.success(results);
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, List<RecommendationDTO>>> batchRecommend(
            @RequestBody Map<String, RecommendationQuery> queries) {
        
        Map<String, List<RecommendationDTO>> results = new HashMap<>();
        for (Map.Entry<String, RecommendationQuery> entry : queries.entrySet()) {
            results.put(entry.getKey(), recommendationFacade.recommend(entry.getValue()));
        }
        return ApiResponse.success(results);
    }
}
```

### 4. 应用配置文件

```yaml
recommendation:
  tag-matching-weight: 0.6
  browse-history-weight: 0.4
  default-limit: 20
  thread-pool-size: 8
  enable-cache: true
```

---

## 完整业务场景示例

### 场景：首页推荐接口

```java
import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeRecommendationService {

    private final PostRecommendationSDK sdk;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BrowseHistoryRepository historyRepository;

    public HomeRecommendationService(
            PostRecommendationSDK sdk,
            UserRepository userRepository,
            PostRepository postRepository,
            BrowseHistoryRepository historyRepository) {
        this.sdk = sdk;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.historyRepository = historyRepository;
    }

    public List<PostDTO> getHomeRecommendations(Long userId, int limit) {
        User user = userRepository.findById(userId);
        List<Post> candidatePosts = postRepository.findActivePosts();
        List<BrowseHistoryEntity> history = historyRepository.findByUserId(userId, 50);

        adjustWeightsBasedOnUserType(user);

        RecommendationRequest request = buildRequest(user, candidatePosts, history, limit);
        List<RecommendationResult> results = sdk.recommend(request);

        return convertToPostDTOs(results, candidatePosts);
    }

    private void adjustWeightsBasedOnUserType(User user) {
        Map<String, Double> weights = new HashMap<>();

        if (user.isNewUser()) {
            weights.put("TAG_MATCHING", 0.8);
            weights.put("BROWSE_HISTORY", 0.2);
        } else if (user.isActiveUser()) {
            weights.put("TAG_MATCHING", 0.3);
            weights.put("BROWSE_HISTORY", 0.7);
        } else {
            weights.put("TAG_MATCHING", 0.5);
            weights.put("BROWSE_HISTORY", 0.5);
        }

        sdk.updateDimensionWeights(weights);
    }

    private RecommendationRequest buildRequest(
            User user, 
            List<Post> candidatePosts,
            List<BrowseHistoryEntity> history,
            int limit) {

        List<UserTag> userTags = user.getTags().stream()
            .map(tag -> new UserTag(tag.getName(), tag.getWeight(), tag.getType()))
            .collect(Collectors.toList());

        List<PostTag> postTags = candidatePosts.stream()
            .flatMap(post -> post.getTags().stream()
                .map(tag -> new PostTag(tag, 1.0, post.getId())))
            .collect(Collectors.toList());

        List<BrowseHistory> browseHistory = history.stream()
            .map(h -> new BrowseHistory(
                h.getPostId(),
                h.getBrowseTime().getTime(),
                h.getPostTags().stream()
                    .map(tag -> new PostTag(tag, 1.0, h.getPostId()))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);
        request.setBrowseHistory(browseHistory);

        RecommendationConfig config = new RecommendationConfig();
        config.setLimit(limit);
        request.setConfig(config);

        return request;
    }

    private List<PostDTO> convertToPostDTOs(
            List<RecommendationResult> results, 
            List<Post> candidatePosts) {
        
        Map<Long, Post> postMap = candidatePosts.stream()
            .collect(Collectors.toMap(Post::getId, p -> p));

        return results.stream()
            .map(result -> {
                Post post = postMap.get(result.getPostId());
                return new PostDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    result.getTotalScore(),
                    result.getDimensionScores()
                );
            })
            .collect(Collectors.toList());
    }
}
```

---

## 相关文档

- [接口使用说明](./README.md)
- [API参考文档](./api-reference.md)
- [数据模型文档](./data-models.md)
- [错误码定义](./error-codes.md)
