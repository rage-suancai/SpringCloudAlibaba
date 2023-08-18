<img src="https://image.itbaima.net/markdown/2023/03/06/pQkSrLx9NZRn8Ub.png"/>

### Nacos 更加全能的注册中心
Nacos(Naming Configuration Service)是一款阿里巴巴开源的服务注册与发现, 配置管理的组件 相当于是Eureka+Config的组合形态

### 安装与部署
Nacos服务器是独立安装部署的 因此我们需要下载最新的Nacos服务端程序 下载地址: https://github.com/alibaba/nacos

<img src="https://image.itbaima.net/markdown/2023/03/06/VStPIABaXxMp2N9.png"/>

可以看到目前最新的版本是1.4.3版本(2022年2月27日发布的) 我们直接下载zip文件即可

接着我们将文件进行解压 得到以下内容:

<img src="https://image.itbaima.net/markdown/2023/03/06/wWbuXRGizrQCT8J.png"/>

我们直接将其拖入到项目文件夹下 便于我们一会在IDEA内部启动 接着添加运行配置:

<img src="https://image.itbaima.net/markdown/2023/03/06/bM8doEZPth7DHfe.png"/>

其中-m standalone表示单节点模式 Mac和Linux下记得将解释器设定为/bin/bash 由于Nacos在Mac/Linux默认是后台启动模式
我们修改一下它的bash文件 让它变成前台启动 这样IDEA关闭了Nacos就自动关闭了 否则开发环境下很容易忘记关:

```bash
                      # 注释掉 nohup $JAVA ${JAVA_OPT} nacos.nacos >> ${BASE_DIR}/logs/start.out 2>&1 &
                      # 替换成下面的
                      $JAVA ${JAVA_OPT} nacos.nacos
```

接着我们点击启动:

<img src="https://image.itbaima.net/markdown/2023/03/06/O3pMSvDbxPKYT5q.png"/>

OK 启动成功 可以看到它的管理页面地址也是给我们贴出来了: http://localhost:8848/nacos/index.html 访问这个地址

默认的用户名和管理员密码都是nacos 直接登陆即可 可以看到进入管理页面之后功能也是相当丰富:

<img src="https://image.itbaima.net/markdown/2023/03/06/dom3WpJsiajgCE7.png"/>

至此 Nacos的安装与部署完成

### 服务注册与发现
现在我们要实现基于Nacos的服务注册与发现 那么就需要导入SpringCloudAlibaba相关的依赖 我们在父工程将依赖进行管理:

```xml
                        <dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.mybatis.spring.boot</groupId>
                                    <artifactId>mybatis-spring-boot-starter</artifactId>
                                    <version>2.2.0</version>
                                </dependency>
                              
                                <!-- 这里引入最新的SpringCloud依赖 -->
                                <dependency>
                                    <groupId>org.springframework.cloud</groupId>
                                    <artifactId>spring-cloud-dependencies</artifactId>
                                    <version>2021.0.1</version>
                                      <type>pom</type>
                                    <scope>import</scope>
                                </dependency>
                        
                                <!-- 这里引入最新的SpringCloudAlibaba依赖 2021.0.1.0版本支持SpringBoot2.6.X -->
                                <dependency>
                                    <groupId>com.alibaba.cloud</groupId>
                                    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                                    <version>2021.0.1.0</version>
                                    <type>pom</type>
                                    <scope>import</scope>
                                </dependency>
                            </dependencies>
                        </dependencyManagement>
```

接着我们就可以在子项目中添加服务发现依赖了 比如我们以图书服务为例:

```xml
                        <dependency>
                            <groupId>com.alibaba.cloud</groupId>
                            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
                        </dependency>
```

和注册到Eureka一样 我们也需要在配置文件中配置Nacos注册中心的地址:

```yaml
                        server:
                          # 之后所有的图书服务节点就81XX端口
                          port: 8101
                        spring:
                          datasource:
                            driver-class-name: com.mysql.cj.jdbc.Driver
                            url: jdbc:mysql://cloudstudy.mysql.cn-chengdu.rds.aliyuncs.com:3306/cloudstudy
                            username: test
                            password: 123456
                          # 应用名称 bookservice
                          application:
                            name: book-service
                          cloud:
                            nacos:
                              discovery:
                                # 配置Nacos注册中心地址
                                server-addr: localhost:8848
```

接着启动我们的图书服务 可以在Nacos的服务列表中找到:

<img src="https://image.itbaima.net/markdown/2023/03/06/9PLBGOXoaERnUwM.png"/>

按照同样的方法 我们接着将另外两个服务也注册到Nacos中:

<img src="https://image.itbaima.net/markdown/2023/03/06/K6VBtqEWSLnMp21.png"/>

接着我们使用OpenFeign 实现服务发现远程调用以及负载均衡 导入依赖:

```xml
                        <dependency>
                            <groupId>org.springframework.cloud</groupId>
                            <artifactId>spring-cloud-starter-openfeign</artifactId>
                        </dependency>
                        <!-- 这里需要单独导入LoadBalancer依赖 -->
                        <dependency>
                            <groupId>org.springframework.cloud</groupId>
                            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                        </dependency>
```

编写接口:

```java
                        @FeignClient("userservice")
                        public interface UserClient() {
        
                            @GetMapping("/user/{uid}")
                            User getUserById(@PathVariable("uid") Integer uid);
        
                        }      
```
```java
                        @FeignClient("bookservice")
                        public interface BookClient {
                        
                            @GetMapping("/book/{bid}")
                            User getBookById(@PathVariable("bid") Integer bid);
                        
                        }
```
```java
                        @Service("borrowService")
                        public class BorrowServiceImpl implements BorrowService {
                        
                            @Resource
                            private BorrowMapper borrowMapper;
                            @Resource
                            private UserClient userClient;
                            @Resource
                            private BookClient bookClient;
                        
                            @Override
                            public UserBorrowDetail getUserBorrowDetailByUid(Integer uid) {
                        
                                List<Borrow> borrow = borrowMapper.getBorrowByUid(uid);
                        
                                User user = userClient.getUserById(uid);
                                List<Book> bookList = borrow.stream()
                                        .map(b -> bookClient.getBookById(b.getBid()))
                                        .collect(Collectors.toList());
                                return new UserBorrowDetail(user, bookList);
                        
                            }
                        
                        }
```
```java
                        @EnableFeignClients
                        @SpringBootApplication
                        public class BorrowApplication {
                        
                            public static void main(String[] args) {
                        
                                SpringApplication.run(BorrowApplication.class, args);
                        
                            }
                        
                        }
```

接着我们进行测试:

<img src="https://image.itbaima.net/markdown/2023/03/06/HIGvXAad1EOVPt6.png"/>

测试正常 可以自动发现服务 接着我们来多配置几个实例 去掉图书服务和用户服务的端口配置

<img src="https://image.itbaima.net/markdown/2023/03/06/WZGdJ5BYpmbMuNT.png"/>

然后我们在图书服务和用户服务中添加一句打印方便之后查看:

```java
                        @RestController
                        public class UserController {
                        
                            @Resource
                            private UserService userService;
                        
                            private int userCallCount = 0;
                        
                            @GetMapping("/api/user/{uid}")
                            public User findUserById(@PathVariable("uid") Integer uid) {
                        
                                int count = userCallCount++; System.err.println("调用了用户服务" + count + "次");
                                return userService.getUserById(uid);
                        
                            }
                        
                        }
```

现在将全部服务启动:

<img src="https://image.itbaima.net/markdown/2023/03/06/GCrm8wgWXLzYhtK.png"/>

可以看到Nacos中的实例数量已经显示为2:

<img src="https://image.itbaima.net/markdown/2023/03/06/p6iYrPa8e1btZkl.png"/>

接着我们调用借阅服务 看看能否负载均衡远程调用:

<img src="https://image.itbaima.net/markdown/2023/03/06/jCl8RGhaIiUDBgm.png"/>

<img src="https://image.itbaima.net/markdown/2023/03/06/2bWdfmnVOyGzlZr.png"/>

OK 负载均衡远程调用没有问题 这样我们就实现了基于Nacos的服务的注册与发现 实际上大致流程与Eureka一致

值得注意的是 Nacos区分了临时实例和非临时实例:

<img src="https://image.itbaima.net/markdown/2023/03/06/cF5MoVX6vNnzx9j.png"/>

那么临时和非临时有什么区别呢?
- 临时实例: 和Eureka一样 采用心跳机制向Nacos发送请求保持在线状态 一旦心跳停止代表实例下线 不保留实例信息
- 非临时实例: 由Nacos主动进行联系 如果连接失败 那么不会移除实例信息 而是将健康状态设定为false 相当于会对某个实例状态持续地进行监控

我们可以通过配置文件进行修改临时实例:

```yaml
                        spring:
                          application:
                            name: borrow-service
                          cloud:
                            nacos:
                              discovery:
                                server-addr: localhost:8848
                                # 将ephemeral修改为false 表示非临时实例
                                ephemeral: false
```

接着我们在Nacos中查看 可以发现实例已经不是临时的了:

<img src="https://image.itbaima.net/markdown/2023/03/06/FdRTjlKszDoOPU3.png"/>

如果这时我们关闭此实例 那么会变成这样:

<img src="https://image.itbaima.net/markdown/2023/03/06/R5Jyhl29UcvuOCb.png"/>

只是将健康状态变为false 而不会删除实例的信息

### 集群分区
实际上集群分区概念在之前的Eureka中也有出现 比如:

```yaml
                         eureka:
                           client:
                                 fetch-registry: false
                             register-with-eureka: false
                             service-url:
                               defaultZone: http://localhost:8888/eureka
                               # 这个defaultZone是个啥玩意 为什么要用这个名称? 为什么要要用这样的形式来声明注册中心?
```

在一个分布式应用中 相同服务的实例可能会在不同的机器 位置上启动 比如我们的用户管理服务 可能在成都有一台服务器部署 重庆有一台服务器部署 而这时
我们在成都的服务器上启动了借阅服务 那么如果我们的借阅读服务现在要调用用户服务 就应该优先选择同一个区域的用户服务进行调用 这样会使得响应速度更快

<img src="https://image.itbaima.net/markdown/2023/03/06/szyGRrEfZ1KWmpj.png"/>

因此 我们可以对部署在不同机房的服务进行分区 可以看到实例的分区是默认:

<img src="https://image.itbaima.net/markdown/2023/03/06/wlO9dQ1NtKCxFTi.png"/>

我们可以直接在配置文件中进行修改:

```yaml
                         spring:
                           application:
                             name: borrow-service
                           cloud:
                             nacos:
                               discovery:
                                 server-addr: localhost:8848
                                 # 修改为重庆地区的集群
                                 cluster-name: Chongqing
```

当然由于我们这里使用的是不同的启动配置 直接在启动配置中添加环境变量

spring.cloud.nacos.discovery.cluster-name也行 这里我们将用户服务和图书服务两个区域都分配一个 借阅服务就配置为成都地区:

<img src="https://image.itbaima.net/markdown/2023/03/06/cwIhdCMmATELvlN.png"/>

修改完成之后 我们来尝试重新启动一下(Nacos也要重启) 观察Nacos中集群分布情况:

<img src="https://image.itbaima.net/markdown/2023/03/06/jrYo3epaLMyQnu4.png"/>

可以看到现在有两个集群 并且都有一个实例正在运行 我们接着去调用借阅服务 但是发现并没有按照区域进行优先调用 而依然使用的是轮询模式的负载均衡调用

我们必须要提供Nacos的负载均衡实现才能开启区域优先调用机制 只需要在配制文件中进行修改即可:

```yaml
                         spring:
                           application:
                             name: borrow-service
                           cloud:
                             nacos:
                               discovery:
                                 server-addr: localhost:8848
                                 cluster-name: Chengdu
                             # 将loadbalancer的nacos支持开启 集成Nacos负载均衡
                             loadbalancer:
                               nacos:
                                 enabled: true
```

现在我们重启借阅服务 会发现优先调用的是同区域的用户和图书服务 现在我们可以将成都地区的服务下线:

<img src="https://image.itbaima.net/markdown/2023/03/06/s1ko9UcD4mMQ5fW.png"/>

可以看到 在下线之后 由于本区域内没有可用服务了 借阅服务将会调用重庆区域的用户服务

除了根据区域优先调用之外 同一个区域内的实例也可以单独设置权重 Nacos会优先选择权重更大的实例进行调用 我们可以直接在管理页面中进行配置:

<img src="https://image.itbaima.net/markdown/2023/03/06/1pAckEZN5ltXKWG.png"/>

或是在配置文件中进行配置:

```yaml
                          spring:
                            application:
                              name: borrowservice
                            cloud:
                              nacos:
                                discovery:
                                  server-addr: localhost:8848
                                  cluster-name: Chengdu
                                  # 权重大小 越大越优先调用 默认为1
                                  weight: 0.5
```

通过配置权重 某些性能不太好的机器就能够更少地被使用 而更多的使用那些网络良好性能更高的主机上的实例

### 配置中心
前面我们学习了SpringCloud Config 我们可以通过配置服务来加载远程配置 这样我们就可以在远端集中管理配置文件

实际上我们可以在bootstrap.yaml中配置远程配置文件获取 然后再进入到配置文件加载环节 而Nacos也支持这样的操作
使用方式也比较类似 比如我们现在想要将借阅服务的配置文件放到Nacos进行管理 那么这个时候就需要在Nacos中创建配置文件:

<img src="https://image.itbaima.net/markdown/2023/03/06/6j2pAmdfyIGz9Cu.png"/>

将借阅服务的配置文件全部(当然正常情况下是不会全部CV的 只会复制那些需要经常修改的部分 这里为了省事就直接全部CV了)复制过来 注意Data ID的格式跟我们之前一样
应用名称-环境.yaml 如果只编写应用名称 那么代表此配置文件无论在什么环境下都会使用 然后每个配置文件都可以进行分组 也算是一种分类方式:

<img src="https://image.itbaima.net/markdown/2023/03/06/7ACoW3txIsjLzu2.png"/>

完成之后点击发布即可:

<img src="https://image.itbaima.net/markdown/2023/03/06/alFpWGfNejImQEw.png"/>

然后在项目中导入依赖:

```xml
                           <dependency>
                               <groupId>org.springframework.cloud</groupId>
                               <artifactId>spring-cloud-starter-bootstrap</artifactId>
                           </dependency>
                           <dependency>
                               <groupId>com.alibaba.cloud</groupId>
                               <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
                           </dependency>
```

接着我们在借阅服务中添加bootstrap.yaml文件:

```yaml
                           spring:
                             application:
                               # 服务名称和配置文件保持一致
                               name: user-service
                             profiles:
                               # 环境也是和配置文件保持一致
                               active: dev
                             cloud:
                               nacos:
                                 config:
                                   # 配置文件后缀名
                                   file-extension: yaml
                                   # 配置中心服务器地址 也就是Nacos地址
                                   server-addr: localhost:8848
```

现在我们启动服务试试看:

<img src="https://image.itbaima.net/markdown/2023/03/06/5J4FfMgtGwZhP3C.png"/>

可以看到成功读取配置文件并启动了 实际上使用上来说跟之前的Config是基本一致的

Nacos还支持配置文件的热更新 比如我们在配置文件中添加了一个属性 而这个时候可能需要实时修改 并在后端实时更新 那么这种该怎么实现呢？我们创建一个新的Controller:

```java
                            @RestController
                            public class TestController {
                            
                                @Value("${test.txt}") // 我们从配置文件中读取test.txt的字符串值 作为test接口的返回值
                                String txt;
                            
                                @GetMapping("/test")
                                public String test() {
                                    return txt;
                                }
                            
                            }
```

我们修改一下配置文件 然后重启服务器:

<img src="https://image.itbaima.net/markdown/2023/03/06/9xthuBpgFs4PTSq.png"/>

可以看到已经可以正常读取了:

<img src="https://image.itbaima.net/markdown/2023/03/06/kacrSVGYMpwK2jx.png"/>

现在我们将配置文件的值进行修改:

<img src="https://image.itbaima.net/markdown/2023/03/06/YLC2H6yGoVi5z1f.png"/>

再次访问接口 会发现没有发生变化:

<img src="https://image.itbaima.net/markdown/2023/03/06/isTaOUQwMVWGCY9.png"/>

但是后台是成功检测到值更新了 但是值却没改变:

<img src="https://image.itbaima.net/markdown/2023/03/06/dR4thB5JTk1cGjm.png"/>

那么如何才能实现配置热更新呢? 我们可以像下面这样:

```java
                            @RefreshScope // 添加此注解就能够实现自动刷新了
                            @RestController
                            public class TestController {
                            
                                @Value("${test.txt}")
                                private String txt;
                            
                                @GetMapping("/api/test")
                                public String out() {
                                    return txt;
                                }
                            
                            }
```

重启服务器 再次重复上述实验 成功

### 命名空间
我们还可以将配置文件或是服务实例划分到不同的命名空间中 其实就是区分开发, 生产环境或是引用归属之类的:

<img src="https://image.itbaima.net/markdown/2023/03/06/7itUIhz3NupRdr6.png"/>

这里我们创建一个新的命名空间:

<img src="https://image.itbaima.net/markdown/2023/03/06/DC2I1MvVFjYmPEq.png"/>

可以看到在dev命名空间下 没有任何配置文件和服务:

<img src="https://image.itbaima.net/markdown/2023/03/06/Ek4APjgGcqbitNm.png"/>

我们在不同的命名空间下 实例和配置都是相互之间隔离的 我们也可以在配置文件中指定当前的命名空间

### 实现高可用
由于Nacos暂不支持Arm架构芯片的Mac集群搭建 本小节用Linxu云主机(Nacos比较吃内存 2个Nacos服务器集群 至少2G内存)环境演示

通过前面的学习 我们已经了解了如何使用Nacos以及Nacos的功能等 最后我们来看看 如果像之前Eureka一样 搭建Nacos集群 实现高可用

官方方案: https://nacos.io/zh-cn/docs/cluster-mode-quick-start.html

<img src="https://image.itbaima.net/markdown/2023/03/06/H1AvxOK78yspP5k.jpg"/>

    http://ip1:port/openAPI 直连ip模式 机器挂则需要修改ip才可以使用
    http://SLB:port/openAPI 挂载SLB模式(内网SLB 不可暴露到公网 以免带来安全风险) 直连SLB即可 下面挂server真实ip 可读性不好
    http://nacos.com:port/openAPI 域名 + SLB模式(内网SLB 不可暴露到公网 以免带来安全风险) 可读性好 而且换ip方便 推荐模式

我们来看看它的架构设计 它推荐我们在所有的Nacos服务端之间建立一个负载均衡 我们通过访问负载均衡服务器来间接访问到各个Nacos服务器 实际上就是
比如有三个Nacos服务器集群 但是每个服务不可能把每个Nacos都去访问一次进行注册 实际上只需要在任意一台Nacos服务器上注册即可 Nacos服务器之间会自动同步信息
但是如果我们随便指定一台Nacos服务器进行注册 如果这台Nacos服务器挂了 但是其它Nacos服务器没挂 这样就没办法完成注册了 但是实际上整个集群还是可用的状态

所以这里就需要在所有Naocs服务器之间搭建一个SLB(服务器负载均衡) 这样就可以避免上面的问题了 但是我们知道 如果要实现外界对服务访问的负载均衡
我们就得用比如之前说到的Gateway来实现 而这里实际上我们可以用一个更加方便的工具: Nginx来实现(之前我们没讲过 但是使用起来很简单 放心后面会带着一起使用)

关于SLB最上方还有一个DNS(我们在计算机网络这门课程中学习过) 这个是因为SLB是裸IP 如果SLB服务器修改了地址 那所有微服务注册的地址也得改
所以这里是通过加域名 通过域名来访问, 让DNS去解析真实IP 这样就算改变IP 只需要修改域名解析记录即可 域名地址是不会变化的

最后就是Nacos的数据存储模式 在单节点的情况下 Nacos实际上是将数据存放在自带的一个嵌入式数据库中:

<img src="https://image.itbaima.net/markdown/2023/03/06/Fuxq9Dl3rGfnTZA.png"/>

而这种模式只适用于单节点 在多节点集群模式下 肯定是不能各存各的 所以 Nacos提供了MySQL统一存储支持
我们只需要让所有的Nacos服务器连接MySQL进行数据存储即可 官方也提供好了SQL文件

现在就可以开始了 第一步 我们直接导入数据库即可 文件在conf目录中:

<img src="https://image.itbaima.net/markdown/2023/03/06/97suBpfdeF54rc2.png"/>

我们来将其导入到数据库 可以看到生成了很多的表:

<img src="https://image.itbaima.net/markdown/2023/03/06/cf76RJ9VUiQBlje.png"/>

然后我们来创建两个Nacos服务器 做一个迷你的集群 这里使用scp命令将nacos服务端上传到Linux服务器(注意需要提前安装好JRE8或更高版本的环境):

<img src="https://image.itbaima.net/markdown/2023/03/06/RW4JIBKVXSbG3lZ.png"/>

解压之后 我们对其配置文件进行修改 首先是application.properties配置文件 修改以下内容 包括MySQL服务器的信息

```properties
                        ### Default web server port:
                        server.port=8801
                        
                        #*************** Config Module Related Configurations ***************#
                        ### If use MySQL as datasource:
                        spring.datasource.platform=mysql
                        
                        ### Count of DB:
                        db.num=1
                        
                        ### Connect URL of DB:
                        db.url.0=jdbc:mysql://cloudstudy.mysql.cn-chengdu.rds.aliyuncs.com:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
                        db.user.0=nacos
                        db.password.0=nacos
```

然后修改集群配置 这里需要重命名一下:

<img src="https://image.itbaima.net/markdown/2023/03/06/2pe51dHQsJkPVY7.png"/>

端口记得使用内网IP地址:

<img src="https://image.itbaima.net/markdown/2023/03/06/5CbEGQ7rX2StUkR.png"/>

最后我们修改一下Nacos的内存分配以及前台启动 直接修改startup.sh文件(内存有限 玩不起高的):

<img src="https://image.itbaima.net/markdown/2023/03/06/kQF3lN24vcBqzDi.png"/>

保存之后 将nacos复制一份 并将端口修改为8802 接着启动这两个Nacos服务器

<img src="https://image.itbaima.net/markdown/2023/03/06/PQYi69aKZUXrNlJ.png"/>

然后我们打开管理面板 可以看到两个节点都已经启动了:

<img src="https://image.itbaima.net/markdown/2023/03/06/Lbf14V39SCdghvO.png"/>

这样 我们第二步就完成了 接着我们需要添加一个SLB 这里我们用Nginx做反向代理:

    Nginx(engine x)是一个高性能的HTTP和反向代理web服务器 同时也提供了IMAP/POP3/SMTP服务
    它相当于在内网与外网之间形成了一个网关 所有的请求都可以由Nginx服务器转交给内网的其它服务器

这里我们直接安装:

```shell
                      sudo apt install nginx
```

可以看到直接请求80端口之后得到 表示安装成功:

<img src="https://image.itbaima.net/markdown/2023/03/06/gVuMlAXcY34Ka2C.png"/>

现在我们需要让其代理我们刚刚启动的两个Nacos服务器 我们需要对其进行一些配置 配置文件位于/etc/nginx/nginx.conf 添加以下内容:

```editorconfig
                        # 添加我们在上游刚刚创建好的两个nacos服务器
                        upstream nacos-server {
                                      server 10.0.0.12:8801;
                                      server 10.0.0.12:8802;
                        }
                        
                        server {
                                      listen    80;
                                      server_name   1.14.121.107;
                                      location /nacos {
                                                  proxy-pass http://nacos-server;
                                      }
                        }
```

重启Nginx服务器 成功连接:

<img src="https://image.itbaima.net/markdown/2023/03/06/2hrxcizHPvSq8be.png"/>

然后我们将所有的服务全部修改为云服务器上Nacos的地址 启动试试看

<img src="https://image.itbaima.net/markdown/2023/03/06/gdh43ciamLnBRFV.png"/>

这样 我们就搭建好了Nacos集群
