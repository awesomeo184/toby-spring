package chapter1.unit4;

public class AccountDao {
    private SimpleConnectionMaker simpleConnectionMaker;

    public AccountDao(SimpleConnectionMaker simpleConnectionMaker) {
        this.simpleConnectionMaker = simpleConnectionMaker;
    }
}
