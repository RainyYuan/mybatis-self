package com.kkb.spring.test.v17;


import com.kkb.spring.dao.UserDaoImpl;
import com.kkb.spring.ioc.BeanDefinition;
import com.kkb.spring.ioc.PropertyValue;
import com.kkb.spring.ioc.RuntimeBeanReference;
import com.kkb.spring.ioc.TypedStringValue;
import com.kkb.spring.po.User;
import com.kkb.spring.service.UserService;
import com.kkb.spring.service.UserServiceImpl;
import com.sun.org.apache.regexp.internal.REUtil;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BEncoderStream;
import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用面向过程思维和配置文件的方式去实现容器化管理Bean
 */
public class TestSpringV1702 {

    // 存储单例Bean实例的Map容器
    private Map<String,Object> singletonObjects = new HashMap<>();

    // 存储BeanDefinition的容器
    private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();

    @Before
    public void beforse(){
        // TODO XML解析
        InputStream is =   TestSpringV1702.class.getClassLoader().getResourceAsStream("beans.xml");
        // 获取Document对象
        Document document = getDocument(is);
        // 根据xml语义进行解析
        parseConfiguration(document.getRootElement());
    }
    private Document getDocument(InputStream is) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(is);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void parseConfiguration(Element rootElement) {
        List<Element> sonElements = rootElement.elements();
        parseBeans(sonElements);
    }

    private void parseBeans(List<Element> sonElements) {
        for(Element bean:sonElements){
            if(bean.getName().equals("bean")){
                //解析bean标签
                parseBeanDefinition(bean);
            }else{
                //解析其他标签
            }


        }
    }

    private void parseBeanDefinition(Element bean) {
        String beanName = bean.attributeValue("id");
        String clazzName = bean.attributeValue("class");
        //将bean的基本信息封装成一个BeanDefinition
        BeanDefinition thisBeanDefinition= new BeanDefinition(clazzName,beanName);
        String scope = bean.attributeValue("scope");
        if(scope == null ){
            scope = "singleton";
        }
        thisBeanDefinition.setScope(scope);

        //解析属性字段
        List<Element> propertys = bean.elements("property");
        List<PropertyValue> propertyValues = thisBeanDefinition.getPropertyValues();
        for(Element property:propertys){
            String name = property.attributeValue("name");
            //判断是否是基本类型 还是引用类型
            String value = property.attributeValue("value");
            String ref = property.attributeValue("ref");

            if(value!=null){
                Class<?> targetType = resoleTargetType(thisBeanDefinition,name);//反射获取属性的类别
                TypedStringValue typedStringValue = new TypedStringValue(value);
                typedStringValue.setTargetType(targetType);
                propertyValues.add(new PropertyValue(name,typedStringValue));
            }else if(ref!=null){
                propertyValues.add(new PropertyValue(name,new RuntimeBeanReference(ref)));
            }
            beanDefinitions.put(beanName,thisBeanDefinition);
        }
    }

    private Class<?> resoleTargetType(BeanDefinition thisBeanDefinition,String fieidName) {

        try {
            Class<?> aClass = Class.forName(thisBeanDefinition.getClazzName());
            Field declaredField = aClass.getDeclaredField(fieidName);
            declaredField.setAccessible(true);
            return declaredField.getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /****************************************   BeamDefinition -> Bean   ***********************************************************/
    @Test
    public void test(){
        // A 程序员他其实只想使用业务对象去调用对应的服务
        // B 程序员编写了一段代码给A程序员提供对象
//        UserService userService = getUserService();
        UserService userService = (UserService) getBean("userService");

        //实现用户查询功能
        Map<String, Object> map = new HashMap<>();
        map.put("username","袁");

        List<User> users = userService.queryUsers(map);
        System.out.println(users);
    }

    // B程序员
    public UserService getUserService(){
        UserServiceImpl userService = new UserServiceImpl();
        UserDaoImpl userDao = new UserDaoImpl();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://39.105.204.66:3306/kkb?characterEncoding=utf-8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        userDao.setDataSource(dataSource);
        userService.setUserDao(userDao);
        return userService;
    }

    // C程序员
//    public Object getBean(String beanName){
//        if ("userService".equals(beanName)){
//            UserServiceImpl userService = new UserServiceImpl();
//            UserDaoImpl userDao = new UserDaoImpl();
//            BasicDataSource dataSource = new BasicDataSource();
//            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//            dataSource.setUrl("jdbc:mysql://39.105.204.66:3306/kkb?characterEncoding=utf-8");
//            dataSource.setUsername("root");
//            dataSource.setPassword("root");
//            userDao.setDataSource(dataSource);
//            userService.setUserDao(userDao);
//            return userService;
//        }// ...
//        return null;
//    }




    // D程序员
    // 解决的是扩展性问题：通过配置的方式去解决扩展性问题
    // 使用XML配置文件进行Bean的创建
    // 1.管理要new出来的bean的class信息（要new几个对象，就需要配置几个class信息）
    // <bean id="bean的唯一name" class="要new的对象的全路径"></bean>
    // 2.管理要new出来的bean的属性的依赖关系（如果A对象依赖了B对象，那么这两个对象都要配置class信息，并且要确定关系）
    // <bean id="bean的唯一name" class="要new的对象的全路径">
    //      <property name="属性名称" ref="要建立关系的另一个bean的唯一name"/>
    // </bean>
    // 3.读取静态的信息，去创建对象
    // BeanDefinition类--->用来存储<bean>标签中的信息
    // Map<String,BeanDefinition>
    // 4.利用反射从BeanDefinition中获取class信息，区创建对象
    public Object getBean(String beanName){
        // 1.首先从singletonObjects集合中获取对应beanName的实例
        Object singletonObject = this.singletonObjects.get(beanName);
        // 2.如果有对象，则直接返回
        if (singletonObject != null){
            return singletonObject;
        }
        // 3.如果没有改对象，则获取对应的BeanDefinition信息
        BeanDefinition beanDefinition = this.beanDefinitions.get(beanName);
        // 4.判断是单例还是多例，如果是单例，则走单例创建Bean流程
//        String scope = beanDefinition.getScope();
//        if ("singleton".equals(scope)){
//
//        }else if ("prototype".equals(scope)){
//
//        }
        if (beanDefinition.isSingleton()){
            singletonObject = doCreateBean(beanDefinition) ;

            this.singletonObjects.put(beanName,singletonObject);
        }else if(beanDefinition.isPrototype()){
            singletonObject = doCreateBean(beanDefinition) ;
        }
        // 5.单例流程中，需要将创建出来的Bean放入singletonObjects集合中
        // 6.如果是多例，走多例的创建Bean流程

        return singletonObject;
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        // 1.Bean的实例化
        Object bean = createBeanByConstructor(beanDefinition);
        // 2.Bean的属性填充（依赖注入）
        populateBean(bean,beanDefinition);
        // 3.Bean的初始化
        initilizeBean(bean,beanDefinition);
        return bean;
    }

    private void initilizeBean(Object bean, BeanDefinition beanDefinition) {
        // TODO 需要针对Aware接口标记的类进行特殊处理

        // TODO 可以进行IntilizingBean接口的处理
        invokeInitMethod(bean,beanDefinition);
    }

    private void invokeInitMethod(Object bean, BeanDefinition beanDefinition) {
        try {
            String initMethod = beanDefinition.getInitMethod();
            if (initMethod == null) {
                return;
            }
            Class<?> clazzType = beanDefinition.getClazzType();
            Method method = clazzType.getDeclaredMethod(initMethod);
            method.setAccessible(true);
            method.invoke(bean);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void populateBean(Object bean, BeanDefinition beanDefinition) {
        List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue pv : propertyValues) {
            String name = pv.getName();
            // 这不是我们需要给Bean设置的value值
            Object value = pv.getValue();
            Object valueToUse = resoleValue(value);

            setProperty(bean,name,valueToUse);
        }
    }

    private void setProperty(Object bean,String name,Object valueToUse){
        try {
            Class<?> aClass = bean.getClass();
            Field field = aClass.getDeclaredField(name);
            field.setAccessible(true);
            field.set(bean,valueToUse);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object resoleValue(Object value) {
        if (value instanceof TypedStringValue){
            TypedStringValue typedStringValue = (TypedStringValue) value;
            Class<?> targetType = typedStringValue.getTargetType();
            String stringValue = typedStringValue.getValue();
            if (targetType == Integer.class){
                return Integer.parseInt(stringValue);
            }else if (targetType == String.class){
                return stringValue;
            }else{
                //TODO 其他类型
                return stringValue;
            }

        //引用类型
        }else if (value instanceof RuntimeBeanReference){
            RuntimeBeanReference beanReference = (RuntimeBeanReference) value;
            String ref = beanReference.getRef();
            // 递归调用
            return getBean(ref);
        }
        return null;
    }

    private Object createBeanByConstructor(BeanDefinition beanDefinition) {
        // TODO 静态工厂方法、工厂实例方法

        // 构造器方式去创建Bean实例
        try {
            Class<?> clazzType = beanDefinition.getClazzType();
            // 选择无参构造器
            Constructor<?> constructor = clazzType.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 暗号：班班很漂亮

}
