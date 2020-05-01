# mybatis学习笔记

1.自定义持久层框架

###### 思路：

使用端（项目）：引入自定义的持久层框架jar包

提供的信息包括：数据库配置文件、sql配置文件、sql语句、入参类型、出参类型

（1）sqlMapConfig.xml 数据库配置文件 存放各种mapper.xml的路径

（2）XXXMapper.xml sql配置文件



自定义持久层框架（工程）：对JDBC代码进行封装（工厂模式、创建者模式）

（1）加载配置文件：根据配置文件的路径，将配置文件转成字节流存储在内存中

创建Resource类 方法：InputStream getResourceAsStream(String path)

（2）创建javaBean 存放配置文件解析出来的内容

Configuration 目的存放sqlMapConfig.xml解析出来的信息（核心配置类）

MappedStatement 目的存放XXXMapper.xml sql解析出来的信息（映射配置类）

（3）解析配置文件 利用dom4J

创建SqlSessionFactoryBuilder类 方法：build(InputSteam in)

第一：使用dom4J解析出来的内容封装在容器对象中

第二：创建SqlSessionFactory对象，生产sqlSession（会话对象）

（4）创建SqlSessionFactory接口、DefaultSqlSessionFactory实现类

方法：openSession() 生产s q lSession

（5）创建SqlSession接口、DefaultSqlSession实现类

CRUD操作：selectList()、selectOne()、update()、delete()

（6）创建Executor接口、SimpleExecutor实现类

query(Configuration,MappedStatement,Object... params)：执行的就是JDBC代码



2.mybatis相关概念、基本使用

mybatis优势：mybatis是一个半自动的持久层框架、开发人员可以优化sql

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">--默认指定的环境名称
    <environment id="development">--当前的环境名称
      <transactionManager type="JDBC"/>--指定事务管理类型为JDBC
      <dataSource type="POOLED">--指定当前数据源类型是连接池
        <property name="driver" value="com.mysql.jdbc.Driver"/>--加载驱动类
        <property name="url" value="jdbc:mysql:///test"/>--数据库url
        <property name="username" value="root"/>--数据库用户名
        <property name="password" value="root"/>--数据库密码
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="com/lagou/mapper/UserMapper.xml"/>--指定sql配置文件路径
  </mappers>
</configuration>
```

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.interface.IUserDao">--命名空间（和标签id组成statementId）、接口全限定名称
  <select id="findAll" resultType="com.lagou.domain.User">--查询标签（包括：insert、update、delete）、接口方法名；resultType：放回的结果对应的实体类
    select * from User
  </select>
</mapper>
```



3.mybatis配置文件深入

resultType自动映射、如果返回的字段和实体类不一致可以使用resultMap指定映射关系

可以把数据库连接信息提到单独的配置文件jdbc.properties，在sql配置文件中使用properties标签加载jdbc.properties

mybatis已经设置好了一些别名：

string String

long Long

int Integer

double Double

boolean Boolean

也可在核心配置文件用<packgage>标签指定所有sql配置文件

4.mybatis复杂映射关系、注解开发

常用sql配置文件标签：<where>、<if>、<foreach>、<association>(一对一)、<collection>(一对多、多对多)

```
<resultMap id="userRoleMap" type="com.lagou.domain.User">
    <result column="id" property="id"></result>
    <result column="username" property="username"></result>
    <result column="password" property="password"></result>
    <result column="birthday" property="birthday"></result>
    <collection property="roleList" ofType="com.lagou.domain.Role">
        <result column="rid" property="id"></result>
        <result column="rolename" property="rolename"></result>
    </collection>
</resultMap>
```

注解标签：@results：代替的是resultMap、@result：代替的是id、result标签、@one一对一、@many一对多、多对多

```
@Select("select * from orders")
    @Results({
            @Result(id=true,property = "id",column = "id"),
            @Result(property = "ordertime",column = "ordertime"),
            @Result(property = "total",column = "total"),
            @Result(property = "user",column = "uid",
                    javaType = User.class,
                    one = @One(select =
"com.lagou.mapper.UserMapper.findById"))
```



5.mybatis缓存

一级缓存：sqlSession级别、底层是一个map、cud操作或调用clearCache()清空缓存

一级缓存是SqlSession级别的缓存。在操作数据库时需要构造sqlSession对象，在对象中有一个数据结构（HashMap）用于存储缓存数据。不同的sqlSession之间的缓存数据区域（HashMap）是互相不影响的

二级缓存：mapper级别、可以跨sqlsession调用

二级缓存是mapper级别的缓存，多个SqlSession去操作同一个Mapper的sql语句，多个SqlSession可以共用二级缓存，二级缓存是跨SqlSession的

对于一级缓存由于不同sqlSession实例之间相互隔离，则可能出现其中一个更新了数据库数据，但是另外一个由于使用了自身内部的缓存，故读取到失效的旧数据；对于二级缓存，由所有sqlSession实例共享，基于namespace隔离，故如果不同namespace定义了同时操作一个表的SQL语句，则会造成不同namespace之间的缓存不一致问题。

6.mybatis插件

主要是对Executor 执行器、StatementHandler sql语法构建器->ParamterHandler 参数处理器、ResultSetHandler 结果集处理器进行拦截

插件机制：为目标对象创建一个代理对象（AOP），通过代理对象就可以拦截到四大对象的每一个执行

@Intercepts({

@Signature(type = StatementHandler.class,//指定接口
 method = "prepare",//指定方法
 args = { Connection.class, Integer.class}),//入参

```
})
public class MyPlugin implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
```

public Object intercept(Invocation invocation) throws Throwable {

//每次执行操作都会进入这个方法增强逻辑 

System.out.println("...."); return invocation.proceed(); //

}

/**

@Description   主要是把这个拦截器生成一个代理放到拦截器链中

@Param target 要拦截的目标对象

@Return 返回代理对象
 */

@Override

public Object plugin(Object target) { System.out.println(""+target);

return Plugin.wrap(target,this);
    }

/**  插件初始化调用只调用一次 插件配置的属性**/

@Override
 public void setProperties(Properties properties) {

System.out.println(""+properties); }

}

sql配置文件中需要配置插件

```
<plugins>
    <plugin interceptor="com.lagou.plugin.MySqlPagingPlugin">
```

<!--配置的参数-->

```
        <property name="name" value="Bob"/>
    </plugin>
```

</plugins>

通用mapper

```
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper</artifactId>
    <version>3.1.2</version>
</dependency>
```

sql配置文件中需要配置插件

<plugins>

<!--  如果有通用接口 分页插件要放到通用mapper之前

<plugin interceptor="com.github.pagehelper.PageHelper">
    <property name="dialect" value="mysql"/>
</plugin>

--!>

​            <plugin
interceptor="tk.mybatis.mapper.mapperhelper.MapperInterceptor">

<!-- 如果有多个通用mapper接口用逗号,隔开--!> 

<property name="mappers"
value="tk.mybatis.mapper.common.Mapper"/>
            </plugin>
</plugins>