package com.example.tobyspring.user;

import com.example.tobyspring.user.dao.ConnectionMaker;
import com.example.tobyspring.user.dao.CountingConnectionMaker;
import com.example.tobyspring.user.dao.UserDao;
import com.example.tobyspring.user.domain.User;
import java.sql.SQLException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoConnectionCountingTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("spring");
        user.setName("정수현");
        user.setPassword("hello");

        dao.add(user);

        User user2 = dao.get(user.getId());

        CountingConnectionMaker ccm = ac.getBean("connectionMaker", CountingConnectionMaker.class);
        int counter = ccm.getCounter();
        System.out.println("counter = " + counter);
    }

}
