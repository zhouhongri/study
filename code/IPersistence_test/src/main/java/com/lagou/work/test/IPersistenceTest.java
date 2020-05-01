package com.lagou.work.test;

import com.lagou.work.dao.IUserDao;
import com.lagou.work.io.Resources;
import com.lagou.work.pojo.User;
import com.lagou.work.sqlSession.SqlSession;
import com.lagou.work.sqlSession.SqlSessionFactory;
import com.lagou.work.sqlSession.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class IPersistenceTest {

    @Test
    public void test() throws DocumentException, PropertyVetoException, IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, SQLException, InvocationTargetException, ClassNotFoundException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        //测试用例 新增
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("test");
        userDao.insertUser(user3);
        //测试用例 更新
        /*User user4 = new User();
        user4.setId(3L);
        user4.setUsername("test_change");
        userDao.updateUser(user4);*/
        //测试用例 删除
        /*User user5 = new User();
        user5.setId(3L);
        userDao.deleteUser(user5);*/
    }
}
