package com.kkb.spring.test.v16;

import com.kkb.spring.dao.UserDaoImpl;
import com.kkb.spring.po.User;
import com.kkb.spring.service.UserServiceImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSpringV1601 {
    // 由A程序员编写
    @Test
    public void test(){
        UserServiceImpl userService = new UserServiceImpl();
        UserDaoImpl userDao = new UserDaoImpl();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.211.133:3306/kkb");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        userDao.setDataSource(dataSource);
        userService.setUserDao(userDao);

        //实现用户查询功能
        Map<String, Object> map = new HashMap<>();
        map.put("username","王五");
        List<User> users = userService.queryUsers(map);
        System.out.println(users);
    }
}
