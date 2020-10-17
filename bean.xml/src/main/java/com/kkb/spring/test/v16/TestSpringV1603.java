package com.kkb.spring.test.v16;

import org.junit.Before;
import org.junit.Test;

/**
 * 使用面向对象思维去实现容器化管理Bean
 */
public class TestSpringV1603 {

//    private DefaultListableBeanFactory beanFactory;

    @Before
    public void before(){
        //完成XML解析，其实就是完成BeanDefinition的注册
        // XML解析，解析的结果，放入beanDefinitions中
//        String location = "beans.xml";
//        // 获取流对象
//        Resource resource = new ClasspathResource(location);
//        InputStream inputStream = resource.getResource();
//
//        beanFactory = new DefaultListableBeanFactory();
//
//        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
//        beanDefinitionReader.registerBeanDefinitions(inputStream);
    }

    @Test
    public void test(){
//        UserService userService = (UserService) beanFactory.getBean("userService");
//        //实现用户查询功能
//        Map<String, Object> map = new HashMap<>();
//        map.put("username","王五");
//        List<User> users = userService.queryUsers(map);
//        System.out.println(users);
    }
}
