package com.example.tobyspring.user;

import static org.assertj.core.api.Assertions.*;

import com.example.tobyspring.user.dao.UserDao;
import com.example.tobyspring.user.domain.User;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

public class UserDaoTest {

    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = ac.getBean("userDao", UserDao.class);

        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        User user1 = new User("awesome", "정수현", "awesome!");
        User user2 = new User("ppippo", "삐뽀", "ppap");

        userDao.add(user1);
        assertThat(userDao.getCount()).isEqualTo(1);

        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);

        User userGet1 = userDao.get(user1.getId());
        assertThat(userGet1.getName()).isEqualTo(user1.getName());
        assertThat(userGet1.getPassword()).isEqualTo(user1.getPassword());

        User userGet2 = userDao.get(user2.getId());
        assertThat(userGet2.getName()).isEqualTo(user2.getName());
        assertThat(userGet2.getPassword()).isEqualTo(user2.getPassword());
    }

    @Test
    void count() throws SQLException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);

        UserDao userDao = ac.getBean("userDao", UserDao.class);
        User user1 = new User("gyumee", "박성철", "spring!");
        User user2 = new User("leeO", "김이오", "kimtwofive");
        User user3 = new User("hoho", "박성호", "spring!");

        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        userDao.add(user1);
        assertThat(userDao.getCount()).isEqualTo(1);

        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);

        userDao.add(user3);
        assertThat(userDao.getCount()).isEqualTo(3);

    }

    @Test
    void getUserFailure() throws SQLException, ClassNotFoundException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);

        UserDao userDao = ac.getBean("userDao", UserDao.class);
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> {
            userDao.get("unknownId");
        }).isInstanceOf(EmptyResultDataAccessException.class);
    }

}
