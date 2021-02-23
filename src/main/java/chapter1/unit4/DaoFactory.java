package chapter1.unit4;

public class DaoFactory {

    public UserDao userDao() {
        SimpleConnectionMaker connectionMaker = new DConnectionMaker();
        return new UserDao(connectionMaker);
    }

}
