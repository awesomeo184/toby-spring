package chapter1.unit2;

import java.sql.SQLException;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException {
        UserDao dao = new NUserDao();

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
