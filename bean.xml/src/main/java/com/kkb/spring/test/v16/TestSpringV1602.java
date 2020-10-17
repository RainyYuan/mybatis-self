package com.kkb.spring.test.v16;

import com.kkb.spring.dao.UserDaoImpl;
import com.kkb.spring.ioc.BeanDefinition;
import com.kkb.spring.ioc.PropertyValue;
import com.kkb.spring.ioc.RuntimeBeanReference;
import com.kkb.spring.ioc.TypedStringValue;
import com.kkb.spring.po.User;
import com.kkb.spring.service.UserService;
import com.kkb.spring.service.UserServiceImpl;
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
public class TestSpringV1602 {

    // 存储从XML或者注解中解析出来的BeanDefinition的Map集合
    private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();

    // 存储单例Bean的Map集合
    private Map<String,Object> singletonObjects = new HashMap<>();

    @Before
    public void before(){
        //完成XML解析，其实就是完成BeanDefinition的注册
        // XML解析，解析的结果，放入beanDefinitions中
        String location = "beans.xml";
        // 获取流对象
        InputStream inputStream = getInputStream(location);
        // 创建文档对象
        Document document = createDocument(inputStream);

        // 按照spring定义的标签语义去解析Document文档
        registerBeanDefinitions(document.getRootElement());
    }
    // 由A程序员编写
    @Test
    public void test(){
//        UserService userService = getUserService();
        UserService userService = (UserService) getBean("userService");
        //实现用户查询功能
        Map<String, Object> map = new HashMap<>();
        map.put("username","王五");
        List<User> users = userService.queryUsers(map);
        System.out.println(users);
    }
    private void registerBeanDefinitions(Element rootElement) {
        // 获取<bean>和自定义标签（比如mvc:interceptors）
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            // 获取标签名称
            String name = element.getName();
            if (name.equals("bean")) {
                // 解析默认标签，其实也就是bean标签
                parseDefaultElement(element);
            } else {
                // 解析自定义标签，比如aop:aspect标签
                parseCustomElement(element);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void parseDefaultElement(Element beanElement) {
        try {
            if (beanElement == null) {
                return;
            }
            // 获取id属性
            String id = beanElement.attributeValue("id");

            // 获取name属性
            String name = beanElement.attributeValue("name");

            // 获取class属性
            String clazzName = beanElement.attributeValue("class");
            if (clazzName == null || "".equals(clazzName)) {
                return;
            }

            // 获取init-method属性
            String initMethod = beanElement.attributeValue("init-method");
            // 获取scope属性
            String scope = beanElement.attributeValue("scope");
            scope = scope != null && !scope.equals("") ? scope : "singleton";

            // 获取beanName
            String beanName = id == null ? name : id;
            Class<?> clazzType = Class.forName(clazzName);
            beanName = beanName == null ? clazzType.getSimpleName() : beanName;
            // 创建BeanDefinition对象
            // 此次可以使用构建者模式进行优化
            BeanDefinition beanDefinition = new BeanDefinition(clazzName, beanName);
            beanDefinition.setInitMethod(initMethod);
            beanDefinition.setScope(scope);
            // 获取property子标签集合
            List<Element> propertyElements = beanElement.elements();
            for (Element propertyElement : propertyElements) {
                parsePropertyElement(beanDefinition, propertyElement);
            }

            // 注册BeanDefinition信息
            this.beanDefinitions.put(beanName, beanDefinition);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void parsePropertyElement(BeanDefinition beanDefination, Element propertyElement) {
        if (propertyElement == null)
            return;

        // 获取name属性
        String name = propertyElement.attributeValue("name");
        // 获取value属性
        String value = propertyElement.attributeValue("value");
        // 获取ref属性
        String ref = propertyElement.attributeValue("ref");

        // 如果value和ref都有值，则返回
        if (value != null && !value.equals("") && ref != null && !ref.equals("")) {
            return;
        }

        /**
         * PropertyValue就封装着一个property标签的信息
         */
        PropertyValue pv = null;

        if (value != null && !value.equals("")) {
            // 因为spring配置文件中的value是String类型，而对象中的属性值是各种各样的，所以需要存储类型
            TypedStringValue typeStringValue = new TypedStringValue(value);

            Class<?> targetType = getTypeByFieldName(beanDefination.getClazzName(), name);
            typeStringValue.setTargetType(targetType);

            pv = new PropertyValue(name, typeStringValue);
            beanDefination.addPropertyValue(pv);
        } else if (ref != null && !ref.equals("")) {

            RuntimeBeanReference reference = new RuntimeBeanReference(ref);
            pv = new PropertyValue(name, reference);
            beanDefination.addPropertyValue(pv);
        } else {
            return;
        }
    }

    private Class<?> getTypeByFieldName(String beanClassName, String name) {
        try {
            Class<?> clazz = Class.forName(beanClassName);
            Field field = clazz.getDeclaredField(name);
            return field.getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseCustomElement(Element element) {
        // AOP、TX、MVC标签的解析，都是走该流程
    }



    // 由B程序员编写
    private UserService getUserService(){
        UserServiceImpl userService = new UserServiceImpl();
        UserDaoImpl userDao = new UserDaoImpl();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.211.133:3306/kkb");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        userDao.setDataSource(dataSource);
        userService.setUserDao(userDao);

        return userService;
    }
    // 由B程序员编写
    // 这是简单工厂模式的最基本写法
    private Object getBean(String beanName){
        // beanName和bean对象的映射关系，可以考虑配置到xml文件中
        // bean对象的创建，只要知道该类的【全路径名称】，就可以通过反射去创建出来该对象
        // bean还需要依赖注入，其实可以通过反射去注入，但是需要知道要注入的对象，以及对象的属性名称，以及要注入的属性值

        // 先从单例Bean集合中去获取对应的Bean
        Object bean = this.singletonObjects.get(beanName);

        // 如果有，直接返回
        if (bean != null){
            return bean;
        }

        // 如果没有，则走创建流程
        // 需要根据beanName获取到对应的BeanDefinition对象
        BeanDefinition bd = this.beanDefinitions.get(beanName);
        if (bd == null) {
            return null;
        }
        /*   // 判断要创建的Bean是单例还是多例
        String scope = bd.getScope();
        // 单例bean创建完成之后，要存储到单例Bean集合中
        if (scope.equals("singleton")){
            bean = doCreateBean(bd);
            this.singletonObjects.put(beanName,bean);
        }else if (scope.equals("prototype")){
            bean = doCreateBean(bd);
        }*/

        // 判断要创建的Bean是单例还是多例
        // 单例bean创建完成之后，要存储到单例Bean集合中
        if (bd.isSingleton()){
            bean = doCreateBean(bd);
            this.singletonObjects.put(beanName,bean);
        }else if (bd.isPrototype()){
            // 多例（原型）bean创建完成之后，不需要存储到单例Bean集合中
            bean = doCreateBean(bd);
        }

        return bean;
    }

    private Object doCreateBean(BeanDefinition bd) {
        // 创建Bean分为三步：
        // Bean的实例化
        Object bean = createBeanInstance(bd);
        // Bean的依赖注入
        populateBean(bean,bd);
        // Bean的初始化
        initializeBean(bean,bd);

        return bean;
    }

    /**
     * 调用Bean的初始化功能
     * @param bean
     * @param bd
     */
    private void initializeBean(Object bean, BeanDefinition bd) {
        //班班很漂亮
        // TODO 初始化的时候，可以通过Aware标记接口，进行一些特殊信息的注入，比如BeanFactory的注入

        // TODO bean标签配置的Class，它是实现了InitializingBean接口，也是会初始化调用该接口的afterPropertiesSet方法

        // bean标签如果配置了init-method属性，那么属性值就是初始化方法名称
        invokeInitMethod(bean,bd);
    }

    private void invokeInitMethod(Object bean, BeanDefinition bd) {
        try{
            String initMethod = bd.getInitMethod();
            if (initMethod == null || "".equals(initMethod)){
                return;
            }
            Class<?> clazzType = bd.getClazzType();
            Method method = clazzType.getDeclaredMethod(initMethod);
            // 通过反射调用初始化方法
            method.invoke(bean);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 依赖注入
     * @param bean
     * @param bd
     */
    private void populateBean(Object bean, BeanDefinition bd) {
        List<PropertyValue> propertyValues = bd.getPropertyValues();
        for (PropertyValue pv : propertyValues) {
            // 属性名称
            String name = pv.getName();
            // 获取属性值（不能直接注入，需要处理）
            Object value = pv.getValue();

            // 将Object类型的value，转换成指定类型的value
            Object valueToUse = resoleValue(value);

            setPropertyValue(bean,name,valueToUse,bd);
        }
    }

    private void setPropertyValue(Object bean, String name, Object value, BeanDefinition bd) {
        // TODO spring中的依赖注入到底是通过属性注入还是setter方法注入
        try{
            Class<?> clazzType = bd.getClazzType();
            Field field = clazzType.getDeclaredField(name);
            field.setAccessible(true);
            field.set(bean,value);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private Object resoleValue(Object value) {
        if (value instanceof TypedStringValue){
            TypedStringValue typedStringValue = (TypedStringValue) value;
            String stringValue = typedStringValue.getValue();
            Class<?> targetType = typedStringValue.getTargetType();
            if (targetType == String.class){
                return stringValue;
            }else if (targetType == Integer.class){
                return Integer.parseInt(stringValue);
            }//....
        }else if (value instanceof RuntimeBeanReference){
            RuntimeBeanReference runtimeBeanReference = (RuntimeBeanReference) value;
            String ref = runtimeBeanReference.getRef();
            return getBean(ref);
        }
        return null;
    }

    /***
     * bean的创建（new）
     * @param bd
     * @return
     */
    private Object createBeanInstance(BeanDefinition bd) {
        // 通过工厂方法方式去创建Bean实例

        // 通过实例工厂去创建Bean实例

        // 通过构造器去创建Bean实例
        Object bean = createBeanByConstructor(bd);
        return bean;
    }

    private Object createBeanByConstructor(BeanDefinition bd) {
        try {
            Class<?> clazzType = bd.getClazzType();
            // 获取无参构造器
            Constructor<?> constructor = clazzType.getDeclaredConstructor();
            Object bean = constructor.newInstance();
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /*private Object getBean(String beanName){
        if ("userService".equals(beanName)){
            UserServiceImpl userService = new UserServiceImpl();
            UserDaoImpl userDao = new UserDaoImpl();
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://192.168.211.133:3306/kkb");
            dataSource.setUsername("root");
            dataSource.setPassword("root");
            userDao.setDataSource(dataSource);
            userService.setUserDao(userDao);
            return userService;
        }else if ("orderService".equals(beanName)){
            //TODO
        }//....


        return null;
    }*/

    private InputStream getInputStream(String location) {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }
    private Document createDocument(InputStream inputStream) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(inputStream);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
