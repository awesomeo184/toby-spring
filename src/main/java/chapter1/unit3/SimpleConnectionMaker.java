package chapter1.unit3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleConnectionMaker {

    private final String url = "";
    private final String username = "";
    private final String password = "";

    public Connection makeConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

}
