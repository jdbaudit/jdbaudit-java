# jdbaudit-scan-api



- 定义好需要扩展的接口方法，最小实现原则，保证扩展者不需要关心系统其他逻辑，即可实现扩展。
- 保证实现类只关心规则加载和核心的匹配逻辑，任何其他的任务调度，规则过滤等都不需要关系，做到让类库扩展者实现最轻松的扩展

## 单例化

抽离公共实现，特别是任务管理。创建 AbstractDBScanner，将实际需重写方法降低到最小，只保留特有规则部分和特有规则匹配部分。

用户有两部分：
1. 一种是类库的使用者，注重对外的API设计。
2. 一种是类库的扩展者，注重扩展的简单性。