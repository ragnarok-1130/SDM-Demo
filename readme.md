## DEMO
#### 本地开发环境  
> IDE: Intellij IDEA  
> JDK: 1.8  
> 数据库: MySQL 8.0  
> 其他工具: Navicat

#### 数据库及表的创建
首先创建好数据库并建表  
`CREATE DATABASE test;`  
表结构如下：  

    USE test;
    DROP TABLE IF EXISTS `user`;
    CREATE TABLE `user` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
      `user_id` bigint(20) NOT NULL COMMENT '用户ID',
      `username` varchar(32) NOT NULL COMMENT '用户名',
      `password` varchar(20) NOT NULL COMMENT '密码',
      PRIMARY KEY (`id`),
      KEY `user_id_idx` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '用户表';  

#### 项目创建
创建一个项目并为其添加三个模块common-api（一般用于放置dto、api接口和工具类等）、provider和consumer，项目结构如下图所示
![项目结构]()  

![common-api]()  

![provider]()  

![consumer]()  
引入springboot、dubbo、mybatis、mysql-connector等相关依赖后开始coding  
##### common-api
首先从common-api模块开始，根据数据库表结构创建UserDto

    package com.example.common.dto;

    import lombok.Data;
    import java.io.Serializable;

    @Data    //lombok提供的注解，可自动生成getter和setter，简化代码
    public class UserDto implements Serializable {
    private Long id;
    private Long userId;
    private String username;
    private String password;
    }
创建UserService接口

    package com.example.common.intf;

    import com.example.common.dto.UserDto;

    public interface UserRpcService {
        int addUser(UserDto userDto);
        UserDto selectUser(long userId);
        int deleteUser(long userId);
        int updateUser(UserDto userDto);
    }
##### provider
从最底层mapper开始，创建UserMapper接口

    package com.example.provider.mapper;

    import com.example.common.dto.UserDto;
    import org.apache.ibatis.annotations.Mapper;
    import org.springframework.stereotype.Repository;

    @Mapper     //@Mapper注解将这个接口标记为一个映射接口,交由Spring管理
    @Repository   //@Repository注解将接口标记为bean，防止IDEA检测报红线（非强迫症可不写）
    public interface UserMapper {
        int insert(UserDto userDto);

        int update(UserDto userDto);

        int delete(long userId);

        UserDto select(long userId);
    }  
然后在resources目录下创建mapper文件夹并在其中为UserMapper添加映射文件UserMapper.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
    <mapper namespace="com.example.provider.mapper.UserMapper">

        <select id="select" resultType="com.example.common.dto.UserDto">
            SELECT * FROM `user`
            WHERE user_id=#{userId}
        </select>

        <insert id="insert" parameterType="com.example.common.dto.UserDto" useGeneratedKeys="true" keyProperty="id">
            INSERT INTO `user`(`user_id`,`username`,`password`)
            VALUES(#{userId},#{username},#{password})
        </insert>

        <update id="update" parameterType="com.example.common.dto.UserDto">
            UPDATE `user`
            SET
            `user_id`=#{userId},
            `username`=#{username},
            `password`=#{password}
            WHERE
            `id`=#{id}
        </update>

        <delete id="delete" parameterType="long">
            DELETE FROM `user` WHERE user_id=#{userId}
        </delete>

    </mapper>
mapper创建完成，再实现UserService接口  
实现类UserRpcServiceImpl代码：

    package com.example.provider.service.impl;

    import com.example.common.dto.UserDto;
    import com.example.common.intf.UserRpcService;
    import com.example.provider.mapper.UserMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    @Service("userRpcService")
    public class UserRpcServiceImpl implements UserRpcService {

        @Autowired
        private UserMapper userMapper;

        @Override
        public int addUser(UserDto userDto) {
            return userMapper.insert(userDto);
        }

        @Override
        public UserDto selectUser(long userId) {
            return userMapper.select(userId);
        }

        @Override
        public int deleteUser(long userId) {
            return userMapper.delete(userId);
        }

        @Override
        public int updateUser(UserDto userDto) {
            return userMapper.update(userDto);
        }
    }
Service完成后需要在dubbo配置文件(resoureces/dubbo-provider.xml)中暴露接口

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://dubbo.apache.org/schema/dubbo
           http://dubbo.apache.org/schema/dubbo/dubbo.xsd">

        <!-- 提供方应用信息 -->
        <dubbo:application name="provider"  />

        <!-- 使用zookeeper注册中心暴露服务地址 -->
        <dubbo:registry address="zookeeper://127.0.0.1:2181" />

        <!-- 用dubbo协议在20880端口暴露服务 -->
        <dubbo:protocol name="dubbo" port="20880" />

        <!-- 声明需要暴露的服务接口 -->
        <dubbo:service interface="com.example.common.intf.UserRpcService" ref="userRpcService" />

    <!--    &lt;!&ndash; 和本地bean一样实现服务，由于已经在UserRpcServiceImpl中使用@Service声明，可省略此操作 &ndash;&gt;-->
    <!--    <bean id="userRpcService" class="com.example.provider.service.impl.UserRpcServiceImpl" />-->
    </beans>

编写启动类

    package com.example.provider;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.annotation.ImportResource;

    @SpringBootApplication
    @ImportResource(value = {"classpath:dubbo-provider.xml"})   //加载dubbo配置
    public class ProviderApplication {

        public static void main(String[] args) {
            SpringApplication.run(ProviderApplication.class, args);
            try {
                //阻塞作用，否则会由于不是web项目，执行main方法后立即停止服务。
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
application.properties配置

    mybatis.mapper-locations=classpath:mapper/**/*.xml
    mybatis.configuration.map-underscore-to-camel-case=true   //开启驼峰命名转换
    //配置数据源
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.datasource.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT
    spring.datasource.username=root
    spring.datasource.password=password

    server.port=8081

    spring.application.name=dubbo-provider  

至此dubbo服务提供者provider模块创建完成，接下来需要创建dubbo服务消费者consumer模块  

##### consumer
创建UserController

    package com.example.consumer.controller;

    import com.example.common.dto.UserDto;
    import com.example.common.intf.UserRpcService;
    import org.apache.dubbo.config.annotation.Reference;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.Map;

    @RequestMapping("/user")
    @RestController
    public class UserController {

        @Reference
        private UserRpcService userRpcService;

        @RequestMapping("/selectUser")
        public UserDto selectUser(@RequestParam Map<String,Object> params){
            try {
                long userId = Long.parseLong(params.get("userId").toString());
                return userRpcService.selectUser(userId);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @RequestMapping("/addUser")
        public String addUser(@RequestBody UserDto userDto){
            try {
                if(userRpcService.addUser(userDto)>0){
                    return "新用户注册成功！";
                }else {
                    return "注册失败！";
                }
            }catch (Exception e){
                e.printStackTrace();
                return "注册异常！";
            }
        }

        @RequestMapping("/updateUser")
        public String updateUser(@RequestBody UserDto userDto){
            try {
                if(userRpcService.updateUser(userDto)>0){
                    return "账户信息修改成功！";
                }else {
                    return "账户信息修改失败！";
                }
            }catch (Exception e){
                e.printStackTrace();
                return "账户信息修改异常！";
            }
        }

        @RequestMapping("/deleteUser")
        public String deleteUser(@RequestParam Map<String,Object> params){
            try {
                long userId = Long.parseLong(params.get("userId").toString());
                if(userRpcService.deleteUser(userId)>0){
                    return "账户注销成功！";
                }else {
                    return "账户注销失败！";
                }
            }catch (Exception e){
                e.printStackTrace();
                return "账户注销异常！";
            }
        }
    }
配置dubbo-consumer.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://dubbo.apache.org/schema/dubbo
           http://dubbo.apache.org/schema/dubbo/dubbo.xsd">

        <!-- 提供方应用信息，用于计算依赖关系 -->
        <dubbo:application name="consumer"  />

        <!-- 使用multicast广播注册中心暴露服务地址 -->
        <dubbo:registry address="zookeeper://127.0.0.1:2181" />

        <!-- 用dubbo协议在20880端口暴露服务 -->
        <dubbo:protocol name="dubbo" port="20880" />

        <dubbo:annotation package="com.example.consumer" />

    </beans>
创建启动类

    package com.example.consumer;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.annotation.ImportResource;

    @SpringBootApplication
    @ImportResource(value = {"classpath:dubbo-consumer.xml"})
    public class ConsumerApplication {
        public static void main(String[] args) {
            SpringApplication.run(ConsumerApplication.class, args);
        }
    }

application.properties配置

    server.port=8082 //与provider不相同，避免端口冲突

    spring.application.name=dubbo-consumer

#### 测试
使用postman进行接口测试  
###### addUser
![addUser]()
###### selectUser
![selectUser]()
###### updateUser
![updateUser]()
###### deleteUser
![deleteUser]()
