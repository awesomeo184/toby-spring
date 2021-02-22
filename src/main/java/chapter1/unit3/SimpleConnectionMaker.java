package chapter1.unit3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface SimpleConnectionMaker {
    Connection makeConnection() throws SQLException;
}
