<img src="https://fast.itbaima.net/2023/03/06/V1dFqQMR7T2GzSJ.png"/>

### 微服务进阶
前面我们了解了微服务的一套解决方案 但是它是基于Netflix的解决方案 实际上我们发现 很多框架都已经停止维护了 来看看目前我们所认识到的SpringCloud各大组件的维护情况
- 注册中心: Eureka(属于Netflix 2.x版本不再开源 1.x版本仍在更新)
- 服务调用: Ribbon(属于Netflix 停止更新 已经彻底被移除), SpringCloud Loadbalancer(属于SpringCloud官方 目前的默认方案)
- 服务降级: Hystrix(属于Netflix 停止更新 已经彻底被移除)
- 路由网关: Zuul(属于Netflix 停止更新 已经彻底被移除), Gateway(属于SpringCloud官方 推荐方案)
- 配置中心: Config(属于SpringCloud官方)

可见 我们之前使用的整套解决方案中 超过半数的组件都已经处于不可用状态 并且部分组件都是SpringCloud官方出手提供框架进行解决
因此 寻找一套更好的解决方案势在必行 也就引出了我们本章的主角: SpringCloud Alibaba

阿里巴巴作为业界的互联网大厂 给出了一套全新的解决方案 官方网站(中文): https://spring-cloud-alibaba-group.github.io/github-pages/2021/zh-cn/index.html

    Spring Cloud Alibaba致力于提供微服务开发的一站式解决方案 此项目包含开发分布式应用服务的必需组件 方便开发者通过Spring Cloud编程模型轻松使用这些组件来开发分布式应用服务
    依托Spring Cloud Alibaba您只需要添加一些注解和少量配置 就可以将Spring Cloud应用接入阿里分布式应用解决方案 通过阿里中间件来迅速搭建分布式应用系统

目前Spring Cloud Alibaba提供了如下功能:
1. 服务限流降级: 支持WebServlet, WebFlux, OpenFeign, RestTemplate, Dubbo 限流降级功能的接入 可以在运行时通过控制台实时修改限流降级规则 还支持查看限流降级Metrics监控
2. 服务注册与发现: 适配Spring Cloud服务注册与发现标准 默认集成了Ribbon的支持
3. 分布式配置管理: 支持分布式系统中的外部化配置 配置更改时自动刷新
4. Rpc服务: 扩展Spring Cloud客户端RestTemplate 和 OpenFeign 支持调用Dubbo RPC服务
5. 消息驱动能力: 基于Spring Cloud Stream为微服务应用构建消息驱动能力
6. 分布式事务: 使用@GlobalTransactional注解 高效并且对业务零侵入地解决分布式事务问题
7. 阿里云对象存储: 阿里云提供的海量, 安全, 低成本, 高可靠的云存储服务 支持在任何应用, 任何时间, 任何地点存储和访问任意类型的数据
8. 分布式任务调度: 提供秒级, 精准, 高可靠, 高可用的定时(基于Cron表达式)任务调度服务 同时提供分布式的任务执行模型 如网格任务 网格任务支持海量子任务均匀分配到所有Worker(schedulerx-client)上执行
9. 阿里云短信服务: 覆盖全球的短信服务, 友好, 高效, 智能的互联化通讯能力 帮助企业迅速搭建客户触达通道

可以看到 SpringCloudAlibaba实际上是对我们的SpringCloud组件增强功能 是SpringCloud的增强框架 可以兼容SpringCloud原生组件和SpringCloudAlibaba的组件

开始学习之前 把我们之前打包好的拆分项目解压 我们将基于它进行学习