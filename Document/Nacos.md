<img src="https://fast.itbaima.net/2023/03/06/pQkSrLx9NZRn8Ub.png"/>

### Nacos 更加全能的注册中心
Nacos(Naming Configuration Service)是一款阿里巴巴开源的服务注册与发现, 配置管理的组件 相当于是Eureka+Config的组合形态

### 安装与部署
Nacos服务器是独立安装部署的 因此我们需要下载最新的Nacos服务端程序 下载地址: https://github.com/alibaba/nacos

<img src="https://fast.itbaima.net/2023/03/06/VStPIABaXxMp2N9.png"/>

可以看到目前最新的版本是1.4.3版本(2022年2月27日发布的) 我们直接下载zip文件即可

接着我们将文件进行解压 得到以下内容:

<img src="https://fast.itbaima.net/2023/03/06/wWbuXRGizrQCT8J.png"/>

我们直接将其拖入到项目文件夹下 便于我们一会在IDEA内部启动 接着添加运行配置:

<img src="https://fast.itbaima.net/2023/03/06/bM8doEZPth7DHfe.png"/>

其中-m standalone表示单节点模式 Mac和Linux下记得将解释器设定为/bin/bash 由于Nacos在Mac/Linux默认是后台启动模式
我们修改一下它的bash文件 让它变成前台启动 这样IDEA关闭了Nacos就自动关闭了 否则开发环境下很容易忘记关:

```bash
                    # 注释掉 nohup $JAVA ${JAVA_OPT} nacos.nacos >> ${BASE_DIR}/logs/start.out 2>&1 &
                    # 替换成下面的
                    $JAVA ${JAVA_OPT} nacos.nacos
```

接着我们点击启动:

<img src="https://fast.itbaima.net/2023/03/06/O3pMSvDbxPKYT5q.png"/>

OK 启动成功 可以看到它的管理页面地址也是给我们贴出来了: http://localhost:8848/nacos/index.html 访问这个地址

默认的用户名和管理员密码都是nacos 直接登陆即可 可以看到进入管理页面之后功能也是相当丰富:

<img src="https://fast.itbaima.net/2023/03/06/dom3WpJsiajgCE7.png"/>

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

<img src="https://fast.itbaima.net/2023/03/06/9PLBGOXoaERnUwM.png"/>

按照同样的方法 我们接着将另外两个服务也注册到Nacos中:

<img src="https://fast.itbaima.net/2023/03/06/K6VBtqEWSLnMp21.png"/>

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

<img src="https://fast.itbaima.net/2023/03/06/HIGvXAad1EOVPt6.png"/>

测试正常 可以自动发现服务 接着我们来多配置几个实例 去掉图书服务和用户服务的端口配置

<img src="https://fast.itbaima.net/2023/03/06/WZGdJ5BYpmbMuNT.png">

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

<img src="https://fast.itbaima.net/2023/03/06/GCrm8wgWXLzYhtK.png">

可以看到Nacos中的实例数量已经显示为2:

<img src="https://fast.itbaima.net/2023/03/06/p6iYrPa8e1btZkl.png">

接着我们调用借阅服务 看看能否负载均衡远程调用:

<img src="https://fast.itbaima.net/2023/03/06/jCl8RGhaIiUDBgm.png">

<img src="https://fast.itbaima.net/2023/03/06/2bWdfmnVOyGzlZr.png">

OK 负载均衡远程调用没有问题 这样我们就实现了基于Nacos的服务的注册与发现 实际上大致流程与Eureka一致

值得注意的是 Nacos区分了临时实例和非临时实例:

<img src="https://fast.itbaima.net/2023/03/06/cF5MoVX6vNnzx9j.png">

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

<img src="https://fast.itbaima.net/2023/03/06/FdRTjlKszDoOPU3.png">

如果这时我们关闭此实例 那么会变成这样:

<img src="https://fast.itbaima.net/2023/03/06/R5Jyhl29UcvuOCb.png">

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

<img src="https://fast.itbaima.net/2023/03/06/szyGRrEfZ1KWmpj.png">

因此 我们可以对部署在不同机房的服务进行分区 可以看到实例的分区是默认:

<img src="https://fast.itbaima.net/2023/03/06/wlO9dQ1NtKCxFTi.png">

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










