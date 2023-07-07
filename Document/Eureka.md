### Eureka注册中心
官方文档: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/

`小贴士`: 各位小伙伴在学习的过程中觉得有什么疑惑的可以直接查阅官方文档 我们会在每一个技术开始之前贴上官方文档的地址 方便各位进行查阅
同时在我们的课程中并不一定完完整整地讲完整个框架的内容 有关详细的功能和使用方法文档中也是写的非常清楚的 感兴趣的可以深入学习哦

### 微服务项目结构
现在我们重新设计一下之前的图书管理系统项目 将原有的小型项目进行拆分(注意项目拆分 一定要尽可能保证单一职责)
相同的业务不要在多个微服务中重复出现 如果出现需要借助其它业务完成的服务 那么可以使用服务之间相互调用的形式来实现(之后会介绍):
- 登录验证服务: 用于处理用户注册、登录、密码重置等 反正就是一切与账户相关的内容 包括用户信息获取等
- 图书管理服务: 用于进行图书添加、删除、更新等操作 图书管理相关的服务 包括图书的存储等和信息获取
- 图书借阅服务: 交互性比较强的服务 需要和登陆验证服务和图书管理服务进行交互

那么既然将单体应用拆分为多个小型服务 我们就需要重新设计一下整个项目目录结构 这里我们就创建多个子项目每一个子项目都是一个服务
这样由父项目统一管理依赖 就无需每子项目都去单独管理依赖了 也更方便一点

我们首先创建一个普通的SpringBoot项目:

<img src="https://fast.itbaima.net/2023/03/06/8qH2jhtfvacbXMw.png">

然后不需要勾选任何依赖 直接创建即可 项目创建完成并初始化后 我们删除父工程的无用文件 只保留必要文件(看个人) 像下面这样:

<img src="https://fast.itbaima.net/2023/03/06/DobOruSEf3PKYyI.png">

接着我们就可以按照我们划分的服务 进行子工程创建了 创建一个新的Maven项目 注意父项目要指定为我们一开始创建的的项目 子项目命名随意:

<img src="https://fast.itbaima.net/2023/03/06/mFvb6c34pILHfn7.png">

子项目创建好之后 接着我们在子项目中创建SpringBoot的启动主类:

<img src="https://fast.itbaima.net/2023/03/06/fo4FYOqe3vxSAZc.png">

接着我们点击运行 即可启动子项目了 实际上这个子项目就一个最简单的SpringBoot web项目 注意启动之后最下方有弹窗
我们点击"使用 服务" 这样我们就可以实时查看当前整个大项目中有哪些微服务了

<img src="https://fast.itbaima.net/2023/03/06/gf4iAMnUwvjR2WX.png">

<img src="https://fast.itbaima.net/2023/03/06/C1jah7NwT4GgJdX.png">

接着我们以同样的方法 创建其他的子项目 注意我们最好将其他子项目的端口设置得不一样 不然会导致端口占用 我们分别为它们创建application.yml文件:

<img src="https://fast.itbaima.net/2023/03/06/E9x27lSeOfMhrHt.png">

接着我们来尝试启动一下这三个服务 正常情况下都是可以直接启动的:

<img src="https://fast.itbaima.net/2023/03/06/nM4ld9jyKzVT3Y5.png">

可以看到它们分别运行在不同的端口上 这样 就方便不同的程序员编写不同的服务了 提交当前项目代码时的冲突率也会降低

接着我们来创建一下数据库 这里还是老样子 创建三个表即可 当然实际上每个微服务单独使用一个数据库服务器也是可以的
因为按照单一职责服务只会操作自己对应的表 这里由于比较穷😥 就只用一个数据库演示了:

<img src="https://fast.itbaima.net/2023/03/06/YewLSsGbTj8aykE.png">

<img src="https://fast.itbaima.net/2023/03/06/KcRX57MGWVLlNqT.png">

<img src="https://fast.itbaima.net/2023/03/06/XjmGn3DbqVKk6Cd.png">

创建好之后 结果如下 一共三张表 各位可以自行添加一些数据到里面 这就不贴出来了:

<img src="https://fast.itbaima.net/2023/03/06/ac76NjHY5byeknP.png">

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






