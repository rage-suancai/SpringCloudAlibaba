<img src="https://fast.itbaima.net/2023/03/06/xnmustzRkFZJWIP.png"/>

### Sentinel 流量防卫兵
注意: 这一章有点小绕 思路理清

经过之前的学习 我们了解了微服务存在的雪崩问题 也就是说一个微服务出现问题 有可能导致整个链路直接不可用
这种时候我们就需要进行及时的熔断和降级 这些策略 我们之前通过使用Hystrix来实现

    随着微服务的流行 服务和服务之间的稳定性变得越来越重要 Sentinel以流量为切入点 从流量控制, 熔断降级, 系统负载保护等多个维度保护服务的稳定性

Sentinel 具有以下特征:
- 丰富的应用场景: Sentinel承接了阿里巴巴近10年的双十一大促流量的核心场景 例如秒杀(即突发流量控制在系统容量可以承受的范围), 消息削峰填谷, 集群流量控制, 实时熔断下游不可用应用等
- 完备的实时监控: Sentinel同时提供实时的监控功能 您可以在控制台中看到接入应用的单台机器秒级数据 甚至500台以下规模的集群的汇总运行情况
- 广泛的开源生态: Sentinel提供开箱即用的与其它开源框架/库的整合模块 例如与Spring Cloud, ApacheDubbo, gRPC, Quarkus的整合 您只需要引入相应的依赖并进行简单的配置即可快速地接入Sentinel 同时Sentinel提供Java/Go/C++ 等多语言的原生实现
- 完善的SPI扩展机制: Sentinel提供简单易用, 完善的SPI扩展接口 您可以通过实现扩展接口来快速地定制逻辑 例如定制规则管理, 适配动态数据源等

### 安装与部署
和Nacos一样 它是独立安装和部署的 下载地址: https://github.com/alibaba/Sentinel/releases

<img src="https://fast.itbaima.net/2023/03/06/oZdLMAJaCD3Uw9F.png"/>

注意: 下载下来之后是一个jar文件(其实就是个SpringBoot项目) 我们需要在IDEA中添加一些运行配置:

<img src="https://fast.itbaima.net/2023/03/06/Hjm4Z38s95YiFvI.png"/>

接着就可以直接启动啦 当然默认端口占用8080 如果需要修改 可以添加环境变量:

<img src="https://fast.itbaima.net/2023/03/06/RfVAdtOqJjWlx6E.png"/>

启动之后 就可以访问到Sentinel的监控页面了 用户名和密码都是sentinel 地址: http://localhost:8858/#/dashboard

<img src="https://fast.itbaima.net/2023/03/06/QpVRTYtBX6kvj2b.png"/>

这样就成功开启监控页面了 接着我们需要让我们的服务连接到Sentinel控制台 老规矩 导入依赖:

```xml
                    <dependency>
                        <groupId>com.alibaba.cloud</groupId>
                        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
                    </dependency>
```

然后在配置文件中添加Sentinel相关信息(实际上Sentinel是本地在进行管理 但是我们可以连接到监控页面 这样就可以图形化操作了)















