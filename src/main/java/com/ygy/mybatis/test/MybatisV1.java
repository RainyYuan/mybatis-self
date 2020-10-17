package com.ygy.mybatis.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.ygy.mybatis.entity.Persons;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
/**
 * 解决硬编码问题（properties文件）
 * properties文件中的内容，最终会被【加载】到Properties集合中
 *
 * @author yuanguiyu
 */
public class MybatisV1 {

    //启动数据源
    static Connection connection = null;
    //配置sql
    private static Properties properties = new Properties();
    static {
        // 加载properties文件中的内容
        loadProperties("jdbc.properties");
        //连接数据源
        connection = getConnection();
    }


    // 入口
    @Test
    public void test(){

        //查询
        //List<Map<String,Object>> users = selectList("queryUserById",3);
        //System.out.println(users);

        //新增
        Persons persons = new Persons();
        persons.setId_P(3);
        persons.setLastName("2323");
        persons.setAddress("adderss");
        persons.setCity("shanghai");
        persons.setFirstName("yuan");
        insertPerson("insertPerson",persons);


    }

    private void insertPerson(String sql, Persons param) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement((String) properties.get("db.sql."+sql));
            preparedStatement.setObject(1,param.getId_P());
            preparedStatement.setObject(2,param.getLastName());
            preparedStatement.setObject(3,param.getFirstName());
            preparedStatement.setObject(4,param.getAddress());
            preparedStatement.setObject(5,param.getCity());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private static void loadProperties(String location) {
        InputStream inputStream = null;
        try {
            inputStream = MybatisV1.class.getClassLoader().getResourceAsStream(location);
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 抽取一个通用的查询方法
     * @param param 只能传递一个参数对象
     * @return
     */
    private <T> List<T> selectList(String statementId,Object param) {
        List<T> results = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            // 定义sql语句 ?表示占位符
            // String sql = "select * from user where id = ?";
            String sql = properties.getProperty("db.sql."+statementId);
            // 获取预处理 statement
            preparedStatement = connection.prepareStatement(sql);

            parseParam(statementId, param, preparedStatement);

            // 向数据库发出 sql 执行查询，查询出结果集
            rs = preparedStatement.executeQuery();

            // 遍历查询结果集
            String resultType = properties.getProperty("db.sql." + statementId + ".resulttype");
            Class<?> clazz = Class.forName(resultType);
            /*this.getClass()
            MybatisV1.class*/
            // 一般都是通过构造器去创建对象
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object result = null;
            while (rs.next()) {
                // result = clazz.newInstance();
                result = constructor.newInstance();

                // 获取结果集中列的信息
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = metaData.getColumnName(i + 1);
                    if(result instanceof Map){
                        ((Map) result).put(columnName,rs.getObject(columnName));
                    }else {
                        // 通过反射给指点列对应的属性名称赋值
                        // 列名和属性名要一致
                        // 暴力破解，破坏封装，可以访问私有成员
                        Field field = clazz.getDeclaredField(columnName);
                        field.setAccessible(true);
                        field.set(result, rs.getObject(columnName));
                    }
                }
                results.add((T) result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

    /**
     * 解析入参
     * @param statementId
     * @param param
     * @param preparedStatement
     * @throws SQLException
     */
    private void parseParam(String statementId, Object param, PreparedStatement preparedStatement) throws SQLException {
        // 设置参数，第一个参数为 sql 语句中参数的序号（从 1 开始），第二个参数为设置的
        // preparedStatement.setObject(1, param);
        // 如果入参是简单类型，那么我们不关心参数名称
        if(param instanceof Integer || param instanceof String){
            preparedStatement.setObject(1,param);
        }else if (param instanceof Map){
            Map<String ,Object> map = (Map<String, Object>) param;

            String columnnames = properties.getProperty("db.sql." + statementId + ".columnnames");
            String[] nameArray = columnnames.split(",");
            if (nameArray != null && nameArray.length > 0){
                for (int i = 0 ; i<nameArray.length;i++) {
                    String name = nameArray[i];
                    Object value = map.get(name);
                    // 给map集合中的参数赋值
                    preparedStatement.setObject(i+1,value);
                }
            }
        }else {
            //TODO
        }
    }

    private static Connection getConnection(){
        Connection connection = null;// 解决连接获取时的硬编码问题和频繁连接的问题
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(properties.getProperty("db.driver"));
        dataSource.setUrl(properties.getProperty("db.url"));
        dataSource.setUsername(properties.getProperty("db.username"));
        dataSource.setPassword(properties.getProperty("db.password"));
        try{
            connection = dataSource.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }

        return connection;
    }
}
