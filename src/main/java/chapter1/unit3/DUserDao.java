package chapter1.unit3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DUserDao extends UserDao {
    private final String url = "";
    private final String username = "";
    private final String password = "";

    @Override
    public Connection getConnection() throws SQLException {
        // D사의 독자적인 DB 커넥션 코드
        return DriverManager.getConnection(url, username, password);
    }
}
