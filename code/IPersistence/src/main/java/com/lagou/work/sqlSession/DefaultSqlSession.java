package com.lagou.work.sqlSession;

import com.lagou.work.pojo.Configuration;
import com.lagou.work.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementId, Object... params) throws IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, SQLException, InvocationTargetException, ClassNotFoundException {

        // 将要去完成simpleExecutor里的query()方法的调用
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        List<Object> list = simpleExecutor.query(configuration, configuration.getMappedStatementMap().get(statementId), params);
        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        List<Object> objects = selectList(statementId, params);
        if (objects.size() == 1) {
            return (T) objects.get(0);
        } else {
            throw new RuntimeException("查询结果为空或者返回结果过多");
        }
    }

    @Override
    public void insertUser(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        selectList(statementId, params);
    }

    @Override
    public void updateUser(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        selectList(statementId, params);
    }

    @Override
    public void deleteUser(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        selectList(statementId, params);
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用jdk动态代理来为dao接口生成代理对象并返回
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 底层都还是去执行JDBC代码 //根据不同情况，来调用selctList或者selectOne
                // 准备参数 1：statmentid :sql语句的唯一标识：namespace.id= 接口全限定名.方法名
                String methodName = method.getName();// 方法名
                String className = method.getDeclaringClass().getName();// 类名

                // 参数1 statementId
                String statementId = className + "." + methodName;

                // 参数2 params:args

                // 获取被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                // 判断是否进行了 泛型类型参数化 是否有泛型 true是集合 false是实体类
                if (genericReturnType instanceof ParameterizedType) {
                    List<Object> objects = selectList(statementId, args);
                    return objects;
                } else {
                    MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
                    switch (mappedStatement.getSqlCommandType()) {
                        case SELECT:
                            return selectOne(statementId, args);
                        case INSERT:
                            insertUser(statementId, args);
                            return null;
                        case UPDATE:
                            updateUser(statementId, args);
                            return null;
                        case DELETE:
                            deleteUser(statementId, args);
                            return null;
                        default:
                            throw new Exception("sql配置文件有问题");
                    }
                }
            }
        });

        return (T) proxyInstance;
    }
}
