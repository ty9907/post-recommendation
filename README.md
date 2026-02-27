# 文章标签提取系统

## 项目简介

本项目是一个基于Java的文章标签提取系统，主要功能是为文章提取关键标签，为后续其他模块的文章推荐做准备。系统使用Maven管理依赖，JDK 21作为开发环境。

## 项目功能

- 文章关键标签提取
- 标签权重计算
- 标签存储与管理
- 为推荐系统提供标签数据支持
- 支持多种分词算法（简单分词、IKAnalyzer、HanLP）
- 灵活的接口设计，易于扩展新的分词实现
- 完善的单元测试覆盖，使用SLF4J日志框架
- 支持日志输出提取的标签详情，便于调试和验证

## 技术栈

- **语言**: Java 21
- **构建工具**: Maven 3.8.1+
- **测试框架**: JUnit 4.13.2
- **日志框架**: SLF4J 1.7.36
- **分词库**: IKAnalyzer 2012_u6, HanLP portable-1.8.4

## 项目结构

```
post-recommendation/
├── src/
│   ├── main/
│   │   ├── java/         # 主源代码
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── demo/
│   │   │               ├── tag/          # 标签提取模块
│   │   │               │   ├── TagExtractor.java        # 标签提取接口
│   │   │               │   ├── impl/                 # 分词实现
│   │   │               │   │   ├── SimpleTagExtractor.java
│   │   │               │   │   ├── IKAnalyzerTagExtractor.java
│   │   │               │   │   └── HanLPTagExtractor.java
│   │   │               │   ├── model/                # 数据模型
│   │   │               │   │   └── Tag.java
│   │   │               │   └── service/              # 服务层
│   │   │               │       └── TagService.java
│   │   │               └── Main.java
│   │   └── resources/    # 资源文件
│   └── test/
│       ├── java/         # 测试代码
│       │   └── com/
│       │       └── example/
│       │           └── demo/
│       │               ├── tag/                  # 标签提取测试模块
│       │               │   ├── service/
│       │               │   │   └── TagServiceTest.java
│       │               │   └── impl/
│       │               │       ├── SimpleTagExtractorTest.java
│       │               │       ├── IKAnalyzerTagExtractorTest.java
│       │               │       └── HanLPTagExtractorTest.java
│       │               └── TestTagExtractor.java
│       └── resources/    # 测试资源文件
├── pom.xml               # Maven配置文件
├── README.md             # 项目说明文档
└── .gitignore            # Git忽略文件配置
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8.1+

### 构建项目

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package
```

### 运行项目

```bash
# 运行主类
java -cp target/demo-1.0-SNAPSHOT.jar com.example.demo.Main
```

## 核心功能模块

1. **标签提取接口层**: 定义统一的标签提取接口，支持灵活扩展
2. **分词实现层**: 
   - SimpleTagExtractor: 基于正则表达式的简单分词
   - IKAnalyzerTagExtractor: 使用IKAnalyzer分词器，适合中文分词
   - HanLPTagExtractor: 使用HanLP分词器，功能更强大的中文分词
3. **数据模型层**: Tag模型，包含标签名称、权重和频率属性
4. **服务层**: TagService，整合不同的分词器，提供统一的标签提取服务
5. **测试模块**: 完善的单元测试，覆盖所有核心功能，使用日志输出测试结果

## 使用示例

```java
// 使用默认的HanLP分词器
TagService tagService = new TagService();
List<Tag> tags = tagService.extractTags(article, 10);

// 指定使用IKAnalyzer分词器
TagService ikService = new TagService("ik");
List<Tag> ikTags = ikService.extractTags(article, 10);

// 指定使用简单分词器
TagService simpleService = new TagService("simple");
List<Tag> simpleTags = simpleService.extractTags(article, 10);

// 输出标签信息
for (Tag tag : tags) {
    System.out.println(tag.getName() + " (权重: " + tag.getWeight() + ", 频率: " + tag.getFrequency() + ")");
}
```

## 测试

项目包含完善的单元测试，覆盖所有核心功能：

```bash
# 运行所有测试
mvn test

# 测试输出会显示提取的标签详情，包括：
# - 标签名称
# - 权重值（格式化为4位小数）
# - 出现频率
```

测试模块包括：
- **TagServiceTest**: 测试服务层功能
- **SimpleTagExtractorTest**: 测试简单分词器
- **IKAnalyzerTagExtractorTest**: 测试IKAnalyzer分词器
- **HanLPTagExtractorTest**: 测试HanLP分词器（包括中文分词测试）

所有测试使用SLF4J日志框架输出详细的标签提取结果，便于调试和验证。

## 后续规划

- 集成自然语言处理库，提高标签提取准确性
- 实现标签同义词合并功能
- 添加标签热度分析
- 开发Web API接口
- 集成到完整的推荐系统中

## 贡献指南

欢迎提交Issue和Pull Request来改进本项目。

## 许可证

本项目采用MIT许可证。
