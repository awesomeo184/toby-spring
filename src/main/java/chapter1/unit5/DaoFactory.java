package chapter1.unit5;

import chapter1.unit4.AccountDao;
import chapter1.unit4.MessageDao;

public class DaoFactory {

    public UserDao userDao() {
        return new UserDao(connectionMaker());
    }

    public SimpleConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }
}
