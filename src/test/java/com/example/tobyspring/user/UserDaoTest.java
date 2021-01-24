package com.example.tobyspring.user;

import static org.assertj.core.api.Assertions.*;

import com.example.tobyspring.user.dao.UserDao;
import com.example.tobyspring.user.domain.User;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

@TestInstance(Lifecycle.PER_CLASS)
public class UserDaoTest {

    private UserDao userDao;
    private User user1;
    private User user2;
    private User user3;

    @BeforeAll
    void setUp() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        userDao = ac.getBean("userDao", UserDao.class);

        user1 = new User("awesome", "정수현", "awesome!");
        user2 = new User("ppippo", "삐뽀", "ppap");
        user3 = new User("hoho", "박성호", "spring!");

    }

    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {

        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

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
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> {
            userDao.get("unknownId");
        }).isInstanceOf(EmptyResultDataAccessException.class);
    }

}
