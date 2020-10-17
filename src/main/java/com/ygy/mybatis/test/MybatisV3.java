package com.ygy.mybatis.test;/*
package com.kkb.mybatis.test;

import com.kkb.mybatis.factory.KKBSqlSessionFactory;
import com.kkb.mybatis.builder.KKBSqlSessionFactoryBuilder;
import com.kkb.mybatis.io.KKBResources;
import com.kkb.mybatis.po.User;
import com.kkb.mybatis.sqlsession.KKBSqlSession;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

*/
/**
 * 1.以面向对象的思维去改造mybatis手写框架 2.将手写的mybatis的代码封装一个通用的框架（java工程）给程序员使用
 *
 * @author 灭霸詹
 *
 *//*

public class MybatisV3 {

    // 提取出来，用于公共调用的
    private KKBSqlSessionFactory sqlSessionFactory;

    @Before
    public void before(){
        // 根据配置文件路径，获取对应的流
        String location = "mybatis-config.xml";
        InputStream inputStream = KKBResources.getResourceAsStream(location);

        // 通过SqlSessionFactoryBuilder去创建SqlSessionFactory，需要注入获取到的流对象
        sqlSessionFactory = new KKBSqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void test(){
        // 规定selectUserList方法的参数只有 两个。
        Map param = new HashMap();
        param.put("username","123");
        param.put("sex","男");

        // 调用公共方法，查询用户信息
        KKBSqlSession sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList("test.queryUserByParams",param);

        System.out.println(users);
    }
}
*/
