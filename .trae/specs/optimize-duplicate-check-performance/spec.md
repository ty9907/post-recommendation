# 帖子重复度检测性能优化 Spec

> 实现状态更新（2026-03-22）：本规格对应的核心代码已补齐到仓库，包括富文本预处理、SimHash/标签索引、候选集管理、异步精检、风险分级、性能监控、索引同步，以及实时/批量检测器扩展。  
> 生产规模性能目标仍需要在接入真实业务数据后继续压测验证。

## Why
当前抄袭检测系统每篇新帖需要与所有历史帖子逐一比对，时间复杂度为 O(n)，在帖子量大时消耗巨大资源。需要实现分层过滤、索引加速、异步检测等优化策略，将检测复杂度从 O(n) 降至 O(log n) 或 O(1)。

## What Changes
- 实现 SimHash 分层过滤策略，快速筛选候选集
- 构建 SimHash 倒排索引，实现毫秒级指纹检索
- 实现基于标签的预筛选机制，缩小比对范围
- 实现异步检测 + 风险分级机制，不阻塞用户发布
- 新增候选集管理器和索引管理器
- 扩展现有检测器支持分层检测流程

## Impact
- Affected specs: 文章抄袭检测功能
- Affected code:
  - 新增 `com.example.demo.duplicate.index` 包（索引管理）
  - 新增 `com.example.demo.duplicate.candidate` 包（候选集管理）
  - 新增 `com.example.demo.duplicate.async` 包（异步检测）
  - 修改 `RealTimeDetector` 支持分层过滤
  - 修改 `BatchDetector` 支持索引加速
  - 扩展 `DuplicateCheckConfig` 添加性能配置项

## ADDED Requirements

### Requirement: 富文本预处理
系统应在计算 SimHash 指纹前对富文本内容进行预处理，确保指纹计算的准确性。

#### Scenario: HTML 标签去除
- **WHEN** 系统接收富文本格式的文章内容
- **THEN** 系统应去除所有 HTML/XML 标签，只保留纯文本内容
- **AND** 应正确处理自闭合标签（如 `<br/>`, `<img/>`）

#### Scenario: 特殊字符处理
- **WHEN** 预处理富文本内容
- **THEN** 系统应解码 HTML 实体（如 `&amp;` → `&`, `&lt;` → `<`）
- **AND** 应移除不可见字符和多余空白

#### Scenario: 格式规范化
- **WHEN** 预处理完成
- **THEN** 系统应将内容规范化为统一的纯文本格式
- **AND** 应保留文本的语义结构（段落、句子）

#### Scenario: 预处理缓存
- **WHEN** 同一文章多次检测
- **THEN** 系统应缓存预处理后的纯文本结果
- **AND** 避免重复执行 HTML 解析

### Requirement: SimHash 分层过滤策略
系统应实现分层过滤策略，先用低成本算法快速筛选候选集，再用精确算法验证。

#### Scenario: 第一层 SimHash 快速筛选
- **WHEN** 新帖子发布需要检测抄袭
- **THEN** 系统应首先计算新帖的 SimHash 指纹
- **AND** 通过海明距离快速筛选出候选文章集（海明距离 < 阈值）

#### Scenario: 第二层标签过滤
- **WHEN** SimHash 筛选完成
- **THEN** 系统应基于标签进一步缩小候选集
- **AND** 只保留有共享标签的候选文章

#### Scenario: 第三层精确计算
- **WHEN** 候选集筛选完成
- **THEN** 系统应对候选集使用 Word2Vec 或 Hybrid 算法进行精确相似度计算
- **AND** 候选集大小应控制在 50 篇以内

#### Scenario: 性能指标
- **WHEN** 执行分层过滤检测
- **THEN** 候选集筛选时间应 < 10ms
- **AND** 总检测时间应 < 200ms（历史帖子 10 万篇以内）

### Requirement: SimHash 倒排索引
系统应构建 SimHash 倒排索引，实现 O(1) 复杂度的指纹检索。

#### Scenario: 索引构建
- **WHEN** 文章被添加到系统
- **THEN** 系统应计算其 64 位 SimHash 指纹
- **AND** 将指纹分成 4 段（每段 16 位）存入倒排索引

#### Scenario: 索引查询
- **WHEN** 查询相似文章
- **THEN** 系统应将查询指纹分成 4 段
- **AND** 查找任一段相同的文章 ID 集合
- **AND** 对候选文章计算精确海明距离

#### Scenario: 索引更新
- **WHEN** 文章被删除或修改
- **THEN** 系统应更新相应的索引条目

#### Scenario: 索引持久化
- **WHEN** 系统关闭
- **THEN** 系统应将索引持久化到磁盘
- **AND** 系统启动时应加载索引

### Requirement: 标签倒排索引预筛选
系统应构建标签倒排索引，快速定位相关文章。

#### Scenario: 标签索引构建
- **WHEN** 文章被添加到系统
- **THEN** 系统应将文章 ID 添加到其所有标签对应的倒排列表中

#### Scenario: 标签预筛选查询
- **WHEN** 新帖需要检测
- **THEN** 系统应通过标签索引快速获取包含相同标签的文章 ID 集合
- **AND** 只对这些文章进行相似度计算

#### Scenario: 多标签合并
- **WHEN** 新帖有多个标签
- **THEN** 系统应合并各标签对应的文章集合
- **AND** 按标签匹配数量排序

### Requirement: 异步检测与风险分级
系统应实现异步检测机制，根据风险等级采取不同策略。

#### Scenario: 同步快速检测
- **WHEN** 用户发布新帖子
- **THEN** 系统应同步执行快速检测（SimHash + 标签）
- **AND** 检测时间应 < 100ms

#### Scenario: 高风险处理
- **WHEN** 快速检测发现高风险（相似度 > 0.7）
- **THEN** 系统应阻止发布并提示用户修改

#### Scenario: 中风险处理
- **WHEN** 快速检测发现中风险（相似度 0.4-0.7）
- **THEN** 系统应允许发布但标记为待审核
- **AND** 将文章加入异步精确检测队列

#### Scenario: 低风险处理
- **WHEN** 快速检测发现低风险（相似度 < 0.4）
- **THEN** 系统应正常发布
- **AND** 将文章加入异步精确检测队列

#### Scenario: 异步精确检测
- **WHEN** 异步任务执行
- **THEN** 系统应使用精确算法进行完整检测
- **AND** 检测完成后根据结果执行相应操作（通知/下架/标记）

### Requirement: 候选集管理器
系统应提供候选集管理器，统一管理候选文章的筛选逻辑。

#### Scenario: 候选集生成
- **WHEN** 需要检测新帖
- **THEN** 系统应通过候选集管理器生成候选文章集
- **AND** 候选集应按相似可能性排序

#### Scenario: 候选集大小控制
- **WHEN** 候选集过大
- **THEN** 系统应按优先级截取 Top-K 候选
- **AND** K 值应可配置（默认 50）

#### Scenario: 候选集缓存
- **WHEN** 相同内容重复检测
- **THEN** 系统应缓存候选集结果
- **AND** 缓存有效期应可配置

### Requirement: 检测性能监控
系统应提供检测性能监控功能。

#### Scenario: 性能指标收集
- **WHEN** 执行检测
- **THEN** 系统应记录各阶段耗时
- **AND** 包括索引查询时间、候选集大小、精确计算时间

#### Scenario: 性能报告
- **WHEN** 管理员请求性能报告
- **THEN** 系统应提供检测性能统计
- **AND** 包括平均耗时、P99 耗时、候选集平均大小

## MODIFIED Requirements

### Requirement: 实时检测器扩展
现有 RealTimeDetector 需要扩展以支持分层过滤流程。

#### Scenario: 分层检测流程
- **WHEN** RealTimeDetector 执行检测
- **THEN** 应依次执行：SimHash 筛选 → 标签筛选 → 精确计算
- **AND** 每层结果应记录在检测报告中

### Requirement: 批量检测器扩展
现有 BatchDetector 需要扩展以支持索引加速。

#### Scenario: 索引加速批量检测
- **WHEN** BatchDetector 执行批量检测
- **THEN** 应优先使用索引进行候选筛选
- **AND** 应并行处理多个检测任务

### Requirement: 配置扩展
DuplicateCheckConfig 需要添加性能相关配置项。

#### Scenario: 性能配置项
- **WHEN** 配置检测参数
- **THEN** 应支持配置：
  - SimHash 海明距离阈值
  - 候选集最大大小
  - 异步检测队列大小
  - 风险分级阈值

## REMOVED Requirements
无移除的需求。
