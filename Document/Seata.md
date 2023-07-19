<img src="	https://fast.itbaima.net/2023/03/06/8OCeNap2Vy6X7WH.png"/>

### Seata与分布式事务
重难点内容 坑也多得离谱 最好保持跟我一样的版本 官方文档: https://seata.io/zh-cn/docs/overview/what-is-seata.html

在前面的阶段中 我们学习过事务 还记得我们之前谈到的数据库事务的特性吗?
- 原子性: 一个事务(transaction)中的所有操作 要么全部完成 要么全部不完成 不会结束在中间某个环节 事务在执行过程中发生错误 会被回滚(Rollback)到事务开始前的状态 就像这个事务从来没有执行过一样
- 一致性: 在事务开始之前和事务结束以后 数据库的完整性没有被破坏 这表示写入的资料必须完全符合所有的预设规则 这包含资料的精确度, 串联型以及后续数据库可以自发行地完成预设的工作
- 隔离性: 数据库允许多个并发事务同时对其数据进行读写和修改的能力 隔离性可以防止多个事务并发执行时由于交叉执行而导致数据的不一致 事务隔离分为不同级别 包括读未提交(Read uncommitted), 读已提交(read committed), 可重复读(repeatable read)和串行化(Serializable)
- 持久性: 事务处理结束后 对数据的修改就是永久的 即便系统故障也不会丢失

那么各位试想一下 在分布式环境下 有可能出现这样一个问题 比如我们下单购物 那么整个流程可能是这样的:
先调用库存服务对库存进行减扣 -> 然后订单开始下单 -> 最后用户服务进行扣款 虽然看似是一个很简单的一个流程 但是如果没有事务的加持
很有可能会由于中途出错 比如整个流程中订单服务出现问题 那么就会导致库存扣了 但是实际上这个订单并没有生成 用户也没有付款

<img src="https://fast.itbaima.net/2023/03/06/AiEXC3wBflPxHGT.png"/>

上面这种情况时间就是一种多服务多数据源的分布式事务模型(比较常见) 因此 为了解决这种情况 我们就得实现分布式事务 让这整个流程保证原子性

SpringCloud Alibaba为我们提供了用于处理分布式事务的组件Seata

<img src="https://fast.itbaima.net/2023/03/06/jDAy7osQ5YIqruP.png"/>

seata 是一款开源的分布式事务解决方案 致力于提供高性能和简单易用的分布式事务服务 Seata将为用户提供了AT, TCC, SAGA 和 XA事务模式为用户打造一站式的分布式解决方案

实际上 就是多了一个中间人来协调所有服务的事务

### 项目环境搭建
这里我们对之前的图书管理系统进行升级:
- 每个用户最多只能同时借阅2本不同的书
- 图书馆中所有的书都有3本
- 用户借书流程: 先调用图书服务书籍数量-1 -> 添加借阅记录 -> 调用用户服务用户可借阅数量-1

那么首先我们对数据库进行修改 这里为了简便 就直接在用户表中添加一个字段用于存储用户能够借阅的书籍数量:

<img src="https://fast.itbaima.net/2023/03/06/TvJL2PiWFU4XoaZ.png"/>

然后修改书籍信息 也是直接添加一个字段用于记录剩余数量:

<img src="https://fast.itbaima.net/2023/03/06/WjEcGbtkNrZi1CL.png"/>

接着我们去编写一下对应的服务吧 首先是用户服务:

```java
                    @Mapper
                    public interface UserMapper {
                    
                        @Select("select uid,name,age,sex from db_user where uid = #{uid}")
                        User getUserById(int uid);
                        
                        @Select("select can_borrow from db_user where uid = #{uid}")
                        int getUserBookRemain(Integer uid);
                        
                        @Update("update db_user set can_borrow = #{count} where uid = #{uid}")
                        int updateBookCount(int uid, int count);
                    
                    }
```
```java
                    @Service("userService")
                    public class UserServiceImpl implements UserService {
                    
                        @Resource
                        private UserMapper userMapper;
                    
                        @Override
                        public User getUserById(int uid) {
                            return userMapper.getUserById(uid);
                        }
                    
                        @Override
                        public int getRemain(int uid) {
                            return userMapper.getUserBookRemain(uid);
                        }
                    
                        @Override
                        public boolean setRemain(int uid, int count) {
                            return userMapper.updateBookCount(uid, uid) > 0;
                        }
                    
                    }
```
```java
                    @RestController
                    public class UserController {
                    
                        @Resource
                        private UserService userService;
                    
                        private int userCallCount = 0;
                    
                        @GetMapping("/user/{uid}")
                        public User findUserById(@PathVariable("uid") Integer uid) {
                    
                            int count = userCallCount++; System.err.println("调用了用户服务" + count + "次");
                            return userService.getUserById(uid);
                    
                        }
                        
                        @GetMapping("/user/remain/{uid}")
                        public int userRemain(@PathVariable("uid") int uid) {
                            return userService.getRemain(uid);
                        }
                        
                        @GetMapping("/user/borrow/{uid}")
                        public boolean userBorrow(@PathVariable("uid") int uid) {
                            
                            int remain = userService.getRemain(uid);
                            return userService.setRemain(uid, remain - 1);
                            
                        }
                    
                    }
```

然后是图书服务 其实跟用户服务差不多:

```java
                    @Mapper
                    public interface BookMapper {
                    
                        @Select("select bid,title,`desc` from db_book where bid = #{bid}")
                        Book getBookById(Integer bid);
                    
                        @Select("select count from db_book where bid = #{bid}")
                        int getRemain(int bid);
                    
                        @Update("update db_book set count = #{count} where bid = #{bid}")
                        int setRemain(int bid, int count);
                    
                    }
```
```java
                    @Service("bookService")
                    public class BookServiceImpl implements BookService {
                    
                        @Resource
                        private BookMapper bookMapper;
                    
                        @Override
                        public Book getBookById(Integer bid) {
                            return bookMapper.getBookById(bid);
                        }
                    
                        @Override
                        public int getRemain(int bid) {
                            return bookMapper.getRemain(bid);
                        }
                    
                        @Override
                        public boolean setRemain(int bid, int count) {
                            return bookMapper.getRemain(bid) > 0;
                        }
                    
                    }
```
```java
                    @RestController
                    public class BookController {
                    
                        @Resource
                        private BookService bookService;
                    
                        private int BookCount = 0;
                    
                        @GetMapping("/book/{bid}")
                        public Book findBookById(@PathVariable Integer bid) {
                    
                            int count = BookCount++; System.err.println("调用图书服务" + count + "次");
                            return bookService.getBookById(bid);
                    
                        }
                    
                        @GetMapping("/book/remain/{bid}")
                        public int bookRemain(@PathVariable("bid") int bid) {
                            return bookService.getRemain(bid);
                        }
                    
                        @GetMapping("/book/borrow/{bid}")
                        public boolean bookBorrow(@PathVariable("bid") int bid) {
                    
                            int remain = bookService.getRemain(bid);
                            return bookService.setRemain(bid, remain - 1);
                    
                        }
                    
                    }
```

最后完善我们的借阅服务:

```java
                    @FeignClient(value = "user-service")
                    public interface UserClient {
                    
                        @GetMapping("/api/user/{uid}")
                        User getUserById(@PathVariable("uid") Integer uid);
                    
                        @GetMapping("/api/user/remain/{uid}")
                        int userRemain(@PathVariable("uid") int uid);
                        
                        @GetMapping("/api/user/borrow/{uid}")
                        boolean userBorrow(@PathVariable("uid") int uid);
                        
                    }
```
```java
                    @FeignClient(value = "book-service")
                    public interface BookClient {
                    
                        @GetMapping("/api/book/{bid}")
                        Book getBookById(@PathVariable("bid") Integer bid);
                        
                        @GetMapping("/api/book/remain/{bid}")
                        int bookRemain(@PathVariable("bid") int bid);
                        
                        @GetMapping("/api/book/borrow/{bid}")
                        boolean bookBorrow(@PathVariable("bid") int bid);
                    
                    }
```
```java
                    @RestController
                    public class BorrowController {
                    
                        @Resource
                        BorrowService service;
                    
                        @RequestMapping("/borrow/{uid}")
                        UserBorrowDetail findUserBorrows(@PathVariable("uid") int uid){
                            return service.getUserBorrowDetailByUid(uid);
                        }
                    
                        @RequestMapping("/borrow/take/{uid}/{bid}")
                        JSONObject borrow(@PathVariable("uid") int uid,
                                          @PathVariable("bid") int bid){
                            
                            service.doBorrow(uid, bid);
                    
                            JSONObject object = new JSONObject();
                            object.put("code", "200");
                            object.put("success", false);
                            object.put("message", "借阅成功！");
                            return object;
                            
                        }
                        
                    }
```
```java
                    @Service
                    public class BorrowServiceImpl implements BorrowService{
                    
                        @Resource
                        BorrowMapper mapper;
                        @Resource
                        UserClient userClient;
                        @Resource
                        BookClient bookClient;
                    
                        @Override
                        public UserBorrowDetail getUserBorrowDetailByUid(int uid) {
                            
                            List<Borrow> borrow = mapper.getBorrowsByUid(uid);
                            User user = userClient.getUserById(uid);
                            List<Book> bookList = borrow
                                    .stream()
                                    .map(b -> bookClient.getBookById(b.getBid()))
                                    .collect(Collectors.toList());
                            return new UserBorrowDetail(user, bookList);
                            
                        }
                    
                        @Override
                        public boolean doBorrow(int uid, int bid) {
                            
                            // 1. 判断图书和用户是否都支持借阅
                            if(bookClient.bookRemain(bid) < 1) throw new RuntimeException("图书数量不足");
                            if(userClient.userRemain(uid) < 1) throw new RuntimeException("用户借阅量不足");
                            // 2. 首先将图书的数量-1
                            if(!bookClient.bookBorrow(bid)) throw new RuntimeException("在借阅图书时出现错误！");
                            // 3. 添加借阅信息
                            if(mapper.getBorrow(uid, bid) != null) throw new RuntimeException("此书籍已经被此用户借阅了！");
                            if(mapper.addBorrow(uid, bid) <= 0) throw new RuntimeException("在录入借阅信息时出现错误！");
                            // 4. 用户可借阅-1
                            if(!userClient.userBorrow(uid)) throw new RuntimeException("在借阅时出现错误！");
                            // 完成
                            return true;
                            
                        }
                        
                    }
```

这样 只要我们的图书借阅过程中任何一步出现问题 都会抛出异常

我们来测试一下:

<img src="https://fast.itbaima.net/2023/03/06/MPkZb1dA2Khjcty.png"/>

在次尝试借阅 后台会直接报错:

<img src="https://fast.itbaima.net/2023/03/06/H43Fy9z76LIvJGd.png"/>

抛出异常 但是我们发现一个问题 借阅信息添加失败了 但是图书的数量依然被-1 也就是说正常情况下 我们是希望中途出现异常之后 之前的操作全部回滚的:

<img src="https://fast.itbaima.net/2023/03/06/l9D8aXBxkvnZejw.png"/>

而这里由于是在另一个服务中进行的数据库操作 所以传统的@Transactional注解无效 这时就得借助Seata提供分布式事务了

### 分布式事务解决方案
要开始实现分布式事务 我们得先从理论开始下手 我们来了解一下常用的分布式事务解决方案:

1. `XA分布式事务协议 - 2PC(两阶段提交实现)`
   这里的PC实际上指的是Prepare和Commit 也就是说它分为两个阶段 一个是准备一个是提交 整个过程的参与者一共有两个角色
   一个是事务的执行者 一个是事务的协调者 实际上整个分布式事务的运作都需要依靠协调性来维持:

   <img src="https://fast.itbaima.net/2023/03/06/BWiUzFrjHAao1kJ.png"/>
   
   在准备和提交阶段 会进行:
   - 准备阶段: 一个分布式事务是由协调者来开启的 首先协调者会向所有的事务执行者发送事务内容 等待所有的事务执行者答复
              各个事务执行者开始执行事务操作 但是不进行提交 并将undo和redo信息记录到事务日志中
              如果事务执行者执行事务成功 那么就告诉协调者成功Yes 否则告诉协调者失败No 不能提交事务

   - 提交阶段: 当所有的执行者都反馈之后 进入第二个阶段
              协调者会检测各个执行者的反馈内容 如果所有的执行者都返回成功 那么就告诉所有的执行者可以提交事务了
              最后再释放所资源 如果有至少一个执行者返回失败或是超时 那么就让所有的执行者都回滚 分布式事务执行失败

   虽然这种方式看起来比较简单 但是存在以下几个问题:
   - 事务协调者是非常核心的角色 一旦出现问题 将导致整个分布式事务不能正常运行
   - 如果提交阶段发生网络问题 导致某些事务执行者没有受到协调者发来的提交命令 将导致某些执行者提交某些执行者没提交 这样肯定是不行的


2. `XA分布式事务协议 - 3PC(三阶段提交实现)`
   三阶段提交是在二阶段提交基础上的改进版本 主要是加入了超时机制 同时在协调者和执行者中都引入了超时机制

   三个阶段分别进行:
   - CanCommit阶段: 协调者向执行者发送CanCommit请求 询问是否可以执行事务提交操作 然后开始等待执行者的响应
                    执行者接收到请求之后 正常情况下 如果其自身认为可以顺利执行事务 则返回Yes响应 并进入预备状态 否则返回No
   
   - PreCommit阶段: 协调者根据执行者的反应情况来决定是否可以进入第二阶段事务的PreCommit操作
                    如果所有的执行者都返回Yes 则协调者向所有执行者发送PreCommit请求 并进入Prepared阶段 执行者接收到请求后
                    会执行事务操作 并将undo和redo信息记录到事务日志中 如果成功执行 则返回成功响应
   
   - DoCommit阶段: 该阶段进行真正的事务提交
                   协调者接收到所有执行者发送的成功响应 那么它将从PreCommit状态进入到DoCommit状态 并向所有执行者发送DoCommit请求 执行者接收到doCommit请求之后 开始执行事务提交 并在完成事务提交之后释放所有事务资源
                   并最后向协调者发送确认响应 协调者接收到所有执行者的确认响应之后 完成事务(如果因为网络问题导致执行者没有收到doCommit请求 执行者会在超时之后直接提交事务
                   虽然执行者只是猜测协调者返回的是DoCommit请求 但是因为前面的两个流程都正常执行 所以能够在一定程度上认为本次事务是成功的 因此会直接提交)
                   协调者没有接收至少一个执行者发送的成功响应(也可能是响应超时)那么就会执行中断事务 协调者会向所有执行者发送abort请求 执行者接收到abort请求之后 利用其在PreCommit阶段记录的undo信息来执行事务的回滚操作
                   并在完成回滚之后释放所有的事务资源 执行者完成事务回滚之后 向协调者发送确认消息 协调者接收到参与者反馈的确认消息之后 执行事务的中断

   相比两阶段提交 三阶段提交的优势是显而易见的 当然也有缺点:
   - 3PC在2PC的第一阶段和第二阶段中传入一个准备阶段 保证了最后提交阶段之前各参与节点的状态是一致的
   - 一旦参与者无法及时收到来自协调者的信息之后 会默认执行Commit 这样就不会因为协调者单方面的故障导致全局出现问题
   - 但是我们知道 实际上超时之后的Commit决策本质上就是一个赌注罢了 如果此时协调者发送的是abort请求但是超时未接收 那么就会直接导致数据一致性问题


3. `TCC(补偿事务)`
    补偿事务TCC就是Try, Confirm, Cancel 它对业务有入侵性 一共分为三个阶段 我们依次来解读一下:
    - Try阶段: 比如我们需要在借书时 将书籍的库存-1 并且用户的借阅量也-1 但是这个操作 除了直接对库存和借阅量进行修改之外
               还需要将减去的值 单独存放到冷冻表中 但是此时不会创建借阅信息 也就是说只是预先把关键的东西给处理了 预留业务资源出来
   
    - Confirm阶段: 如果Try执行成功 那么就进入到Confirm阶段 接着之前 我们就该创建借阅信息了 只能使用Try阶段预留的业务资源
                   如果创建成功 那么就对Try阶段冻结的值 进行解冻 整个流程就完成了 当然 如果失败了 那么进入到Cancel阶段
   
    - Cancel阶段: 不用猜了 那肯定是把冻结的东西还给人家 因为整个借阅操作压根就没成功 就像你款买了东西但是网络问题 导致交易失败 那么进入到Cancel阶段


跟XA协议相比 TCC就没有协调者这一角色的参与了 而是自主通过上一阶段的执行情况来确保正常 充分利用了集群的优势 性能也是有很大的提升
但是缺点也很明显 它与业务具有一定的关联性 需要开发者去编写更多的补偿代码 同时并不一定所有的业务流程都适用于这种形式

### Seata机制简介
前面我们了解了一些分布式事务的解决方案 那么我们来看一下Seata是如何进行分布式事务的处理的

<img src="https://fast.itbaima.net/2023/03/06/LsUq3AvrfhQJPCz.png"/>

官网给出的是这样的一个架构图 那么图中的RM, TM, TC代表着什么意思呢?
- RM(Transaction Coordinator): 用于直接执行本地事务的提交和回滚
- TM(Transaction Participant): TM是分布式事务的核心管理者 比如现在我们需要在借阅服务中开启全局事务 来让其自身, 图书服务, 用户服务都参与进来 也就是说一般全局事务发起者就是TM
- TC(Transaction Log Storage) 这个就是我们的Seata服务器 用于全局控制 比如在XA模式下就是一个协调者的角色 而一个分布式事务的启动就是由TM向TC发起请求 TC再来与其他的RM进行协调操作


    TM请求TC开启一个全局事务 TC会生成一个XID作为该全局事务的编号 XID会在微服务的调用链路中传播 保证将多个微服务的子事务关联在一起
    RM请求TC将本地事务注册为全局事务的分支事务 通过全局事务的XID进行关联 TM请求TC告诉XID对应的全局事务是进行提交还是回滚 TC驱动RM将XID对应的自己的本地事务进行提交还是回滚

Seata支持4种事务模式 官网文档: https://seata.io/zh-cn/docs/overview/what-is-seata.html
- AT: 本质上就是2PC的升级版 在AT模式下 用户只需关心自己的"业务SQL"
1. 一阶段 Seata会拦截"业务SQL" 首先解析SQL语义 找到"业务SQL"要更新的业务数据 在业务数据被更新前 将其保存成"before image"
   最后生成行锁 以上操作全部在一个数据库事务内完成 这样保证了一阶段操作的原子性
2. 二阶段如果确认提交的话 因为"业务SQL"在一阶段已经提交至数据库 所以Seata框架只需将一阶段保存的快照数据和行锁删掉 完成数据清理即可 当然如果需要回滚 那么就用"before image"还原业务数据
   
   
- TCC: 
- XA: 
- Sage: 















