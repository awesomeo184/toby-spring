package com.example.tobyspring.user;

import static org.assertj.core.api.Assertions.*;

import com.example.tobyspring.user.dao.UserDao;
import com.example.tobyspring.user.domain.User;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoTest {

    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = ac.getBean("userDao", UserDao.class);

        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        User user = new User();
        user.setId("awesomeo");
        user.setName("정수현");
        user.setPassword("awesome!");

        userDao.add(user);
        assertThat(userDao.getCount()).isEqualTo(1);

        User user2 = userDao.get(user.getId());
        assertThat(user.getId()).isEqualTo(user2.getId());
        assertThat(user.getName()).isEqualTo(user2.getName());
        assertThat(user.getPassword()).isEqualTo(user2.getPassword());
    }

}
