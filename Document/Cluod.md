<img src="https://fast.itbaima.net/2023/03/06/XeofrwYMN5GQnlC.png"/>

### 微服务基础
`注意`: 此阶段学习推荐的电脑配置 至少配备4核心CPU(主频3.0Ghz以上) + 16GB内存 否则卡到你怀疑人生

前面我们讲解了SpringBoot框架 通过使用SpringBoot框架 我们的项目开发速度可以说是得到了质的提升 同时 我们对于项目的维护和理解
也会更加的轻松 可见 SpringBoot为我们的开发带来了巨大便捷 而这一部分 我们将基于SpringBoot 继续深入到企业实际场景
探讨微服务架构下的SpringCloud 这个部分我们会更加注重于架构设计上的讲解 弱化实现原理方面的研究

### 传统项目转型
要说近几年最火热的话题 那还得是微服务 那么什么是微服务呢?

我们可以先从技术的演变开始看起 在我们学习JavaWeb之后 一般的网站开发模式为 Servlet+JSP 但是实际上我们在学习了SSM之后 会发现这种模式已经远远落后了
第一 一个公司不可能去招那么多同时会前端+后端的开发人员 就算招到 也并不一定能保证两个方面都比较擅长 相比前后端分开学习的开发人员 显然后者的学习成本更低 专注度更高
因此前后端分离成为了一种新的趋势 通过使用SpringBoot 我们几乎可以很快速地开发一个高性能的单体应用 只需要启动一个服务端 我们整个项目就开始运行了 各项功能融于一体 开发起来也更加轻松

但是随着我们项目的不断扩大 单体应用似乎显得有点乏力了

随着越来越多的功能不断地加入到一个SpringBoot项目中 随着接口不断增加 整个系统就要在同一时间内响应更多类型的请求 显然 这种扩展方式是不可能无限使用下去的
总有一天 这个SpringBoot项目会庞大到运行缓慢 并且所有的功能如果都集成在单端上 那么所有的请求都会全部汇集到一台服务器上 对此服务器造成巨大压力

可以试想一下 如果我们的电脑已经升级到i9-12900K 但是依然在运行项目的时候缓慢 无法同一时间响应成千上万的请求 那么这个问题就已经不是单纯升级机器配置可以解决的了

<img src="https://fast.itbaima.net/2023/03/06/dk931jubHw6KifZ.png">

传统单体架构应用随着项目规模的扩大 实际上会暴露越来越多的问题 尤其是一台服务器无法承受庞大的单体应用部署 并且单体应用的维护也会越来越困难 我们得寻找一种新的开发架构来解决这些问题了

    In short, the microservice architectural style is an approach to developing a single application as
    a suite of small services, each running in its own process and communicating with lightweight
    mechanisms, often an HTTP resource API. These services are built around business capabilities and
    independently deployable by fully automated deployment machinery. There is a bare minimum of centralized
    management of these services, which may be written in different programming languages and use different data storage technologies.

Martin Fowler在2014年提出了"微服务"架构 它是一种全新的架构风格:
- 微服务把一个庞大的单体应用拆分为一个个的小型服务 比如我们原来的图书管理项目中 有登录, 注册, 添加, 删除, 搜索等功能
  那么我们可以将这些功能单独做成一个个小型的SpringBoot项目 独立运行
- 每个小型的微服务 都可以独立部署和升级 这样 就算整个系统崩溃 那么也只会影响一个服务的运行
- 微服务之间使用HTTP进行数据交互 不再是单体应用内部交互了 虽然这样会显得更麻烦 但是带来的好处也是很直接的 甚至能突破语言限制
  使用不同的编程语言进行微服务开发 只需要使用HTTP进行数据交互即可
- 我们可以同时购买多台主机来分别部署这些微服务 这样 单机的压力就被分散到多台机器 并且每台机器的配置不一定需要太高 这样就能节省大量的成本 同时安全性也得到很大的保证
- 甚同一个微服务可以同时存在多个 这样当其中一个服务出现问题时 其它服务器也在运行同样的微服务 这样可以保证一个微服务的高可用

<img src="https://fast.itbaima.net/2023/03/06/xSAhFqJUfmoa1Pv.png"/>

当然 这里只是简单的演示一下微服务架构 实际开发中肯定是比这个复杂得多的

可见 采用微服务架构 更加能够应对当今时代下的种种考验 传统项目的开发模式 需要进行架构上的升级

### 走进SpringCloud
前面我们介绍了微服务架构的优点 那么同样的 这些优点的背后也存在着诸多的问题:
- 要实现微服务并不是说只需要简单地将项目进行拆分 我们还需要考虑对各个微服务进行管理 监控等 这样我们才能够及时地寻找和排查问题
  因此微服务往往需要的是一整套解决方案 包括服务注册和发现, 容灾处理, 负载均衡, 配置管理等
- 它不像单体架构那种方便维护 由于部署在多个服务器 我们不得不去保证各个微服务能够稳定运行 在管理难度上肯定是高于传统单体应用的
- 在分布式的环境下 单体应用的某些功能可能会变得比较麻烦 比如分布式事务

所以 为了更好地解决这些问题 SpringCloud正式登场

SpringCloud是Spring提供的一套分布式解决方案 集合了一些大型互联网公司的开源产品 包括诸多组件 共同组成SpringCloud框架 并且
它利用SpringBoot的开发便利性巧妙地简化了分布式系统基础设施的开发 
如服务发现注册 配置中心, 消息总线, 负载均衡, 熔断机制, 数据监控等 都可用利用SpringBoot的开发风格做到一键启动和部署

由于中小型公司没用独立开发自己的分布式基础设施的能力 使用SpringCloud解决方案能够以最低的成本应对当前时代的业务发展

<img src="https://fast.itbaima.net/2023/03/06/1ulvL5q4PpbcoGD.png"/>

可以看到 SpringCloud整体架构的亮点是非常明显的 分布式架构下的各个场景 都有对应的组件来处理 比如基于Netflix(奈飞)的开源分布式解决方案提供的组件:
- Eureka - 实现服务治疗理(服务注册与发现) 我们可以对所有的微服务进行集中管理 包块他们的运行状态, 信息等
- Ribbon - 为服务之间相互调用提供负载均衡算法(现在被SpringCloudLoadBalancer取代)
- Hystrix - 断路器 保护系统, 控制故障范围 暂时可以跟家里电闸的保险丝类比 当触电危险发生时能够防止进一步的发展
- Zuul - api网关, 路由, 负载均衡等多种作用 就像我们的路由器 可能有很多个设备都连接了路由器 但是数据包要转发给谁则是路由器在进行(已经被SpringCloudGateway取代)
- Config - 配置管理 可以实现配置文件集中管理

当然 这里只是进行简单的了解即可 实际上微服务的玩法非常多 我们后面的学习中将会逐步进行探索

那么首先 我们就从注册中心开始说起