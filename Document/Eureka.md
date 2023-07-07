### Eureka注册中心
官方文档: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/

`小贴士`: 各位小伙伴在学习的过程中觉得有什么疑惑的可以直接查阅官方文档 我们会在每一个技术开始之前贴上官方文档的地址 方便各位进行查阅
同时在我们的课程中并不一定完完整整地讲完整个框架的内容 有关详细的功能和使用方法文档中也是写的非常清楚的 感兴趣的可以深入学习哦

### 微服务项目结构(单体 Thymeleaf)
现在我们重新设计一下之前的图书管理系统项目 将原有的小型项目进行拆分(注意项目拆分 一定要尽可能保证单一职责)
相同的业务不要在多个微服务中重复出现 如果出现需要借助其它业务完成的服务 那么可以使用服务之间相互调用的形式来实现(之后会介绍):
- 登录验证服务: 用于处理用户注册、登录、密码重置等 反正就是一切与账户相关的内容 包括用户信息获取等
- 图书管理服务: 用于进行图书添加、删除、更新等操作 图书管理相关的服务 包括图书的存储等和信息获取
- 图书借阅服务: 交互性比较强的服务 需要和登陆验证服务和图书管理服务进行交互

那么既然将单体应用拆分为多个小型服务 我们就需要重新设计一下整个项目目录结构 这里我们就创建多个子项目每一个子项目都是一个服务
这样由父项目统一管理依赖 就无需每子项目都去单独管理依赖了 也更方便一点

我们首先创建一个普通的SpringBoot项目:

<img src="https://fast.itbaima.net/2023/03/06/8qH2jhtfvacbXMw.png"/>

然后不需要勾选任何依赖 直接创建即可 项目创建完成并初始化后 我们删除父工程的无用文件 只保留必要文件(看个人) 像下面这样:

<img src="https://fast.itbaima.net/2023/03/06/DobOruSEf3PKYyI.png"/>

接着我们就可以按照我们划分的服务 进行子工程创建了 创建一个新的Maven项目 注意父项目要指定为我们一开始创建的的项目 子项目命名随意:

<img src="https://fast.itbaima.net/2023/03/06/mFvb6c34pILHfn7.png"/>

子项目创建好之后 接着我们在子项目中创建SpringBoot的启动主类:

<img src="https://fast.itbaima.net/2023/03/06/fo4FYOqe3vxSAZc.png"/>

接着我们点击运行 即可启动子项目了 实际上这个子项目就一个最简单的SpringBoot web项目 注意启动之后最下方有弹窗
我们点击"使用 服务" 这样我们就可以实时查看当前整个大项目中有哪些微服务了

<img src="https://fast.itbaima.net/2023/03/06/gf4iAMnUwvjR2WX.png"/>

<img src="https://fast.itbaima.net/2023/03/06/C1jah7NwT4GgJdX.png"/>

接着我们以同样的方法 创建其他的子项目 注意我们最好将其他子项目的端口设置得不一样 不然会导致端口占用 我们分别为它们创建application.yml文件:

<img src="https://fast.itbaima.net/2023/03/06/E9x27lSeOfMhrHt.png"/>

接着我们来尝试启动一下这三个服务 正常情况下都是可以直接启动的:

<img src="https://fast.itbaima.net/2023/03/06/nM4ld9jyKzVT3Y5.png"/>

可以看到它们分别运行在不同的端口上 这样 就方便不同的程序员编写不同的服务了 提交当前项目代码时的冲突率也会降低

接着我们来创建一下数据库 这里还是老样子 创建三个表即可 当然实际上每个微服务单独使用一个数据库服务器也是可以的
因为按照单一职责服务只会操作自己对应的表 这里由于比较穷😥 就只用一个数据库演示了:

<img src="https://fast.itbaima.net/2023/03/06/YewLSsGbTj8aykE.png"/>

<img src="https://fast.itbaima.net/2023/03/06/KcRX57MGWVLlNqT.png"/>

<img src="https://fast.itbaima.net/2023/03/06/XjmGn3DbqVKk6Cd.png"/>

创建好之后 结果如下 一共三张表 各位可以自行添加一些数据到里面 这就不贴出来了:

<img src="https://fast.itbaima.net/2023/03/06/ac76NjHY5byeknP.png"/>

如果各位嫌麻烦的话可以下载.sql文件自行导入

接着我们来稍微写一点业务 比如用户信息查询业务 我们先把数据库相关的依赖进行导入 这里依然使用Mybatis框架 首先在父项目中添加MySQL驱动和Lombok依赖:

                    <dependency>
                        <groupId>mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                    </dependency>
                    
                    <dependency>
                         <groupId>org.projectlombok</groupId>
                         <artifactId>lombok</artifactId>
                    </dependency>

由于不是所有的子项目都需要用到Mybatis 我们在父项目中只进行版本管理即可:

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.mybatis.spring.boot</groupId>
                                <artifactId>mybatis-spring-boot-starter</artifactId>
                                <version>2.2.0</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

接着我们就可以在用户服务子项目中添加此依赖了:

                    <dependency>
                        <groupId>org.mybatis.spring.boot</groupId>
                        <artifactId>mybatis-spring-boot-starter</artifactId>
                    </dependency>

接着添加数据源信息(这里用到是阿里云的MySQL云数据库 各位注意修改一下数据库地址):

                    spring:
                        datasource:
                            driver-class-name: com.mysql.cj.jdbc.Driver
                            url: jdbc:mysql://cloudstudy.mysql.cn-chengdu.rds.aliyuncs.com:3306/cloudstudy
                            username: test
                            password: 123456

接着我们来写用户查询相关的业务:

                    @Data
                    public class User {
                    
                        Integer uid;
                        String name;
                        String age;
                        String sex;
                    
                    }

                    @Mapper
                    public interface UserMapper {
                    
                        @Select("select * from db_user where uid = #{uid}")
                        User getUserById(int uid);
                    
                    }

                    public interface UserService {
                        User getUserById(int uid);
                    }

                    @Service("userService")
                    public class UserServiceImpl implements UserService {
                    
                        @Resource
                        private UserMapper userMapper;
                        
                        @Override
                        public User getUserById(int uid) {
                            return userMapper.getUserById(uid);
                        }
                    
                    }

                    @RestController
                    public class UserController {
                    
                        @Resource
                        private UserService userService;
                        
                        @GetMapping("/api/user/{uid}")
                        public User findUserById(@PathVariable("uid") Integer uid) {
                            return userService.getUserById(uid);
                        }

                    }
    
现在我们访问即可拿到数据:

<img src="https://fast.itbaima.net/2023/03/06/SC9MUQxdzPmcZij.png"/>

同样的方式 我们完成一下图书查询业务 注意现在是在图书管理微服务中编写(别忘了导入Mybatis依赖以及配置数据源):

                    @Data
                    public class Book {
                    
                        Integer bid;
                        String title;
                        String desc;
                    
                    }

                    @Mapper
                    public interface BookMapper {
                    
                        @Select("select * from db_book where bid = #{bid}")
                        Book getBookById(Integer bid);
                    
                    }

                    @Service("bookService")
                    public class BookServiceImpl implements BookService {
                    
                        @Resource
                        private BookMapper bookMapper;
                    
                        @Override
                        public Book getBookById(Integer bid) {
                            return bookMapper.getBookById(bid);
                        }
                    
                    }

                    @RestController
                    public class BookController {
                    
                        @Resource
                        private BookService bookService;
                    
                        @GetMapping("/api/book/{bid}")
                        public Book findBookById(@PathVariable Integer bid) {
                            return bookService.getBookById(bid);
                        }
                    
                    }

同样进行一下测试:

<img src="https://fast.itbaima.net/2023/03/06/x9ZOvniJcSp5Cf7.png"/>

这样 我们一个完整的项目就拆分成了多个微服务 不同微服务之间是独立进行开发和部署的

### 服务间调用
前面我们完成了用户信息查询和图书信息查询 现在我们来接着完成借阅服务

借阅服务是一个关联性比较强的服务 它不仅仅需要查询 同时可能还需要获取借阅信息下的详细信息 比如具体那个用户借阅了哪本书
并且用户和书籍的详情也需要同时出现 那么这种情况下 我们根据需要去访问除了借阅表以外的用户表和图书表

<img src="https://fast.itbaima.net/2023/03/06/T3zKfpqmYkD9VxI.png"/>

但是这显然是违反我们之前所说的单一职责的 相同的业务功能不应该重复出现 但是现在又需要在此服务中查询用户的信息和图书信息 那怎么办呢? 我们可以让一个服务去调用另一个服务来获取信息

<img src="https://fast.itbaima.net/2023/03/06/jBtM2k7CFZsu4cU.png"/>

这样 图书管理服务和用户管理微服务相当于借阅记录 就形成了一个生产者和消费者的关系 前者是生产者 后者便是消费者

现在我们先将借阅关联信息查询完善了:

                    @Data
                    public class Borrow {
                    
                        Integer id;
                        Integer uid;
                        Integer bid;
                    
                    }

                    @Mapper
                    public interface BorrowMapper {

                        @Select("select id,bid,uid from db_borrow where uid = #{uid}")
                        List<Borrow> getBorrowByUid(Integer uid);

                        @Select("select id,bid,uid from db_borrow where bid = #{bid}")
                        List<Borrow> getBorrowByBid(Integer bid);

                        @Select("select id,bid,uid from db_borrow where bid = #{bid} and uid = #{uid}")
                        Borrow getBorrow(Integer uid, Integer bid);

                    }

现在有一个需求 需要查询用户的借阅详细信息 也就是说需要查询某个用户具体借了那些书 并且需要此用户的信息和所有已借阅的书籍信息一起返回 那么我们先来设计一下返回实体:

                    @Data
                    @AllArgsConstructor
                    public class UserBorrowDetail {
                    
                        private User user;
                        private List<Book> bookList;
                    
                    }

但是有一个问题 我们发现User和Book实体实际上是在另外两个微服务中定义的 相当于当前项目并没有定义这些实体类 那么怎么解决呢?

我们可以将所有服务需要用到的实体类单独放入另一个项目中 然后这些项目引用集中存放实体类的那个项目 这样就可以保证每个微服务的实体类信息都可以共用了:

<img src="https://fast.itbaima.net/2023/03/06/QbfRFayHmqpLVdE.png"/>

然后只需要在对应的类中引用此项目作为依赖即可:

                    <dependency>
                        <groupId>com.example</groupId>
                        <artifactId>commons</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                    </dependency>

之后新的公共实体类都可以在commons项目中进行定义了 现在我们接着来完成刚刚的需求 先定义接口:

                    public interface BorrowService {

                        UserBorrowDetail getUserBorrowDetailByUid(Integer uid);
                    
                    }

                    @Service("borrowService")
                    public class BorrowServiceImpl implements BorrowService {
                    
                        @Resource
                        private BorrowMapper borrowMapper;
                    
                        @Override
                        public UserBorrowDetail getUserBorrowDetailByUid(Integer uid) {
                    
                            List<Borrow> borrow = borrowMapper.getBorrowByUid(uid);
                            // 那么问题来了 现在拿到借阅关联信息了 怎么调用其它服务获取信息呢?
                            
                        }
                    
                    }

需要进行服务远程调用我们需要用到RestTemplate来进行:

                    @Service("borrowService")
                    public class BorrowServiceImpl implements BorrowService {
                    
                        @Resource
                        private BorrowMapper borrowMapper;
                    
                        @Override
                        public UserBorrowDetail getUserBorrowDetailByUid(Integer uid) {
                    
                            List<Borrow> borrow = borrowMapper.getBorrowByUid(uid);
                            // RestTemplate支持多种方式的远程调用
                            RestTemplate template = new RestTemplate();
                            // 这里通过调用getForObject来请求其它服务 并将结果自动进行封装
                            // 获取User信息
                            User user = template.getForObject("http://localhost:8082/api/user/" + uid, User.class);
                            // 获取每一本书的详细信息
                            List<Book> bookList = borrow.stream()
                                    .map(b -> template.getForObject("http://localhost:8080/api/book/" + b.getBid(), Book.class))
                                    .collect(Collectors.toList());
                            return new UserBorrowDetail(user, bookList);
                    
                        }
                    
                    }

现在我们再最后完善一下Controller:

                    @RestController
                    public class BorrowController {
                    
                        @Resource
                        private BorrowService borrowService;
                    
                        @GetMapping("/api/borrow/{uid}")
                        public UserBorrowDetail getUserBorrowDetailByUid(@PathVariable("uid") Integer uid) {
                            return borrowService.getUserBorrowDetailByUid(uid);
                        }
                    
                    }

在数据库中添加一点借阅信息 测试看看能不能正常获取(注意: 一定要保证三个服务都处于开启状态 否则远程调用会失败):

<img src="https://fast.itbaima.net/2023/03/06/OPw5XMghApNrWKe.png"/>

可以看到 结果正常 没有问题 远程调用成功

这样 一个简易的图书管理系统的分布式项目就搭建完成了 这里记得把整个项目压缩打包备份一下 下一章学习SpringCloud Alibaba也需要进行配置

### 服务注册与发现
前面我们了解了如何对单体应用进行拆分 并且也学习了如何进行服务之间的相互调用 但是存在一个问题 就是虽然服务拆分完成 但是没有一个比较合理的管理机制 如果单纯只是这样编写
在部署和维护起来 肯定是很麻烦的 可以想象一下 如果某一天这些微服务的端口或是地址大规模地发生改变 我们就不得不将服务之间的调用路径大规模的同步进行修改 这是多么可怕的事情
我们需要消弱这种服务之间的强关联性 因此我们需要一个集中管理微服务的平台 这时就要借助我们这一部分的主角了

Eureka能够自动注册并发现微服务 然后对服务的状态、信息进行集中管理 这样当我们需要获取其他服务的信息时 我们只需要向Eureka进行查询就可以了

<img src="https://fast.itbaima.net/2023/03/06/A2mxhZ5jBkPrOdc.png">

像这样的话 服务之间的强关联性就会被进一步削弱

那么现在我们就来搭建一个Eureka服务器 只需要创建一个新的Maven项目即可 然后我们需要在父工程中添加一下SpringCloud的依赖
这里选用2021.0.1版本(Spring Cloud 最新的版本命名方式变更了 现在是 YEAR.x 这种命名方式 具体可以在官网查看：https://spring.io/projects/spring-cloud#learn)

                    <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-dependencies</artifactId>
                        <version>2021.0.1</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>

接着我们为新创建的项目添加依赖:

                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.cloud</groupId>
                            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
                        </dependency>
                    </dependencies>











