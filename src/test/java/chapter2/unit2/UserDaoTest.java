package chapter2.unit2;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

class UserDaoTest {

    @Test
    @DisplayName("DB에 데이터 저장하고 가져오기가 잘 되는지")
    void addAndGet() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user1 = new User("awesome", "정수현", "HIhi");
        User user2 = new User("whiteship", "백기선", "spring");


        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);


        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        User getUser1 = dao.get(user1.getId());
        assertThat(getUser1.getName()).isEqualTo(user1.getName());
        assertThat(getUser1.getPassword()).isEqualTo(user1.getPassword());

        User getUser2 = dao.get(user2.getId());
        assertThat(getUser2.getName()).isEqualTo(user2.getName());
        assertThat(getUser2.getPassword()).isEqualTo(user2.getPassword());
    }

    @Test
    @DisplayName("레코드 수를 잘 세는지")
    void count() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user1 = new User("awesome", "정수현", "hi");
        User user2 = new User("whiteship", "백기선", "developer");
        User user3 = new User("toby", "이일민", "springno1");

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        dao.add(user1);
        assertThat(dao.getCount()).isEqualTo(1);

        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        dao.add(user3);
        assertThat(dao.getCount()).isEqualTo(3);

    }

    @Test
    @DisplayName("전달된 id가 존재하지 않는 경우")
    void getUserFailure() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> {
            dao.get("unknownId");
        }).isInstanceOf(EmptyResultDataAccessException.class);
    }
}