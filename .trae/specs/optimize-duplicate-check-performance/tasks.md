# Tasks

> 状态更新（2026-03-22）：代码已完成富文本预处理、索引、候选集、异步检测、风险分级、检测器扩展、配置扩展、性能监控、索引同步，以及对应的单元/集成测试骨架。  
> 其中生产规模压测指标（如 10 万篇文章级别的性能目标）仍建议结合真实业务数据继续验证。

## 第零阶段：富文本预处理

- [x] Task 0: 创建富文本预处理器
  - [x] SubTask 0.1: 创建 RichTextPreprocessor 接口，定义预处理规范
  - [x] SubTask 0.2: 实现 HtmlTagRemover 类，去除 HTML/XML 标签
  - [x] SubTask 0.3: 实现 HtmlEntityDecoder 类，解码 HTML 实体
  - [x] SubTask 0.4: 实现 TextNormalizer 类，规范化文本格式
  - [x] SubTask 0.5: 实现 PreprocessedTextCache 类，缓存预处理结果
  - [x] SubTask 0.6: 集成到现有 TextPreprocessor 工具类

## 第一阶段：索引基础设施

- [x] Task 1: 创建 SimHash 倒排索引管理器
  - [x] SubTask 1.1: 创建 SimHashIndex 接口，定义索引操作规范
  - [x] SubTask 1.2: 实现 InMemorySimHashIndex 类，使用分段倒排索引
  - [x] SubTask 1.3: 实现索引的添加、删除、查询方法
  - [x] SubTask 1.4: 实现海明距离计算和候选集检索
  - [x] SubTask 1.5: 实现索引持久化（序列化到磁盘）

- [x] Task 2: 创建标签倒排索引管理器
  - [x] SubTask 2.1: 创建 TagInvertedIndex 接口
  - [x] SubTask 2.2: 实现 InMemoryTagInvertedIndex 类
  - [x] SubTask 2.3: 实现标签到文章 ID 的映射管理
  - [x] SubTask 2.4: 实现多标签合并查询

## 第二阶段：候选集管理

- [x] Task 3: 创建候选集管理器
  - [x] SubTask 3.1: 创建 CandidateManager 接口
  - [x] SubTask 3.2: 实现 CandidateManagerImpl 类
  - [x] SubTask 3.3: 实现分层候选筛选逻辑（SimHash → 标签）
  - [x] SubTask 3.4: 实现候选集大小控制和优先级排序
  - [x] SubTask 3.5: 实现候选集缓存功能

## 第三阶段：异步检测机制

- [x] Task 4: 创建异步检测框架
  - [x] SubTask 4.1: 创建 AsyncDetectionTask 类，封装异步检测任务
  - [x] SubTask 4.2: 创建 AsyncDetectionQueue 类，管理检测任务队列
  - [x] SubTask 4.3: 创建 AsyncDetectionWorker 类，执行异步检测
  - [x] SubTask 4.4: 创建 AsyncDetectionService 类，统一管理异步检测
  - [x] SubTask 4.5: 实现检测结果回调机制

- [ ] Task 5: 实现风险分级策略
  - [x] SubTask 5.1: 创建 RiskLevel 枚举（HIGH, MEDIUM, LOW）
  - [x] SubTask 5.2: 创建 RiskAssessor 类，评估风险等级
  - [x] SubTask 5.3: 实现分级处理策略配置
  - [ ] SubTask 5.4: 实现分级处理执行器

## 第四阶段：检测器扩展

- [x] Task 6: 扩展 RealTimeDetector
  - [x] SubTask 6.1: 添加分层过滤检测流程
  - [x] SubTask 6.2: 集成 SimHashIndex 和 TagInvertedIndex
  - [x] SubTask 6.3: 集成 CandidateManager
  - [x] SubTask 6.4: 添加性能指标记录
  - [x] SubTask 6.5: 更新检测报告格式，包含分层信息

- [ ] Task 7: 扩展 BatchDetector
  - [x] SubTask 7.1: 添加索引加速支持
  - [x] SubTask 7.2: 优化并行处理策略
  - [ ] SubTask 7.3: 添加批量索引更新方法

## 第五阶段：配置扩展

- [x] Task 8: 扩展检测配置
  - [x] SubTask 8.1: 在 DuplicateCheckConfig 中添加性能配置项
  - [x] SubTask 8.2: 添加 SimHash 海明距离阈值配置
  - [x] SubTask 8.3: 添加候选集最大大小配置
  - [x] SubTask 8.4: 添加风险分级阈值配置
  - [x] SubTask 8.5: 添加异步检测队列配置

## 第六阶段：性能监控

- [x] Task 9: 实现性能监控
  - [x] SubTask 9.1: 创建 PerformanceMetrics 类，记录性能指标
  - [x] SubTask 9.2: 创建 PerformanceMonitor 类，收集和统计指标
  - [x] SubTask 9.3: 实现性能报告生成
  - [x] SubTask 9.4: 添加性能日志输出

## 第七阶段：索引同步与维护

- [x] Task 10: 实现索引同步服务
  - [x] SubTask 10.1: 创建 IndexSyncService 类
  - [x] SubTask 10.2: 实现文章变更事件监听
  - [x] SubTask 10.3: 实现索引增量更新
  - [x] SubTask 10.4: 实现索引重建功能

## 第八阶段：测试

- [x] Task 11: 编写单元测试
  - [x] SubTask 11.1: 编写 SimHashIndex 测试
  - [x] SubTask 11.2: 编写 TagInvertedIndex 测试
  - [x] SubTask 11.3: 编写 CandidateManager 测试
  - [x] SubTask 11.4: 编写 AsyncDetectionService 测试
  - [x] SubTask 11.5: 编写 RiskAssessor 测试

- [x] Task 12: 编写集成测试
  - [x] SubTask 12.1: 编写分层过滤完整流程测试
  - [x] SubTask 12.2: 编写异步检测流程测试
  - [x] SubTask 12.3: 编写索引同步测试

- [ ] Task 13: 编写性能测试
  - [ ] SubTask 13.1: 编写大规模数据索引构建测试（10 万篇文章）
  - [ ] SubTask 13.2: 编写候选集筛选性能测试（目标 < 10ms）
  - [ ] SubTask 13.3: 编写完整检测流程性能测试（目标 < 200ms）
  - [ ] SubTask 13.4: 编写并发检测性能测试

# Task Dependencies

- [Task 1] depends on [Task 0]
- [Task 2] depends on [Task 0]
- [Task 3] depends on [Task 1, Task 2]
- [Task 4] depends on [Task 6]
- [Task 5] depends on [Task 6]
- [Task 6] depends on [Task 1, Task 2, Task 3, Task 8]
- [Task 7] depends on [Task 1, Task 2, Task 8]
- [Task 9] depends on [Task 6, Task 7]
- [Task 10] depends on [Task 1, Task 2]
- [Task 11] depends on [Task 0, Task 1, Task 2, Task 3, Task 4, Task 5]
- [Task 12] depends on [Task 6, Task 7, Task 10]
- [Task 13] depends on [Task 6, Task 7, Task 10]
