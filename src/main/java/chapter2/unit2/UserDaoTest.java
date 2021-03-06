package chapter2.unit2;

import java.sql.SQLException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException {

        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);

        UserDao dao = ac.getBean("userDao", UserDao.class);

        System.out.println("dao = " + dao);

        User user = new User();
        user.setId("whiteship");
        user.setName("백기선");
        user.setPassword("married");

        dao.add(user);

        User findUser = dao.get(user.getId());

        System.out.println("name = " + findUser.getName());
        System.out.println("password = " + findUser.getPassword());
    }

}
