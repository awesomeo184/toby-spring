package chapter1.unit7;

import java.sql.SQLException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoConnectionCountingTest {

    public static void main(String[] args) throws SQLException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(CountingDaoFactory.class);
        UserDao dao = ac.getBean("userDao", UserDao.class);

        //Dao 사용 코드
        User user1 = new User();
        user1.setId("whiteship");
        user1.setName("백기선");
        user1.setPassword("married");

        dao.add(user1);
        dao.get(user1.getId());

        User user2 = new User();
        user2.setId("toby");
        user2.setName("이일민");
        user2.setPassword("spring");

        dao.add(user2);
        dao.get(user2.getId());

        CountingConnectionMaker ccm = ac.getBean("connectionMaker", CountingConnectionMaker.class);
        System.out.println("Connection Counter = " + ccm.getCounter());
    }

}
