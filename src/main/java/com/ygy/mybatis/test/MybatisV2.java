package com.ygy.mybatis.test;

import com.ygy.mybatis.entity.User;
import com.ygy.mybatis.framework.config.Configuration;
import com.ygy.mybatis.framework.config.MappedStatement;
import com.ygy.mybatis.framework.config.ParameterMapping;
import com.ygy.mybatis.framework.sqlsource.SqlSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * 1.properties配置文件升级为XML配置文件
 * 2.使用面向过程思维去优化代码
 * 3.使用面向对象思维去理解配置文件封装的类的作用
 * 
 * @author 灭霸詹
 *
 */

public class MybatisV2 {

    private Configuration configuration;
    private String namespace;
    private boolean isDynamic = false;

    // 根据用户参数（不止一个），查询用户信息集合
    @Test
    public void test(){
        // 加载XML文件（全局配置文件和映射文件）
        loadXML("mybatis-config.xml");

        // 执行查询
        Map<String,Object> param = new HashMap<>();
        param.put("username","千年老亚瑟");
        param.put("sex","男");
        List<User> users = selectList("test.queryUserByParams",param);
        System.out.println(users);
    }

    private void loadXML(String location) {
        configuration = new Configuration();
        // TODO 解析XML文件，最终将信息封装到Configuration对象中
        // 获取全局配置文件对应的流对象
        InputStream is = getResourceAsStream(location);
        // 获取Document对象
        Document document = getDocument(is);
        // 根据xml语义进行解析
        parseConfiguration(document.getRootElement());
    }

    /**
     *
     * @param rootElement <configuration>
     */
    private void parseConfiguration(Element rootElement) {
        Element environments = rootElement.element("environments");
        parseEnvironments(environments);
        Element mappers = rootElement.element("mappers");
        parseMappers(mappers);
    }

    /**
     * 解析全局配置文件中的mappers标签
     * @param mappers <mappers></mappers>
     */
    private void parseMappers(Element mappers) {
        List<Element> list = mappers.elements("mapper");
        for (Element element : list) {
            String resource = element.attributeValue("resource");
            // 根据xml的路径，获取对应的输入流
            InputStream inputStream = getResourceAsStream(resource);
            // 将流对象，转换成Document对象
            Document document = getDocument(inputStream);
            // 针对Document对象，按照Mybatis的语义去解析Document
            parseMapper(document.getRootElement());
        }
    }

    /**
     * 解析映射文件的mapper信息
     * @param rootElement <mapper></mapper>
     */
    private void parseMapper(Element rootElement) {
        // statementid是由namespace+statement标签的id值组成的。
        namespace = rootElement.attributeValue("namespace");
        // TODO 获取动态SQL标签，比如<sql>
        // TODO 获取其他标签
        List<Element> selectElements = rootElement.elements("select");
        for (Element selectElement : selectElements) {
            parseStatementElement(selectElement);
        }
    }

    /**
     * 解析映射文件中的select标签
     * @param selectElement <select></select>
     */
    private void parseStatementElement(Element selectElement) {
        String statementId = selectElement.attributeValue("id");

        if (statementId == null || statementId.equals("")) {
            System.out.println("select id不能为空");
            return;
        }
        // 一个CURD标签对应一个MappedStatement对象
        // 一个MappedStatement对象由一个statementId来标识，所以保证唯一性
        // statementId = namespace + "." + CRUD标签的id属性
        statementId = namespace + "." + statementId;

        // 注意：parameterType参数可以不设置也可以不解析
      /*  String parameterType = selectElement.attributeValue("parameterType");
        Class<?> parameterClass = resolveType(parameterType);*/

        String resultType = selectElement.attributeValue("resultType");
        Class<?> resultClass = resolveType(resultType);

        String statementType = selectElement.attributeValue("statementType");
        statementType = statementType == null || statementType == "" ? "prepared" : statementType;

        //TODO SqlSource和SqlNode的封装过程
        SqlSource sqlSource = createSqlSource(selectElement);

        // TODO 建议使用构建者模式去优化
        MappedStatement mappedStatement = new MappedStatement(statementId, resultClass, statementType,
                sqlSource);
        configuration.addMappedStatement(statementId, mappedStatement);
    }

    private SqlSource createSqlSource(Element selectElement) {
        //TODO 其他子标签的解析处理

        SqlSource sqlSource = parseScriptNode(selectElement);

        return sqlSource;
    }

    private SqlSource parseScriptNode(Element selectElement) {
        //解析所有SQL节点，最终封装到MixedSqlNode中
        SqlNode mixedSqlNode = parseDynamicTags(selectElement);

        SqlSource sqlSource;
        //如果带有${}或者动态SQL标签
        if (isDynamic){
            sqlSource = new DynamicSqlSource(mixedSqlNode);
        }else{
            sqlSource = new RawSqlSource(mixedSqlNode);
        }
        return sqlSource;
    }

    private SqlNode parseDynamicTags(Element selectElement) {
        List<SqlNode> sqlNodes = new ArrayList<>();

        //获取select标签的子元素 ：文本类型或者Element类型
        int nodeCount = selectElement.nodeCount();
        for (int i = 0; i < nodeCount; i++) {
            Node node = selectElement.node(i);
            if (node instanceof Text){
                String text = node.getText();
                if (text==null){
                    continue;
                }
                if ("".equals(text.trim())){
                    continue;
                }
                // 先将sql文本封装到TextSqlNode中
                TextSqlNode textSqlNode = new TextSqlNode(text.trim());
                if (textSqlNode.isDynamic()){
                    sqlNodes.add(textSqlNode);
                    isDynamic = true;
                }else{
                    sqlNodes.add(new StaticTextSqlNode(text.trim()));
                }

            }else if (node instanceof Element){
                isDynamic = true;
                Element element  = (Element) node;
                String name = element.getName();

                if ("if".equals(name)){
                    String test = element.attributeValue("test");
                    //递归去解析子元素
                    SqlNode sqlNode = parseDynamicTags(element);

                    IfSqlNode ifSqlNode = new IfSqlNode(test,sqlNode);
                    sqlNodes.add(ifSqlNode);
                }else{
                    // TODO
                }
            }else{
                //TODO
            }
        }           //Java是最好的语言
        return new MixedSqlNode(sqlNodes);
    }


    /**
     * 根据全限定名获取Class对象
     * @param parameterType
     * @return
     */
    private Class<?> resolveType(String parameterType) {
        try {
            Class<?> aClass = Class.forName(parameterType);
            return aClass;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseEnvironments(Element environments) {
        String aDefault = environments.attributeValue("default");
        List<Element> elements = environments.elements("environment");
        for (Element element : elements) {
            String id = element.attributeValue("id");
            if (aDefault.equals(id)){
                parseDataSource(element.element("dataSource"));
            }
        }
    }

    private void parseDataSource(Element dataSource) {
        String type = dataSource.attributeValue("type");
        if (type.equals("DBCP")){
            BasicDataSource ds = new BasicDataSource();
            Properties properties = parseProperties(dataSource);
            ds.setDriverClassName(properties.getProperty("db.driver"));
            ds.setUrl(properties.getProperty("db.url"));
            ds.setUsername(properties.getProperty("db.username"));
            ds.setPassword(properties.getProperty("db.password"));

            configuration.setDataSource(ds);
        }
    }

    private Properties parseProperties(Element dataSource) {
        Properties properties = new Properties();
        List<Element> list = dataSource.elements("property");
        for (Element element : list) {
            String name = element.attributeValue("name");
            String value = element.attributeValue("value");
            properties.put(name,value);
        }
        return properties;
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

    private InputStream getResourceAsStream(String location) {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }

    private <T> List<T> selectList(String statementId, Object param) {
        List<T> results = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try{
            // 获取statement相关的信息MappedStatement
            MappedStatement mappedStatement = configuration.getMappedStatementById(statementId);
            // 连接的获取
            connection = getConnection();
            // TODO SQL的获取(SqlSource和SqlNode的处理流程)
            SqlSource sqlSource = mappedStatement.getSqlSource();
            // 触发SqlSource和SqlNode的解析处理流程
            BoundSql boundSql = sqlSource.getBoundSql(param);
            String sql = boundSql.getSql();
            // 创建statement
            statement = createStatement(mappedStatement,sql,connection);
            // TODO 设置参数
            setParameters(param,statement,boundSql);
            // 执行statement
            rs = executeQuery(statement);

            // 处理结果
            handleResult(rs,mappedStatement,results);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 释放资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }

    private <T> void handleResult(ResultSet rs, MappedStatement mappedStatement, List<T> results)  throws Exception{
        // 遍历查询结果集
        Class clazz = mappedStatement.getResultTypeClass();

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

                // 通过反射给指点列对应的属性名称赋值
                // 列名和属性名要一致
                Field field = clazz.getDeclaredField(columnName);
                // 暴力破解，破坏封装，可以访问私有成员
                field.setAccessible(true);
                field.set(result,rs.getObject(columnName));
            }
            results.add((T) result);
        }
    }

    private ResultSet executeQuery(Statement statement)  throws Exception{
        ResultSet rs = null;
        if (statement instanceof PreparedStatement){
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            // 向数据库发出 sql 执行查询，查询出结果集
            rs = preparedStatement.executeQuery();
        }

        return rs;
    }

    private void setParameters(Object param, Statement statement, BoundSql boundSql)  throws Exception{
        if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;

            // 设置参数，第一个参数为 sql 语句中参数的序号（从 1 开始），第二个参数为设置的
            // preparedStatement.setObject(1, param);
            // 如果入参是简单类型，那么我们不关心参数名称
            if (param instanceof Integer || param instanceof String) {
                preparedStatement.setObject(1, param);
            } else if (param instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) param;

                // TODO 需要解析#{}之后封装的参数集合List<ParameterMapping>
                List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
                for (int i = 0; i < parameterMappings.size(); i++) {
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                    String name = parameterMapping.getName();
                    Object value = map.get(name);
                    // 给map集合中的参数赋值
                    preparedStatement.setObject(i + 1, value);
                }

                // map集合中的key和要映射的参数名称要一致
            } else {
                //TODO
            }
        }
    }

    private Statement createStatement(MappedStatement mappedStatement, String sql, Connection connection)  throws Exception{
        String statementType = mappedStatement.getStatementType();
        if ("prepared".equals(statementType)){
            return connection.prepareStatement(sql);
        }else{
            //TODO
        }
        return null;
    }

    private String getSql(MappedStatement mappedStatement) {
        return null;
    }

    private Connection getConnection() throws Exception{
        DataSource dataSource = configuration.getDataSource();
        return dataSource.getConnection();
    }

    // 暗号：天王盖地虎
}
