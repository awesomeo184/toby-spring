package chapter1.unit4;

public class DaoFactory {

    public UserDao userDao() {
        SimpleConnectionMaker connectionMaker = new DConnectionMaker();
        return new UserDao(connectionMaker);
    }

    public AccountDao accountDao() {
        SimpleConnectionMaker connectionMaker = new DConnectionMaker();
        return new AccountDao(connectionMaker);
    }

    public MessageDao messageDao() {
        SimpleConnectionMaker connectionMaker = new DConnectionMaker();
        return new MessageDao(connectionMaker);
    }
}
