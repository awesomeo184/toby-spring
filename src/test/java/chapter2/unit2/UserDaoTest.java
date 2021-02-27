package chapter2.unit2;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

class UserDaoTest {

    @Test
    @DisplayName("DB에 데이터 저장하고 가져오기가 잘 되는지")
    void addAndGet() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("gyumee");
        user.setName("박성철");
        user.setPassword("springno1");

        dao.add(user);

        User user2 = dao.get(user.getId());

        assertThat(user2.getName()).isEqualTo(user.getName());
        assertThat(user2.getPassword()).isEqualTo(user.getPassword());
    }
}